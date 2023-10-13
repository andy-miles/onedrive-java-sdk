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
package com.amilesend.onedrive.resource.site;

import com.amilesend.onedrive.connection.OneDriveConnection;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.amilesend.onedrive.data.SiteTestDataHelper.newListItemVersion;
import static com.amilesend.onedrive.resource.site.ListItemVersion.NO_CONTENT_RESPONSE_HTTP_CODE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListItemVersionTest {
    private static final String BASE_URL = "http://localhost";
    private static final String SITE_ID = "SiteIdValue";
    private static final String LIST_ID = "ListIdValue";
    private static final String LIST_ITEM_ID_VALUE = "ListItemIdValue";

    @Mock
    private OneDriveConnection mockConnection;

    private ListItemVersion versionUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newSignedForRequestBuilder()).thenReturn(new Request.Builder());
        // VersionId == ListItemVersionIdValue
        versionUnderTest = newListItemVersion(mockConnection, SITE_ID, LIST_ID, LIST_ITEM_ID_VALUE);
    }

    @Test
    public void restore_withValidResponseCode_shouldReturnTrue() {
        when(mockConnection.execute(any(Request.class))).thenReturn(NO_CONTENT_RESPONSE_HTTP_CODE);

        final boolean actual = versionUnderTest.restore();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertTrue(actual),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue" +
                                "/items/ListItemIdValue/versions/ListItemVersionIdValue/restoreVersion",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()));
    }

    @Test
    public void restore_withInvalidResponseCode_shouldReturnFalse() {
        when(mockConnection.execute(any(Request.class))).thenReturn(400);

        final boolean actual = versionUnderTest.restore();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertFalse(actual),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue" +
                                "/items/ListItemIdValue/versions/ListItemVersionIdValue/restoreVersion",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()));
    }
}
