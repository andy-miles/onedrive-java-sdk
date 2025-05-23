/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2025 Andy Miles (andy.miles@amilesend.com)
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

import lombok.Builder;
import lombok.Data;

/**
 * Describes the photo attributes for a drive item file.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/photo">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class Photo {
    /** The date and time the photo was taken. */
    private final String takenDateTime;
    /** The camera manufacturer. */
    private final  String cameraMake;
    /** The camera model. */
    private final  String cameraModel;
    /** The F-stop value from the camera. */
    private final  double fNumber;
    /** The denominator for the exposure time fraction from the camera. */
    private final  double exposureDenominator;
    /** The numerator for the exposure time fraction from the camera. */
    private final  double exposureNumerator;
    /** The focal length from the camera. */
    private final  double focalLength;
    /** The ISO value from the camera. */
    private final  int iso;
}
