/*
 * SPDX-License-Identifier: GPL-3.0-only
 */

package com.tiefensuche.soundcrowd.waveform

/**
 * Created by tiefensuche on 6/14/17.
 */

class CuePoint(var mediaId: String, val position: Int, var description: String) {

    override fun toString(): String {
        return description
    }
}