/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023 Andy Miles (andy.miles@amilesend.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.amilesend.onedrive.resource.item.type;

import lombok.Data;

/**
 * If a drive item is a video file, this describes its attributes.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/video">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Data
public class Video {
    /** Number of bits per sample. */
    private int audioBitsPerSample;
    /** Number of channels. */
    private int audioChannels;
    /** Encoded audio format. */
    private String audioFormat;
    /** Number of samples per second for audio. */
    private int audioSamplesPerSecond;
    /** The video bitrate. */
    private int bitrate;
    /** The duration of the video in milliseconds. */
    private long duration;
    /** The video format ("Four Character Code"). */
    private String fourCC;
    /** The video frame rate (frams per second). */
    private double frameRate;
    /** The video height in pixels. */
    private int height;
    /** The video width in pixels. */
    private int width;
}
