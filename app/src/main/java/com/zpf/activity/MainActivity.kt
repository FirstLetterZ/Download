package com.zpf.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zpf.download.R
import com.zpf.util.DownloadSettingInfo
import com.zpf.util.DownloaderType
import com.zpf.util.HistoryDialog

class MainActivity : AppCompatActivity() {
    private lateinit var etAddress: EditText
    private val downloadSettingInfo = DownloadSettingInfo()
    private val historyDialog: HistoryDialog by lazy {
        val dialog = HistoryDialog(this)
        dialog.listener = object : HistoryDialog.OnSelectListener {
            override fun onSelect(content: String) {
                etAddress.setText(content)
            }
        }
        dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        val rgTypes: RadioGroup = findViewById(R.id.rg_types)
        etAddress = findViewById(R.id.et_address)
        etAddress.setText("https://book.kotlincn.net/kotlincn-docs.pdf")
        findViewById<View>(R.id.btn_download).setOnClickListener {
            val address = etAddress.text.toString()
            if (address.isEmpty()) {
                Toast.makeText(this, "需要输入下载地址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            downloadSettingInfo.url = address
            val newIntent = Intent(this, LoadingActivity::class.java)
            newIntent.putExtra("setting", downloadSettingInfo)
            startActivity(newIntent)
        }
        findViewById<View>(R.id.btn_record).setOnClickListener {
            historyDialog.show()
        }
        initTypeGroup(rgTypes)
        setLimitHint(tvLimit, sbLimit.progress)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    private fun setLimitHint(tvLimit: TextView, progress: Int) {
        if (progress == 0) {
            tvLimit.text = "下载限制：仅连接"
        } else {
            tvLimit.text = "下载限制：$progress %"
        }
        downloadSettingInfo.process = progress
    }

    private fun initTypeGroup(rgTypes: RadioGroup) {
        var type = DownloaderType.ANDROID
        val d = resources.displayMetrics.density
        var typeName: String? = DownloaderType.typeName(type)
        while (typeName?.isNotEmpty() == true) {
            val item = RadioButton(this)
            item.id = type
            item.text = typeName
            item.layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (44 * d).toInt())
            rgTypes.addView(item)
            type++
            typeName = DownloaderType.typeName(type)
        }
        rgTypes.setOnCheckedChangeListener { _, checkedId ->
            downloadSettingInfo.type = checkedId
        }
        rgTypes.check(DownloaderType.ANDROID)
    }

}