/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class ListItemTestBase {
    protected static final String BASE_URL = "http://localhost";
    protected static final String SITE_ID = "SiteIdValue";
    protected static final String LIST_ID = "ListIdValue";
    protected static final String LIST_ITEM_ID = "ListItemIdValue";
    protected static final String LIST_ITEM_NAME = "ListItemNameValue";

    @Mock
    protected GsonFactory mockGsonFactory;
    @Mock
    protected Gson mockGson;
    @Mock
    protected OneDriveConnection mockConnection;
    protected ListItem listItemUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGsonFactory.getInstance(any(OneDriveConnection.class))).thenReturn(mockGson);
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.getGsonFactory()).thenReturn(mockGsonFactory);
        final Request.Builder requestBuilder = new Request.Builder();
        lenient().when(mockConnection.newRequestBuilder()).thenReturn(requestBuilder);
        lenient().when(mockConnection.newWithBodyRequestBuilder()).thenReturn(requestBuilder);

        listItemUnderTest = ListItem.builder()
                .connection(mockConnection)
                .name(LIST_ITEM_NAME)
                .id(LIST_ITEM_ID)
                .listId(LIST_ID)
                .siteId(SITE_ID)
                .build();
    }
}
