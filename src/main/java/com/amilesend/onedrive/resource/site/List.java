/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.site.request.CreateListItemRequest;
import com.amilesend.onedrive.resource.site.type.ColumnDefinition;
import com.amilesend.onedrive.resource.site.type.ContentType;
import com.amilesend.onedrive.resource.site.type.ListInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import okhttp3.RequestBody;

import java.util.Map;
import java.util.Objects;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.ITEM_ACTIVITY_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemParser;
import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;
import static com.amilesend.onedrive.resource.site.ListItem.LIST_ITEM_URL_SUFFIX;
import static com.amilesend.onedrive.resource.site.Site.SITE_BASE_URL_PATH;

/**
 * Represents a list resource in a site that describes the top-level properties of a list.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/list">API Documentation.</a>
 * @see Site
 * @see ListItem
 */
@Getter
@SuperBuilder
@ToString(callSuper = true)
public class List extends BaseItem {
    public static final String LIST_BASE_URL_PATH = "/lists/";
    public static final String LIST_URL_PATH_SUFFIX = "/lists";

    private static final String ACTIVITIES_URL_SUFFIX = "/activities";

    /** Indicates that this is a system-managed list. Note: Either {@code null} or defined as empty. */
    private final Object system;
    /** The column definitions that apply to this list. */
    private final java.util.List<ColumnDefinition> columns;
    /** The content types for this list. */
    private final java.util.List<ContentType> contentTypes;
    /** When defined, indicates that this list exists within a document library and can be treated as a drive. */
    private final Drive drive;
    /** The associated site identifier that this list is associated with. */
    @GsonExclude
    private final String siteId;
    /** The OneDrive API connection. */
    @GsonExclude
    private final OneDriveConnection connection;
    /** The list title. */
    private String displayName;
    /** Additional list information. */
    private ListInfo list;

    /**
     * Creates a new {@link ListItem} for the given {@link CreateListItemRequest}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_create">
     * API Documentation</a>.
     *
     * @param fields The map of fields, with the key being the column name and the Object being its value
     * @return the created list item
     */
    public ListItem createListItem(final Map<String, Object> fields) {
        final CreateListItemRequest request = CreateListItemRequest.builder()
                .fields(fields)
                .gson(connection.getGsonFactory().getInstance(connection))
                .build();
        request.validate();

        return connection.execute(
                connection.newWithBodyRequestBuilder()
                        .url(newStringBuilderForListUrl()
                                .append(LIST_ITEM_URL_SUFFIX)
                                .toString())
                        .post(RequestBody.create(request.toJson(), JSON_MEDIA_TYPE))
                        .build(),
                newListItemParser(getSiteId(), getId()));
    }

    /**
     * Queries and fetches the activities associated with this {@code List}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/activities_list">
     * API Documentation</a>.
     *
     * @return the list of activities
     */
    public java.util.List<ItemActivity> getActivities() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(newStringBuilderForListUrl()
                                .append(ACTIVITIES_URL_SUFFIX)
                                .toString())
                        .build(),
                ITEM_ACTIVITY_LIST_PARSER);
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

        final List that = (List) obj;
        return objectDefinedEquals(getSystem(), that.getSystem())
                && Objects.equals(getColumns(), that.getColumns())
                && Objects.equals(getContentTypes(), that.getContentTypes())
                && Objects.equals(getDrive(), that.getDrive())
                && Objects.equals(getSiteId(), that.getSiteId())
                && Objects.equals(getDisplayName(), that.getDisplayName())
                && Objects.equals(getList(), that.getList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                Objects.nonNull(getSystem()),
                getColumns(),
                getContentTypes(),
                getDrive(),
                getSiteId(),
                getDisplayName(),
                getList());
    }

    private StringBuilder newStringBuilderForListUrl() {
        return new StringBuilder(connection.getBaseUrl())
                .append(SITE_BASE_URL_PATH)
                .append(getSiteId())
                .append(LIST_BASE_URL_PATH)
                .append(getId());
    }
}
