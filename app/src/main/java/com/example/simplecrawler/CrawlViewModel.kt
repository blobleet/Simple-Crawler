package com.example.simplecrawler

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.UnknownHostException
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class CrawlViewModel : ViewModel() {
    private lateinit var mStartUrl: URL
    private var mMaxDepth: Int = 0
    var mIsCrawling: LiveData<Boolean> = MutableLiveData()
    var mUrlListLiveData: LiveData<ArrayList<String>> = MutableLiveData()
    private var mAllUrls: ArrayList<String> =
        ArrayList()  // Keeps track of all urls inside the live data as strings
    private var mFoundUrls: ArrayList<String> = ArrayList()  // Urls found from crawling
    private lateinit var mRepo: Fetcher
    private lateinit var mCrawlThread: Thread
    private var mInterrupted = false

    companion object {
        val CRAWLED_URLS = arrayListOf<String>()
    }

    private val TAG = "CrawlViewModel"

    fun init(startUrl: URL, maxDepth: Int) {
        mStartUrl = startUrl
        mMaxDepth = maxDepth
        initRepo()
        (mIsCrawling as MutableLiveData).value = true

        // This creates a new thread in which all crawling happens
        mCrawlThread = Thread(Runnable {
            try {
                fetchFromRepo()
                // After everything is crawled:
                (mIsCrawling as MutableLiveData).postValue(false)
            } catch (e: InterruptedException) {
                // Or if thread is interrupted:
                (mIsCrawling as MutableLiveData).postValue(false)
            }
        })
        mCrawlThread.start()
    }


    private fun fetchFromRepo(depth: Int = 0) {
        // Recursion bottom (">=" instead of "==" for safety purposes idk)
        if (depth == mMaxDepth)
            return

        // If it's the first iteration of crawling, fetch with an array of just the starting url
        // else fetch with the array of already found urls
        if (depth == 0){
            mFoundUrls = arrayListOf(mStartUrl.toString())
            mFoundUrls = mRepo.fetch()
        } else{
            mFoundUrls = mRepo.fetch()
        }

        // Skip adding duplicates
        for (url in mFoundUrls)
            if (url !in mAllUrls)
                mAllUrls.add(url)
        // Update the live data with new urls only if there are any
        if (mFoundUrls.size != 0)
            (mUrlListLiveData as MutableLiveData).postValue(mAllUrls)
        Log.d(TAG, "fetchFromRepo: ===== FINISHED FETCHING ON DEPTH ${depth + 1} =====")

        // Call self with increased depth unless thread has been interrupted
        if (!mInterrupted)
            fetchFromRepo(depth.inc())
    }

    private fun initRepo(){
        mRepo = object: Fetcher{
            override fun fetch(): ArrayList<String> {
                Log.d(TAG, "fetch: Called")
                var connection: HttpURLConnection

                // Declare empty list to be filled with crawled urls
                val urlList = arrayListOf<String>()

                // Declare a regex that reads only urls starting with href (I made it myself!!)
                val regex = Pattern.compile(
                    "(?<=href=\")https://(www\\.)?([a-zA-Z\\d-]+)(\\.[a-z]+)+(/[a-zA-Z\\d?+=\\-_#%~:,.]+)*(\\.[a-z]+)?"
                )
                val uselessExt = arrayOf(
                    ".css", ".png", ".jpg", ".xml", ".html", ".js", ".php", ".zip", ".svg", ".ico"
                )

                // These variables are for getting an html string from http connection
                var input: BufferedReader
                var inputLine: String?
                val content = StringBuffer()

                // Iterate through all of the passed urls
                for (url in mFoundUrls) {
                    if (url in CRAWLED_URLS)  // Don't crawl already crawled urls
                        continue

                    CRAWLED_URLS.add(url)  // Add url to the list of crawled urls
                    Log.d(TAG, "fetchUrls: Crawling $url")

                    connection = URL(url).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    try {
                        // Try block is mainly for this line since a lot of pages are not accessible
                        input = BufferedReader(InputStreamReader(connection.inputStream))
                        // Add the input stream from http connection line by line inside of "content"
                        while (input.readLine().also { inputLine = it } != null) {
                            content.append(inputLine + "\n")
                        }
                    } catch (u: UnknownHostException) {
                        Log.d(TAG, "fetchUrls: Caught UnknownHostException, can't open site!")
                        u.message
                        u.stackTrace
                        continue
                    } catch (f: FileNotFoundException) {
                        Log.d(TAG, "fetchUrls: Caught FileNotFoundException")
                        f.message
                        f.stackTrace
                        continue
                    } catch (p: ProtocolException) {
                        Log.d(TAG, "fetchUrls: Caught ProtocolException")
                        p.message
                        p.stackTrace
                        continue
                    } catch (i: InterruptedIOException) {
                        // If the thread is interrupted it just returns the url list so far
                        i.message
                        i.stackTrace
                        mInterrupted = true
                        content.setLength(0)
                        connection.disconnect()
                        break
                    }

                    val matcher = regex.matcher(content)
                    var validUrl: Boolean  // Used to check for extensions in the urls
                    while (matcher.find()) {
                        val match = matcher.group()  // This gets just the matching string from content

                        // Prevent adding the same url multiple times ONLY on this current url crawl
                        // There will still be duplicates that's why in view model I clear duplicates again
                        if (match in urlList)
                            continue

                        // Checking if url has an extension like css, png.. etc
                        validUrl = true
                        for (ext in uselessExt)
                            if (match.endsWith(ext)) {
                                validUrl = false
                                break
                            }

                        if (validUrl)
                            urlList.add(match)
                    }

                    content.setLength(0) // Clears the string buffer
                    connection.disconnect()
                }

                return urlList
            }
        }
    }

    fun stopCrawling() {
        mCrawlThread.interrupt()
    }
}