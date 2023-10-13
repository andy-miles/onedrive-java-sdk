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
package com.amilesend.onedrive;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.parse.resource.parser.DriveListParser;
import com.amilesend.onedrive.parse.resource.parser.DriveParser;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.identity.Identity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.amilesend.onedrive.resource.drive.Drive.DRIVES_BASE_URL_PATH;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVES_URL_PATH_SUFFIX;
import static com.amilesend.onedrive.resource.drive.Drive.DRIVE_URL_PATH_SUFFIX;

/**
 * The primary exposed object to consumers to access drives available to the authenticated user.
 * <p>
 * While this object can be instantiated directly by consumers, it is recommended to leverage the
 * {@link OneDriveFactoryStateManager} instead. Doing so simplifies OAUTH authentication and persists the
 * authentication tokens for reuse. Without it, consumers are responsible for initiating the OAUTH handshake
 * to obtain the authorization code and managing persisted refresh tokens for future instantiation without
 * requiring the user to re-authorize the consumer application with their OneDrive account.
 *
 * @see OneDriveFactoryStateManager
 */
@Slf4j
@RequiredArgsConstructor
public class OneDrive {
    protected static final int MAX_ID_LENGTH = 512;
    protected static final DriveParser DRIVE_PARSER = new DriveParser();
    protected static final DriveListParser DRIVE_LIST_PARSER = new DriveListParser();

    @NonNull
    @Getter(AccessLevel.PROTECTED)
    private final OneDriveConnection connection;

    /**
     * Gets the user's default drive that is associated with their account.
     *
     * @return the user's drive
     * @see Drive
     */
    public Drive getUserDrive() {
        return new Drive(connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(connection.getBaseUrl() + DRIVE_URL_PATH_SUFFIX)
                        .build(),
                DRIVE_PARSER));
    }

    /**
     * Gets the list of drives available to the authenticated user.
     *
     * @return the list of available drives
     * @see Drive
     */
    public List<Drive> getAvailableDrives() {
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(connection.getBaseUrl() + DRIVES_URL_PATH_SUFFIX)
                        .build(),
                DRIVE_LIST_PARSER)
                .stream()
                .map(d -> new Drive(d))
                .collect(Collectors.toList());
    }

    /**
     * Gets the {@link Drive} for the given {@code driveId}.
     *
     * @param driveId the drive identifier
     * @return the drive
     * @see Drive
     */
    public Drive getDrive(final String driveId) {
        final String encodedDriveId = validateIdAndUrlEncode(driveId, "driveId");
        return new Drive(connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVES_BASE_URL_PATH)
                                .append(encodedDriveId)
                                .toString())
                        .build(),
                DRIVE_PARSER));
    }

    /**
     * Gets the authenticated user's display name associated with their default drive.
     *
     * @return the user's display name
     */
    public String getUserDisplayName() {
        return Optional.ofNullable(getUserDrive())
                .map(Drive::getOwner)
                .map(IdentitySet::getUser)
                .map(Identity::getDisplayName)
                .orElse("Unknown");
    }

    /**
     * Gets the authentication information associated with the current user connection. Since this
     * includes the access and refresh tokens, this should be treated as the customer's credentials.
     * This can be persisted if consumers are managing their own token state for instantiation.
     *
     * @return the authentication information
     */
    public AuthInfo getAuthInfo() {
        return connection.getAuthManager().getAuthInfo();
    }

    protected String validateIdAndUrlEncode(final String id, final String name) {
        Validate.notBlank(id, name + " must not be blank");
        Validate.isTrue(id.length() < MAX_ID_LENGTH, name + " must be less than " + MAX_ID_LENGTH);
        return URLEncoder.encode(id, StandardCharsets.UTF_8);
    }
}
