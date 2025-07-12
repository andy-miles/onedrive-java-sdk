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

import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.resource.site.List;
import com.amilesend.onedrive.resource.site.ListItem;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Parses a response body that contains a list of {@link ListItem}s.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/list_list">
 * API Documentation</a>
 * @see ListItem
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ListListParser implements GsonParser<java.util.List<List>> {
    private static final ListResponseBodyParser<List> RESPONSE_BODY_PARSER =
            new ListResponseBodyParser<>(List.class);

    /** The associated site identifier. */
    private final String siteId;

    @Override
    public java.util.List<List> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return RESPONSE_BODY_PARSER.parse(gson, jsonStream)
                .stream()
                .filter(Objects::nonNull)
                .map(l -> {
                    // Copy and inject the site identifier
                    return List.builder()
                            .columns(l.getColumns())
                            .connection(l.getConnection())
                            .contentTypes(l.getContentTypes())
                            .displayName(l.getDisplayName())
                            .drive(l.getDrive())
                            .list(l.getList())
                            .siteId(siteId)
                            .system(l.getSystem())
                            // BaseItem
                            .createdBy(l.getCreatedBy())
                            .createdDateTime(l.getCreatedDateTime())
                            .description(l.getDescription())
                            .eTag(l.getETag())
                            .id(l.getId())
                            .lastModifiedBy(l.getLastModifiedBy())
                            .lastModifiedDateTime(l.getLastModifiedDateTime())
                            .name(l.getName())
                            .parentReference(l.getParentReference())
                            .webUrl(l.getWebUrl())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
