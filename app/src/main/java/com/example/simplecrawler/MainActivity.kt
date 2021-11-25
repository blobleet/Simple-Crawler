package com.example.simplecrawler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    object Constants {
        const val INPUT_URL_KEY = "com.example.simplecrawler.MainActivity.urlkey"
        const val INPUT_DEPTH_KEY = "com.example.simplecrawler.MainActivity.depthkey"
    }

    private lateinit var buttonCrawl: Button
    private lateinit var textUrl: EditText
    private lateinit var textDepth: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonCrawl = findViewById(R.id.btn_crawl)
        textUrl = findViewById(R.id.text_edit_url)
        textDepth = findViewById(R.id.text_edit_depth)

        buttonCrawl.setOnClickListener {
            val intent = Intent(this, CrawlActivity::class.java)
            intent.putExtra(Constants.INPUT_URL_KEY, textUrl.text.toString())
            intent.putExtra(Constants.INPUT_DEPTH_KEY, textDepth.text.toString().toInt())
            
            startActivity(intent)
        }
    }
}