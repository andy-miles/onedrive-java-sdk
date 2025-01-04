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
package com.amilesend.onedrive.resource.site;

import com.amilesend.onedrive.parse.resource.parser.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.ListItemParser;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListItemCrudTest extends ListItemTestBase {
    private static final int SUCCESS_RESPONSE_CODE = 200;

    @Test
    public void update_shouldUpdateListItem() {
        final ListItem expected = mock(ListItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        when(mockGson.toJson(any(ListItem.class))).thenReturn("ListItemJsonValue");

        final ListItem actual = listItemUnderTest.update();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListItemParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue" +
                                "/lists/ListIdValue/items/ListItemIdValue",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("PATCH", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void delete_shouldDeleteListItem() {
        when(mockConnection.execute(any(Request.class))).thenReturn(SUCCESS_RESPONSE_CODE);

        listItemUnderTest.delete();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertTrue(listItemUnderTest.isDeleted()),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/sites/SiteIdValue" +
                                "/lists/ListIdValue/items/ListItemIdValue",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("DELETE", requestCaptor.getValue().method()));
    }
}
