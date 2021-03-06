package com.example.simplecrawler

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.net.UnknownHostException
import java.util.regex.Pattern

class URLRepository {
    var interrupted = false  // Flag to let viewmodel know if crawling is interrupted

    private val TAG = "URLRepository"

    companion object {
        // List of all crawled urls
        val crawledUrls = arrayListOf<String>()
    }

    fun fetchUrls(urlsToCrawl: ArrayList<String>): ArrayList<String> {
        Log.d(TAG, "fetchUrls: Called")
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
        for (url in urlsToCrawl) {
            if (url in crawledUrls)  // Don't crawl already crawled urls
                continue

            crawledUrls.add(url)  // Add url to the list of crawled urls
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
                interrupted = true
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