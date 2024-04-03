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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Defines a {@link GsonParser} implementation for a map of key value pairs.
 *
 * @param <K> the key object type
 * @param <V> the value object type
 */
public class MapParser<K, V> implements GsonParser<Map<K, V>> {
    private final Type typeSpecifier;

    /**
     * Creates a new {@code ListParser} for the given class type.
     *
     * @param keyClazz the class type for the key
     * @param valueClazz the class type for the value
     */
    public MapParser(@NonNull final Class<K> keyClazz, @NonNull final Class<V> valueClazz) {
        typeSpecifier = TypeToken.getParameterized(Map.class, keyClazz, valueClazz).getType();
    }

    @Override
    public Map<K, V> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return gson.fromJson(new InputStreamReader(jsonStream), typeSpecifier);
    }
}
