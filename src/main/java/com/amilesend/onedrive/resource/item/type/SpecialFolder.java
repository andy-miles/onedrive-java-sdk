/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2024 Andy Miles (andy.miles@amilesend.com)
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Describes if the item is a special managed folder.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/specialfolder">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class SpecialFolder {
    /**
     * The special folder name. Valid values include:
     * <ul>
     *     <li>App Root (ID: {@literal approot})</li>
     *     <li>Camera Roll (ID: {@literal cameraroll})</li>
     *     <li>Desktop (ID: {@literal desktop}</li>
     *     <li>Documents (ID: {@literal documents}</li>
     *     <li>Music (ID: {@literal music}</li>
     *     <li>Photos (ID: {@literal photos}</li>
     * </ul>
     */
    private final String name;

    /** Describes the special folder types. */
    @RequiredArgsConstructor
    @Getter
    public enum Type {
        APP_ROOT("App Root", "approot"),
        CAMERA_ROLL("Camera Roll", "cameraroll"),
        DESKTOP("Desktop", "desktop"),
        DOCUMENTS("Documents", "documents"),
        MUSIC("Music", "music"),
        PHOTOS("Photos", "photos");

        private final String specialFolderName;
        private final String id;
    }
}
