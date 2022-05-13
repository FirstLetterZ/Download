package com.zpf.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import com.zpf.download.R

class HistoryDialog(context: Context) : Dialog(context) {
    private val historyList = ArrayList<String>()
    private val historyAdapter = ArrayAdapter(context, R.layout.item_text, historyList)
    var listener: OnSelectListener? = null

    init {
        val d = context.resources.displayMetrics.density
        val padding = (8 * d).toInt()

        window?.run {
            requestFeature(1)
            decorView.setPadding(0, 0, 0, 0)
            attributes?.gravity = Gravity.CENTER
            attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes?.height = (600 * d).toInt()
            setBackgroundDrawableResource(android.R.color.transparent)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
        val listView = ListView(context)
        listView.setPadding(padding, padding, padding, padding)
        listView.setBackgroundColor(Color.WHITE)
        listView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val contentView = LinearLayout(context)
        contentView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        contentView.setPadding(3 * padding, 3 * padding, 3 * padding, 3 * padding)
        contentView.addView(listView)
        setContentView(contentView)
        listView.adapter = historyAdapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                listener?.onSelect(historyList[position])
                dismiss()
            }
        setCanceledOnTouchOutside(true)
    }

    interface OnSelectListener {
        fun onSelect(content: String)
    }

    override fun onStart() {
        historyList.clear()
        historyList.addAll(LoadHistory.historyList)
        historyAdapter.notifyDataSetChanged()
        super.onStart()
    }

}