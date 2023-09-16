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

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import lombok.Data;
import org.apache.commons.lang3.Validate;

import java.util.List;

import static com.amilesend.onedrive.resource.item.DriveItem.DRIVE_ITEM_BASE_URL_PATH;

/**
 * Describes sharing permissions granted for a drive item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/permission">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Data
public class Permission {
    /** The permission identifier. */
    private String id;
    /** Describes the users and applications for this permission. */
    private IdentitySet grantedTo;
    /** Details of users to whom permission was granted for links. */
    private List<IdentitySet> grantedToIdentities;
    /** Sharing invitation for this permission. */
    private SharingInvitation invitation;
    /** Reference to the parent permission (if applicable). */
    private ItemReference inheritedFrom;
    /** Link type permission details. */
    private SharingLink link;
    /**
     * The type of permission. Valid values include:
     * <ul>
     *     <li>{@literal read}</li>
     *     <li>{@literal write}</li>
     *     <li>{@literal owner} - For SharePoint and Business</li>
     *     <li>{@literal member} - For SharePoint and Business</li>
     * </ul>
     */
    private List<String> roles;
    /** The token that can be used to access the shared item. */
    private String shareId;

    /** The associated drive item identifier that this permission applies to. */
    @GsonExclude
    private String driveItemId;

    @GsonExclude
    private final OneDriveConnection connection;

    /**
     * Deletes this permission.
     */
    public void deletePermission() {
        // Members are mutable; validate
        final String driveItemId = getDriveItemId();
        final String permissionId = getId();
        Validate.notBlank(driveItemId, "driveItemId must not be blank");
        Validate.notBlank(permissionId, "permissionId must not be blank");

        connection.execute(
                connection.newSignedForRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_ITEM_BASE_URL_PATH)
                                .append(driveItemId)
                                .append("/permissions/")
                                .append(permissionId)
                                .toString())
                        .delete()
                        .build());
    }
}
