package com.zpf.activity

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.OkDownload
import com.liulishuo.okdownload.core.cause.EndCause
import com.liulishuo.okdownload.core.cause.ResumeFailedCause
import com.liulishuo.okdownload.core.dispatcher.DownloadDispatcher
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist
import com.lyc.downloader.Configuration
import com.lyc.downloader.SubmitListener
import com.lyc.downloader.YCDownloader
import com.lyc.downloader.db.DownloadInfo
import com.mocmna.lib.download.DownloadUtils
import com.mocmna.lib.download.bean.DownloadItem
import com.mocmna.lib.request.URLUtils
import com.zpf.download.Constants.TAG
import com.zpf.download.R
import com.zpf.util.*
import java.lang.Exception

class LoadingActivity : AppCompatActivity() {
    private lateinit var tvInfo: TextView
    private lateinit var tvProgress: TextView
    private val record = DownloadRecordInfo()
    private val downloadSettingInfo = DownloadSettingInfo()

    @Volatile
    private var loading = false
    private var managerListener: DownloadListener = object : DownloadListener {
        override fun onStartLoad(id: Int, message: String) {
            loading = true
            LoadHistory.recordHistory(downloadSettingInfo.url)
            runOnUiThread {
                tvProgress.text = message
            }
        }

        override fun onLoading(id: Int, message: String) {
            runOnUiThread {
                tvProgress.text = message
            }
        }

        override fun onFinish(id: Int, message: String) {
            writeToFile(this@LoadingActivity, tvInfo.text.toString() + message)
            runOnUiThread {
                tvProgress.text = message
            }
            loading = false
        }
    }
    private var okListener: com.liulishuo.okdownload.DownloadListener? = null
    private var tencentListener: com.mocmna.lib.download.bean.DownloadListener? = null
    private var ycListener: com.lyc.downloader.DownloadListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        deleteFile(cacheDir)
        val setting = intent.getParcelableExtra<DownloadSettingInfo>("setting")
        tvInfo = findViewById(R.id.tv_hint1)
        tvProgress = findViewById(R.id.tv_hint2)
        tvProgress.setOnLongClickListener {
            if (!loading) {
                copyMessage(tvProgress)
            }
            return@setOnLongClickListener true
        }
        if (setting == null) {
            tvInfo.text = "====== 下载信息 ======\n下载配置解析失败"
            return
        }
        downloadSettingInfo.url = setting.url
        downloadSettingInfo.process = setting.process
        downloadSettingInfo.type = setting.type
        val limit = if (setting.process <= 0) {
            "仅连接"
        } else {
            setting.process.toString() + "%"
        }
        val typeName = DownloaderType.typeName(setting.type)
        tvInfo.text = "====== 下载信息 ======\n" +
                "下载地址：${setting.url}\n" + "下载限制：$limit\n" +
                "下载器：${typeName}\n"
        when (setting.type) {
            DownloaderType.ANDROID -> {
                DownloadInfoManager.listener = managerListener
                val info =
                    DownloadInfoManager.startDownload(this, setting.url, false, setting.process)
                record.id = info.mId
            }
            DownloaderType.XIAOMI -> {
                DownloadInfoManager.listener = managerListener
                val info =
                    DownloadInfoManager.startDownload(this, setting.url, true, setting.process)
                record.id = info.mId
            }
            DownloaderType.TENCENT -> {
                DownloadUtils.init(applicationContext, 3, getTencentListener(), true)
                val info = DownloadItem()
                info.downloadURL = URLUtils.getNoQueryUrl(setting.url)
                info.downloadIcon = "https://blog.bihe0832.com/public/img/head.jpg"
                info.downloadTitle = typeName
                info.isForceDownloadNew = true
                info.extraInfo = "hardytestInfo"
                info.actionKey = "hardytestKey"
                info.isAutoInstall = false
                info.isDownloadWhenAdd = true
                info.isDownloadWhenUseMobile = false
                record.id = info.downloadID
                DownloadUtils.startDownload(applicationContext, info)
            }
            DownloaderType.YC_DOWNLOAD -> {
                val config = Configuration.Builder()
                    .setMaxRunningTask(5)
                    .setAllowDownload(true)
                    .setMultiProcess(false)
                    .build()
                YCDownloader.install(applicationContext, config)
                YCDownloader.registerDownloadListener(getYcListener())
                YCDownloader.submit(
                    setting.url,
                    cacheDir.absolutePath,
                    null,
                    object : SubmitListener {
                        override fun submitSuccess(downloadInfo: DownloadInfo?) {
                            if (downloadInfo != null) {
                                record.id = downloadInfo.id
                                YCDownloader.startOrResume(downloadInfo.id, false)
                            } else {
                                tvProgress.text = "下载器启动失败"
                            }
                        }

                        override fun submitFail(e: Exception?) {
                            tvProgress.text = "下载器启动失败"
                        }
                    })
            }
            DownloaderType.OK_DOWNLOAD -> {
                DownloadDispatcher.setMaxParallelRunningCount(4)
                val taskBuilder = com.liulishuo.okdownload.DownloadTask.Builder(
                    setting.url,
                    cacheDir.absolutePath,
                    null
                ).setConnectionCount(4)
                    .setMinIntervalMillisCallbackProcess(48)
                    .setAutoCallbackToUIThread(false)
                    .setPassIfAlreadyCompleted(false)
                val task = taskBuilder.build()
                task.enqueue(getOkListener())
            }
        }
    }

    override fun onDestroy() {
        DownloadInfoManager.listener = null
        DownloadInfoManager.cancelAll()
        tencentListener?.let {
            DownloadUtils.removeDownloadListener(it)
            DownloadUtils.deleteTask(record.id, true)
            DownloadUtils.onDestroy()
        }
        ycListener?.let {
            YCDownloader.unregisterDownloadListener(it)
            YCDownloader.cancel(record.id)
        }
        okListener?.let {
            OkDownload.with().downloadDispatcher().cancelAll()
        }
        super.onDestroy()
    }

    private fun getOkListener(): com.liulishuo.okdownload.DownloadListener {
        var listener = okListener
        if (listener != null) {
            return listener
        }
        listener = object : com.liulishuo.okdownload.core.listener.DownloadListener3() {
            override fun retry(task: DownloadTask, cause: ResumeFailedCause) {
                Log.w(TAG, "retry:cause=$cause")
            }

            override fun taskEnd(
                task: DownloadTask,
                cause: EndCause,
                realCause: Exception?,
                model: Listener1Assist.Listener1Model
            ) {
                super.taskEnd(task, cause, realCause, model)
                Log.w(TAG, "taskEnd:cause=$cause")

            }

            override fun fetchEnd(task: DownloadTask, blockIndex: Int, contentLength: Long) {
                super.fetchEnd(task, blockIndex, contentLength)
                Log.w(TAG, "fetchEnd:blockIndex=$blockIndex contentLength=$contentLength")
                record.onFinish()
                managerListener.onFinish(task.id, record.getShowText())
            }

            override fun connected(
                task: DownloadTask,
                blockCount: Int,
                currentOffset: Long,
                totalLength: Long
            ) {
                Log.w(
                    TAG,
                    "connected:blockCount=$blockCount ;currentOffset=$currentOffset ;totalLength=$totalLength"
                )
            }

            override fun progress(task: DownloadTask, currentOffset: Long, totalLength: Long) {
                Log.w(TAG, "progress:currentOffset=$currentOffset ;totalLength=$totalLength")
                record.onLoading(currentOffset, totalLength)
                managerListener.onLoading(task.id, record.getShowText())
                if (downloadSettingInfo.process <= currentOffset * 100f / totalLength) {
                    OkDownload.with().downloadDispatcher().cancelAll()
                }
            }

            override fun started(task: DownloadTask) {
                Log.w(TAG, "started")
                record.onConnected()
                managerListener.onStartLoad(task.id, record.getShowText())
                if (downloadSettingInfo.process <= 0) {
                    OkDownload.with().downloadDispatcher().cancelAll()
                }
            }

            override fun completed(task: DownloadTask) {
                Log.w(TAG, "completed")
                record.onFinish()
                managerListener.onFinish(task.id, record.getShowText())
            }

            override fun canceled(task: DownloadTask) {
                Log.w(TAG, "canceled")
                record.onFinish()
                managerListener.onFinish(task.id, record.getShowText())
            }

            override fun error(task: DownloadTask, e: Exception) {
                Log.w(TAG, "error:$e")
                record.onFinish()
                managerListener.onFinish(task.id, e.message ?: "出错了")
            }

            override fun warn(task: DownloadTask) {
                Log.w(TAG, "warn")
            }
        }
        okListener = listener
        return listener
    }

    private fun getYcListener(): com.lyc.downloader.DownloadListener {
        var listener = ycListener
        if (listener != null) {
            return listener
        }
        listener = object : com.lyc.downloader.DownloadListener {
            override fun onDownloadConnecting(id: Long) {
            }

            override fun onDownloadProgressUpdate(id: Long, total: Long, cur: Long, bps: Double) {
                record.onLoading(cur, total)
                managerListener.onLoading(0, record.getShowText())
                if (downloadSettingInfo.process <= cur * 100f / total) {
                    YCDownloader.pause(id)
                }
            }

            override fun onDownloadUpdateInfo(downloadInfo: DownloadInfo?) {
                record.onConnected()
                managerListener.onStartLoad(0, record.getShowText())
                if (downloadSettingInfo.process <= 0) {
                    YCDownloader.pause(downloadInfo?.id ?: 0)
                }
            }

            override fun onDownloadError(id: Long, code: Int, fatal: Boolean) {
                record.onFinish()
                managerListener.onFinish(0, record.getShowText())
            }

            override fun onDownloadStart(downloadInfo: DownloadInfo?) {
            }

            override fun onDownloadStopping(id: Long) {
            }

            override fun onDownloadPaused(id: Long) {
                record.onFinish()
                managerListener.onFinish(0, record.getShowText())
            }

            override fun onDownloadTaskWait(id: Long) {
            }

            override fun onDownloadCanceled(id: Long) {
//                record.onFinish()
//                managerListener.onFinish(0, record.getShowText())
            }

            override fun onDownloadFinished(downloadInfo: DownloadInfo?) {
                record.onFinish()
                managerListener.onFinish(0, record.getShowText())
            }

        }
        ycListener = listener
        return listener
    }

    private fun getTencentListener(): com.mocmna.lib.download.bean.DownloadListener {
        var listener = tencentListener
        if (listener != null) {
            return listener
        }
        listener = object : com.mocmna.lib.download.bean.DownloadListener {
            override fun onWait(item: DownloadItem) {
                Log.w(TAG, "onWait:$item")
                record.onConnected()
            }

            override fun onStart(item: DownloadItem) {
                Log.w(TAG, "onStart:$item")
                record.onConnected()
                managerListener.onStartLoad(0, record.getShowText())
                if (downloadSettingInfo.process <= 0) {
                    DownloadUtils.pauseDownload(item.downloadID)
                }
            }

            override fun onProgress(item: DownloadItem) {
                Log.w(TAG, "onProgress:$item")
                record.onLoading(item.finished, item.fileLength)
                managerListener.onLoading(0, record.getShowText())
                if (downloadSettingInfo.process <= item.finished * 100f / item.fileLength) {
                    DownloadUtils.pauseDownload(item.downloadID)
                }
            }

            override fun onPause(item: DownloadItem) {
                Log.w(TAG, "onPause:$item")
                record.onFinish()
                managerListener.onFinish(0, record.getShowText())
            }

            override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {
                Log.w(TAG, "onFail:$item")
            }

            override fun onComplete(filePath: String, item: DownloadItem) {
                Log.w(TAG, "onComplete:$item")
                record.onFinish()
                managerListener.onFinish(0, record.getShowText())
            }

            override fun onDelete(item: DownloadItem) {
                Log.w(TAG, "onDelete:$item")
            }
        }
        tencentListener = listener
        return listener
    }

}