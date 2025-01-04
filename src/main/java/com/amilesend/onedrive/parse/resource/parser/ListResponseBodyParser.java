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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Defines a {@link GsonParser} implementation for a response body that contains a list of items.
 *
 * @param <T> the object type
 */
public class ListResponseBodyParser<T> implements GsonParser<List<T>> {
    private final Type typeSpecifier;

    public ListResponseBodyParser(@NonNull final Class<T> clazz) {
        typeSpecifier = TypeToken.getParameterized(ListResponseBody.class, clazz).getType();
    }

    @Override
    public List<T> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final ListResponseBody<T> responseBody = gson.fromJson(new InputStreamReader(jsonStream), typeSpecifier);
        return responseBody.getValue();
    }

    /** Used to deserialize a response body that contains a list of drive items. */
    @Data
    public static class ListResponseBody<T> {
        /** The list of items returned for a response body. */
        private List<T> value;
    }
}
