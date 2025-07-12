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
package com.amilesend.onedrive.resource.drive;

import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.parser.ListResponseBodyParser;
import com.amilesend.onedrive.parse.resource.parser.SpecialDriveItemParser;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDrive;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveTest {
    private static final String BASE_URL = "http://localhost/me";
    private static final String DRIVE_ID = "DriveIdValue";
    private static final String NEXT_LINK_URL = "http://localhost/NextPageUrl";

    @Mock
    private OneDriveConnection mockConnection;
    private Drive driveUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newRequestBuilder()).thenReturn(new Request.Builder());
        driveUnderTest = Drive.builder()
                .connection(mockConnection)
                .id(DRIVE_ID)
                .build();
    }

    @Test
    public void ctor_withNullConnection_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new Drive(null));
    }

    @SneakyThrows
    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final List<ItemActivity> expected = List.of(mock(ItemActivity.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<ItemActivity> actual = driveUnderTest.getActivities();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListResponseBodyParser.class)),
                () -> assertEquals("http://localhost/me/drive/DriveIdValue/activities",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @SneakyThrows
    @Test
    public void getRootFolder_shouldReturnDriveItem() {
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final DriveItem actual = driveUnderTest.getRootFolder();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/me/drive/root", requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @SneakyThrows
    @Test
    public void getChanges_shouldReturnDriveItemList() {
        setUpPaginatedDriveItemResponseBehavior();

        final List<DriveItem> actual = driveUnderTest.getChanges();

        validatePaginatedDriveItemResponseBehavior(actual, "http://localhost/me/drive/root/delta");
    }

    @SneakyThrows
    @Test
    public void search_withValidQuery_shouldReturnDriveItemList() {
        setUpPaginatedDriveItemResponseBehavior();

        final List<DriveItem> actual = driveUnderTest.search("SearchQuery");

        validatePaginatedDriveItemResponseBehavior(actual, "http://localhost/me/drive/root/search(q='SearchQuery')");
    }

    @SneakyThrows
    @Test
    public void search_withInvalidQuery_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> driveUnderTest.search(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> driveUnderTest.search(StringUtils.EMPTY)));
    }

    @SneakyThrows
    @Test
    public void getSpecialFolder_withValidType_shouldReturnSpecialDriveItem() {
        final SpecialDriveItem expected = mock(SpecialDriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final SpecialDriveItem actual = driveUnderTest.getSpecialFolder(SpecialFolder.Type.DOCUMENTS);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SpecialDriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/special/documents",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @SneakyThrows
    @Test
    public void getSpecialFolder_withNullType_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveUnderTest.getSpecialFolder(null));
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final Drive thisItem = newDrive(mockConnection);
        final Drive thatItem = newDrive(mockConnection);
        assertAll(
                () -> assertTrue(thisItem.equals(thisItem)),
                () -> assertTrue(thisItem.equals(thatItem)),
                () -> assertFalse(thisItem.equals(null)),
                () -> assertFalse(thisItem.equals(DriveItemVersion.builder().connection(mockConnection).build())),
                () -> {
                    thatItem.setName("DifferentName");
                    assertFalse(thisItem.equals(thatItem));
                    thatItem.setName(thisItem.getName());
                },
                () -> {
                    final Drive differentSystemDrive = newDrive(mockConnection, null);
                    assertFalse(thisItem.equals(differentSystemDrive));
                });
    }

    private void setUpPaginatedDriveItemResponseBehavior() {
        final List<DriveItem> mockDriveItemList = List.of(mock(DriveItem.class));
        final DriveItemPage mockPage = mock(DriveItemPage.class);
        when(mockPage.getValue()).thenReturn(mockDriveItemList);
        when(mockPage.getNextLink())
                .thenReturn(NEXT_LINK_URL) // First for the check
                .thenReturn(NEXT_LINK_URL) // Actually retrieve it for the next call
                .thenReturn(null); // For the check on the second loop iteration
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(mockPage);
    }

    private void validatePaginatedDriveItemResponseBehavior(final List<DriveItem> actual, final String apiUrl) {
        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(2, actual.size()),
                () -> verify(mockConnection, times(2))
                        .execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals(apiUrl, requestCaptor.getAllValues().get(0).url().toString()),
                () -> assertEquals("GET", requestCaptor.getAllValues().get(0).method()),
                () -> assertEquals(NEXT_LINK_URL, requestCaptor.getAllValues().get(1).url().toString()),
                () -> assertEquals("GET", requestCaptor.getAllValues().get(1).method()));
    }
}
