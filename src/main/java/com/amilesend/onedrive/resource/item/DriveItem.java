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

import com.amilesend.client.connection.file.ProgressReportingRequestBody;
import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.parse.strategy.GsonExclude;
import com.amilesend.client.parse.strategy.GsonSerializeExclude;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.item.type.Audio;
import com.amilesend.onedrive.resource.item.type.Deleted;
import com.amilesend.onedrive.resource.item.type.File;
import com.amilesend.onedrive.resource.item.type.FileSystemInfo;
import com.amilesend.onedrive.resource.item.type.Folder;
import com.amilesend.onedrive.resource.item.type.GeoCoordinates;
import com.amilesend.onedrive.resource.item.type.Image;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.Package;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.Photo;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.PublicationFacet;
import com.amilesend.onedrive.resource.item.type.RemoteItem;
import com.amilesend.onedrive.resource.item.type.SearchResult;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.item.type.Shared;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.item.type.Video;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import com.amilesend.onedrive.resource.request.PreviewRequest;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PAGE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.ITEM_ACTIVITY_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.THUMBNAIL_SET_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newDriveItemVersionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPermissionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPermissionParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPreviewParser;
import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * Describes a resource stored in a drive.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/driveitem">
 * API Documentation</a>.
 */
/*
 * TODO:
 *  1. Implement support for upload sessions if there's a use-case for it
 *     (https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_createuploadsession)
 *  2. Implement support for URL based uploads if there's a use-case for it
 *     (https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_upload_url)
 */
@Getter
@SuperBuilder
@ToString(callSuper = true)
public class DriveItem extends BaseItem {
    public static final String DRIVE_ITEM_BASE_URL_PATH = "/drive/items/";

    private static final String CONTENT_URL_SUFFIX = "/content";
    private static final int MAX_QUERY_LENGTH = 1000;

    /** The audio file attributes (read-only). */
    private final Audio audio;
    /** An eTag for the content of the item (read-only). */
    private final String cTag;
    /** Indicates if an item is a file (read-only). */
    private final File file;
    /** Describes if a given drive item is a folder resource type (read-only). */
    private final Folder folder;
    /** The image attributes for a file (read-only). */
    private final Image image;
    /** The geographic coordinates and elevation of a file (read-only). */
    private final GeoCoordinates location;
    /** If defined, malware was detected in the file (read-only). */
    private final Object malware;
    /** Indicates that a drive item is the top level item in a collection of items (read-only). */
    @SerializedName("package")
    private final Package _package;
    /** The photo attributes for a drive item file (read-only). */
    private final Photo photo;
    /** The published status of a drive item or version (read-only). */
    private final PublicationFacet publication;
    /** Indicates that a drive item references one that exists in another drive (read-only). */
    private final RemoteItem remoteItem;
    /* An empty object if defined; else is null */
    /** If defined, indicates that the item is the top-most folder in the drive (read-only). */
    private final Object root;
    /** Indicates that the item is in response to a search query (read-only). */
    private final SearchResult searchResult;
    /** Indicates that a drive item has been shared with others (read-only). */
    private final Shared shared;
    /** SharePoint resource identifiers for SharePoint and Business account items (read-only). */
    private final SharePointIds sharepointIds;
    /** The size of the item in bytes (read-only). */
    private final long size;
    /** Describes if the item is a special managed folder (read-only). */
    private final SpecialFolder specialFolder;
    /** The video file attributes (read-only). */
    private final Video video;
    /** The URL that can be used to download the file's content (read-only). */
    @SerializedName("@microsoft.graph.downloadUrl")
    @GsonSerializeExclude
    private final String downloadUrl;
    /** Gets the underlying connection instance. */
    @GsonExclude
    private final OneDriveConnection connection;

    /** Indicates if an item was deleted (read-only). */
    private Deleted deleted;
    @Setter
    /** Describes drive (client-side) properties of the local version of a drive item. */
    private FileSystemInfo fileSystemInfo;
    /**
     * Describes how to handle conflicts upon copy/move operations. Valid values include:
     * <ul>
     *     <li>{@literal fail}</li>
     *     <li>{@literal replace}</li>
     *     <li>{@literal rename}</li>
     * </ul>
     * This member is write-only.
     */
    @SerializedName("@microsoft.graph.conflictBehavior")
    @Setter
    private String conflictBehavior;
    /** The source URL for remote uploading of file contents (write-only). Currently not tested nor supported. */
    @EqualsAndHashCode.Exclude
    @GsonSerializeExclude
    @SerializedName("@microsoft.graph.sourceUrl")
    @Setter
    private String sourceUrl;

    ////////////////////////
    // Download
    ////////////////////////

    /**
     * Downloads the drive item and reports transfer progress to the given {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_get_content">
     * API Documentation</a>.
     *
     * @param folderPath the path of the folder to download the drive item content to
     * @param callback the callback to inform of transfer progress
     */
    public void download(@NonNull final Path folderPath, @NonNull final TransferProgressCallback callback) {
        connection.download(
                connection.newRequestBuilder()
                        .url(getContentUrl(validateAndGetUrlEncodedId()))
                        .build(),
                folderPath,
                getName(),
                getSize(),
                callback);
    }

    /**
     * Downloads the drive item asynchronously that reports transfer progress and completion to the specified
     * {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_get_content">
     * API Documentation</a>.
     *
     * @param folderPath the path of the folder to download the drive item content to
     * @param callback the callback to inform of transfer progress
     * @return the CompletableFuture used to fetch the number of bytes downloaded
     */
    public CompletableFuture<Long> downloadAsync(
            @NonNull final Path folderPath,
            @NonNull final TransferProgressCallback callback) {
        return connection.downloadAsync(
                connection.newRequestBuilder()
                        .url(getContentUrl(validateAndGetUrlEncodedId()))
                        .build(),
                folderPath,
                getName(),
                getSize(),
                callback);
    }

    ////////////////////////
    // Upload
    ////////////////////////

    /**
     * Uploads a file to replace the contents of this {@code DriveItem} and reports the
     * transfer status to the given {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_put_content">
     * API Documentation</a>.
     *
     * @param filePath the file to upload
     * @param callback the callback to inform of transfer progress
     * @return the updated drive item information
     * @throws IOException if unable to read or determine the file's content type
     */
    public DriveItem upload(@NonNull final Path filePath, @NonNull final TransferProgressCallback callback)
            throws IOException {
        return uploadInternal(
                getContentUrl(validateAndGetUrlEncodedId()),
                ProgressReportingRequestBody.builder()
                        .file(filePath)
                        .callback(callback)
                        .build());
    }

    /**
     * Uploads a file asynchronously to replace the contents of this {@code DriveItem} and reports the
     * transfer status to the given {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_put_content">
     * API Documentation</a>.
     *
     * @param filePath the file to upload
     * @param callback the callback to inform of transfer progress
     * @return the CompletableFuture to fetch the updated drive item information
     * @throws IOException if unable to read or determine the file's content type
     */
    public CompletableFuture<DriveItem> uploadAsync(
            @NonNull final Path filePath,
            @NonNull final TransferProgressCallback callback) throws IOException {
        return uploadInternalAsync(
                getContentUrl(validateAndGetUrlEncodedId()),
                ProgressReportingRequestBody.builder()
                        .file(filePath)
                        .callback(callback)
                        .build());
    }

    ////////////////////////
    // uploadNew
    ////////////////////////

    /**
     * Uploads a new file as a child of this {@code DriveItem} and reports the transfer status to the given
     * {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_put_content">
     * API Documentation</a>.
     *
     * @param filePath the file to upload
     * @param callback the callback to inform of transfer progress
     * @return the new child drive item associated with the uploaded file
     * @throws IOException if unable to read or determine the file's content type
     */
    public DriveItem uploadNew(@NonNull final Path filePath, @NonNull final TransferProgressCallback callback)
            throws IOException {
        return uploadInternal(
                getContentUrl(validateAndGetUrlEncodedId(), filePath.getFileName().toString()),
                ProgressReportingRequestBody.builder()
                        .file(filePath)
                        .callback(callback)
                        .build());
    }

    /**
     * Uploads a new file asynchronously as a child of this {@code DriveItem} and reports the transfer status to the
     * given {@link TransferProgressCallback}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_put_content">
     * API Documentation</a>.
     *
     * @param filePath the file to upload
     * @param callback the callback to inform of transfer progress
     * @return the completable future to fetch the updated drive item information
     * @throws IOException if unable to read or determine the file's content type
     */
    public CompletableFuture<DriveItem> uploadNewAsync(
            @NonNull final Path filePath,
            @NonNull final TransferProgressCallback callback) throws IOException {
        return uploadInternalAsync(
                getContentUrl(validateAndGetUrlEncodedId(), filePath.getFileName().toString()),
                ProgressReportingRequestBody.builder()
                        .file(filePath)
                        .callback(callback)
                        .build());
    }

    private DriveItem uploadInternal(final String url, final ProgressReportingRequestBody body) {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(url)
                        .addHeader(CONTENT_TYPE, body.contentType().toString())
                        .put(body)
                        .build(),
                DRIVE_ITEM_PARSER);
    }

    private CompletableFuture<DriveItem> uploadInternalAsync(
            final String url,
            final ProgressReportingRequestBody body) {
        return connection.executeAsync(
                connection.newRequestBuilder()
                        .url(url)
                        .addHeader(CONTENT_TYPE, body.contentType().toString())
                        .put(body)
                        .build(),
                DRIVE_ITEM_PARSER);
    }

    ////////////////////////
    // CRUD
    ////////////////////////

    /**
     * Creates a new {@code DriveItem} as a child of {@code this}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_post_children">
     * API Documentation</a>.
     *
     * @param newChildDriveItem the new drive item to create
     * @return the new drive item
     */
    public DriveItem create(@NonNull final DriveItem newChildDriveItem) {
        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(getChildrenUrl(validateAndGetUrlEncodedId()))
                        .post(RequestBody.create(newChildDriveItem.toJson(), JSON_MEDIA_TYPE))
                        .build(),
                DRIVE_ITEM_PARSER);
    }

    /**
     * Updates this drive item.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_update">
     * API Documentation</a>.
     *
     * @return the updated drive item
     */
    public DriveItem update() {
        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .toString())
                        .patch(RequestBody.create(toJson(), JSON_MEDIA_TYPE))
                        .build(),
                DRIVE_ITEM_PARSER);
    }

    /**
     * Moves this {@code DriveItem} as a child to the given {@code newParentId}, updates the name, or both.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_move">
     * API Documentation</a>.
     *
     * @param destinationParentId the new parent identifier
     * @param newName the new name
     * @return the updated drive item
     */
    public DriveItem move(final String destinationParentId, final String newName) {
        validateDestinationParentIdAndNewName(destinationParentId, newName);
        return update();
    }

    /**
     * Copies this {@code DriveItem} to a child of the given {@code destinationParentId} and updates the name of the
     * copied {@link DriveItem}.
     *
     * @param destinationParentId the new destination parent identifier
     * @param newName the new name
     * @return the {@code AsyncJob} that can be used to poll for the remote asynchronous operation progress.
     * @see AsyncJob
     */
    public AsyncJob copy(final String destinationParentId, final String newName) {
        validateDestinationParentIdAndNewName(destinationParentId, newName);
        final String monitoringUrl = connection.executeRemoteAsync(
                connection.newWithBodyRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/copy")
                                .toString())
                        .post(RequestBody.create(toJson(), JSON_MEDIA_TYPE))
                        .build());
        return new AsyncJob(monitoringUrl, connection);
    }

    /**
     * Deletes this drive item.
     */
    public void delete() {
        connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .toString())
                        .delete()
                        .build());
        // Set the deleted state for this drive item for consumers that still have reference to the object.
        this.deleted = Deleted.builder().build();
    }

    ////////////////////////
    // References (has-a)
    ////////////////////////

    /**
     * Queries and fetches the activities associated with this {@code DriveItem}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/activities_list">
     * API Documentation</a>.
     *
     * @return the list of activities
     */
    public List<ItemActivity> getActivities() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/activities")
                                .toString())
                        .build(),
                ITEM_ACTIVITY_LIST_PARSER);
    }

    /**
     * Fetches the list of child {@link DriveItem}s associated with this {@code DriveItem}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_children">
     * API Documentation</a>.
     *
     * @return list of child drive items
     */
    public List<DriveItem> getChildren() {
        final List<DriveItem> changes = new ArrayList<>();
        final String urlEncodedId = validateAndGetUrlEncodedId();

        DriveItemPage currentPage = null;
        do {
            currentPage = connection.execute(
                    connection.newRequestBuilder()
                            .url(getChildrenUrl(currentPage, urlEncodedId))
                            .build(),
                    DRIVE_ITEM_PAGE_PARSER);
            changes.addAll(currentPage.getValue());
        } while (hasNextPage(currentPage));

        return changes;
    }

    /**
     * Fetches the list of versions of this {@code DriveItem}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_versions">
     * API Documentation</a>.
     *
     * @return the list of versions
     * @see DriveItemVersion
     */
    public List<DriveItemVersion> getVersions() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/versions")
                                .toString())
                        .build(),
                newDriveItemVersionListParser(getId(), getName()));
    }

    /**
     * Fetches the list of permissions associated with this {@code DriveItem}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_versions">
     * API Documentation</a>.
     *
     * @return the list of permissions
     * @see Permission
     */
    public List<Permission> getPermissions() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/permissions")
                                .toString())
                        .build(),
                newPermissionListParser(getId()));
    }

    /**
     * Adds a permission to this {@code DriveItem} for the given request.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_invite">
     * API Documentation</a>.
     *
     * @param requestBody the descriptor of the permission to add
     * @return the list of permissions for the associate item.
     */
    public List<Permission> addPermission(@NonNull final AddPermissionRequest requestBody) {
        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/invite")
                                .toString())
                        .post(RequestBody.create(
                                connection.getGsonFactory().getInstance(connection).toJson(requestBody),
                                JSON_MEDIA_TYPE))
                        .build(),
                newPermissionListParser(getId()));
    }

    /**
     * Creates a sharing link for the given request.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_createlink">
     * API Documentation</a>.
     *
     * @param requestBody the descriptor of the type of link to share
     * @return the sharing permissions that includes the link
     */
    public Permission createSharingLink(@NonNull final CreateSharingLinkRequest requestBody) {
        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/createLink")
                                .toString())
                        .post(RequestBody.create(
                                connection.getGsonFactory().getInstance(connection).toJson(requestBody),
                                JSON_MEDIA_TYPE))
                        .build(),
                newPermissionParser(getId()));
    }

    /**
     * Gets the embeddable file preview URLs for inclusion in a web-based UI. Note: For long-lived embeddable links,
     * use {@link #createSharingLink(CreateSharingLinkRequest)} instead.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_preview">
     * API Documentation</a>.
     *
     * @param requestBody the preview item request body
     * @return the preview URLs
     */
    public Preview previewItem(@NonNull final PreviewRequest requestBody) {
        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/preview")
                                .toString())
                        .post(RequestBody.create(
                                connection.getGsonFactory().getInstance(connection).toJson(requestBody),
                                JSON_MEDIA_TYPE))
                        .build(),
                newPreviewParser(getId()));
    }

    /**
     * Fetches the list of thumbnail sets associated with this {@code DriveItem}.
     *
     * @return the list of thumbnail sets
     * @see ThumbnailSet
     */
    public List<ThumbnailSet> getThumbnails() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(validateAndGetUrlEncodedId())
                                .append("/thumbnails")
                                .toString())
                        .build(),
                THUMBNAIL_SET_LIST_PARSER);
    }

    /**
     * Search for items associated with this {@code DriveItem}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_search">
     * API Documentation</a>.
     *
     * @param query the search query
     * @return the list of drive items associated with the query
     */
    public List<DriveItem> search(final String query) {
        Validate.notBlank(query, "query must not be blank");
        Validate.isTrue(query.length() < MAX_QUERY_LENGTH,
                "query length must be less than " + MAX_QUERY_LENGTH);

        final List<DriveItem> results = new ArrayList<>();

        DriveItemPage currentPage = null;
        do {
            currentPage = connection.execute(
                    connection.newRequestBuilder()
                            .url(getSearchUrl(currentPage, validateAndGetUrlEncodedId(), query))
                            .build(),
                    DRIVE_ITEM_PAGE_PARSER);
            results.addAll(currentPage.getValue());
        } while (hasNextPage(currentPage));

        return results;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        final DriveItem driveItem = (DriveItem) obj;
        return getSize() == driveItem.getSize()
                && Objects.equals(getAudio(), driveItem.getAudio())
                && Objects.equals(getCTag(), driveItem.getCTag())
                && Objects.equals(getDeleted(), driveItem.getDeleted())
                && Objects.equals(getFile(), driveItem.getFile())
                && Objects.equals(getFileSystemInfo(), driveItem.getFileSystemInfo())
                && Objects.equals(getFolder(), driveItem.getFolder())
                && Objects.equals(getImage(), driveItem.getImage())
                && Objects.equals(getLocation(), driveItem.getLocation())
                && objectDefinedEquals(getMalware(), driveItem.getMalware())
                && Objects.equals(get_package(), driveItem.get_package())
                && Objects.equals(getPhoto(), driveItem.getPhoto())
                && Objects.equals(getPublication(), driveItem.getPublication())
                && Objects.equals(getRemoteItem(), driveItem.getRemoteItem())
                && objectDefinedEquals(getRoot(), driveItem.getRoot())
                && Objects.equals(getSearchResult(), driveItem.getSearchResult())
                && Objects.equals(getShared(), driveItem.getShared())
                && Objects.equals(getSharepointIds(), driveItem.getSharepointIds())
                && Objects.equals(getSpecialFolder(), driveItem.getSpecialFolder())
                && Objects.equals(getVideo(), driveItem.getVideo())
                && Objects.equals(getConflictBehavior(), driveItem.getConflictBehavior());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getAudio(),
                getCTag(),
                getDeleted(),
                getFile(),
                getFileSystemInfo(),
                getFolder(),
                getImage(),
                getLocation(),
                Objects.nonNull(getMalware()),
                get_package(),
                getPhoto(),
                getPublication(),
                getRemoteItem(),
                Objects.nonNull(getRoot()),
                getSearchResult(),
                getShared(),
                getSharepointIds(),
                getSize(),
                getSpecialFolder(),
                getVideo(),
                getConflictBehavior());
    }

    @VisibleForTesting
    String toJson() {
        return connection.getGsonFactory().getInstance(connection).toJson(this);
    }

    private String validateAndGetUrlEncodedId() {
        final String driveItemId = getId();
        Validate.notBlank(driveItemId, "id must not be blank");
        return URLEncoder.encode(driveItemId, StandardCharsets.UTF_8);
    }

    private void validateDestinationParentIdAndNewName(final String destinationParentId, final String newName) {
        Validate.isTrue(StringUtils.isNotBlank(destinationParentId)
                        || StringUtils.isNotBlank(newName),
                "Both destinationParentId and newName must not be blank");

        final boolean isSameDestinationId = Optional.ofNullable(getParentReference())
                .map(r -> r.getId().equals(destinationParentId))
                .orElse(false);
        final boolean isSameName = StringUtils.equals(getName(), newName);
        Validate.isTrue(!(isSameDestinationId && isSameName),
                "Both destinationParentId and newName must not be the same as the original drive item");

        if (StringUtils.isNotBlank(destinationParentId)) {
            final ItemReference parentReference = getParentReference() != null
                    ? getParentReference()
                    : ItemReference.builder().id(destinationParentId).build();
            setParentReference(parentReference);
        }

        if (StringUtils.isNotBlank(newName)) {
            setName(newName);
        }
    }

    private String getContentUrl(final String urlEncodedDriveItemId) {
        return new StringBuilder(connection.getBaseUrl())
                .append(DRIVE_ITEM_BASE_URL_PATH)
                .append(urlEncodedDriveItemId)
                .append(CONTENT_URL_SUFFIX)
                .toString();
    }

    private String getContentUrl(final String urlEncodedDriveItemId, final String filename) {
        return new StringBuilder(connection.getBaseUrl())
                .append(DRIVE_ITEM_BASE_URL_PATH)
                .append(urlEncodedDriveItemId)
                .append(":/")
                .append(URLEncoder.encode(filename, StandardCharsets.UTF_8))
                .append(":")
                .append(CONTENT_URL_SUFFIX)
                .toString();
    }

    private String getChildrenUrl(final DriveItemPage page, final String urlEncodedDriveItemId) {
        return page == null ? getChildrenUrl(urlEncodedDriveItemId) : page.getNextLink();
    }

    private String getChildrenUrl(final String urlEncodedDriveItemId) {
        return new StringBuilder(connection.getBaseUrl())
                .append(DRIVE_ITEM_BASE_URL_PATH)
                .append(urlEncodedDriveItemId)
                .append("/children")
                .toString();
    }

    private String getSearchUrl(
            final DriveItemPage page,
            final String urlEncodedDriveItemId,
            final String searchQuery) {
        return page == null
                ? new StringBuilder(connection.getBaseUrl())
                        .append(DRIVE_ITEM_BASE_URL_PATH)
                        .append(urlEncodedDriveItemId)
                        .append("/search(q='")
                        .append(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8))
                        .append("')")
                        .toString()
                : page.getNextLink();
    }
}
