package com.zpf.util

interface DownloadListener {
    fun onStartLoad(id: Int, message: String)
    fun onLoading(id: Int, message: String)
    fun onFinish(id: Int, message: String)
}