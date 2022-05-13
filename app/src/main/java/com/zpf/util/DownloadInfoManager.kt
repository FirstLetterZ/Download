package com.zpf.util

import android.content.Context
import android.util.SparseArray
import androidx.core.util.forEach
import com.zpf.download.DownloadInfo
import com.zpf.download.Helpers
import java.io.File

object DownloadInfoManager {
    var listener: DownloadListener? = null
    private val infoArray = SparseArray<DownloadInfo>()
    private val loadingTemp = SparseArray<DownloadRecordInfo>()

    fun onStartLoad(id: Int) {
        val temp = loadingTemp.get(id)
        temp.onConnected()
        listener?.onStartLoad(id, temp.getShowText())
    }

    fun onLoading(id: Int, currentBytes: Long, totalBytes: Long) {
        val temp = loadingTemp.get(id)
        temp.onLoading(currentBytes, totalBytes)
        listener?.onStartLoad(id, temp.getShowText())
    }

    fun onFinish(id: Int) {
        val temp = loadingTemp.get(id)
        temp.onFinish()
        listener?.onFinish(id, temp.getShowText())
    }

    fun startDownload(
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
        infoArray.put(infoId, info)
        val temp = DownloadRecordInfo()
        loadingTemp.put(infoId, temp)
        listener?.onStartLoad(infoId, "=====准备下载=====")
        Helpers.scheduleJob(context, info)
        return info
    }

    fun cancelAll() {
        infoArray.forEach { _, value ->
            value.mCanceled = true
        }
    }

    fun cancelDownload(id: Long) {
        infoArray.get(id.toInt())?.mCanceled = true
    }

    fun query(id: Int): DownloadInfo? {
        return infoArray.get(id)
    }
}