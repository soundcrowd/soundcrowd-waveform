/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.tiefensuche.soundcrowd.waveform

import android.content.Context
import android.net.Uri

import com.ringdroid.soundfile.SoundFile

import org.json.JSONArray
import org.json.JSONException

import java.io.IOException

/**
 * Created by tiefensuche on 6/10/17.
 */
object WaveformExtractor {

    @Throws(IOException::class, JSONException::class, SoundFile.InvalidInputException::class)
    fun extractWaveform(context: Context, uri: Uri): JSONArray {
        val soundFile = SoundFile.create(context, uri)
                ?: throw IOException("Error when creating sound file")
        val values = computeDoublesForAllZoomLevels(soundFile.numFrames, soundFile.frameGains)

        val sampleSize = (values.size / 1800).coerceAtLeast(1)
        var sampleValues = DoubleArray(sampleSize)
        val array = JSONArray()
        for (i in values.indices) {
            if (i > 0 && i % sampleSize == 0) {
                array.put(WaveformGenerator.rootMeanSquare(sampleValues))
                sampleValues = DoubleArray(sampleSize)
                sampleValues[0] = values[i].toDouble()
            } else {
                sampleValues[i % sampleSize] = values[i].toDouble()
            }
        }
        return array
    }

    private fun getGain(i: Int, numFrames: Int, frameGains: IntArray): Float {
        return if (numFrames < 2) {
            frameGains[i].toFloat()
        } else {
            when (i) {
                0 -> frameGains[0] / 2.0f + frameGains[1] / 2.0f
                numFrames - 1 -> frameGains[numFrames - 2] / 2.0f + frameGains[numFrames - 1] / 2.0f
                else -> frameGains[i - 1] / 3.0f + frameGains[i] / 3.0f + frameGains[i + 1] / 3.0f
            }
        }
    }

    private fun computeDoublesForAllZoomLevels(numFrames: Int, frameGains: IntArray): FloatArray {

        // Make sure the range is no more than 0 - 255
        var maxGain = 1.0f
        for (i in 0 until numFrames) {
            val gain = getGain(i, numFrames, frameGains)
            if (gain > maxGain) {
                maxGain = gain
            }
        }
        var scaleFactor = 1.0f
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0f
        val gainHist = IntArray(256)
        for (i in 0 until numFrames) {
            var smoothedGain = (getGain(i, numFrames, frameGains) * scaleFactor).toInt()
            if (smoothedGain < 0)
                smoothedGain = 0
            if (smoothedGain > 255)
                smoothedGain = 255

            if (smoothedGain > maxGain)
                maxGain = smoothedGain.toFloat()

            gainHist[smoothedGain]++
        }

        // Re-calibrate the min to be 5%
        var minGain = 0f
        var sum = 0
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[minGain.toInt()]
            minGain++
        }

        // Re-calibrate the max to be 99%
        sum = 0
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[maxGain.toInt()]
            maxGain--
        }

        val range = maxGain - minGain
        val values = FloatArray(numFrames)

        for (i in 0 until numFrames) {
            var value = (getGain(i, numFrames, frameGains) * scaleFactor - minGain) / range
            if (value < 0.0)
                value = 0.0f
            if (value > 1.0)
                value = 1.0f
            values[i] = value
        }

        return values
    }
}