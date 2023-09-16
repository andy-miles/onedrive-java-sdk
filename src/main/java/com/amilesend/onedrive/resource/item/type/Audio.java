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
 * If a drive item is an audio file, this describes its attributes.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/audio">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Data
public class Audio {
    /** The album title. */
    private String album;
    /** The album artist. */
    private String albumArtist;
    /** The performing artist. */
    private String artist;
    /** The bitrate in kbps. */
    private long bitrate;
    /** The composer. */
    private String composers;
    /** The copyright information. */
    private String copyright;
    /** The disc number associated with the track. */
    private short disc;
    /** The total number of discs associated with the album. */
    private short discCount;
    /** The duration in milliseconds. */
    private long duration;
    /** The music genre. */
    private String genre;
    /** Indicates if the audio file is protected with digital rights management. */
    private boolean hasDrm;
    /** indicates if the file isencoded with a variable bitrate. */
    private boolean isVariableBitrate;
    /** The song title. */
    private String title;
    /** The track number for the associated album. */
    private int track;
    /** The total number of tracks for the associated album. */
    private int trackCount;
    /** The year the track was recorded. */
    private int year;
}
