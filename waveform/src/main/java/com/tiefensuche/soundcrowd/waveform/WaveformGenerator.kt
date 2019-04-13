/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.tiefensuche.soundcrowd.waveform

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import org.json.JSONArray
import org.json.JSONException

/**
 * Created by tiefensuche on 6/10/17.
 */
object WaveformGenerator {

    private const val BAR_WIDTH = 7
    private const val BAR_GAP = 3
    private const val BOTTOM_BORDER = 3

    @Throws(JSONException::class)
    @JvmOverloads
    fun generateWaveform(array: JSONArray, width: Int, height: Int, barWidth: Int = BAR_WIDTH, barGap: Int = BAR_GAP, bottomBorder: Int = BOTTOM_BORDER): Bitmap {
        var norm = 0.0
        for (i in 0 until array.length()) {
            if (norm < array.getDouble(i)) {
                norm = array.getDouble(i)
            }
        }
        val image = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        image.eraseColor(Color.TRANSPARENT)
        val g = Canvas(image)

        val paint = Paint()
        paint.isAntiAlias = false
        paint.color = Color.BLACK

        val sampleSize = Math.ceil((array.length().toFloat() / (width / (barWidth + barGap))).toDouble()).toInt()
        val actualBarWidth = width.toFloat() / (barWidth + barGap) / (array.length() / sampleSize)
        var samples = DoubleArray(sampleSize)
        for (i in 0 until array.length()) {
            val value = (array.getDouble(i) / norm).toFloat()
            if (sampleSize == 0) {
                g.drawRect(i.toFloat() * (barWidth + barGap), height - value * height, i * (barWidth + barGap) + barWidth * actualBarWidth, height.toFloat() - bottomBorder, paint)
            } else if (i > 0 && i % sampleSize == 0 || i == array.length() - 1) {
                val rootMeanSquare = rootMeanSquare(samples)
                g.drawRect((i - 1) / sampleSize * (barWidth + barGap) * actualBarWidth, height - rootMeanSquare.toFloat() * height, ((i - 1) / sampleSize * (barWidth + barGap) + barWidth) * actualBarWidth, height.toFloat() - barGap, paint)
                samples = DoubleArray(sampleSize)
                samples[0] = value.toDouble()
            } else {
                samples[i % sampleSize] = value.toDouble()
            }
        }
        invert(image)
        return image
    }

    internal fun rootMeanSquare(values: DoubleArray): Double {
        var squares = 0.0
        for (value in values) {
            squares += value * value
        }
        return Math.sqrt(squares / values.size)
    }

    private fun invert(bitmap: Bitmap) {
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                val color = bitmap.getPixel(x, y)
                if (color == Color.BLACK)
                    bitmap.setPixel(x, y, Color.TRANSPARENT)
                else
                    bitmap.setPixel(x, y, Color.BLACK)
            }
        }
    }
}