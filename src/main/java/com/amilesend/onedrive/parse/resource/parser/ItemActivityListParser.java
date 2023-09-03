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
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Parses a response body that contains a list of {@link ItemActivity} objects.
 * <p>
 * Example response body:
 * <pre>
 * {
 *   "value": [
 *     {
 *       "action": {
 *         "comment": {}
 *       },
 *       "actor": {
 *         "user": {
 *           "displayName": "Xavier Wilke"
 *         }
 *       },
 *       "id": "EJalEvjV1EgIYFQAAAAAAA==",
 *       "times": {
 *           "recordedDateTime": "2017-07-29T18:34:40Z"
 *       }
 *     },
 *   {
 *       "action": {
 *         "edit": {},
 *         "version": {
 *           "newVersion": "2.0"
 *         }
 *       },
 *       "actor": {
 *         "user": {
 *           "displayName": "Judith Clemons"
 *         }
 *       },
 *       "id": "cInT6/fV1EgFYFQAAAAAAA==",
 *       "times": {
 *         "recordedDateTime": "2017-07-29T16:23:35Z"
 *       }
 *     },
 *     {
 *       "action": {
 *         "mention": {
 *           "mentionees": [
 *             {
 *               "user": {
 *                 "displayName": "Judith Clemons"
 *               }
 *             }
 *           ]
 *         }
 *       },
 *       "actor": {
 *         "user": {
 *           "displayName": "Misty Suarez"
 *         }
 *       },
 *       "id": "EBJa0vPV1EjFX1QAAAAAAA==",
 *       "times": {
 *         "recordedDateTime": "2017-07-28T20:14:14Z"
 *       }
 *     },
 *     {
 *       "action": {
 *         "rename": {
 *           "oldName": "Document2.docx"
 *         }
 *       },
 *       "actor": {
 *         "user": {
 *           "displayName": "Misty Suarez"
 *         }
 *       },
 *       "id": "QFJFlfPV1Ei/X1QAAAAAAA==",
 *       "times": {
 *         "recordedDateTime": "2017-07-28T20:12:32Z"
 *       }
 *     },
 *     {
 *       "action": {
 *         "create": {}
 *       },
 *       "actor": {
 *         "user": {
 *           "displayName": "Misty Suarez"
 *         }
 *       },
 *       "id": "IJydkPPV1Ei9X1QAAAAAAA==",
 *       "times": {
 *         "recordedDateTime": "2017-07-28T20:02:24Z"
 *       }
 *     }
 *   ]
 * }
 * </pre>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/activities_list">
 * API Documentation</a>
 * @see ItemActivity
 */
public class ItemActivityListParser implements GsonParser<List<ItemActivity>> {
    @Override
    public List<ItemActivity> parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final InputStreamReader reader = new InputStreamReader(jsonStream);
        return gson.fromJson(reader, ItemActivityResponseBody.class).getValue();
    }

    @Data
    public static class ItemActivityResponseBody {
        private List<ItemActivity> value;
    }
}
