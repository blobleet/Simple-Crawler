package com.example.simplecrawler

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    companion object {
        const val INPUT_URL_KEY = "com.example.simplecrawler.MainActivity.urlkey"
        const val INPUT_DEPTH_KEY = "com.example.simplecrawler.MainActivity.depthkey"
        const val SHAREDPREFS_SET_KEY = "com.example.simplecrawler.MainActivity.setkey"
    }

    private lateinit var buttonCrawl: Button
    private lateinit var autoTextUrl: AutoCompleteTextView
    private lateinit var textDepth: EditText

    private lateinit var sharedprefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedprefs = getPreferences(Context.MODE_PRIVATE)
        // Put an empty set of strings in sharedprefs if there's no set there already
        if (!sharedprefs.contains(SHAREDPREFS_SET_KEY)) {
            sharedprefs.edit()
                .putStringSet(SHAREDPREFS_SET_KEY, HashSet<String>())
                .apply()
        }
        // Copy the contents of sharedprefs' set into savedUrls cause I can't modify it otherwise
        val savedUrls = HashSet<String>(sharedprefs.getStringSet(SHAREDPREFS_SET_KEY, null) as HashSet)

        buttonCrawl = findViewById(R.id.btn_crawl)
        autoTextUrl = findViewById(R.id.auto_text_edit_url)
        textDepth = findViewById(R.id.text_edit_depth)

        val autoTextUrlAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, savedUrls.toList())
        autoTextUrl.setAdapter(autoTextUrlAdapter)

        buttonCrawl.setOnClickListener {
            val intent = Intent(this, CrawlActivity::class.java)
            intent.putExtra(INPUT_URL_KEY, autoTextUrl.text.toString())
            if (textDepth.text.toString() == "")
                intent.putExtra(INPUT_DEPTH_KEY, textDepth.text)
            else
                intent.putExtra(INPUT_DEPTH_KEY, textDepth.text.toString().toInt())

            // Modify the copy of the set in sharedprefs
            savedUrls.add(autoTextUrl.text.toString())
            // Then replace the old set with the new one in sharedprefs
            with (sharedprefs.edit()){
                putStringSet(SHAREDPREFS_SET_KEY, savedUrls)
                apply()
            }
            
            startActivity(intent)
        }
    }
}