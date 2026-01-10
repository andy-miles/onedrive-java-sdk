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

import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.client.parse.parser.MapParser;
import com.amilesend.onedrive.resource.site.response.GetColumnValuesResponse;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newRandomString;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newFieldValueSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListItemColumnsTest extends ListItemTestBase {
    private static final int TOO_MANY_SELECTED_COLUMNS_COUNT = 13;
    private static final int TOO_LARGE_COLUMN_LENGTH = 129;

    ////////////////////
    // getColumnValues
    ////////////////////

    @Test
    public void getColumnValues_shouldReturnResponse() {
        final GetColumnValuesResponse expected = mock(GetColumnValuesResponse.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final GetColumnValuesResponse actual = listItemUnderTest.getColumnValues();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue" +
                                "/lists/ListIdValue/items/ListItemIdValue?expand=fields",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getColumnValues_withSelectedColumns_shouldReturnResponse() {
        final GetColumnValuesResponse expected = mock(GetColumnValuesResponse.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        final java.util.List<String> selectedColumns = java.util.List.of("Col1", "Col2", "Col3");

        final GetColumnValuesResponse actual = listItemUnderTest.getColumnValues(selectedColumns);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue" +
                                "/items/ListItemIdValue?expand=fields(select=Col1,Col2,Col3)",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getColumnValues_withEmptySelectedColumns_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> listItemUnderTest.getColumnValues(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> listItemUnderTest.getColumnValues(Collections.emptyList())));
    }

    @Test
    public void getColumnValues_withTooManySelectedColumns_shouldThrowException() {
        final java.util.List<String> selectedColumns = new ArrayList<>(TOO_MANY_SELECTED_COLUMNS_COUNT);
        for (int i = 0; i <= TOO_MANY_SELECTED_COLUMNS_COUNT; ++i) {
            selectedColumns.add("Col" + i);
        }

        assertThrows(IllegalArgumentException.class, () -> listItemUnderTest.getColumnValues(selectedColumns));
    }

    @Test
    public void getColumnValues_withOneTooLongSelectedColumn_shouldThrowException() {
        final java.util.List<String> selectedColumns =
                java.util.List.of("Col1", newRandomString(TOO_LARGE_COLUMN_LENGTH));

        assertThrows(IllegalArgumentException.class, () -> listItemUnderTest.getColumnValues(selectedColumns));
    }

    @Test
    public void getColumnValues_withOneNullSelectedColumn_shouldThrowException() {
        final java.util.List<String> selectedColumns = new ArrayList<>(3);
        selectedColumns.add("Col1");
        selectedColumns.add(null);
        selectedColumns.add("Col3");

        assertThrows(NullPointerException.class, () -> listItemUnderTest.getColumnValues(selectedColumns));
    }

    ///////////////////////
    // updateColumnValues
    ///////////////////////

    @Test
    public void updateColumnValues_withValidFields_shouldReturnUpdatedFields() {
        final Map<String, Object> expected = newFieldValueSet();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        when(mockGson.toJson(any(Map.class))).thenReturn("FieldsJsonValue");

        final Map<String, Object> actual = listItemUnderTest.updateColumnValues(expected);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(MapParser.class)),
                () -> assertEquals("http://localhost/sites/SiteIdValue/lists/ListIdValue" +
                                "/items/ListItemIdValue/fields",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("PATCH", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void updateColumnValues_withEmptyFields_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> listItemUnderTest.updateColumnValues(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> listItemUnderTest.updateColumnValues(Collections.emptyMap())));
    }
}
