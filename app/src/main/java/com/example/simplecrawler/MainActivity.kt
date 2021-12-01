package com.example.simplecrawler

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    companion object {
        const val INPUT_URL_KEY = "com.example.simplecrawler.MainActivity.urlkey"
        const val INPUT_DEPTH_KEY = "com.example.simplecrawler.MainActivity.depthkey"
        const val mSharedPrefs_SET_KEY = "com.example.simplecrawler.MainActivity.setkey"
    }

    private lateinit var mButtonCrawl: Button
    private lateinit var mAutoTextUrl: AutoCompleteTextView
    private lateinit var mTextDepth: EditText

    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSharedPrefs = getPreferences(Context.MODE_PRIVATE)
        // Put an empty set of strings in mSharedPrefs if there's no set there already
        if (!mSharedPrefs.contains(mSharedPrefs_SET_KEY)) {
            mSharedPrefs.edit()
                .putStringSet(mSharedPrefs_SET_KEY, HashSet<String>())
                .apply()
        }
        // Copy the contents of mSharedPrefs' set into savedUrls cause I can't modify it otherwise
        val savedUrls =
            HashSet<String>(mSharedPrefs.getStringSet(mSharedPrefs_SET_KEY, null) as HashSet)

        mButtonCrawl = findViewById(R.id.btn_crawl)
        mAutoTextUrl = findViewById(R.id.auto_text_edit_url)
        mTextDepth = findViewById(R.id.text_edit_depth)

        val mAutoTextUrlAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, savedUrls.toList())
        mAutoTextUrl.setAdapter(mAutoTextUrlAdapter)

        mButtonCrawl.setOnClickListener {
            val intent = Intent(this, CrawlActivity::class.java)
            intent.putExtra(INPUT_URL_KEY, mAutoTextUrl.text.toString())
            if (mTextDepth.text.toString() == "")
                intent.putExtra(INPUT_DEPTH_KEY, mTextDepth.text)
            else
                intent.putExtra(INPUT_DEPTH_KEY, mTextDepth.text.toString().toInt())

            // Modify the copy of the set in mSharedPrefs
            savedUrls.add(mAutoTextUrl.text.toString())
            // Then replace the old set with the new one in mSharedPrefs
            with(mSharedPrefs.edit()) {
                putStringSet(mSharedPrefs_SET_KEY, savedUrls)
                apply()
            }

            startActivity(intent)
        }
    }
}