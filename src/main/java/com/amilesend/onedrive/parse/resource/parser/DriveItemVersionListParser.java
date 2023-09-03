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
package com.amilesend.onedrive.parse.resource.parser;

import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.InputStreamReader;
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
@RequiredArgsConstructor
public class DriveItemVersionListParser implements GsonParser<List<DriveItemVersion>> {
    private final String driveItemId;
    private final String name;

    @Override
    public List<DriveItemVersion> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final InputStreamReader reader = new InputStreamReader(jsonStream);
        return gson.fromJson(reader, DriveItemVersionListResponseBody.class)
                .getValue()
                .stream()
                .filter(Objects::nonNull)
                .map(div -> {
                    // Inject the associated drive item id and name
                    div.setDriveItemId(driveItemId);
                    div.setName(name);
                    return div;
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class DriveItemVersionListResponseBody {
        private List<DriveItemVersion> value;
    }
}
