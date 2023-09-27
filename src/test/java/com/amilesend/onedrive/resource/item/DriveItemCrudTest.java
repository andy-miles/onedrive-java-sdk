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
package com.amilesend.onedrive.resource.item;

import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.DriveItemParser;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.google.gson.Gson;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static com.amilesend.onedrive.connection.parse.resource.parser.TestDataHelper.newDriveItem;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DriveItemCrudTest extends DriveItemTestBase {
    @Test
    public void ctor_withNullConnection_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> new DriveItem(null));
    }

    @Test
    public void create_withDriveItem_shouldReturnDriveItem() {
        final DriveItem expected = mock(DriveItem.class);
        when(expected.toJson()).thenReturn("JsonDriveItem");
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final DriveItem actual = driveItemUnderTest.create(expected);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/children",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void update_shouldReturnDriveItem() {
        doReturn("JsonDriveItemChanges").when(driveItemUnderTest).toJson();
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final DriveItem actual = driveItemUnderTest.update();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("PATCH", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void move_withBlankDestinationParentIdAndName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.move(StringUtils.EMPTY, null));
    }

    @Test
    public void move_withNoNewDestinationParentIdAndName_shouldThrowException() {
        driveItemUnderTest.setParentReference(ItemReference.builder().id("ParentIdValue").build());
        driveItemUnderTest.setName("ItemName");

        assertThrows(IllegalArgumentException.class,
                () -> driveItemUnderTest.move("ParentIdValue", "ItemName"));
    }

    @Test
    public void move_withNewDestinationParentId_shouldReturnUpdatedDriveItem() {
        doReturn(driveItemUnderTest).when(driveItemUnderTest).update();

        final DriveItem actual = driveItemUnderTest.move("NewParentIdValue", null);

        assertAll(
                () -> assertEquals(driveItemUnderTest, actual),
                () -> assertEquals("NewParentIdValue", actual.getParentReference().getId()),
                () -> verify(driveItemUnderTest).update());
    }

    @Test
    public void move_withNewDestinationParentIdAndExitingParentDefined_shouldReturnUpdatedDriveItem() {
        driveItemUnderTest.setParentReference(ItemReference.builder().id("NewParentIdValue").build());
        doReturn(driveItemUnderTest).when(driveItemUnderTest).update();

        final DriveItem actual = driveItemUnderTest.move("NewParentIdValue", null);

        assertAll(
                () -> assertEquals(driveItemUnderTest, actual),
                () -> assertEquals("NewParentIdValue", actual.getParentReference().getId()),
                () -> verify(driveItemUnderTest).update());
    }

    @Test
    public void move_withNewName_shouldReturnUpdatedDriveItem() {
        doReturn(driveItemUnderTest).when(driveItemUnderTest).update();

        final DriveItem actual = driveItemUnderTest.move(null, "NewName");

        assertAll(
                () -> assertEquals(driveItemUnderTest, actual),
                () -> assertEquals("NewName", actual.getName()),
                () -> verify(driveItemUnderTest).update());
    }

    @Test
    public void move_withNewNameAndSameDestinationParentId_shouldReturnUpdatedDriveItem() {
        doReturn(driveItemUnderTest).when(driveItemUnderTest).update();
        driveItemUnderTest.setParentReference(ItemReference.builder().id("ParentIdValue").build());

        final DriveItem actual = driveItemUnderTest.move("ParentIdValue", "NewName");

        assertAll(
                () -> assertEquals(driveItemUnderTest, actual),
                () -> assertEquals("NewName", actual.getName()),
                () -> verify(driveItemUnderTest).update());
    }

    @Test
    public void move_withBothNewParentIdAndName_shouldReturnUpdatedDriveItem() {
        doReturn(driveItemUnderTest).when(driveItemUnderTest).update();

        final DriveItem actual = driveItemUnderTest.move("NewParentIdValue", "NewName");

        assertAll(
                () -> assertEquals(driveItemUnderTest, actual),
                () -> assertEquals("NewParentIdValue", actual.getParentReference().getId()),
                () -> assertEquals("NewName", actual.getName()),
                () -> verify(driveItemUnderTest).update());
    }

    @Test
    public void copy_withNullParameters_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.copy(null, null));
    }

    @Test
    public void copy_withNoNewParentIdAndName_shouldThrowException() {
        driveItemUnderTest.setParentReference(ItemReference.builder().id("ParentIdValue").build());
        driveItemUnderTest.setName("ItemName");

        assertThrows(IllegalArgumentException.class,
                () -> driveItemUnderTest.copy("ParentIdValue", "ItemName"));
    }

    @Test
    public void copy_withDestinationParentId_shouldReturnAsyncJob() {
        copy_withAttributes_shouldReturnAsyncJob("NewDestinationParentId", null);
    }

    @Test
    public void copyWithDestinationParentIdAndExistingDefined_shouldReturnAsyncJob() {
        driveItemUnderTest.setParentReference(ItemReference.builder().id("OldParentIdValue").build());
        copy_withAttributes_shouldReturnAsyncJob("NewDestinationParentId", null);
    }

    @Test
    public void copy_withNewNameAndBlankDestinationParentId_shouldReturnAsyncJob() {
        copy_withAttributes_shouldReturnAsyncJob(null, "NewName");
    }

    @Test
    public void copy_withNewNameAndNoChangeInDestinationParentId_shouldReturnAsyncJob() {
        driveItemUnderTest.setParentReference(ItemReference.builder().id("ParentIdValue").build());
        copy_withAttributes_shouldReturnAsyncJob("ParentIdValue", "NewName");
    }

    @Test
    public void copy_withBothDestinationParentIdAndName_shouldReturnAsyncJob() {
        copy_withAttributes_shouldReturnAsyncJob("NewDestinationParentId", "NewName");
    }

    private void copy_withAttributes_shouldReturnAsyncJob(final String newParentId, final String newName) {
        doReturn("JsonBody").when(driveItemUnderTest).toJson();
        when(mockConnection.executeRemoteAsync(any(Request.class))).thenReturn("http://localhost/monitor");

        final AsyncJob actual = driveItemUnderTest.copy(newParentId, newName);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("http://localhost/monitor", actual.getMonitorUrl()),
                () -> verify(mockConnection).executeRemoteAsync(requestCaptor.capture()),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/copy",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void delete_shouldDelete() {
        when(mockConnection.execute(any(Request.class))).thenReturn(200);

        driveItemUnderTest.delete();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(driveItemUnderTest.getDeleted()),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("DELETE", requestCaptor.getValue().method()));
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final DriveItem thisItem = newDriveItem(mockConnection, 1);
        final DriveItem thatItem = newDriveItem(mockConnection, 1);
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
                    final DriveItem differentItem = newDriveItem(mockConnection, 1, null, new Object());
                    assertFalse(thisItem.equals(differentItem));
                },
                () -> {
                    final DriveItem differentItem = newDriveItem(mockConnection, 1, new Object(), null);
                    assertFalse(thisItem.equals(differentItem));
                });
    }

    @Test
    public void toJson_shouldReturnJsonString() {
        final Gson mockGson = mock(Gson.class);
        when(mockGson.toJson(any(DriveItem.class))).thenReturn("JsonStringValue");
        when(mockConnection.getGson()).thenReturn(mockGson);

        assertEquals("JsonStringValue", driveItemUnderTest.toJson());
    }
}
