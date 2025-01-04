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

import com.amilesend.onedrive.resource.item.type.Preview;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

/**
 * Parses a response body that contains a {@link Preview}.
 * @see Preview
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PreviewParser implements GsonParser<Preview> {
    private static final BasicParser<Preview> BASIC_PARSER = new BasicParser<>(Preview.class);

    /** The permission associated with the permission to parse. */
    private final String driveItemId;

    @Override
    public Preview parse(final Gson gson, final InputStream jsonStream) {
        final Preview preview = BASIC_PARSER.parse(gson, jsonStream);
        return Preview.builder()
                .driveItemId(driveItemId)
                .getUrl(preview.getGetUrl())
                .postParameters(preview.getPostParameters())
                .postUrl(preview.getPostUrl())
                .build();
    }
}
