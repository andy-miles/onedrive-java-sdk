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
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

/**
 * Parses a response body that contains a {@link SpecialDriveItem}.
 * @see SpecialDriveItem
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SpecialDriveItemParser implements GsonParser<SpecialDriveItem> {
    private static final BasicParser<SpecialDriveItem> BASIC_PARSER = new BasicParser<>(SpecialDriveItem.class);

    /** The special folder type associated with the special drive item to parse. */
    private final SpecialFolder.Type specialFolderType;

    @Override
    public SpecialDriveItem parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final SpecialDriveItem item = BASIC_PARSER.parse(gson, jsonStream);
        item.setSpecialFolderType(specialFolderType);
        return item;
    }
}
