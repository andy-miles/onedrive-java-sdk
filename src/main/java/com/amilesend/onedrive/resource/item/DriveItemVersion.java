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
package com.amilesend.onedrive.resource.item;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.type.PublicationFacet;
import com.google.common.annotations.VisibleForTesting;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.nio.file.Path;

import static com.amilesend.onedrive.resource.item.DriveItem.DRIVE_ITEM_BASE_URL_PATH;

/**
 * Represents a specific version of a drive item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/driveitemversion">
 * API Documentation.</a>
 * @see DriveItem
 */
@Data
public class DriveItemVersion {
    @VisibleForTesting
    static final int NO_CONTENT_RESPONSE_HTTP_CODE = 204;

    private static final String VERSIONS_URL_SPECIFIER = "/versions/";
    private static final String CONTENT_URL_SUFFIX = "/content";
    private static final String RESTORE_URL_SUFFIX = "/restore";

    /** The version identifier. */
    private String id;
    /** The identity of whom last modified the version. */
    private IdentitySet lastModifiedBy;
    /** The date and time of when the version was last modified. */
    private String lastModifiedDateTime;
    /** Describes the published status of the version. */
    private PublicationFacet publication;
    /** The size in bytes of the version. */
    private long size;

    /** The name of the item associated with the version. */
    // Used to structure the name of the download.
    @GsonExclude
    private String name;
    /** The associated item identifier for the version. */
    @GsonExclude
    private String driveItemId;

    @GsonExclude
    @EqualsAndHashCode.Exclude
    private final OneDriveConnection connection;

    /**
     * Downloads a specific drive item version.
     *
     * @param folderPath the path of the folder to download the drive item version content to
     */
    public void download(final Path folderPath) {
        download(folderPath, LogProgressCallback.builder()
                .transferType(LogProgressCallback.TransferType.DOWNLOAD)
                .build());
    }

    /**
     * Downloads a specific drive item version and reports transfer progress to the specified
     * {@link TransferProgressCallback}.
     *
     * @param folderPath the path of the folder to download the drive item version content to
     * @param callback the callback be notified of transfer progress
     */
    public void download(@NonNull final Path folderPath, @NonNull TransferProgressCallback callback) {
        final String versionId = getId();
        final String name = getName();
        final String driveItemId = getDriveItemId();
        Validate.notBlank(versionId, "id must not be blank");
        Validate.notBlank(name, "name must not be blank");
        Validate.notBlank(driveItemId, "driveItemId must not be blank");

        connection.download(
                connection.newSignedForRequestBuilder()
                        .url(getContentsUrl(driveItemId, versionId))
                        .build(),
                folderPath,
                name,
                getSize(),
                callback);
    }

    public boolean restore() {
        final String versionId = getId();
        final String driveItemId = getDriveItemId();
        Validate.notBlank(versionId, "id must not be blank");
        Validate.notBlank(driveItemId, "driveItemId must not be blank");

        final int responseCode = connection.execute(connection.newSignedForRequestBuilder()
                .url(getRestoreUrl(driveItemId, versionId))
                .build());
        return responseCode == NO_CONTENT_RESPONSE_HTTP_CODE;
    }

    private String getContentsUrl(final String driveItemId, final String versionId) {
        return getVersionBasedUrl(driveItemId, versionId, CONTENT_URL_SUFFIX);
    }

    private String getRestoreUrl(final String driveItemId, final String versionId) {
        return getVersionBasedUrl(driveItemId, versionId, RESTORE_URL_SUFFIX);
    }

    private String getVersionBasedUrl(final String driveItemId, final String versionId, final String suffix) {
        return new StringBuilder(connection.getBaseUrl())
                .append(DRIVE_ITEM_BASE_URL_PATH)
                .append(driveItemId)
                .append(VERSIONS_URL_SPECIFIER)
                .append(versionId)
                .append(suffix)
                .toString();
    }
}
