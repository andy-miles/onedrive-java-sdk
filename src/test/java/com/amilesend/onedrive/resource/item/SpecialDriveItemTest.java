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
package com.amilesend.onedrive.resource.item;

import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.parser.ListResponseBodyParser;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newSpecialDriveItem;
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
public class SpecialDriveItemTest {
    private static final SpecialFolder.Type TYPE = SpecialFolder.Type.APP_ROOT;
    private static final String BASE_URL = "http://localhost/me";

    @Mock
    private OneDriveConnection mockConnection;
    private SpecialDriveItem itemUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newRequestBuilder()).thenReturn(new Request.Builder());
        itemUnderTest = SpecialDriveItem.builder()
                .connection(mockConnection)
                .specialFolderType(TYPE)
                .build();
    }

    @Test
    public void getChildren_withValidType_shouldReturnDriveItemList() {
        final List<DriveItem> expected = List.of(mock(DriveItem.class), mock(DriveItem.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<DriveItem> actual = itemUnderTest.getChildren();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListResponseBodyParser.class)),
                () -> assertEquals("http://localhost/me/drive/special/approot/children",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getChildren_withNullType_shouldThrowException() {
        itemUnderTest.setSpecialFolderType(null);
        assertThrows(NullPointerException.class, () -> itemUnderTest.getChildren());
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final SpecialDriveItem thisItem = newSpecialDriveItem(mockConnection);
        final SpecialDriveItem thatItem = newSpecialDriveItem(mockConnection);
        assertAll(
                () -> assertTrue(thisItem.equals(thisItem)),
                () -> assertTrue(thisItem.equals(thatItem)),
                () -> assertFalse(thisItem.equals(null)),
                () -> assertFalse(thisItem.equals(DriveItemVersion.builder().connection(mockConnection).build())),
                () -> {
                    thatItem.setSpecialFolderType(TYPE);
                    assertFalse(thisItem.equals(thatItem));
                });
    }
}
