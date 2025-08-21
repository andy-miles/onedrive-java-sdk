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
package com.amilesend.onedrive;

import com.amilesend.client.util.Validate;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.Site;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.SITE_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.SITE_PARSER;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVES_URL_PATH_SUFFIX;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVE_URL_PATH_SUFFIX;
import static com.amilesend.onedrive.resource.site.Site.SITE_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.site.Site.SITE_URL_PATH_SUFFIX;

/**
 * The primary exposed object to consumers to access drives available to the authenticated user for business accounts.
 * <p>
 * While this object can be instantiated directly by consumers, it is recommended to leverage the
 * {@link OneDriveFactoryStateManager} instead. Doing so simplifies OAUTH authentication and persists the
 * authentication tokens for reuse. Without it, consumers are responsible for initiating the OAUTH handshake
 * to obtain the authorization code and managing persisted refresh tokens for future instantiation without
 * requiring the user to re-authorize the consumer application with their OneDrive account.
 *
 * @see OneDriveFactoryStateManager
 */
public class BusinessOneDrive extends OneDrive {
    private static final String GROUPS_BASE_URL_PATH = "/groups/";
    private static final int MAX_QUERY_LENGTH = 1000;

    /**
     * Creates a new {@code BusinessOneDrive}.
     *
     * @param connection the authenticated connection
     */
    public BusinessOneDrive(final OneDriveConnection connection) {
        super(connection);
    }

    //////////
    // Site
    //////////

    /**
     * Gets the root site.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">
     * API Documentation</a>.
     *
     * @return the root site
     * @see Site
     */
    public Site getRootSite() {
        final OneDriveConnection connection = getConnection();
        return new Site(connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append("root")
                                .toString())
                        .build(),
                SITE_PARSER));
    }

    /**
     * Gets the {@link Site} for the given {@code siteId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/site">
     * API Documentation</a>.
     *
     * @param siteId the site identifier
     * @return the site
     * @see Site
     */
    public Site getSite(final String siteId) {
        final String encodedSiteId = validateIdAndUrlEncode(siteId, "siteId");
        final OneDriveConnection connection = getConnection();
        return new Site(connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append(encodedSiteId)
                                .toString())
                        .build(),
                SITE_PARSER));
    }

    /**
     * Gets the root {@link Site} for the given {@code groupId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/site_get">
     * API Documentation</a>.
     *
     * @param groupId the group identifier
     * @return the site
     */
    public Site getSiteForGroup(final String groupId) {
        final String encodedGroupId = validateIdAndUrlEncode(groupId, "groupId");
        final OneDriveConnection connection = getConnection();
        return new Site(connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(GROUPS_BASE_URL_PATH)
                                .append(encodedGroupId)
                                .append(SITE_BASE_URL_PATH)
                                .append("root")
                                .toString())
                        .build(),
                SITE_PARSER));
    }

    /**
     * Gets the list of {@link Site}s in an organization.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/site_list">
     * API Documentation</a>.
     *
     * @return the list of sites
     * @see Site
     */
    public List<Site> getRootSites() {
        final OneDriveConnection connection = getConnection();
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_URL_PATH_SUFFIX)
                                .append("?select=siteCollection,webUrl&filter=siteCollection/root%20ne%20null")
                                .toString())
                        .build(),
                SITE_LIST_PARSER)
                .stream()
                .map(s -> new Site(s))
                .collect(Collectors.toList());
    }

    /**
     * Searches for sites for the given {@code query}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/site_search">
     * API Documentation</a>.
     *
     * @param query the search query
     * @return the list of sites
     * @see Site
     */
    public List<Site> searchForSite(final String query) {
        Validate.notBlank(query, "query must not be blank");
        Validate.isTrue(query.length() < MAX_QUERY_LENGTH,
                "query length must be less than " + MAX_QUERY_LENGTH);

        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final OneDriveConnection connection = getConnection();
        return connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_URL_PATH_SUFFIX)
                                .append("?search=")
                                .append(encodedQuery)
                                .toString())
                        .build(),
                SITE_LIST_PARSER)
                .stream()
                .map(s -> new Site(s))
                .collect(Collectors.toList());
    }


    //////////
    // Drive
    //////////

    /**
     * Gets the default {@link Drive} for the given {@code groupId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_get">
     * API Documentation</a>.
     *
     * @param groupId the group identifier
     * @return the drive
     */
    public Drive getDefaultDriveForGroup(final String groupId) {
        final String encodedGroupId = validateIdAndUrlEncode(groupId, "groupId");
        final OneDriveConnection connection = getConnection();
        return new Drive(connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(GROUPS_BASE_URL_PATH)
                                .append(encodedGroupId)
                                .append(DRIVE_URL_PATH_SUFFIX)
                                .toString())
                        .build(),
                DRIVE_PARSER));
    }

    /**
     * Gets the default {@link Drive} for the given {@code siteId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_get">
     * API Documentation</a>.
     *
     * @param siteId the site identifier
     * @return the drive
     */
    public Drive getDefaultDriveForSite(final String siteId) {
        final String encodedSiteId = validateIdAndUrlEncode(siteId, "siteId");
        final OneDriveConnection connection = getConnection();
        return new Drive(connection.execute(
                connection.newRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(SITE_BASE_URL_PATH)
                                .append(encodedSiteId)
                                .append(DRIVE_URL_PATH_SUFFIX)
                                .toString())
                        .build(),
                DRIVE_PARSER));
    }

    /**
     * Gets the list of drives available to the given {@code siteId}.
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_list">
     * API Documentation</a>.
     *
     * @param siteId the site identifier
     * @return the list of drives
     */
    public List<Drive> getDrivesForSite(final String siteId) {
        return getDrivesForId(siteId, "siteId", "/sites/");
    }

    /**
     * Gets the list of drives available to the given {@code groupId}. Note: Business accounts only
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_list">
     * API Documentation</a>.
     *
     * @param groupId the group identifier
     * @return the list of drives
     */
    public List<Drive> getDrivesForGroup(final String groupId) {
        return getDrivesForId(groupId, "groupId", "/groups/");
    }

    /**
     * Gets the list of drives available to the given {@code userId}. Note: Business accounts only
     * <p>
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_list">
     * API Documentation</a>.
     *
     * @param userId the user identifier
     * @return the list of drives
     */
    public List<Drive> getDrivesForUser(final String userId) {
        return getDrivesForId(userId, "userId", "/users/");
    }

    private List<Drive> getDrivesForId(final String id, final String idName, final String urlSubPath) {
        final String encodedGroupId = validateIdAndUrlEncode(id, idName);
        final OneDriveConnection connection = getConnection();
        return connection.execute(
                        connection.newRequestBuilder()
                                .url(new StringBuilder(connection.getBaseUrl())
                                        .append(urlSubPath)
                                        .append(encodedGroupId)
                                        .append(DRIVES_URL_PATH_SUFFIX)
                                        .toString())
                                .build(),
                        DRIVE_LIST_PARSER)
                .stream()
                .map(d -> new Drive(d))
                .collect(Collectors.toList());
    }
}
