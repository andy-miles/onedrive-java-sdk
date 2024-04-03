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

import com.amilesend.onedrive.resource.site.ListItemVersion;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parses a response body that contains a list of {@link ListItemVersion}s.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_list_versions">
 * API Documentation</a>
 * @see ListItemVersion
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ListItemVersionListParser implements GsonParser<List<ListItemVersion>> {
    private static final ListResponseBodyParser<ListItemVersion> RESPONSE_BODY_PARSER =
            new ListResponseBodyParser<>(ListItemVersion.class);

    /** The associated site identifier. */
    private final String siteId;
    /** The associated list identifier. */
    private final String listId;
    /** The associated list item identifier. */
    private final String listItemId;

    @Override
    public List<ListItemVersion> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return RESPONSE_BODY_PARSER.parse(gson, jsonStream)
                .stream()
                .filter(Objects::nonNull)
                .map(liv -> {
                    // Copy and inject the associated identifiers
                    return ListItemVersion.builder()
                            .connection(liv.getConnection())
                            .fields(liv.getFields())
                            .id(liv.getId())
                            .lastModifiedBy(liv.getLastModifiedBy())
                            .lastModifiedDateTime(liv.getLastModifiedDateTime())
                            .listId(listId)
                            .listItemId(listItemId)
                            .published(liv.getPublished())
                            .siteId(siteId)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
