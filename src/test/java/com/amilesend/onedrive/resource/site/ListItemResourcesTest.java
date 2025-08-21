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

import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.client.util.StringUtils;
import com.amilesend.onedrive.parse.resource.parser.ListItemVersionListParser;
import com.amilesend.onedrive.parse.resource.parser.ListItemVersionParser;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListItemResourcesTest extends ListItemTestBase {
    @Test
    public void getListItemVersions_shouldReturnResponse() {
        final java.util.List<ListItemVersion> expected = java.util.List.of(mock(ListItemVersion.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final java.util.List<ListItemVersion> actual = listItemUnderTest.getListItemVersions();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListItemVersionListParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue" +
                                "/lists/ListIdValue/items/ListItemIdValue/versions",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getVersion_withValidVersionId_shouldReturnListItemVersion() {
        final ListItemVersion expected = mock(ListItemVersion.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final ListItemVersion actual = listItemUnderTest.getVersion("ListItemVersionId");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListItemVersionParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue" +
                                "/lists/ListIdValue/items/ListItemIdValue/versions/ListItemVersionId",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getVersion_withInvalidVersionId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> listItemUnderTest.getVersion(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> listItemUnderTest.getVersion(StringUtils.EMPTY)));
    }
}
