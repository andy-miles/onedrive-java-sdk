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

import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.site.List;
import com.amilesend.onedrive.resource.site.SiteCollection;
import com.amilesend.onedrive.resource.site.type.ColumnDefinition;
import com.amilesend.onedrive.resource.site.type.ContentType;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;

/**
 * A wrapper around a {@link com.amilesend.onedrive.resource.site.Site} that represents a sharepoint site.
 */
@RequiredArgsConstructor
@EqualsAndHashCode
public class Site {
    @NonNull
    private final com.amilesend.onedrive.resource.site.Site delegate;

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

    // Site

    /**
     * Gets the full title for the site.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    /**
     * Indicates if this site is the root site.
     *
     * @return {@code true} if this site is the root; else, {@code false}
     */
    public boolean isRoot() {
        return delegate.getRoot() != null;
    }

    /**
     * Gets the SharePoint resource identifiers for SharePoint and Business account items
     *
     * @return the SharePoint resource identifiers
     * @see SharePointIds
     */
    public SharePointIds getSharepointIds() {
        return delegate.getSharepointIds();
    }

    /**
     * Gets the details about the site's collection (only applicable to root sites).
     *
     * @return the site collection
     * @see SiteCollection
     */
    public SiteCollection getSiteCollection() {
        return delegate.getSiteCollection();
    }

    /**
     * Gets the content types for the site.
     *
     * @return the list of content types
     */
    public java.util.List<ContentType> getContentTypes() {
        return delegate.getContentTypes();
    }

    /**
     * Gets the column definitions that are reusable across lists under this site.
     *
     * @return the list of column definitions
     * @see ColumnDefinition
     */
    public java.util.List<ColumnDefinition> getColumns() {
        return delegate.getColumns();
    }

    /**
     * Gets the items contained within this site. Note: Cannot be enumerated.
     *
     * @return the list of items
     */
    public java.util.List<BaseItem> getItems() {
        return delegate.getItems();
    }

    @Override
    public String toString() {
        return new StringBuilder("Site(name=")
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
     * Gets the default document library for this site.
     *
     * @return the drive that represents the document library
     * @see Drive
     */
    public Drive getDefaultDocumentLibrary() {
        return new Drive(delegate.getDefaultDocumentLibrary());
    }

    /**
     * Gets all available document libraries for this site.
     *
     * @return the list of drive sthat represent the document libraries
     * @see Drive
     */
    public java.util.List<Drive> getDocumentLibraries() {
        return delegate.getDocumentLibraries()
                .stream()
                .map(d -> new Drive(d))
                .collect(Collectors.toList());
    }

    /**
     * Gets all lists under the site.
     *
     * @return the list of lists
     * @see List
     */
    public java.util.List<List> getLists() {
        return delegate.getLists();
    }
}
