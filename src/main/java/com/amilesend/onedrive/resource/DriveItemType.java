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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.AsyncJob;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.RemoteItem;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Defines the base class that wraps a {@link com.amilesend.onedrive.resource.item.DriveItem} for direct access by
 * consumers of this SDK.
 */
@RequiredArgsConstructor
public abstract class DriveItemType {
    @Getter(AccessLevel.PROTECTED)
    @NonNull
    private final DriveItem delegate;

    //////////////////////
    // Accessors
    //////////////////////

    /**
     * Gets the identity set that indicates who created this drive item.
     *
     * @return the identity set
     * @see IdentitySet
     */
    public IdentitySet getCreatedBy() {
        return delegate.getCreatedBy();
    }

    /**
     * Gets the timestamp (formatted as a string) that indicates when this drive item was created.
     *
     * @return the timestamp
     */
    public String getCreatedDateTime() {
        return delegate.getCreatedDateTime();
    }

    /**
     * Gets the eTag associated with this drive.
     *
     * @return the etag
     */
    public String getETag() {
        return delegate.getETag();
    }

    /**
     * Gets the drive item identifier.
     *
     * @return the drive item identifier
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * Gets the identity set that indicates who made the last modification to this drive item.
     *
     * @return the identity set
     * @see IdentitySet
     */
    public IdentitySet getLastModifiedBy() {
        return delegate.getLastModifiedBy();
    }

    /**
     * Gets the timestamp (formatted as a string) that indicates when this drive item was modified.
     *
     * @return the timestamp
     */
    public String getLastModifiedDateTime() {
        return delegate.getLastModifiedDateTime();
    }

    /**
     * Gets the name of this drive item.
     *
     * @return the name
     */
    public String getName() {
        return delegate.getName();
    }

    /**
     * Gets the parent reference of this drive item.
     *
     * @return the parent reference
     * @see ItemReference
     */
    public ItemReference getParentReference() {
        return delegate.getParentReference();
    }

    /**
     * Describes if this drive item has been marked for deletion.
     *
     * @return {@code true} if marked for deletion; else, {@code false}
     */
    public boolean isDeleted() {
        return delegate.getDeleted() != null;
    }

    /**
     * Describes if this drive item represents a remote item from another drive.
     *
     * @return {@code true} if this item is a remote item; else, {@code false}
     */
    public boolean isRemote() {
        return getRemoteItem() != null;
    }

    /**
     * Gets the remote item information. Can be {@code null}.
     *
     * @return the remote item information
     */
    public RemoteItem getRemoteItem() {
        return delegate.getRemoteItem();
    }

    /**
     * Gets the list of activities associated with this drive item.
     *
     * @return the list of activities
     * @see ItemActivity
     */
    public List<ItemActivity> getActivities() {
        return delegate.getActivities();
    }

    /**
     * Gets the list of permissions associated with this drive item.
     *
     * @return the list of permissions
     * @see Permission
     */
    public List<Permission> getPermissions() {
        return delegate.getPermissions();
    }

    /**
     * Gets the list thumbnails associated with this drive item.
     *
     * @return the list of thumbnail sets
     * @see ThumbnailSet
     */
    public List<ThumbnailSet> getThumbnails() {
        return delegate.getThumbnails();
    }

    /**
     * Gets the list of versions for this drive item.
     *
     * @return the list of versions
     * @see DriveItemVersion
     */
    public List<DriveItemVersion> getVersions() {
        return delegate.getVersions();
    }

    /**
     * Describes if this is a folder.
     *
     * @return {@code true} if this item is a folder; else, {@code false}
     */
    public boolean isFolder() {
        return delegate.getFolder() != null;
    }

    /**
     * Describes if this is a file.
     *
     * @return {@code true} if this item is a file; else, {@code false}
     */
    public boolean isFile() {
        return !isFolder();
    }

    //////////////////////
    // Shared Methods
    //////////////////////

    /**
     * Copies this drive item type to the specified {@code DriveFolder}.
     *
     * @param destinationFolder the destination drive folder
     * @return the {@code AsyncJob} that can be used to poll for the remote asynchronous operation progress.
     */
    public AsyncJob copy(final DriveFolder destinationFolder) {
        return copy(destinationFolder, StringUtils.EMPTY);
    }

    /**
     * Copies this drive item type to the specified {@code DriveFolder} along with the new file name.
     *
     * @param destinationFolder the destination drive folder
     * @param newName the new name of the file
     * @return the {@code AsyncJob} that can be used to poll for the remote asynchronous operation progress.
     */
    public AsyncJob copy(@NonNull final DriveFolder destinationFolder, String newName) {
        return copy(destinationFolder.getId(), newName);
    }

    /**
     * Copies this drive item type to the specified {@code newName} within the same folder.
     *
     * @param newName the new name of the file
     * @return the {@code AsyncJob} that can be used to poll for the remote asynchronous operation progress.
     */
    public AsyncJob copy(final String newName) {
        return copy(StringUtils.EMPTY, newName);
    }

    /**
     * Copies this drive item type to the specified {@code newParentId} (i.e., represents a new DriveFolder's ID)
     * along with the new file name.
     *
     * @param destinationParentId the destination parent identifier
     * @param newName the new name of the file
     * @return the {@code AsyncJob} that can be used to poll for the remote asynchronous operation progress.
     */
    public AsyncJob copy(final String destinationParentId, final String newName) {
        return delegate.copy(destinationParentId, newName);
    }

    /**
     * Mark this drive item type for deletion. This item will be accessible via the drive's recycle bin.
     */
    public void delete() {
        delegate.delete();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final DriveItemType that = (DriveItemType) obj;
        return Objects.equals(getDelegate(), that.getDelegate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDelegate());
    }
}
