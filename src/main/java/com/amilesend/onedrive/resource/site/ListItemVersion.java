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
package com.amilesend.onedrive.resource.site;

import com.amilesend.client.parse.strategy.GsonExclude;
import com.amilesend.client.util.StringUtils;
import com.amilesend.client.util.VisibleForTesting;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.type.PublicationFacet;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import okhttp3.RequestBody;

import java.util.Map;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.resource.site.List.LIST_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.site.ListItem.LIST_ITEM_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.site.Site.SITE_BASE_URL_PATH;

/**
 * Describes a previous version of a list item.
 * <p>
 * Required permissions (one of):
 * <ul>
 *     <li>{@literal Sites.ReadWrite.All}</li>
 *     <li>{@literal Sites.Manage.All}</li>
 *     <li>{@literal Sites.FullControl.All}</li>
 * </ul>
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/listitemversion">
 * API Documentation</a>.
 * @see ListItem
 */
@Builder
@Data
public class ListItemVersion {
    public static final String LIST_ITEM_VERSION_BASE_URL_PATH = "/versions/";
    public static final String LIST_ITEM_VERSIONS_URL_SUFFIX = "/versions";

    @VisibleForTesting
    static final int NO_CONTENT_RESPONSE_HTTP_CODE = 204;

    /** The key is the name of the column, the value is the associated value of the columns set on this list item. */
    private final Map<String, Object> fields;
    /** The version identifier. */
    private final String id;
    /** The user who last modified the version. */
    private final IdentitySet lastModifiedBy;
    /** The timestamp of when the version was last modifed. */
    private final String lastModifiedDateTime;
    /** Describes the publication status of a version. */
    private final PublicationFacet published;
    /** The associated site identifier. */
    @GsonExclude
    private final String siteId;
    /** The associated list identifier. */
    @GsonExclude
    private final String listId;
    /** The associated list item identifier. */
    @GsonExclude
    private final String listItemId;
    /** The OneDrive API connection. */
    @EqualsAndHashCode.Exclude
    @GsonExclude
    private final OneDriveConnection connection;

    /**
     * Restores this version.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitemversion_restore">
     * API Documentation</a>.
     *
     * @return {@code true} if successful; else, {@code false}
     */
    public boolean restore() {
        final int responseCode = connection.execute(connection.newRequestBuilder()
                .url(new StringBuilder(connection.getBaseUrl())
                        .append(SITE_BASE_URL_PATH)
                        .append(getSiteId())
                        .append(LIST_BASE_URL_PATH)
                        .append(getListId())
                        .append(LIST_ITEM_BASE_URL_PATH)
                        .append(getListItemId())
                        .append(LIST_ITEM_VERSION_BASE_URL_PATH)
                        .append(getId())
                        .append("/restoreVersion")
                        .toString())
                .post(RequestBody.create(StringUtils.EMPTY, JSON_MEDIA_TYPE))
                .build())
                .code();
        return responseCode == NO_CONTENT_RESPONSE_HTTP_CODE;
    }
}
