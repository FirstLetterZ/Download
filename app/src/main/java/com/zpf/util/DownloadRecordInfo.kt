package com.zpf.util

class DownloadRecordInfo {
    private val createTime = System.currentTimeMillis()
    private var connectTime = 0L
    private var finishTime = 0L
    private var currentBytes: Long = 0L
    private var totalBytes: Long = 0L
    private var speed: Float = 0F
    private var showText: String = ""
    var id: Long = 0L

    private fun showCurrentBytes(): String {
        return (currentBytes.toString() + "B>>" + toMB(currentBytes).toString() + "MB")
    }

    private fun showTotalBytes(): String {
        return (totalBytes.toString() + "B>>" + toMB(totalBytes).toString() + "MB")
    }

    fun onConnected() {
        val title: String
        if (connectTime == 0L) {
            connectTime = System.currentTimeMillis()
            title = "开始下载"
        } else {
            title = "重新连接"
        }
        showText = "=====$title=====\n" +
                "准备工作耗时：${connectTime - createTime} 毫秒"

    }

    fun onLoading(currentBytes: Long, totalBytes: Long) {
        this.currentBytes = currentBytes
        this.totalBytes = totalBytes
        val timeCost = System.currentTimeMillis() - connectTime
        speed = toKB(currentBytes) * 1000 / timeCost
        showText = "=====正在下载=====\n" +
                "准备工作耗时：${connectTime - createTime} 毫秒 \n" +
                "下载文件用时：${timeCost} 毫秒 \n" +
                "文件大小：${showTotalBytes()}\n" +
                "下载大小：${showCurrentBytes()}\n" +
                "平均下载速度：${speed} KB/s"
    }

    fun onFinish() {
        finishTime = System.currentTimeMillis()
        speed = toKB(currentBytes) * 1000 / (finishTime - connectTime)
        showText = "=====下载结束=====\n" +
                "准备工作耗时：${connectTime - createTime} 毫秒 \n" +
                "下载文件用时：${finishTime - connectTime} 毫秒 \n" +
                "下载总用时：${finishTime - createTime} 毫秒 \n" +
                "文件大小：${showTotalBytes()}\n" +
                "下载大小：${showCurrentBytes()}\n" +
                "平均下载速度：${speed} KB/s"
    }

    fun getShowText(): String = showText
}