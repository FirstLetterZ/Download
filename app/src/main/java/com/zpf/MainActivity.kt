package com.zpf

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zpf.download.Helpers
import com.zpf.download.R
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.math.abs

class MainActivity : AppCompatActivity() {
    private lateinit var vMask: View
    private val hintViewMap = ConcurrentHashMap<Int, TextView?>()
    private val hintViewArray = ArrayList<TextView>()

    //    private var limitBytes = 0L
//    private val limitArray = ArrayList<Pair<String, Long>>()
//    private val defLimit = Pair("无限制", 0L)
    private var lastClick = 0L
    private var loading = HashSet<Int>()

    private val urls = arrayOf(
        "https://appdl-1-drcn.dbankcdn.com/dl/appdl/application/apk/0f/0f24612b639341b5b79f0962fdbd20a3/com.tencent.tmgp.cod.2201190904.apk",
        "https://appdl-1-drcn.dbankcdn.com/dl/appdl/application/apk/2c/2c8feaebc38646409ca189edb0a15438/com.netease.mrzh.huawei.2202221748.apk",
        "https://appdlc-drcn.hispace.dbankcloud.cn/dl/appdl/application/apk/a7/a773217c1cb344249af6c41775a65a5d/com.netease.aceracer.huawei.2203031642.apk"
    )

    private val downloadListener = object : DownloadListener {

        override fun onStartLoad(id: Int, message: String) {
            showHintMessage(id, message, false)
        }

        override fun onLoading(id: Int, message: String) {
            showHintMessage(id, message, false)
        }

        override fun onFinish(id: Int, message: String) {
            writeToFile(message)
            showHintMessage(id, message, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vMask = findViewById(R.id.v_mask)
        vMask.setOnClickListener {
            if (System.currentTimeMillis() - lastClick > 2000) {
                Toast.makeText(this, "正在下载，请稍后再试", Toast.LENGTH_SHORT).show()
                lastClick = System.currentTimeMillis()
            }
        }
        val tvHint1: TextView = findViewById(R.id.tv_hint1)
        val tvHint2: TextView = findViewById(R.id.tv_hint2)
        tvHint1.setOnLongClickListener {
            copyMessage(tvHint1)
            return@setOnLongClickListener true
        }
        tvHint2.setOnLongClickListener {
            copyMessage(tvHint2)
            return@setOnLongClickListener true
        }
        val swType: SwitchCompat = findViewById(R.id.sw_type)
        val swCompare: SwitchCompat = findViewById(R.id.sw_compare)
        swCompare.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                swType.visibility = View.GONE
            } else {
                swType.visibility = View.VISIBLE
            }
        }
        val sbLimit: SeekBar = findViewById(R.id.sb_limit)
        val tvLimit: TextView = findViewById(R.id.tv_limit)
        sbLimit.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var lastChange = 0L
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (System.currentTimeMillis() - lastChange > 200) {
                    setLimitHint(tvLimit, sbLimit.progress)
                    lastChange = System.currentTimeMillis()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setLimitHint(tvLimit, sbLimit.progress)
                lastChange = System.currentTimeMillis()
            }
        })
        setLimitHint(tvLimit, sbLimit.progress)
        val btnDownload: Button = findViewById(R.id.btn_download)
        val etAddress: EditText = findViewById(R.id.et_address)
        val defUrl = urls[abs(Random().nextInt()) % urls.size]
        etAddress.setText(defUrl)
//        etAddress.setText("https://book.kotlincn.net/kotlincn-docs.pdf")
//        etAddress.setText("https://down11.qwp365.cn/app/yuanshen_2.1.0.apk")
        btnDownload.setOnClickListener {
            vMask.visibility = View.VISIBLE
            hintViewMap.clear()
            deleteFile(cacheDir)
            val address = etAddress.text.toString()
            if (address.isEmpty()) {
                Toast.makeText(this, "需要输入下载地址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            for (item in hintViewArray) {
                item.text = null
                item.isSelected = false
            }
            val limit = sbLimit.progress
            if (swCompare.visibility == View.VISIBLE && swCompare.isChecked) {
                val info1 = DownloadInfoManager.create(this, address, false, limit)
                Helpers.scheduleJob(this, info1)
                val info2 = DownloadInfoManager.create(this, address, true, limit)
                Helpers.scheduleJob(this, info2)
            } else {
                val info = DownloadInfoManager.create(this, address, swType.isChecked, limit)
                Helpers.scheduleJob(this, info)
            }
        }
        DownloadInfoManager.listener = downloadListener
//        limitArray.add(defLimit)
//        limitArray.add(Pair("5GB", 5L * 1024 * 1024 * 1024))
//        limitArray.add(Pair("2GB", 2L * DownloadInfoManager.GB))
//        limitArray.add(Pair("1GB", 1L * DownloadInfoManager.GB))
//        limitArray.add(Pair("500M", 500L * DownloadInfoManager.MB))
//        limitArray.add(Pair("200M", 200L * DownloadInfoManager.MB))
//        limitArray.add(Pair("100M", 100L * DownloadInfoManager.MB))
//        limitArray.add(Pair("50M", 50L * DownloadInfoManager.MB))
//        limitArray.add(Pair("20M", 20L * DownloadInfoManager.MB))
//        limitArray.add(Pair("10M", 10L * DownloadInfoManager.MB))
//        limitArray.add(Pair("5M", 5L * DownloadInfoManager.MB))
//        limitArray.add(Pair("2M", 2L * DownloadInfoManager.MB))
        hintViewArray.add(tvHint1)
        hintViewArray.add(tvHint2)
    }

    private fun setLimitHint(tvLimit: TextView, progress: Int) {
        if (progress == 0) {
            tvLimit.text = "下载限制：仅连接"
        } else {
            tvLimit.text = "下载限制：$progress %"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadInfoManager.listener = null
    }

    private fun deleteFile(file: File) {
        if (!file.exists()) {
            return
        }
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children?.isNotEmpty() == true) {
                for (item in children) {
                    deleteFile(item)
                }
            }
        }
        file.delete()
    }

    @Synchronized
    fun writeToFile(message: String) {
        val record = File(filesDir, "downloadSpeed.txt")
        if (!record.exists()) {
            record.createNewFile()
        }
        val writeInfo = "\r\n" + Date().toString() + "\r\n" + message + "\r\n"
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(record, true), "UTF-8"))
            writer.write(writeInfo)
        } catch (e: Exception) {
            e.printStackTrace();
        } finally {
            writer?.close()
        }
    }

    @Synchronized
    private fun showHintMessage(id: Int, message: String, remove: Boolean) {
        var view: TextView? = hintViewMap[id]
        if (remove) {
            loading.remove(id)
            if (view != null) {
                view.isSelected = false
            }
        } else {
            loading.add(id)
            if (view == null) {
                for (item in hintViewArray) {
                    if (!item.isSelected) {
                        view = item
                        break
                    }
                }
            }
            view?.isSelected = true
            hintViewMap[id] = view
        }
        runOnUiThread {
            if (view == null) {
                Log.e("TAG", "no views available to display messages")
            }
            view?.text = message
            Log.e("TAG", "loading = ${loading.size}")
            if (loading.size > 0) {
                vMask.visibility = View.VISIBLE
            } else {
                vMask.visibility = View.GONE
                for (item in hintViewArray) {
                    item.isSelected = false
                }
            }
        }
    }

    private fun copyMessage(view: TextView) {
        if (vMask.visibility == View.VISIBLE) {
            return
        }
        val message = view.text.toString()
        if (message.length < 10) {
            return
        }
        val cm: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("download", view.text.toString())
        cm.setPrimaryClip(mClipData)
        Toast.makeText(this, "已复制到剪切板", Toast.LENGTH_SHORT).show()
    }
}