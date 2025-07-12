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

import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.GsonFactory;
import com.amilesend.onedrive.parse.resource.parser.ListListParser;
import com.amilesend.onedrive.parse.resource.parser.ListResponseBodyParser;
import com.amilesend.onedrive.resource.drive.Drive;
import com.google.gson.Gson;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.amilesend.onedrive.data.SiteTestDataHelper.newSite;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SiteTest {
    private static final String BASE_URL = "http://localhost";
    private static final String SITE_ID = "SiteIdValue";

    @Mock
    private GsonFactory mockGsonFactory;
    @Mock
    private Gson mockGson;
    @Mock
    private OneDriveConnection mockConnection;
    private Site siteUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGsonFactory.getInstance(any(OneDriveConnection.class))).thenReturn(mockGson);
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.getGsonFactory()).thenReturn(mockGsonFactory);
        final Request.Builder requestBuilder = new Request.Builder();
        lenient().when(mockConnection.newRequestBuilder()).thenReturn(requestBuilder);

        siteUnderTest = Site.builder()
                .connection(mockConnection)
                .id(SITE_ID)
                .build();
    }

    @Test
    public void getDefaultDocumentLibrary_shouldReturnDrive() {
        final Drive expected = mock(Drive.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final Drive actual = siteUnderTest.getDefaultDocumentLibrary();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/drive",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDocumentLibraries_shouldReturnDriveList() {
        final java.util.List<Drive> expected = java.util.List.of(mock(Drive.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final java.util.List<Drive> actual = siteUnderTest.getDocumentLibraries();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListResponseBodyParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/drives",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getLists_shouldReturnListOfLists() {
        final java.util.List<List> expected = java.util.List.of(mock(List.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final java.util.List<List> actual = siteUnderTest.getLists();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListListParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final Site thisSite = newSite(mockConnection);
        final Site thatSite = newSite(mockConnection);
        assertAll(
                () -> assertTrue(thisSite.equals(thisSite)),
                () -> assertTrue(thisSite.equals(thatSite)),
                () -> assertFalse(thisSite.equals(null)),
                () -> assertFalse(thisSite.equals(Site.builder().connection(mockConnection).build())),
                () -> assertFalse(thisSite.equals(List.builder().connection(mockConnection).build())),
                () -> {
                    thatSite.setName("DifferentName");
                    assertFalse(thisSite.equals(thatSite));
                    thatSite.setName(thisSite.getName());
                },
                () -> {
                    final Site differentItem = newSite(mockConnection, null);
                    assertFalse(thisSite.equals(differentItem));
                });
    }
}
