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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.type.Folder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.amilesend.onedrive.connection.file.LogProgressCallback.formatPrefix;

/**
 * Defines shared operations between drive folders and packages.
 * @see DriveFolder
 * @see DrivePackage
 */
public abstract class DriveItemFolderType extends DriveItemType {
    /**
     * Creates a new {@code DriveItemFolderType} that wraps the given {@code delegate} {@link DriveItem}.
     *
     * @param delegate the drive item to wrap
     */
    public DriveItemFolderType(final DriveItem delegate) {
        super(delegate);
    }

    //////////////////////
    // Upload
    //////////////////////

    /**
     * Uploads the given {@code file} under this folder.
     *
     * @param file the file to upload
     * @return the drive file that represents the uploaded file
     * @throws IOException if an error occurred while uploading the file
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
     * Uploads the given {@code file} under this folder and reports transfer progress to the specified
     * {@link TransferProgressCallback}.
     *
     * @param file the file to upload
     * @param callback the callback be notified of transfer progress
     * @return the drive file that represents the uploaded file
     * @throws IOException if an error occurred while uploading the file
     * @see DriveFile
     */
    public DriveFile upload(final File file, final TransferProgressCallback callback)
            throws IOException {
        return new DriveFile(getDelegate().uploadNew(file, callback));
    }

    /**
     * Uploads the given {@code file} asynchronously under this folder.
     *
     * @param file the file to upload
     * @return the async execution used to obtain the drive file once it has completed
     * @throws IOException if an error occurred while uploading the file
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
     * Uploads the given {@code file} asynchronously under this folder and reports transfer progress to the specified
     * {@link TransferProgressCallback}.
     *
     * @param file the file to upload
     * @param callback the callback be notified of transfer progress
     * @return the async execution used to obtain the drive file once it has completed
     * @throws IOException if an error occurred while uploading the file
     * @see DriveFileUploadExecution
     */
    public DriveFileUploadExecution uploadAsync(
            final File file,
            final TransferProgressCallback callback) throws IOException {
        return new DriveFileUploadExecution(getDelegate().uploadNewAsync(file, callback));
    }

    //////////////////////
    // Operations
    //////////////////////

    /**
     * Creates a new folder with the given {@code name} under this folder.
     *
     * @param name the name of the new folder
     * @return the new drive folder
     */
    public DriveFolder createFolder(final String name) {
        Validate.notBlank(name, "name must not be blank");

        final DriveItem newFolderItem = DriveItem.builder()
                .connection(getDelegate().getConnection())
                .name(name)
                .folder(Folder.builder().build())
                .build();
        return new DriveFolder(getDelegate().create(newFolderItem));
    }

    /**
     * Gets the list of child drive packages under this folder.
     *
     * @return the list of drive packages
     */
    public List<DrivePackage> getChildPackages() {
        return getDelegate().getChildren()
                .stream()
                .filter(di -> di.get_package() != null)
                .map(di -> new DrivePackage(di))
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of child drive folders under this folder.
     *
     * @return the list of drive folders
     */
    public List<DriveFolder> getChildFolders() {
        return getDelegate().getChildren()
                .stream()
                .filter(di -> di.getFolder() != null)
                .filter(di -> di.get_package() == null)
                .map(di -> new DriveFolder(di))
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of child drive files under this folder.
     *
     * @return the list of drive files
     */
    public List<DriveFile> getChildFiles() {
        return getDelegate().getChildren()
                .stream()
                .filter(di -> di.getFile() != null)
                .map(di -> new DriveFile(di))
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of drive items (both folders and files) under this folder.
     *
     * @return the list of drive items
     * @see DriveItemType
     * @see DriveItem
     */
    public List<? extends DriveItemType> getChildren() {
        return getDelegate().getChildren()
                .stream()
                .map(DriveItemType::wrapDriveItemToType)
                .collect(Collectors.toList());
    }

    /**
     * Searches this folder for the given query (e.g., file name).
     *
     * @param query the search query
     * @return the list of drive item types (either a DriveFolder or DriveFile).
     * @see DriveItemType
     * @see DriveFile
     */
    public List<? extends DriveItemType> search(final String query) {
        return getDelegate().search(query)
                .stream()
                .map(DriveItemType::wrapDriveItemToType)
                .collect(Collectors.toList());
    }

    /**
     * Updates the attributes for this folder.
     *
     * @return the updated drive folder
     */
    public DriveFolder update() {
        return new DriveFolder(getDelegate().update());
    }

    /**
     * Moves this folder to the specified {@link DriveFolder}.
     *
     * @param destinationFolder the destination drive folder
     * @return the updated drive file that represents this moved file
     */
    public DriveFolder move(final DriveFolder destinationFolder) {
        return move(destinationFolder, StringUtils.EMPTY);
    }

    /**
     * Moves this folder to the specified {@link DriveFolder} along with the new file name.
     *
     * @param destinationFolder the destination drive folder
     * @param newName the new name of the file
     * @return the updated drive folder that represents this moved folder
     */
    public DriveFolder move(@NonNull final DriveFolder destinationFolder, final String newName) {
        return move(destinationFolder.getId(), newName);
    }

    /**
     * Moves this folder to the specified {@code newParentId} (i.e., represents a new DriveFolder's ID)
     * along with the new folder name.
     *
     * @param destinationParentId the new parent destination ID
     * @param newName the new name of the file
     * @return the updated drive folder that represents this moved folder
     */
    public DriveFolder move(final String destinationParentId, final String newName) {
        return new DriveFolder(getDelegate().move(destinationParentId, newName));
    }
}
