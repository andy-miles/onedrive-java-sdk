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
package com.amilesend.onedrive.parse.resource.parser;

import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parses a response body that contains a list of {@link DriveItemVersion}s.
 * <p>
 * Example response body:
 * <pre>
 * {
 *   "value":
 *   [
 *     {
 *       "id": "3.0",
 *       "lastModifiedBy": {
 *         "user": {
 *           "id": "CE251278-EF9E-4FE5-833C-1D89EEAE68E0",
 *           "displayName": "Ryan Gregg"
 *         }
 *       },
 *       "lastModifiedDateTime": "2017-09-14T12:34:53.912Z",
 *       "size": 123
 *     },
 *     {
 *       "id": "2.0",
 *       "lastModifiedBy": {
 *         "user": {
 *           "id": "CE251278-EF9E-4FE5-833C-1D89EEAE68E0",
 *           "displayName": "Ryan Gregg"
 *         }
 *       },
 *       "lastModifiedDateTime": "2017-09-11T10:21:03.000Z",
 *       "size": 62
 *     },
 *     {
 *       "id": "1.0",
 *       "lastModifiedBy": {
 *         "user": {
 *           "id": "CE251278-EF9E-4FE5-833C-1D89EEAE68E0",
 *           "displayName": "Ryan Gregg"
 *         }
 *       },
 *       "lastModifiedDateTime": "2017-09-10T15:20:01.125Z",
 *       "size": 16
 *     }
 *   ]
 * }
 * </pre>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_versions">
 * API Documentation</a>
 * @see DriveItemVersion
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DriveItemVersionListParser implements GsonParser<List<DriveItemVersion>> {
    private static final ListResponseBodyParser<DriveItemVersion> RESPONSE_BODY_PARSER =
            new ListResponseBodyParser<>(DriveItemVersion.class);

    /** The drive item identifier associated with the version to parse. */
    private final String driveItemId;
    /** The drive item name associated with the version to parse. */
    private final String name;

    @Override
    public List<DriveItemVersion> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return RESPONSE_BODY_PARSER.parse(gson, jsonStream)
                .stream()
                .filter(Objects::nonNull)
                .map(div -> {
                    // Copy and inject the associated drive item id and name
                    return DriveItemVersion.builder()
                            .connection(div.getConnection())
                            .driveItemId(driveItemId)
                            .id(div.getId())
                            .lastModifiedBy(div.getLastModifiedBy())
                            .lastModifiedDateTime(div.getLastModifiedDateTime())
                            .name(name)
                            .publication(div.getPublication())
                            .size(div.getSize())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
