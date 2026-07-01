package com.lisitede.preset.preset.dialog

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kongzue.dialogx.dialogs.CustomDialog
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import com.kongzue.dialogx.interfaces.OnBindView
import com.lisitede.preset.deviceinfo.DeviceInfoEntry
import com.lisitede.preset.preset.R
import com.stx.xhb.xbanner.XBanner
import com.stx.xhb.xbanner.entity.SimpleBannerInfo

data class DetailInfoDialogData(
    val appInfo: List<DeviceInfoEntry>,
    val identityInfo: List<DeviceInfoEntry>
)

private data class DetailInfoSlide(
    val title: String,
    val rows: List<Pair<String, String>>
) : SimpleBannerInfo() {

    override fun getXBannerUrl(): Any = ""
}

class DetailInfoDialogRenderer(private val data: DetailInfoDialogData) {

    fun show() {
        val slides = listOf(
            DetailInfoSlide(
                title = "App Info",
                rows = data.appInfo.toRows()
            ),
            DetailInfoSlide(
                title = "Identity",
                rows = data.identityInfo.toRows()
            )
        )

        CustomDialog.build()
            .setMaskColor(Color.parseColor("#80000000"))
            .setDialogLifecycleCallback(object : DialogLifecycleCallback<CustomDialog>() {
                override fun onShow(dialog: CustomDialog) {
                    Log.d(TAG, "onShow")
                }

                override fun onDismiss(dialog: CustomDialog) {
                    Log.d(TAG, "onDismiss")
                }
            })
            .setCustomView(object : OnBindView<CustomDialog>(R.layout.dialog_detail_info) {
                override fun onBind(dialog: CustomDialog, v: View) {
                    val displayMetrics = v.resources.displayMetrics
                    dialog.setWidth((displayMetrics.widthPixels * 0.8).toInt())
                    val banner = v.findViewById<XBanner>(R.id.detailInfoBanner)
                    banner.setAutoPlayAble(false)
                    banner.setAllowUserScrollable(true)
                    banner.setPointsIsVisible(true)
                    banner.setBannerData(R.layout.item_detail_info_slide, slides)
                    banner.loadImage(object : XBanner.XBannerAdapter {
                        override fun loadBanner(
                            banner: XBanner,
                            model: Any?,
                            view: View,
                            position: Int
                        ) {
                            val slide = model as DetailInfoSlide
                            view.findViewById<TextView>(R.id.detailInfoSlideTitle).text = slide.title
                            val rowsContainer =
                                view.findViewById<LinearLayout>(R.id.detailInfoSlideRows)
                            rowsContainer.removeAllViews()
                            slide.rows.forEach { (label, value) ->
                                val labelTv = TextView(view.context).apply {
                                    text = label
                                    textSize = 13f
                                    setPadding(0, dp(8, view), 0, 0)
                                }
                                val valueTv = TextView(view.context).apply {
                                    text = value
                                    textSize = 15f
                                }
                                rowsContainer.addView(labelTv)
                                rowsContainer.addView(valueTv)
                            }
                        }
                    })

                    v.findViewById<View>(R.id.detailInfoClose).setOnClickListener {
                        dialog.dismiss()
                    }
                }
            })
            .show()
    }

    private fun dp(value: Int, view: View): Int {
        val density = view.resources.displayMetrics.density
        return (value * density + 0.5f).toInt()
    }

    private fun List<DeviceInfoEntry>.toRows(): List<Pair<String, String>> {
        return filter { it.value.isNotEmpty() }
            .map { it.label to it.value }
    }

    private companion object {
        const val TAG = "DetailInfoDialog"
    }
}
