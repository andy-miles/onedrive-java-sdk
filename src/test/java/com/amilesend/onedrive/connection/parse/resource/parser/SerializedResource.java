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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

public enum SerializedResource {
    ASYNC_JOB_STATUS("/AsyncJobStatus.json"),
    DRIVE("/Drive.json"),
    DRIVE_ITEM("/DriveItem.json"),
    DRIVE_ITEM_FOLDER("/DriveItemFolder.json"),
    DRIVE_ITEM_LIST("/DriveItemList.json"),
    DRIVE_ITEM_PAGE("/DriveItemPage.json"),
    DRIVE_ITEM_ROOT_FOLDER("/DriveItemRootFolder.json"),
    DRIVE_ITEM_VERSION_LIST("/DriveItemVersionList.json"),
    DRIVE_ITEM_ZIP_FILE("/DriveItemZipFile.json"),
    DRIVE_LIST("/DriveList.json"),
    ITEM_ACTIVITY_LIST("/ItemActivityList.json"),
    PERMISSION("/Permission.json"),
    PERMISSION_LIST("/PermissionList.json"),
    PREVIEW("/Preview.json"),
    SINGLE_DRIVE_ITEM_PAGE("/SingleDriveItemPage.json"),
    SPECIAL_DRIVE_ITEM("/SpecialDriveItem.json"),
    THUMBNAIL_SET_LIST("/ThumbnailSetList.json"),
    UPDATED_DRIVE_ITEM_ZIP_FILE("/UpdatedDriveItemZipFile.json");

    private final String resourcePath;

    SerializedResource(final String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public InputStream getResource() {
        return new BufferedInputStream(this.getClass().getResourceAsStream(resourcePath));
    }

    public byte[] toGzipCompressedBytes() throws IOException {
        final byte[] uncompressed = getResource().readAllBytes();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(uncompressed.length);
        try(final GZIPOutputStream gos = new GZIPOutputStream(baos)) {
            gos.write(uncompressed);
        } finally {
            baos.close();
        }

        return baos.toByteArray();
    }
}
