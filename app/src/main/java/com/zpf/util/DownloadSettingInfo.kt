package com.zpf.util

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.IntRange

class DownloadSettingInfo() : Parcelable {
    var url: String = ""
    var type: Int = DownloaderType.ANDROID

    @IntRange(from = 0, to = 100)
    var process: Int = 100

    constructor(parcel: Parcel) : this() {
        url = parcel.readString() ?: ""
        type = parcel.readInt()
        process = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeInt(type)
        parcel.writeInt(process)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadSettingInfo> {
        override fun createFromParcel(parcel: Parcel): DownloadSettingInfo {
            return DownloadSettingInfo(parcel)
        }

        override fun newArray(size: Int): Array<DownloadSettingInfo?> {
            return arrayOfNulls(size)
        }
    }

}
