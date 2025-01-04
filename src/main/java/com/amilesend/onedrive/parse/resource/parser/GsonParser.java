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

import java.io.InputStream;

/**
 * Defines a parser that deserializes a JSON-formatted source input stream to the defined POJO type.
 *
 * @param <T> the POJO type
 */
public interface GsonParser<T> {
    /**
     * Deserializes a JSON-formatted input stream to the defined POJO type.
     *
     * @param gson the Gson instance used to deserialize the string
     * @param jsonStream stream with expected JSON-formatted contents
     * @return the parsed POJO instance
     */
    T parse(Gson gson, InputStream jsonStream);
}
