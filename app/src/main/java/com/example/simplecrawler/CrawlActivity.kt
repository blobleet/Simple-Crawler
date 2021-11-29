package com.example.simplecrawler

import android.content.Context
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

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: RecyclerAdapter
    private lateinit var loadingText: TextView
    private lateinit var btnStop: Button
    private lateinit var viewModel: CrawlViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crawl)

        // Get views
        loadingText = findViewById(R.id.text_loading)
        btnStop = findViewById(R.id.btn_stop)

        // Get user input from main activity
        val url = URL(intent.getStringExtra(MainActivity.INPUT_URL_KEY))
        val maxDepth = intent.getIntExtra(MainActivity.INPUT_DEPTH_KEY, -1)

        viewModel = ViewModelProvider(this)[CrawlViewModel::class.java]
        viewModel.init(url, maxDepth)

        observeCrawling()
        observeUrls()

        btnStop.setOnClickListener {
            viewModel.stopCrawling()
        }

        initRecyclerView()
    }

    private fun initRecyclerView(){
        recyclerView = findViewById(R.id.recycler_view)
        recyclerAdapter = RecyclerAdapter(viewModel.mUrlListLiveData.value)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerAdapter
    }

    private fun observeCrawling(){
        // Observe if app is crawling
        viewModel.isCrawling.observe(this, Observer {
            val isCrawling = it
            loadingText.visibility = if(isCrawling) View.VISIBLE else View.GONE
        })
    }
    private fun observeUrls(){
        // Observe changes in urls data
        viewModel.mUrlListLiveData.observe(this, Observer {
            val startPos: Int
            if (recyclerAdapter.items == null){
                startPos = 0
                recyclerAdapter.items = it
            } else {
                startPos = recyclerAdapter.itemCount - 1
                (recyclerAdapter.items as ArrayList).addAll(it)
            }
            recyclerAdapter.notifyItemRangeInserted(startPos, it.size)
        })
    }
}