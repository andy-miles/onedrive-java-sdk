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
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.google.gson.Gson;
import lombok.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses a paginated response body that contains a list of {@link DriveItem}s.
 * <p>
 * Example response body:
 * <pre>
 * {
 *   "value": [
 *     {"name": "myfile.jpg", "size": 2048, "file": {} },
 *     {"name": "Documents", "folder": { "childCount": 4} },
 *     {"name": "Photos", "folder": { "childCount": 203} },
 *     {"name": "my sheet(1).xlsx", "size": 197 }
 *   ],
 *   "@odata.nextLink": "https://..."
 * }
 * </pre>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_list_children">
 * API Documentation</a>
 * @see DriveItem
 */
public class DriveItemPageParser implements GsonParser<DriveItemPage> {
    @Override
    public DriveItemPage parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return gson.fromJson(new InputStreamReader(jsonStream), DriveItemPage.class);
    }
}
