/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.tiefensuche.soundcrowd.waveform

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.*
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Created by tiefensuche on 6/10/17.
 */
class WaveformView : RelativeLayout, OnTouchListener {

    private val position = Point()
    lateinit var imageView: ImageView
    private lateinit var mContext: Context
    private lateinit var mWaveform: View
    private lateinit var imageViewWaveformBackground: ImageView
    private lateinit var imageViewWaveformArea: ImageView
    private lateinit var cuePointView: RelativeLayout
    private lateinit var displaySize: Point
    var desiredWidth: Int = 0
    var desiredHeight: Int = 0
    private var x = 0
    private var click = false
    private var scrolling = false
    private var callback: Callback? = null

    private var duration: Long = 0

    constructor(context: Context, attribute: AttributeSet, defStyle: Int) : super(context, attribute, defStyle) {
        init(context)
    }

    constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {
        init(context)
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.waveform, this, true)
        this.mContext = context
        mWaveform = findViewById(R.id.waveform)
        imageView = findViewById(R.id.imageViewWaveform)
        imageViewWaveformArea = findViewById(R.id.imageViewWaveformArea)
        imageViewWaveformBackground = findViewById(R.id.imageViewWaveformBackground)
        cuePointView = findViewById(R.id.starView)
        imageView.setOnTouchListener(this)

        imageView.setBackgroundColor(Color.TRANSPARENT)
        imageView.setColorFilter(Color.BLACK)
    }

    fun setWaveform(waveform: Bitmap, duration: Int) {
        cuePointView.removeAllViews()
        this.duration = duration.toLong()
        imageView.setImageBitmap(waveform)
        imageViewWaveformArea.minimumWidth = desiredWidth
        imageViewWaveformArea.minimumHeight = desiredHeight
        imageViewWaveformBackground.minimumWidth = desiredWidth
        imageViewWaveformBackground.minimumHeight = desiredHeight
        setProgress(0, 1)
        scrolling = false
        callback?.onWaveformLoaded()
    }

    fun colorizeWaveform(vibrantColor: Int) {
        imageViewWaveformBackground.setBackgroundColor(vibrantColor)
    }

    fun setProgress(position: Int, duration: Int) {
        if (click) {
            return
        }
        if (duration > 0) {
            val displayPosition = Math.round(position.toFloat() / duration * desiredWidth)
            if (!scrolling) {
                x = displayPosition - displaySize.x / 2
                doScroll()
            }
        }
    }

    private fun doScroll() {
        if (x < -displaySize.x / 2) {
            x = -displaySize.x / 2
        }
        if (x > desiredWidth - displaySize.x / 2) {
            x = (desiredWidth - displaySize.x / 2).toFloat().roundToInt()
        }
        imageView.scrollTo(x, 0)
        cuePointView.scrollTo(x, 0)
        if (x < 0) {
            imageViewWaveformBackground.minimumWidth = displaySize.x / 2 + x + 1
            imageViewWaveformBackground.maxWidth = displaySize.x / 2 + x + 1
            imageViewWaveformBackground.x = (displaySize.x / 2 - (displaySize.x / 2 + x)).toFloat()
        } else {
            imageViewWaveformBackground.minimumWidth = displaySize.x / 2
            imageViewWaveformBackground.maxWidth = displaySize.x / 2
            imageViewWaveformBackground.x = 0f
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            click = true
            callback?.onSeeking()
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            if (!click) {
                x += (position.x - event.x).toInt()
                doScroll()
            }
            click = false
            position.set(event.x.roundToInt(), event.y.roundToInt())
            scrolling = true
        }
        if (event.action == MotionEvent.ACTION_UP) {
            callback?.onSeek(((x + displaySize.x.toFloat() / 2) * (duration.toFloat() / desiredWidth)).roundToLong())
            click = false
            scrolling = false
        }
        return true
    }

    fun setVisible(visibility: Int) {
        mWaveform.visibility = visibility
    }

    fun setDisplaySize(displaySize: Point) {
        this.displaySize = displaySize
        desiredHeight = displaySize.y / 6
        desiredWidth = (WAVEFORM_WIDTH * (desiredHeight.toFloat() / WAVEFORM_HEIGHT)).roundToInt()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun drawCuePoint(cuePoint: CuePoint, duration: Int, icon: Bitmap) {
        val imageViewStar = ImageView(mContext)
        imageViewStar.setOnClickListener { callback?.onSeek(cuePoint.position.toLong()) }

        val descLayout = LinearLayout(mContext)
        val textView = TextView(mContext)
        textView.maxWidth = 200
        textView.setBackgroundColor(Color.argb(150, 0, 0, 0))
        textView.textSize = 12f
        textView.text = cuePoint.description
        imageViewStar.setOnLongClickListener {
            val menu = PopupMenu(mContext, imageViewStar)
            menu.menuInflater.inflate(R.menu.popup_cue_point, menu.menu)
            menu.setOnMenuItemClickListener { menuItem ->
                val i = menuItem.itemId
                if (i == R.id.edit) {
                    val layoutInflater = LayoutInflater.from(mContext)
                    val promptView = layoutInflater.inflate(R.layout.input_dialog, null)
                    val alertDialogBuilder = AlertDialog.Builder(mContext)
                    alertDialogBuilder.setView(promptView)
                    alertDialogBuilder.setTitle(mContext.getString(R.string.title))

                    val editText = promptView.findViewById<EditText>(R.id.edittext)
                    editText.setText(textView.text)
                    // setup a dialog window
                    alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("OK") { _, _ ->
                                textView.text = editText.text
                                callback?.onCuePointSetText(cuePoint.mediaId, cuePoint.position, editText.text.toString())
                            }
                            .setNegativeButton(mContext.getString(R.string.cancel)
                            ) { dialog, _ -> dialog.cancel() }

                    // create an alert dialog
                    val alert = alertDialogBuilder.create()
                    alert.show()
                } else if (i == R.id.delete) {
                    cuePointView.removeView(imageViewStar)
                    cuePointView.removeView(descLayout)
                    callback?.onCuePointDelete(cuePoint.mediaId, cuePoint.position)
                }
                true
            }
            menu.show()
            true
        }
        imageViewStar.setImageBitmap(icon)
        imageViewStar.setColorFilter(Color.WHITE)
        val params = LayoutParams(icon.width, icon.height)
        params.leftMargin = (cuePoint.position.toLong() * desiredWidth / duration).toFloat().roundToInt() - icon.width / 2
        cuePointView.addView(imageViewStar, params)

        val paramsDesc = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        descLayout.addView(textView, paramsDesc)

        val paramsDescLayout = LayoutParams(textView.maxWidth, textView.maxHeight)
        paramsDescLayout.leftMargin = params.leftMargin + icon.width
        paramsDescLayout.topMargin = icon.height / 4
        cuePointView.addView(descLayout, paramsDescLayout)
    }

    interface Callback {
        fun onSeek(position: Long)
        fun onSeeking()
        fun onCuePointSetText(mediaId: String, position: Int, text: String)
        fun onCuePointDelete(mediaId: String, position: Int)
        fun onWaveformLoaded()
    }

    companion object {
        private const val WAVEFORM_WIDTH = 1800
        private const val WAVEFORM_HEIGHT = 140
    }
}