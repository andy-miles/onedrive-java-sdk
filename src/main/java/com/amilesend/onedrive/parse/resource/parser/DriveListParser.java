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
import com.amilesend.onedrive.resource.drive.Drive;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Parses a response body that contains a list of {@link Drive}s.
 * <p>
 * Example response body:
 * <pre>
 * {
 *   "value": [
 *     {
 *       "id": "942CAEB0-13AE-491B-85E4-7557CDC0F25F",
 *       "driveType": "documentLibrary",
 *       "name": "Shared Documents",
 *       "owner": {
 *         "user": {
 *           "id": "AE2A1EE9-81A7-423C-ABE4-B945F47509BB",
 *           "displayName": "Ryan Gregg"
 *         }
 *       }
 *     },
 *     {
 *       "id": "C1CD3ED9-0E98-4B0B-82D3-C8FB784B9DCC",
 *       "driveType": "documentLibrary",
 *       "name": "Contoso Project Files",
 *       "owner": {
 *         "user": {
 *           "id": "406B2281-18E8-4416-9857-38C531B904F1",
 *           "displayName": "Daron Spektor"
 *         }
 *       }
 *     }
 *   ]
 * }
 * </pre>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_list">
 * API Documentation</a>
 * @see Drive
 */
public class DriveListParser implements GsonParser<List<Drive>> {
    @Override
    public List<Drive> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        return gson.fromJson(new InputStreamReader(jsonStream), DriveListResponseBody.class).getValue();
    }

    /** Used to deserialize a response body that contains a list of drives. */
    @Data
    public static class DriveListResponseBody {
        /** The list of drives returned for a response body. */
        private List<Drive> value;
    }
}
