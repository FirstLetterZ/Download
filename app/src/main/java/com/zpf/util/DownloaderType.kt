package com.zpf.util

object DownloaderType {
    const val ANDROID: Int = 1
    const val XIAOMI: Int = 2
    const val TENCENT: Int = 3
    const val YC_DOWNLOAD: Int = 4
    const val OK_DOWNLOAD: Int = 5

    fun typeName(type: Int): String? {
        return when (type) {
            ANDROID -> "android下载器"
            XIAOMI -> "miui下载器"
            TENCENT -> "腾讯下载器"
            YC_DOWNLOAD -> "YC下载器"
            OK_DOWNLOAD -> "OkDownload下载器"
            else -> null
        }
    }
}