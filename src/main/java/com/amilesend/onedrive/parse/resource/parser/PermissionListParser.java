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
package com.amilesend.onedrive.parse.resource.parser;

import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.amilesend.onedrive.parse.resource.parser.PermissionParser.copyWithDriveItemId;

/**
 * Parses a response body that contains a list of {@link Permission}s.
 * <p>Example response body:</p>
 * <pre>
 * {
 *   "value": [
 *     {
 *       "id": "1",
 *       "roles": ["write"],
 *       "link": {
 *         "webUrl": "webUrlValue",
 *         "type": "edit"
 *       }
 *     },
 *     {
 *       "id": "2",
 *       "roles": ["write"],
 *       "grantedTo": {
 *         "user": {
 *           "id": "5D33DD65C6932946",
 *           "displayName": "John Doe"
 *         }
 *       },
 *       "inheritedFrom": {
 *         "driveId": "1234567890ABD",
 *         "id": "1234567890ABC!123",
 *         "path": "/drive/root:/Documents" }
 *     },
 *     {
 *       "id": "3",
 *       "roles": ["write"],
 *       "link": {
 *         "webUrl": "webUrlValue",
 *         "type": "edit",
 *         "application": {
 *           "id": "12345",
 *           "displayName": "Contoso Time Manager"
 *         }
 *       }
 *     }
 *   ]
 * }
 * </pre>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_permissions">
 * API Documentation</a>
 * @see Permission
 */
@RequiredArgsConstructor
public class PermissionListParser implements GsonParser<List<Permission>> {
    /** The drive item identifier associated with the list of permissions to parse. */
    private final String driveItemId;

    public List<Permission> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return gson.fromJson(new InputStreamReader(jsonStream), PermissionsListResponseBody.class)
                .getValue()
                .stream()
                .filter(Objects::nonNull)
                .map(div -> copyWithDriveItemId(div, driveItemId))
                .collect(Collectors.toList());
    }

    /** Used to deserialize a response body that contains a list of permissions. */
    @Data
    public static class PermissionsListResponseBody {
        /** The list of permissions returned for a response body. */
        private List<Permission> value;
    }
}
