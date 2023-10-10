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
package com.amilesend.onedrive.resource.site;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.parser.DriveListParser;
import com.amilesend.onedrive.parse.resource.parser.DriveParser;
import com.amilesend.onedrive.parse.resource.parser.ListListParser;
import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.site.type.ColumnDefinition;
import com.amilesend.onedrive.resource.site.type.ContentType;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVES_URL_PATH_SUFFIX;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVE_URL_PATH_SUFFIX;
import static com.amilesend.onedrive.resource.site.List.LIST_URL_PATH_SUFFIX;

/**
 * Top-level object that represents a SharePoint document library.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">API Documentation.</a>
 */
@Getter
@SuperBuilder
@ToString(callSuper = true)
public class Site extends BaseItem {
    public static final String SITE_BASE_URL_PATH = "/sites/";
    public static final String SITE_URL_PATH_SUFFIX = "/sites";

    private static final DriveParser DRIVE_PARSER = new DriveParser();
    private static final DriveListParser DRIVE_LIST_PARSER = new DriveListParser();

    /** The full title for the site. */
    private final String displayName;
    /* An empty object if defined; else is null */
    /** If defined, indicates that the item is the top-most folder in the drive (read-only). */
    private final Object root;
    /** SharePoint resource identifiers for SharePoint and Business account items (read-only). */
    private final SharePointIds sharepointIds;
    /** Provides details about the site's collection. Only applies to root sites.*/
    private final SiteCollection siteCollection;
    /** The content types for the site. */
    private final java.util.List<ContentType> contentTypes;
    /** The column definitions that are reusable across lists under this site. */
    private final java.util.List<ColumnDefinition> columns;
    /** The items contained within this site. Note: Cannot be enumerated. */
    private final java.util.List<BaseItem> items;

    @GsonExclude
    private final OneDriveConnection connection;

    /**
     * Gets the default document library for this site.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">
     * API Documentation</a>.
     *
     * @return the drive representing the default document library
     */
    public Drive getDefaultDocumentLibrary() {
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append(getId())
                                .append(DRIVE_URL_PATH_SUFFIX)
                                .toString())
                        .build(),
                DRIVE_PARSER);
    }

    /**
     * Gets all available document libraries for this site.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">
     * API Documentation</a>.
     *
     * @return the list of drives representing the document libraries.
     */
    public java.util.List<Drive> getDocumentLibraries() {
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append(getId())
                                .append(DRIVES_URL_PATH_SUFFIX)
                                .toString())
                        .build(),
                DRIVE_LIST_PARSER);
    }

    /**
     * Gets all lists under the site.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">
     * API Documentation</a>.
     *
     * @return the associated lists
     */
    public java.util.List<List> getLists() {
        final String siteId = getId();
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append(siteId)
                                .append(LIST_URL_PATH_SUFFIX)
                                .toString())
                        .build(),
                new ListListParser(siteId));
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

        final Site that = (Site) obj;
        return Objects.equals(getDisplayName(), that.getDisplayName())
                && objectDefinedEquals(getRoot(), that.getRoot())
                && Objects.equals(getSharepointIds(), that.getSharepointIds())
                && Objects.equals(getSiteCollection(), that.getSiteCollection())
                && Objects.equals(getContentTypes(), that.getContentTypes())
                && Objects.equals(getColumns(), that.getColumns())
                && Objects.equals(getItems(), that.getItems());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getDisplayName(),
                Objects.nonNull(getRoot()),
                getSharepointIds(),
                getSiteCollection(),
                getContentTypes(),
                getColumns(),
                getItems());
    }
}
