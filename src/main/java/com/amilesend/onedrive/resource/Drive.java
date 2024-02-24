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

import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.drive.Quota;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A wrapper around a {@link com.amilesend.onedrive.resource.drive.Drive} that represents a drive associated
 * with the authenticated user.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public class Drive {
    @NonNull
    private final com.amilesend.onedrive.resource.drive.Drive delegate;

    //////////////////////
    // Accessors
    //////////////////////

    // BaseItem

    /**
     * Gets the drive identifier.
     *
     * @return the drive identifier
     */
    public String getId() {
        return delegate.getId();
    }

    /**
     * Gets the identity set that indicates who created this drive.
     *
     * @return the identity set
     * @see IdentitySet
     */
    public IdentitySet getCreatedBy() {
        return delegate.getCreatedBy();
    }

    /**
     * Gets the timestamp (formatted as a string) that indicates when this drive was created.
     *
     * @return the timestamp
     */
    public String getCreatedDateTime() {
        return delegate.getCreatedDateTime();
    }

    /**
     * Gets the description associated with this drive.
     *
     * @return the description
     */
    public String getDescription() {
        return delegate.getDescription();
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
     * Gets the identity set that indicates who made the last modification to this drive.
     *
     * @return the identity set
     * @see IdentitySet
     */
    public IdentitySet getLastModifiedBy() {
        return delegate.getLastModifiedBy();
    }

    /**
     * Gets the timestamp (formatted as a string) that indicate when this drive was last modified.
     *
     * @return the timestamp
     */
    public String getLastModifiedDateTime() {
        return delegate.getLastModifiedDateTime();
    }

    /**
     * Gets the name of this drive.
     *
     * @return the name
     */
    public String getName() {
        return delegate.getName();
    }

    /**
     * Gets the parent reference of this drive.
     *
     * @return the parent reference
     * @see ItemReference
     */
    public ItemReference getParentReference() {
        return delegate.getParentReference();
    }

    /**
     * Gets the web URL formatted as a string for this drive.
     *
     * @return the web URL
     */
    public String getWebUrl() {
        return delegate.getWebUrl();
    }

    // Drive

    /**
     * Gets the type for this drive.
     *
     * @return the drive type
     */
    public String getDriveType() {
        return delegate.getDriveType();
    }

    /**
     * Gets the owner information for this drive.
     *
     * @return the identity set
     * @see IdentitySet
     */
    public IdentitySet getOwner() {
        return delegate.getOwner();
    }

    /**
     * Gets the quota information for this drive.
     *
     * @return the quota information
     * @see Quota
     */
    public Quota getQuota() {
        return delegate.getQuota();
    }

    /**
     * Gets the Sharepoint identifiers for this drive.
     *
     * @return the sharepoint identifiers
     * @see SharePointIds
     */
    public SharePointIds getSharepointIds() {
        return delegate.getSharepointIds();
    }

    /**
     * Describes if this drive is a system-managed drive. Most consumers should ignore this drive type for use
     * in their application.
     *
     * @return {@code true} if this drive is system managed; else, {@code false}
     */
    public boolean isSystemManaged() {
        return delegate.getSystem() != null;
    }

    @Override
    public String toString() {
        return new StringBuilder("Drive(name=")
                .append(getName())
                .append(", id=")
                .append(getId())
                .append(")")
                .toString();
    }

    //////////////////////
    // Operations
    //////////////////////

    /**
     * Gets the root folder for this drive.
     *
     * @return the root drive folder
     * @see DriveFolder
     */
    public DriveFolder getRootFolder() {
        return new DriveFolder(delegate.getRootFolder());
    }

    /**
     * Searches this drive for the given query (e.g., file name).
     *
     * @param query the search query
     * @return the list of drive item types (either a DriveFolder or DriveFile).
     * @see DriveItemType
     * @see DriveFolder
     * @see DriveFile
     */
    public List<? extends DriveItemType> search(final String query) {
        return delegate.search(query)
                .stream()
                .map(DriveItemType::wrapDriveItemToType)
                .collect(Collectors.toList());
    }

    /**
     * Gets the list of changes associated with this drive.
     *
     * @return the list of drive item types (either a DriveFolder or DriveFile).
     * @see DriveItemType
     * @see DriveFolder
     * @see DriveFile
     */
    public List<? extends DriveItemType> getChanges() {
        return delegate.getChanges()
                .stream()
                .map(DriveItemType::wrapDriveItemToType)
                .collect(Collectors.toList());
    }

    /**
     * Gets the special folder for the given {@link com.amilesend.onedrive.resource.item.type.SpecialFolder.Type}.
     *
     * @param type the type
     * @return the special drive folder
     * @see com.amilesend.onedrive.resource.item.type.SpecialFolder.Type
     * @see DriveFolder
     */
    public DriveFolder getSpecialFolder(final SpecialFolder.Type type) {
        return new DriveFolder(delegate.getSpecialFolder(type));
    }

    /**
     * Gets the list of activities that took place for this drive.
     *
     * @return the list of activities
     * @see ItemActivity
     */
    public List<ItemActivity> getActivities() {
        return delegate.getActivities();
    }
}
