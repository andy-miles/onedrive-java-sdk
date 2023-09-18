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
package com.amilesend.onedrive.connection.parse.resource.parser;

import java.io.BufferedInputStream;
import java.io.InputStream;

public enum SerializedResource {
    ASYNC_JOB_STATUS("/AsyncJobStatus.json"),
    DRIVE("/Drive.json"),
    DRIVE_ITEM("/DriveItem.json"),
    DRIVE_ITEM_LIST("/DriveItemList.json"),
    DRIVE_ITEM_PAGE("/DriveItemPage.json"),
    DRIVE_ITEM_VERSION_LIST("/DriveItemVersionList.json"),
    DRIVE_ITEM_ZIP_FILE("/DriveItemZipFile.json"),
    DRIVE_LIST("/DriveList.json"),
    ITEM_ACTIVITY_LIST("/ItemActivityList.json"),
    PERMISSION("/Permission.json"),
    PERMISSION_LIST("/PermissionList.json"),
    PREVIEW("/Preview.json"),
    SPECIAL_DRIVE_ITEM("/SpecialDriveItem.json"),
    THUMBNAIL_SET_LIST("/ThumbnailSetList.json");

    private final String resourcePath;

    SerializedResource(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public InputStream getResource() {
        return new BufferedInputStream(this.getClass().getResourceAsStream(resourcePath));
    }
}
