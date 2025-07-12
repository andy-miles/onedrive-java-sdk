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

import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.resource.site.ListItem;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

/**
 * Parses a response body that contains a {@link ListItem}.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/listitem_create">
 * API Documentation</a>
 * @see ListItem
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ListItemParser implements GsonParser<ListItem> {
    private static final BasicParser<ListItem> BASIC_PARSER = new BasicParser<>(ListItem.class);

    /** The associated site identifier. */
    private final String siteId;
    /** The associated list identifier. */
    private final String listId;

    @Override
    public ListItem parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final ListItem response = BASIC_PARSER.parse(gson, jsonStream);
        return ListItem.builder()
                .connection(response.getConnection())
                .contentType(response.getContentType())
                .driveItem(response.getDriveItem())
                .fields(response.getFields())
                .listId(listId)
                .sharepointIds(response.getSharepointIds())
                .siteId(siteId)
                // BaseItem
                .createdBy(response.getCreatedBy())
                .createdDateTime(response.getCreatedDateTime())
                .description(response.getDescription())
                .eTag(response.getETag())
                .id(response.getId())
                .lastModifiedBy(response.getLastModifiedBy())
                .lastModifiedDateTime(response.getLastModifiedDateTime())
                .name(response.getName())
                .parentReference(response.getParentReference())
                .webUrl(response.getWebUrl())
                .build();
    }
}
