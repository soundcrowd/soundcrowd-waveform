# soundcrowd-waveform

[![android](https://github.com/soundcrowd/soundcrowd-waveform/actions/workflows/android.yml/badge.svg)](https://github.com/soundcrowd/soundcrowd-waveform/actions/workflows/android.yml)

This android module provides a waveform control view intended to use for media players as seek control. It provides functionality to extract waveform data from music files (mp3, wav, ogg) in JSON format and generates waveform images based on that data.

This module is part of the [soundcrowd](https://github.com/soundcrowd/soundcrowd) android media player.

## Features

- waveform control view
- waveform data extraction in JSON format for various file formats
- waveform generation based on that JSON data
- place markers on the waveform and jump to that positions

## License

This module is licensed under GPLv3.

## Dependencies

The waveform extraction is based on [RingDroid](https://github.com/google/ringdroid) - (c) 2016 Google Apache 2 license.
