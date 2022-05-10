package com.zpf

import android.content.Context
import android.util.SparseArray
import com.zpf.download.DownloadInfo
import java.io.File

object DownloadInfoManager {
    const val GB = 1024L * 1024L * 1024L
    const val MB = 1024L * 1024L
    const val KB = 1024L

    var listener: DownloadListener? = null
    private val infoArray = SparseArray<DownloadInfo>()
    private val loadingTemp = SparseArray<DownloadTempInfo>()


    fun onStartLoad(id: Int) {
        val temp = loadingTemp.get(id)
        val title: String
        if (temp.time2 == 0L) {
            temp.time2 = System.currentTimeMillis()
            title = "重新连接"
        } else {
            title = "开始下载"
        }
        listener?.onStartLoad(
            id, "=====$title=====\n" +
                    "使用小米下载线程：${temp.useMiui} \n" +
                    "准备工作耗时：${temp.time2 - temp.time1} 毫秒"
        )
    }

    fun onLoading(id: Int, currentBytes: Long, totalBytes: Long) {
        val temp = loadingTemp.get(id)
        temp.currentBytes = currentBytes
        temp.totalBytes = totalBytes
        val timeCost = System.currentTimeMillis() - temp.time2
        temp.speed = currentBytes * 1000f / KB / timeCost
        listener?.onStartLoad(
            id, "=====正在下载=====\n" +
                    "使用小米下载线程：${temp.useMiui} \n" +
                    "准备工作耗时：${temp.time2 - temp.time1} 毫秒 \n" +
                    "下载文件用时：${timeCost} 毫秒 \n" +
                    "文件大小：${temp.showTotalBytes()}\n" +
                    "下载大小：${temp.showCurrentBytes()}\n" +
                    "平均下载速度：${temp.speed} KB/s"
        )
    }

    fun onFinish(id: Int) {
        val temp = loadingTemp.get(id)
        temp.time3 = System.currentTimeMillis()
        temp.speed = temp.currentBytes * 1000f / KB / (temp.time3 - temp.time2)
        listener?.onFinish(
            id, "=====下载结束=====\n" +
                    "使用小米下载线程：${temp.useMiui} \n" +
                    "准备工作耗时：${temp.time2 - temp.time1} 毫秒 \n" +
                    "下载文件用时：${temp.time3 - temp.time2} 毫秒 \n" +
                    "下载总用时：${temp.time3 - temp.time1} 毫秒 \n" +
                    "文件大小：${temp.showTotalBytes()}\n" +
                    "下载大小：${temp.showCurrentBytes()}\n" +
                    "平均下载速度：${temp.speed} KB/s"
        )
    }

    fun create(
        context: Context,
        url: String,
        useMiuiDownload: Boolean,
        shutdownProcess: Int
    ): DownloadInfo {
        val info = DownloadInfo(context)
        val infoId = info.hashCode()
        info.mId = infoId.toLong()
        info.mUri = url
        info.mShutdownProcess = shutdownProcess
        info.mFileName = File(context.cacheDir, "download_" + info.mId).absolutePath
        info.mMiui = useMiuiDownload
//        info.mDescription
        infoArray.put(infoId, info)
        val temp = DownloadTempInfo()
        temp.useMiui = useMiuiDownload
        temp.time1 = System.currentTimeMillis()
        loadingTemp.put(infoId, temp)
        listener?.onStartLoad(infoId, "=====准备下载=====")
        return info
    }

    fun query(id: Int): DownloadInfo? {
        return infoArray.get(id)
    }

    class DownloadTempInfo {
        var time1 = 0L
        var time2 = 0L
        var time3 = 0L
        var currentBytes: Long = 0L
        var totalBytes: Long = 0L
        var speed: Float = 0F
        var useMiui: Boolean = false

        fun showCurrentBytes(): String {
            return (currentBytes.toString() + "B>>" + (currentBytes.toFloat() / MB).toString() + "MB")
        }

        fun showTotalBytes(): String {
            return (totalBytes.toString() + "B>>>" + (totalBytes.toFloat() / MB).toString() + "MB")
        }
    }
}