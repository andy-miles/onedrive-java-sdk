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
package com.amilesend.onedrive.resource.drive;

import com.amilesend.client.parse.strategy.GsonExclude;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.Validate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PAGE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.ITEM_ACTIVITY_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newSpecialDriveItemParser;
import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;

/**
 * Top-level object that represents a user's OneDrive or SharePoint document library.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/drive">API Documentation.</a>
 */
@Getter
@SuperBuilder
@ToString(callSuper = true)
public class Drive extends BaseItem {
    public static final String DRIVE_BASE_URL_PATH = "/drive/";
    public static final String DRIVES_BASE_URL_PATH = "/drives/";
    public static final String DRIVE_URL_PATH_SUFFIX = "/drive";
    public static final String DRIVES_URL_PATH_SUFFIX = "/drives";

    private static final String ACTIVITIES_URL_SUFFIX = "/activities";
    private static final String ROOT_FOLDER_URL_PATH = DRIVE_BASE_URL_PATH + "root";
    private static final String CHANGES_URL_PATH = ROOT_FOLDER_URL_PATH + "/delta";
    private static final String SEARCH_URL_PATH = ROOT_FOLDER_URL_PATH + "/search";
    private static final String SPECIAL_FOLDER_URL_PATH = DRIVE_BASE_URL_PATH + "special/";
    private static final int MAX_QUERY_LENGTH = 1000;

    /**
     * The drive type descriptor. Valid types are:
     * <ul>
     *     <li>{@literal personal} - Personal drive</li>
     *     <li>{@literal business} - Business drive</li>
     *     <li>{@literal documentLibrary} - Sharepoint document library</li>
     * </ul>
     */
    private final String driveType;
    /** The user account that owns the drive. */
    private final IdentitySet owner;
    /** Drive storage space quota information. */
    private final Quota quota;
    /** Identifiers used for SharePoint. */
    private final SharePointIds sharepointIds;
    /** Indicates that this is a system-managed drive:  Note: Either {@code null} or defined as empty. */
    private final Object system;
    @GsonExclude
    private final OneDriveConnection connection;

    /**
     * Queries and fetches the activities associated with this {@code Drive}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/activities_list">
     * API Documentation</a>.
     *
     * @return the list of activities
     */
    public List<ItemActivity> getActivities() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(getActivitiesUrl(getId()))
                        .build(),
                ITEM_ACTIVITY_LIST_PARSER);
    }

    /**
     * Fetches the root folder associated with this {@code Drive}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_get">
     * API Documentation</a>.
     * @return the root folder drive item
     */
    public DriveItem getRootFolder() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(connection.getBaseUrl() + ROOT_FOLDER_URL_PATH)
                        .build(),
                DRIVE_ITEM_PARSER);
    }

    /**
     * Fetches the list of changes associated with this {@code Drive}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_delta">
     * API Documentation</a>.
     *
     * @return the list of drive item changes
     */
    public List<DriveItem> getChanges() {
        final List<DriveItem> changes = new ArrayList<>();

        DriveItemPage currentPage = null;
        do {
            currentPage = connection.execute(
                    connection.newRequestBuilder()
                            .url(getChangesUrl(currentPage))
                            .build(),
                    DRIVE_ITEM_PAGE_PARSER);
            changes.addAll(currentPage.getValue());
        } while (hasNextPage(currentPage));

        return changes;
    }

    /**
     * Search for items associated with this {@code Drive}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_search">
     * API Documentation</a>.
     *
     * @param query the search query
     * @return the list of drive items associated with the query
     */
    public List<DriveItem> search(final String query) {
        Validate.notBlank(query, "query must not be blank");
        Validate.isTrue(query.length() < MAX_QUERY_LENGTH,
                "query length must be less than " + MAX_QUERY_LENGTH);

        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final List<DriveItem> results = new ArrayList<>();

        DriveItemPage currentPage = null;
        do {
            currentPage = connection.execute(
                    connection.newRequestBuilder()
                            .url(getSearchUrl(currentPage, encodedQuery))
                            .build(),
                    DRIVE_ITEM_PAGE_PARSER);
            results.addAll(currentPage.getValue());
        } while (hasNextPage(currentPage));

        return results;
    }

    /**
     * Retrieves a special folder for the given {@link SpecialFolder.Type}.
     *
     * @param type the special folder type
     * @return the special folder drive item
     * @see SpecialFolder.Type
     */
    public SpecialDriveItem getSpecialFolder(@NonNull final SpecialFolder.Type type) {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(getSpecialFolderUrl(type))
                        .build(),
                newSpecialDriveItemParser(type));
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

        final Drive drive = (Drive) obj;
        return Objects.equals(getDriveType(), drive.getDriveType())
                && Objects.equals(getOwner(), drive.getOwner())
                && Objects.equals(getQuota(), drive.getQuota())
                && Objects.equals(getSharepointIds(), drive.getSharepointIds())
                && objectDefinedEquals(getSystem(), drive.getSystem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getDriveType(),
                getOwner(),
                getQuota(),
                getSharepointIds(),
                Objects.nonNull(getSystem()));
    }

    private String getActivitiesUrl(final String driveId) {
        return new StringBuilder(connection.getBaseUrl())
                .append(DRIVE_BASE_URL_PATH)
                .append(URLEncoder.encode(driveId, StandardCharsets.UTF_8))
                .append(ACTIVITIES_URL_SUFFIX)
                .toString();
    }

    private String getChangesUrl(final DriveItemPage page) {
        return page == null ? connection.getBaseUrl() + CHANGES_URL_PATH : page.getNextLink();
    }

    private String getSearchUrl(final DriveItemPage page, final String searchQuery) {
        return page == null
                ? new StringBuilder(connection.getBaseUrl())
                        .append(SEARCH_URL_PATH)
                        .append("(q='")
                        .append(URLEncoder.encode(searchQuery, StandardCharsets.UTF_8))
                        .append("')")
                        .toString()
                : page.getNextLink();
    }

    private String getSpecialFolderUrl(final SpecialFolder.Type type) {
        return connection.getBaseUrl() + SPECIAL_FOLDER_URL_PATH + type.getId();
    }
}
