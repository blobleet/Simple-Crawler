package com.example.simplecrawler

import android.util.AndroidRuntimeException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.*
import java.lang.Exception
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.UnknownHostException
import java.util.regex.Pattern
import kotlin.math.log

class URLRepository{
    private val TAG = "URLRepository"

    companion object {
        // List of all crawled urls
        @JvmStatic val crawledUrls = arrayListOf<String>()
    }

    fun fetchUrls(urlsToCrawl: ArrayList<String>): ArrayList<String>{
        Log.d(TAG, "fetchUrls: Called")
        var connection: HttpURLConnection

        // Declare empty list to be filled with crawled urls
        val urlList = arrayListOf<String>()

        // Declare a regex that reads only urls (I made it myself!!)
        // and a list of extensions
        val regex = Pattern.compile(
            "(?<=href=\")https://(www\\.)?([a-zA-Z\\d-]+)(\\.[a-z]+)+(/[a-zA-Z\\d?+=\\-_#%~:,.]+)*(\\.[a-z]+)?"
        )
        val uselessExt = arrayOf(".css", ".png", ".jpg", ".xml", ".html", ".js", ".php", ".zip")

        // These variables are for getting an html string from http connection
        var input: BufferedReader
        var inputLine: String?
        val content = StringBuffer()

        // Iterate through all of the passed urls
        for(url in urlsToCrawl){
            if (url in crawledUrls)  // Don't crawl already crawled urls
                continue

            crawledUrls.add(url)  // Add url to the list of crawled urls
            Log.d(TAG, "fetchUrls: Crawling $url")
            try{
                connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                // Try block is mainly for this line since a lot of pages are not accessible
                input = BufferedReader(InputStreamReader(connection.inputStream))
            } catch(u: UnknownHostException) {
                Log.d(TAG, "fetchUrls: Caught UnknownHostException, can't open site!")
                u.message
                u.stackTrace
                continue
            } catch(f: FileNotFoundException){
                Log.d(TAG, "fetchUrls: Caught FileNotFoundException")
                f.message
                f.stackTrace
                continue
            } catch(p: ProtocolException){
                Log.d(TAG, "fetchUrls: Caught ProtocolException")
                p.message
                p.stackTrace
                continue
            }

            try{
                // Add the input stream from http connection line by line inside of "content"
                while (input.readLine().also { inputLine = it } != null) {
                    content.append(inputLine + "\n")
                }
            } catch(i: InterruptedIOException){
                // If the thread is interrupted it just returns the url list so far
                i.message
                i.stackTrace
                return urlList
            }

            val matcher = regex.matcher(content)
            var validUrl: Boolean  // Used to check for extensions in the urls
            while(matcher.find()){
                val match = matcher.group()  // This gets just the matching string from content

                // Prevent adding the same url multiple times ONLY on this current url crawl
                // There will still be duplicates that's why in view model I clear duplicates again
                if(match in urlList)
                    continue

                // Checking if url has an extension like css, png.. etc
                validUrl = true
                for (ext in uselessExt)
                    if(match.endsWith(ext)){
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