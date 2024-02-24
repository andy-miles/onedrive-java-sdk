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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.type.Audio;
import com.amilesend.onedrive.resource.item.type.GeoCoordinates;
import com.amilesend.onedrive.resource.item.type.Image;
import com.amilesend.onedrive.resource.item.type.Photo;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.Video;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import com.amilesend.onedrive.resource.request.PreviewRequest;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.amilesend.onedrive.connection.file.LogProgressCallback.formatPrefix;

/** A wrapper around a {@link DriveItem} that represents a file. */
public class DriveFile extends DriveItemType {

    /**
     * Creates a new {@code DriveFile} that wraps the given {@code getDelegate()} {@link DriveItem}.
     *
     * @param delegate the drive item to wrap
     */
    public DriveFile(final DriveItem delegate) {
        super(delegate);
    }

    //////////////////////
    // Accessors
    //////////////////////

    /**
     * Describes if this is an audio file.
     *
     * @return {@code true} if this file is an audio file; else, {@code false}
     */
    public boolean isAudio() {
        return getAudioAttributes() != null;
    }

    /**
     * Gets the audio file attributes. Can be {@code null}.
     *
     * @return the audio file attributes
     * @see Audio
     */
    public Audio getAudioAttributes() {
        return getDelegate().getAudio();
    }

    /**
     * Describes if this is an image file.
     *
     * @return {@code true} if this file is an image file; else, {@code false}
     */
    public boolean isImage() {
        return getImageAttributes() != null;
    }

    /**
     * Gets the image file attributes. Can be {@code null}.
     *
     * @return the image file attributes
     * @see Image
     */
    public Image getImageAttributes() {
        return getDelegate().getImage();
    }

    /**
     * Determines if there is geolocation attributes for this file.
     *
     * @return {@code true} if this file has location attributes; else, {@code false}
     */
    public boolean isLocationAvailable() {
        return getLocationAttributes() != null;
    }

    /**
     * Gets the location attributes (e.g., photo taken from a cell phone). Can be {@code null}.
     *
     * @return the location file attributes
     * @see GeoCoordinates
     */
    public GeoCoordinates getLocationAttributes() {
        return getDelegate().getLocation();
    }

    /**
     * Describes if this has photo attributes. Can be {@code null}.
     *
     * @return {@code true} if this file contains photo attributes; else, {@code false}
     */
    public boolean isPhoto() {
        return getPhotoAttributes() != null;
    }

    /**
     * Gets the photo attributes (e.g., photo taken from a cell phone). Can be {@code null}.
     *
     * @return the photo attributes
     * @see Photo
     */
    public Photo getPhotoAttributes() {
        return getDelegate().getPhoto();
    }

    /**
     * Describes if this is a video file.
     *
     * @return {@code true} if this file is a video file; else, {@code false}
     */
    public boolean isVideo() {
        return getVideoAttributes() != null;
    }

    /**
     * Gets the vidoe file attributes. Can be {@code null}.
     *
     * @return the video file attributes
     * @see Video
     */
    public Video getVideoAttributes() {
        return getDelegate().getVideo();
    }

    @Override
    public String toString() {
        return new StringBuilder("DriveFile(name=")
                .append(getName())
                .append(", id=")
                .append(getId())
                .append(", isDeleted=")
                .append(isDeleted())
                .append(", isRemote=")
                .append(isRemote())
                .append(", isAudio=")
                .append(isAudio())
                .append(", isImage=")
                .append(isImage())
                .append(", isVideo=")
                .append(isVideo())
                .append(")")
                .toString();
    }

    //////////////////////
    // Download
    //////////////////////

    /**
     * Downloads this file to the given {@code folderPath}.
     *
     * @param folderPath the folder to download the file to
     */
    public void download(final Path folderPath) {
        getDelegate().download(
                folderPath,
                LogProgressCallback.builder()
                        .prefix(formatPrefix("OneDrive", folderPath.toFile().getName()))
                        .transferType(LogProgressCallback.TransferType.DOWNLOAD)
                        .build());
    }

    /**
     * Downloads this file to the given {@code folderPath} and reports transfer progress to the
     * specified {@link TransferProgressCallback}.
     *
     * @param folderPath the folder to download the file to
     * @param callback the callback be notified of transfer progress
     * @see TransferProgressCallback
     */
    public void download(final Path folderPath, final TransferProgressCallback callback) {
        getDelegate().download(folderPath, callback);
    }

    /**
     * Downloads this file asynchronously to the given {@code folderPath}. Consumers can block on transfer completion
     * by invoking {@link DriveFileDownloadExecution#get()} ()}.
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
     * Downloads this file asynchronously to the given {@code folderPath} and reports transfer progress to the specified
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
            final Path folderPath,
            final TransferProgressCallback callback) {
        return new DriveFileDownloadExecution(getDelegate().downloadAsync(folderPath, callback));
    }

    //////////////////////
    // Upload
    //////////////////////

    /**
     * Uploads and replaces this drive file's contents with the given {@code file}.
     *
     * @param file the updated file contents
     * @return a new drive file that represents this updated file
     * @throws IOException if unable to read the file.
     * @see DriveFile
     */
    public DriveFile upload(final File file) throws IOException {
        return upload(
                file,
                LogProgressCallback.builder()
                        .prefix(formatPrefix(file.getName(), "OneDrive"))
                        .transferType(LogProgressCallback.TransferType.UPLOAD)
                        .build());
    }

    /**
     * Uploads and replaces this drive file's contents with the given {@code file} and reports transfer
     * progress to the specified {@link TransferProgressCallback}.
     *
     * @param file the updated file contents
     * @param callback the callback be notified of transfer progress
     * @return a new drive file that represents this updated file
     * @throws IOException if unable to read the file.
     * @see DriveFile
     */
    public DriveFile upload(final File file, final TransferProgressCallback callback)
        throws IOException {
        return new DriveFile(getDelegate().upload(file, callback));
    }

    /**
     * Uploads and replaces this drive file's contents asynchronously with the given {@code file}.
     * Consumers can block on transfer completion by invoking {@link DriveFileUploadExecution#get()}.
     *
     * @param file the updated file contents
     * @return a new drive file that represents this updated file
     * @throws IOException if unable to read the file.
     * @see DriveFileUploadExecution
     */
    public DriveFileUploadExecution uploadAsync(final File file) throws IOException {
        return uploadAsync(
                file,
                LogProgressCallback.builder()
                        .prefix(formatPrefix(file.getName(), "OneDrive"))
                        .transferType(LogProgressCallback.TransferType.UPLOAD)
                        .build());
    }

    /**
     * Uploads and replaces this drive file's contents asynchronously with the given {@code file} and reports transfer
     * progress to the specified {@link TransferProgressCallback}.
     *
     * @param file the updated file contents
     * @param callback the callback be notified of transfer progress
     * @return the async execution used to obtain the drive file once it has completed
     * @throws IOException if unable to read the file.
     * @see TransferProgressCallback
     */
    public DriveFileUploadExecution uploadAsync(
            final File file,
            final TransferProgressCallback callback) throws IOException {
        return new DriveFileUploadExecution(getDelegate().uploadAsync(file, callback));
    }

    //////////////////////
    // Operations
    //////////////////////

    /**
     * Updates the attributes for this file.
     *
     * @return the updated drive file
     */
    public DriveFile update() {
        return new DriveFile(getDelegate().update());
    }

    /**
     * Gets the embeddable file preview URLs for inclusion in a web-based UI. Note: For long-lived embeddable links,
     * use {@link #createSharingLink(CreateSharingLinkRequest)} instead.
     *
     * @param request the preview item request
     * @return the preview URLs
     */
    public Preview preview(final PreviewRequest request) {
        return getDelegate().previewItem(request);
    }

    /**
     * Moves this file to the specified {@link DriveFolder}.
     *
     * @param destinationFolder the destination drive folder
     * @return the updated drive file that represents this moved file
     */
    public DriveFile move(final DriveFolder destinationFolder) {
        return move(destinationFolder, StringUtils.EMPTY);
    }

    /**
     * Moves this file to the specified {@link DriveFolder} along with the new file name.
     *
     * @param destinationFolder the destination drive folder
     * @param newName the new name of the file
     * @return the updated drive file that represents this moved file
     */
    public DriveFile move(@NonNull final DriveFolder destinationFolder, final String newName) {
        return move(destinationFolder.getId(), newName);
    }

    /**
     * Moves this file to the specified {@code newParentId} (i.e., represents a new DriveFolder's ID)
     * along with the new file name.
     *
     * @param destinationParentId the new parent destination ID
     * @param newName the new name of the file
     * @return the updated drive file that represents this moved file.
     */
    public DriveFile move(final String destinationParentId, final String newName) {
        return new DriveFile(getDelegate().move(destinationParentId, newName));
    }
}
