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

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.parser.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.ListItemParser;
import com.amilesend.onedrive.parse.resource.parser.ListResponseBodyParser;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.site.request.CreateListItemRequest;
import com.google.gson.Gson;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newFieldValueSet;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListTest {
    private static final String BASE_URL = "http://localhost";
    private static final String SITE_ID = "SiteIdValue";
    private static final String LIST_ID = "ListIdValue";

    @Mock
    private Gson mockGson;
    @Mock
    private OneDriveConnection mockConnection;
    private List listUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.getGson()).thenReturn(mockGson);
        final Request.Builder requestBuilder = new Request.Builder();
        lenient().when(mockConnection.newSignedForApiRequestBuilder()).thenReturn(requestBuilder);
        lenient().when(mockConnection.newSignedForApiWithBodyRequestBuilder()).thenReturn(requestBuilder);

        listUnderTest = List.builder()
                .connection(mockConnection)
                .siteId(SITE_ID)
                .id(LIST_ID)
                .build();
    }

    ///////////////////
    // createListItem
    ///////////////////

    @Test
    public void createListItem_withValidRequest_shouldReturnListItem() {
        final ListItem expected = mock(ListItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        when(mockGson.toJson(any(CreateListItemRequest.class))).thenReturn("RequestJsonValue");

        final ListItem actual = listUnderTest.createListItem(newFieldValueSet());

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListItemParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue/items",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void createListItem_withInvalidInput_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> listUnderTest.createListItem(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> listUnderTest.createListItem(Collections.emptyMap())));
    }

    ///////////////////
    // getActivities
    ///////////////////

    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final java.util.List<ItemActivity> expected = java.util.List.of(mock(ItemActivity.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final java.util.List<ItemActivity> actual = listUnderTest.getActivities();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListResponseBodyParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue/activities",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final List thisItem = newList(mockConnection, SITE_ID);
        final List thatItem = newList(mockConnection, SITE_ID);
        assertAll(
                () -> assertTrue(thisItem.equals(thisItem)),
                () -> assertTrue(thisItem.equals(thatItem)),
                () -> assertFalse(thisItem.equals(null)),
                () -> assertFalse(thisItem.equals(List.builder().connection(mockConnection).build())),
                () -> assertFalse(thisItem.equals(ListItem.builder().connection(mockConnection).build())),
                () -> {
                    thatItem.setName("DifferentName");
                    assertFalse(thisItem.equals(thatItem));
                    thatItem.setName(thisItem.getName());
                },
                () -> {
                    final List differentItem = newList(mockConnection, SITE_ID, new Object());
                    assertFalse(thisItem.equals(differentItem));
                });
    }
}
