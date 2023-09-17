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
package com.amilesend.onedrive.connection.auth.store;

import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.google.gson.Gson;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A simple {@link AuthInfoStore} implementation that stores and retrieves user authentication information
 * to the filesystem. This assumes a single user associated with the specified file path, which means
 * that any provided keyed identifiers are ignored.
 * @see AuthInfo
 * @see AuthInfoStore
 */
@RequiredArgsConstructor
public class SingleUserFileBasedAuthInfoStore implements AuthInfoStore {
    /** The configured GSON instance for JSON marshalling. */
    @NonNull
    private final Gson stateGson;
    /** The path to save and read from. */
    @NonNull
    private final Path stateFilePath;

    /**
     * Saves the given {@code authInfo} to the file system.
     *
     * @param id is ignored for this implementation
     * @param authInfo the authentication information
     * @throws IOException if an error occurred while saving the authentication info to the file system
     */
    @Override
    public void store(final String id, @NonNull final AuthInfo authInfo) throws IOException {
        Files.write(stateFilePath, authInfo.toJson(stateGson).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Retrieves the {@link AuthInfo} from the file system.
     *
     * @param id is ignored for this implementation
     * @return the authentication information, or {@code null}
     * @throws IOException if an error occurred while retrieving the authentication info from the file system
     */
    @Override
    public AuthInfo retrieve(final String id) throws IOException {
        if (!Files.exists(stateFilePath) || !Files.isReadable(stateFilePath)) {
            return null;
        }

        final String jsonState = Files.readString(stateFilePath);
        if (StringUtils.isBlank(jsonState)) {
            return null;
        }

        return AuthInfo.fromJson(stateGson, jsonState);
    }
}
