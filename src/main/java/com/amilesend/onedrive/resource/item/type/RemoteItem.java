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
package com.amilesend.onedrive.resource.item.type;

import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Indicates and describes that a drive item references one that exists in another drive.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/remoteitem">
 * API Documentation</a>.
 */
@Data
public class RemoteItem {
    /** The identity that created the drive item. */
    private IdentitySet createdBy;
    /** The date and time of item creation. */
    private String createdDateTime;
    /** Indicates and describes if the remote item is a file. */
    private File file;
    /** Describes the (client-side) properties of the local version of the remtoe item. */
    private FileSystemInfo fileSystemInfo;
    /** Describes that the remote item is a folder. */
    private Folder folder;
    /** The remote item identifier. */
    private String id;
    /** The identity that last modified the remote item. */
    private IdentitySet lastModifiedBy;
    /** Describes when the remote item was last modified. */
    private String lastModifiedDateTime;
    /** The name of the remote item. */
    private String name;
    /** Indicates that a remote item is the top level item in a collection of items. */
    @SerializedName("package")
    private Package _package;
    /** Describes the parent information. */
    private ItemReference parentReference;
    /** Indicates and describes that the remote item has been shared with others. */
    private Shared shared;
    /** Identifiers used for SharePoint. */
    private SharePointIds sharepointIds;
    /** Size of the remote item in bytes. */
    private long size;
    /** Describes if the current remote item is a special folder. */
    private SpecialFolder specialFolder;
    /** DAV compatible URL for the remote item. */
    private String webDavUrl;
    /** URL for the resource shown in a browser. */
    private String webUrl;
}
