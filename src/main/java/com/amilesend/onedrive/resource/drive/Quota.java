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
package com.amilesend.onedrive.resource.drive;

import lombok.Data;

/**
 * Describes details about storage space for a drive.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/quota"> API Documentation.</a>
 */
@Data
public class Quota {
    /** Total storage space in bytes. */
    private long total;
    /** Used storage space in bytes. */
    private long used;
    /** Remaining storage space in bytes. */
    private long remaining;
    /** Storage space used by deleted items (recycle bin) in bytes. */
    private long deleted;
    /** The storage space state.  Valid values include:
     * <ul>
     *     <li>{@literal normal}</li>
     *     <li>{@literal nearing}</li>
     *     <li>{@literal critical}</li>
     *     <li>{@literal exceeded}</li>
     * </ul>
     */
    private String state;
    /** The total number of files (not applicable to personal OneDrive accounts). */
    private long fileCount;
}
