package com.example.simplecrawler

interface Fetcher {
    fun fetch(): ArrayList<String>
}