package com.example.simplecrawler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.URL


class CrawlActivity : AppCompatActivity() {
    private val TAG = "CrawlActivity"

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mRecyclerAdapter: RecyclerAdapter
    private lateinit var mLoadingText: TextView
    private lateinit var mBtnStop: Button
    private lateinit var mViewModel: CrawlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crawl)

        // Get views
        mLoadingText = findViewById(R.id.text_loading)
        mBtnStop = findViewById(R.id.btn_stop)

        // Get user input from main activity
        val url = URL(intent.getStringExtra(MainActivity.INPUT_URL_KEY))
        val maxDepth = intent.getIntExtra(MainActivity.INPUT_DEPTH_KEY, -1)

        mViewModel = ViewModelProvider(this)[CrawlViewModel::class.java]
        mViewModel.init(url, maxDepth)

        observeCrawling()
        observeUrls()

        mBtnStop.setOnClickListener {
            mViewModel.stopCrawling()
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view)
        mRecyclerAdapter = RecyclerAdapter(mViewModel.mUrlListLiveData.value)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mRecyclerAdapter
    }

    private fun observeCrawling() {
        // Observe if app is crawling
        mViewModel.mIsCrawling.observe(this, Observer {
            val isCrawling = it
            mLoadingText.visibility = if (isCrawling) View.VISIBLE else View.GONE
        })
    }

    private fun observeUrls() {
        // Observe changes in urls data
        mViewModel.mUrlListLiveData.observe(this, Observer {
            val startPos: Int
            if (mRecyclerAdapter.items == null) {
                startPos = 0
                mRecyclerAdapter.items = it
            } else {
                startPos = mRecyclerAdapter.itemCount - 1
                (mRecyclerAdapter.items as ArrayList).addAll(it)
            }
            mRecyclerAdapter.notifyItemRangeInserted(startPos, it.size)
        })
    }
}