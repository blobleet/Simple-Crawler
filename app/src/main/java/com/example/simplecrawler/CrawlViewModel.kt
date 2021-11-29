package com.example.simplecrawler

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.net.URL
import kotlin.collections.ArrayList

class CrawlViewModel : ViewModel() {
    private lateinit var mStartUrl: URL
    private var mMaxDepth: Int = 0
    var isCrawling: LiveData<Boolean> = MutableLiveData()
    var mUrlListLiveData: LiveData<ArrayList<String>> = MutableLiveData()
    private var mAllUrls: ArrayList<String> =
        ArrayList()  // Keeps track of all urls inside the live data as strings
    private var mFoundUrls: ArrayList<String> = ArrayList()  // Urls found from crawling
    lateinit var mRepo: URLRepository
    private lateinit var crawlThread: Thread

    private val TAG = "CrawlViewModel"

    fun init(startUrl: URL, maxDepth: Int) {
        mStartUrl = startUrl
        mMaxDepth = maxDepth
        mRepo = URLRepository()
        (isCrawling as MutableLiveData).value = true

        // This creates a new thread in which all crawling happens
        crawlThread = Thread(Runnable {
            try {
                fetchFromRepo()
                // After everything is crawled:
                (isCrawling as MutableLiveData).postValue(false)
            } catch (e: InterruptedException) {
                // Or if thread is interrupted:
                (isCrawling as MutableLiveData).postValue(false)
            }
        })
        crawlThread.start()
    }


    private fun fetchFromRepo(depth: Int = 0) {
        // Recursion bottom (">=" instead of "==" for safety purposes idk)
        if (depth == mMaxDepth)
            return

        // If it's the first iteration of crawling, fetch with an array of just the starting url
        // else fetch with the array of already found urls
        mFoundUrls = if (depth == 0)
            mRepo.fetchUrls(arrayListOf(mStartUrl.toString()))
        else
            mRepo.fetchUrls(mFoundUrls)

        // Skip adding duplicates
        for (url in mFoundUrls)
            if (url !in mAllUrls)
                mAllUrls.add(url)
        // Update the live data with new urls only if there are any
        if (mFoundUrls.size != 0)
            (mUrlListLiveData as MutableLiveData).postValue(mAllUrls)
        Log.d(TAG, "fetchFromRepo: ===== FINISHED FETCHING ON DEPTH ${depth + 1} =====")

        // Call self with increased depth unless thread has been interrupted
        if (!mRepo.interrupted)
            fetchFromRepo(depth.inc())
    }

    fun stopCrawling() {
        crawlThread.interrupt()
    }
}