# soundcrowd-waveform
[![Build Status](https://travis-ci.org/soundcrowd/soundcrowd-waveform.svg?branch=master)](https://travis-ci.org/soundcrowd/soundcrowd-waveform)[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7f11d5baaf674e10bcb7f14b13b78232)](https://www.codacy.com/app/tiefensuche/soundcrowd-waveform?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=soundcrowd/soundcrowd-waveform&amp;utm_campaign=Badge_Grade)

This android module provides a waveform control view intended to use for media players as seek control. It provides functionality to extract waveform data from music files (mp3, wav, ogg) in JSON format and generates waveform images based on that data.

This module is part of the [soundcrowd](https://github.com/soundcrowd/soundcrowd) android media player.

## Functionality

- waveform control view
- waveform data extraction in JSON format for various file formats
- waveform generation based on that JSON data
- place markers on the waveform and jump to that positions

## License

This module is licensed under GPLv3.

## Dependencies

The waveform extraction is based on [RingDroid](https://github.com/google/ringdroid) - (c) 2016 Google Apache 2 license.
