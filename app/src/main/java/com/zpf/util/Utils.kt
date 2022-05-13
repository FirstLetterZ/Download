package com.zpf.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import kotlin.collections.ArrayList

const val GB = 1024L * 1024L * 1024L
const val MB = 1024L * 1024L
const val KB = 1024L

fun toGB(bytes: Long): Float = bytes.toFloat() / GB
fun toMB(bytes: Long): Float = bytes.toFloat() / MB
fun toKB(bytes: Long): Float = bytes.toFloat() / KB

fun copyMessage(view: TextView) {
    val message = view.text.toString()
    if (message.length < 10) {
        return
    }
    val context = view.context
    val cm: ClipboardManager =
        context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
    val mClipData = ClipData.newPlainText("download", view.text.toString())
    cm.setPrimaryClip(mClipData)
    Toast.makeText(context, "已复制到剪切板", Toast.LENGTH_SHORT).show()
}

@Synchronized
fun writeToFile(context: Context, message: String) {
    val record = File(context.filesDir, "downloadSpeed.txt")
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

fun deleteFile(file: File) {
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

object LoadHistory {
    private val preparedDownloadUrls = arrayOf(
        "https://book.kotlincn.net/kotlincn-docs.pdf",
        "https://down11.qwp365.cn/app/yuanshen_2.1.0.apk",
        "https://appdl-1-drcn.dbankcdn.com/dl/appdl/application/apk/0f/0f24612b639341b5b79f0962fdbd20a3/com.tencent.tmgp.cod.2201190904.apk",
        "https://appdl-1-drcn.dbankcdn.com/dl/appdl/application/apk/2c/2c8feaebc38646409ca189edb0a15438/com.netease.mrzh.huawei.2202221748.apk",
        "https://appdlc-drcn.hispace.dbankcloud.cn/dl/appdl/application/apk/a7/a773217c1cb344249af6c41775a65a5d/com.netease.aceracer.huawei.2203031642.apk"
    )
    val historyList = LinkedList<String>()

    init {
        historyList.addAll(preparedDownloadUrls)
    }

    fun recordHistory(url: String) {
        if (preparedDownloadUrls.contains(url)) {
            return
        } else {
            historyList.remove(url)
            historyList.add(preparedDownloadUrls.size, url)
        }
        while (historyList.size > 10) {
            historyList.pollLast()
        }
    }
}