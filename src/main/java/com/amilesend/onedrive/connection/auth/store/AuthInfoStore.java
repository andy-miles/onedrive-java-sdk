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
package com.amilesend.onedrive.connection.auth.store;

import com.amilesend.onedrive.connection.auth.OneDriveAuthInfo;

import java.io.IOException;

/**
 * Defines the interface used to store and load persisted user auth tokens.
 * @see OneDriveAuthInfo
 */
public interface AuthInfoStore {
    /**
     * Stores the given {@code authInfo} for the associated keyed identifier.
     *
     * @param id the identifier to associate the authentication information for a given user.
     * @param authInfo the authentication information
     * @throws IOException if an error occurred while saving the authentication information
     * @see OneDriveAuthInfo
     */
    void store(String id, OneDriveAuthInfo authInfo) throws AuthInfoStoreException;

    /**
     * Retrieves the {@link OneDriveAuthInfo} for the given keyed identifier.
     *
     * @param id the identifier associated with authentication information to fetch for
     * @return the authentication information
     * @throws IOException if an error occurred while retrieving the authentication information
     */
    OneDriveAuthInfo retrieve(String id) throws AuthInfoStoreException;
}
