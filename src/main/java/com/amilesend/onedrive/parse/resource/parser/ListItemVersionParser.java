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
import com.amilesend.onedrive.resource.site.ListItemVersion;
import com.google.gson.Gson;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parses a response body that contains a {@link ListItemVersion}.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitemversion_get">
 * API Documentation</a>
 * @see ListItemVersion
 */
@RequiredArgsConstructor
public class ListItemVersionParser implements GsonParser<ListItemVersion> {
    /** The associated site identifier. */
    private final String siteId;
    /** The associated list identifier. */
    private final String listId;
    /** The associated list item identifier. */
    private final String listItemId;

    @Override
    public ListItemVersion parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final ListItemVersion version = gson.fromJson(new InputStreamReader(jsonStream), ListItemVersion.class);
        return ListItemVersion.builder()
                .connection(version.getConnection())
                .fields(version.getFields())
                .id(version.getId())
                .lastModifiedBy(version.getLastModifiedBy())
                .lastModifiedDateTime(version.getLastModifiedDateTime())
                .listId(listId)
                .listItemId(listItemId)
                .published(version.getPublished())
                .siteId(siteId)
                .build();
    }
}
