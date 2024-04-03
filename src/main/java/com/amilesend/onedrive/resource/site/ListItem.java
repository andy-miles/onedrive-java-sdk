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
package com.amilesend.onedrive.resource.site;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.site.response.GetColumnValuesResponse;
import com.amilesend.onedrive.resource.site.type.ContentType;
import com.google.common.annotations.VisibleForTesting;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import okhttp3.RequestBody;
import org.apache.commons.lang3.Validate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.FIELD_VALUE_SET_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.GET_COLUMN_VALUES_RESPONSE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemVersionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemVersionParser;
import static com.amilesend.onedrive.resource.site.List.LIST_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.site.ListItemVersion.LIST_ITEM_VERSIONS_URL_SUFFIX;
import static com.amilesend.onedrive.resource.site.ListItemVersion.LIST_ITEM_VERSION_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.site.Site.SITE_BASE_URL_PATH;

/**
 * Represents an item in a list.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/listitem">API Documentation.</a>
 * @see List
 */
@EqualsAndHashCode(callSuper=true)
@Getter
@SuperBuilder
@ToString(callSuper = true)
public class ListItem extends BaseItem {
    public static final String LIST_ITEM_BASE_URL_PATH = "/items/";
    public static final String LIST_ITEM_URL_SUFFIX = "/items";

    private static final int MAX_COLUMN_LENGTH = 128;
    private static final int MAX_SELECTED_COLUMNS = 12;

    /** The key is the name of the column, the value is the associated value of the columns set on this list item. */
    private final Map<String, Object> fields;
    /** The Sharepoint identifiers. */
    private final SharePointIds sharepointIds;
    /** For document libraries, the list item as a drive item. */
    private final DriveItem driveItem;
    /** The associated site identifier. */
    @GsonExclude
    private final String siteId;
    /** The associated list identifier. */
    @GsonExclude
    private final String listId;
    /** The OneDrive API connection. */
    @EqualsAndHashCode.Exclude
    @GsonExclude
    private final OneDriveConnection connection;
    /** The content type of this list item. */
    @Setter
    private ContentType contentType;
    /** Indicates if this item is deleted or not (read-only). */
    @GsonExclude
    private boolean deleted;

    //////////////////
    // Column values
    //////////////////

    /**
     * Gets the associated columns and its values.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_get">
     * API Documentation</a>.
     *
     * @return the response
     * @see GetColumnValuesResponse
     */
    public GetColumnValuesResponse getColumnValues() {
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(newStringBuilderForListItemUrl()
                                .append("?expand=fields")
                                .toString())
                        .build(),
                GET_COLUMN_VALUES_RESPONSE_PARSER);
    }

    /**
     * Gets the selected columns and its values for the given list of column names.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_get">
     * API Documentation</a>.
     *
     * @param columnsToSelect the list of column names (max 12)
     * @return the response
     * @see GetColumnValuesResponse
     */
    public GetColumnValuesResponse getColumnValues(final java.util.List<String> columnsToSelect) {
        final String selectedColumns = validateAndJoinColumns(columnsToSelect);
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(newStringBuilderForListItemUrl()
                                .append("?expand=fields(select=")
                                .append(selectedColumns)
                                .append(")")
                                .toString())
                        .build(),
                GET_COLUMN_VALUES_RESPONSE_PARSER);
    }

    /**
     * Updates the defined columns and associated values for this list item.  All other values on the list item
     * are left alone.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_update">
     * API Documentation</a>.
     *
     * @param fields the fields to update
     * @return the field value set containing all the columns and values
     */
    public Map<String, Object> updateColumnValues(final Map<String, Object> fields) {
        Validate.notEmpty(fields, "fields must not be blank");
        return connection.execute(
                connection.newSignedForApiWithBodyRequestBuilder()
                        .url(newStringBuilderForListItemUrl()
                                .append("/fields")
                                .toString())
                        .patch(RequestBody.create(connection.getGson().toJson(fields), JSON_MEDIA_TYPE))
                        .build(),
                FIELD_VALUE_SET_PARSER);
    }

    ////////////////////
    // ListItemVersion
    ////////////////////

    /**
     * Gets the list of versions for this list item.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_list_versions">
     * API Documentation</a>.
     *
     * @return the list item versions
     */
    public java.util.List<ListItemVersion> getListItemVersions() {
        final String siteId = getSiteId();
        final String listId = getListId();
        final String listItemId = getId();

        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(newStringBuilderForListItemUrl()
                                .append(LIST_ITEM_VERSIONS_URL_SUFFIX)
                                .toString())
                        .build(),
                newListItemVersionListParser(siteId, listId, listItemId));
    }

    /**
     * Gets the {@link ListItemVersion} for the given {@code versionId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitemversion_get">
     * API Documentation</a>.
     *
     * @param versionId the list item version identifier
     * @return the list item version
     */
    public ListItemVersion getVersion(final String versionId) {
        Validate.notBlank(versionId, "versionId must not be blank");

        final String siteId = getSiteId();
        final String listId = getListId();
        final String listItemId = getId();

        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(newStringBuilderForListItemUrl()
                                .append(LIST_ITEM_VERSION_BASE_URL_PATH)
                                .append(versionId)
                                .toString())
                        .build(),
                newListItemVersionParser(siteId, listId, listItemId));
    }

    //////////
    // CRUD
    //////////

    /**
     * Updates this list item (e.g., name, description, parentReference, content type).
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/listitem">
     * API Documentation</a>.
     *
     * @return the updated list item
     */
    public ListItem update() {
        final String siteId = getSiteId();
        final String listId = getListId();
        return connection.execute(
                connection.newSignedForApiWithBodyRequestBuilder()
                        .url(newStringBuilderForListItemUrl().toString())
                        .patch(RequestBody.create(toJson(), JSON_MEDIA_TYPE))
                        .build(),
                newListItemParser(siteId, listId));
    }

    /**
     * Deletes this item from the list.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/listitem">
     * API Documentation</a>.
     */
    public void delete() {
        connection.execute(
                connection.newSignedForRequestBuilder()
                        .url(newStringBuilderForListItemUrl().toString())
                        .delete()
                        .build());
        // Set the deleted state for this list item for consumers that still have reference to the object.
        deleted = true;
    }

    /**
     * Gets the JSON representation of this list item.
     *
     * @return the list item JSON value
     */
    @VisibleForTesting
    String toJson() {
        return connection.getGson().toJson(this);
    }

    private String validateAndJoinColumns(final java.util.List<String> columns) {
        Validate.notEmpty(columns, "columns list must not be empty");
        Validate.isTrue(columns.size() <= MAX_SELECTED_COLUMNS,
                "cannot select more than " + MAX_SELECTED_COLUMNS + "columns");

        final java.util.List<String> encodedColumns = new ArrayList<>(columns.size());
        for (final String column : columns) {
            Validate.notNull(column, "column name must not be null");
            Validate.isTrue(column.length() < MAX_COLUMN_LENGTH);
            encodedColumns.add(URLEncoder.encode(column, StandardCharsets.UTF_8));
        }

        return String.join(",", encodedColumns);
    }

    private StringBuilder newStringBuilderForListItemUrl() {
        return new StringBuilder(connection.getBaseUrl())
                .append(SITE_BASE_URL_PATH)
                .append(getSiteId())
                .append(LIST_BASE_URL_PATH)
                .append(getListId())
                .append(LIST_ITEM_BASE_URL_PATH)
                .append(getId());
    }
}
