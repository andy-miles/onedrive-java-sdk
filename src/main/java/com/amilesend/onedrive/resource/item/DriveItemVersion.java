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
package com.amilesend.onedrive.resource.item;

import com.amilesend.client.connection.file.LogProgressCallback;
import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.parse.strategy.GsonExclude;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.DriveFileDownloadExecution;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.type.PublicationFacet;
import com.google.common.annotations.VisibleForTesting;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;

import static com.amilesend.client.connection.file.LogProgressCallback.formatPrefix;
import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.resource.item.DriveItem.DRIVE_ITEM_BASE_URL_PATH;

/**
 * Represents a specific version of a drive item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/driveitemversion">
 * API Documentation.</a>
 * @see DriveItem
 */
@Builder
@Data
public class DriveItemVersion {
    @VisibleForTesting
    static final int NO_CONTENT_RESPONSE_HTTP_CODE = 204;

    private static final String VERSIONS_URL_SPECIFIER = "/versions/";
    private static final String CONTENT_URL_SUFFIX = "/content";
    private static final String RESTORE_URL_SUFFIX = "/restoreVersion";

    /** The version identifier. */
    private final String id;
    /** The identity of whom last modified the version. */
    private final IdentitySet lastModifiedBy;
    /** The date and time of when the version was last modified. */
    private final String lastModifiedDateTime;
    /** Describes the published status of the version. */
    private final PublicationFacet publication;
    /** The size in bytes of the version. */
    private final long size;
    /** The name of the item associated with the version. */
    // Used to structure the name of the download.
    @GsonExclude
    private final String name;
    /** The associated item identifier for the version. */
    @GsonExclude
    private final String driveItemId;
    @GsonExclude
    @EqualsAndHashCode.Exclude
    private final OneDriveConnection connection;

    /**
     * Downloads a specific drive item version.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitemversion_get_contents">
     * API Documentation</a>.
     *
     * @param folderPath the path of the folder to download the drive item version content to
     */
    public void download(final Path folderPath) {
        download(
                folderPath,
                LogProgressCallback.builder()
                        .prefix(formatPrefix("OneDrive", folderPath.toFile().getName()))
                        .transferType(LogProgressCallback.TransferType.DOWNLOAD)
                        .build());
    }

    /**
     * Downloads a specific drive item version and reports transfer progress to the specified
     * {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitemversion_get_contents">
     * API Documentation</a>.
     *
     * @param folderPath the path of the folder to download the drive item version content to
     * @param callback the callback be notified of transfer progress
     */
    public void download(@NonNull final Path folderPath, @NonNull TransferProgressCallback callback) {
        connection.download(
                connection.newRequestBuilder()
                        .url(getContentUrl(getDriveItemId(), getId()))
                        .build(),
                folderPath,
                getName(),
                getSize(),
                callback);
    }

    /**
     * Downloads a specific drive item version asynchronously.
     *
     * @param folderPath the folder to download the file to
     * @return the asynchronous execution that contains the number of bytes downloaded
     * @see DriveFileDownloadExecution
     */
    public DriveFileDownloadExecution downloadAsync(final Path folderPath) {
        return downloadAsync(
                folderPath,
                LogProgressCallback.builder()
                        .prefix(formatPrefix("OneDrive", folderPath.toFile().getName()))
                        .transferType(LogProgressCallback.TransferType.DOWNLOAD)
                        .build());
    }

    /**
     * Downloads a specific drive item version asynchronously and reports transfer progress to the specified
     * {@link TransferProgressCallback}. Consumers can block on transfer completion by invoking
     * {@link DriveFileDownloadExecution#get()}.
     *
     * @param folderPath the folder to download the file to
     * @param callback the callback be notified of transfer progress
     * @return the asynchronous execution that contains the number of bytes downloaded
     * @see TransferProgressCallback
     * @see DriveFileDownloadExecution
     */
    public DriveFileDownloadExecution downloadAsync(
            @NonNull final Path folderPath,
            @NonNull TransferProgressCallback callback) {
        return new DriveFileDownloadExecution(connection.downloadAsync(
                connection.newRequestBuilder()
                        .url(getContentUrl(getDriveItemId(), getId()))
                        .build(),
                folderPath,
                getName(),
                getSize(),
                callback));
    }

    /**
     * Restores this version as the primary drive item version.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitemversion_restore">
     * API Documentation</a>.
     *
     * @return {@code true} if successful; else, {@code false}
     */
    public boolean restore() {
        final int responseCode = connection.execute(
                connection.newRequestBuilder()
                        .url(getRestoreUrl(getDriveItemId(), getId()))
                        .post(RequestBody.create(StringUtils.EMPTY, JSON_MEDIA_TYPE))
                        .build())
                .code();
        return responseCode == NO_CONTENT_RESPONSE_HTTP_CODE;
    }

    private String getContentUrl(final String driveItemId, final String versionId) {
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
