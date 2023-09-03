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
import com.amilesend.onedrive.parse.resource.parser.DriveItemPageParser;
import com.amilesend.onedrive.parse.resource.parser.DriveItemVersionListParser;
import com.amilesend.onedrive.parse.resource.parser.ItemActivityListParser;
import com.amilesend.onedrive.parse.resource.parser.PermissionListParser;
import com.amilesend.onedrive.parse.resource.parser.PermissionParser;
import com.amilesend.onedrive.parse.resource.parser.ThumbnailSetListParser;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import com.google.gson.Gson;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amilesend.onedrive.connection.OneDriveConnection.JSON_MEDIA_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveItemResourcesTest extends DriveItemTestBase {
    private static final String NEXT_LINK_URL = "http://localhost/NextPageUrl";

    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final List<ItemActivity> expected = List.of(mock(ItemActivity.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<ItemActivity> actual = driveItemUnderTest.getActivities();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ItemActivityListParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/activities",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getActivities_withInvalidDriveItemId_shouldThrowException() {
        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.getActivities());
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.getActivities());
                });
    }

    @Test
    public void getChildren_shouldReturnDriveItemList() {
        setUpPaginatedDriveItemResponseBehavior();

        final List<DriveItem> actual = driveItemUnderTest.getChildren();

        validatePaginatedDriveItemResponseBehavior(actual, "http://localhost/me/drive/items/DriveItemId/children");
    }

    @Test
    public void getChildren_withInvalidDriveItemId_shouldThrowException() {
        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.getChildren());
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.getChildren());
                });
    }

    @Test
    public void getVersions_shouldReturnDriveItemVersionList() {
        final List<DriveItemVersion> expected = List.of(mock(DriveItemVersion.class), mock(DriveItemVersion.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<DriveItemVersion> actual = driveItemUnderTest.getVersions();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveItemVersionListParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/versions",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getVersions_withInvalidDriveItemId_shouldThrowException() {
        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.getVersions());
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.getVersions());
                });
    }

    @Test
    public void getPermissions_shouldReturnDriveItemVersionList() {
        final List<Permission> expected = List.of(mock(Permission.class), mock(Permission.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<Permission> actual = driveItemUnderTest.getPermissions();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(PermissionListParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/permissions",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getPermissions_withInvalidDriveItemId_shouldThrowException() {
        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.getPermissions());
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.getPermissions());
                });
    }

    @Test
    public void addPermission_withValidRequest_shouldReturnPermission() {
        final Permission expected = mock(Permission.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        final Gson mockGson = mock(Gson.class);
        when(mockGson.toJson(any(AddPermissionRequest.class))).thenReturn("JsonPermissionRequestBody");
        when(mockConnection.getGson()).thenReturn(mockGson);

        final Permission actual = driveItemUnderTest.addPermission(mock(AddPermissionRequest.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(PermissionParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/invite",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void addPermission_withInvalidParameters_shouldThrowException() {
        final AddPermissionRequest mockRequest = mock(AddPermissionRequest.class);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> driveItemUnderTest.addPermission(null)),
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.addPermission(mockRequest));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.addPermission(mockRequest));
                });
    }

    @Test
    public void createSharingLink_withValidRequest_shouldReturnPermission() {
        final Permission expected = mock(Permission.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        final Gson mockGson = mock(Gson.class);
        when(mockGson.toJson(any(CreateSharingLinkRequest.class))).thenReturn("JsonPermissionRequestBody");
        when(mockConnection.getGson()).thenReturn(mockGson);

        final Permission actual = driveItemUnderTest.createSharingLink(mock(CreateSharingLinkRequest.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(PermissionParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/createLink",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()),
                () -> assertEquals(JSON_MEDIA_TYPE, requestCaptor.getValue().body().contentType()));
    }

    @Test
    public void createSharingLink_withInvalidParameters_shouldThrowException() {
        final CreateSharingLinkRequest mockRequest = mock(CreateSharingLinkRequest.class);
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.createSharingLink(null)),
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.createSharingLink(mockRequest));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class,
                            () -> driveItemUnderTest.createSharingLink(mockRequest));
                });
    }

    @Test
    public void getThumbnails_shouldReturnThumbnailSetList() {
        final List<ThumbnailSet> expected = List.of(mock(ThumbnailSet.class), mock(ThumbnailSet.class));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final List<ThumbnailSet> actual = driveItemUnderTest.getThumbnails();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ThumbnailSetListParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/thumbnails",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getThumbnails_withInvalidDriveItemId_shouldThrowException() {
        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.getThumbnails());
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.getThumbnails());
                });
    }

    @Test
    public void search_shouldReturnDriveItemList() {
        setUpPaginatedDriveItemResponseBehavior();

        final List<DriveItem> actual = driveItemUnderTest.search("SearchQuery");

        validatePaginatedDriveItemResponseBehavior(actual,
                "http://localhost/me/drive/items/DriveItemId/search(q='SearchQuery')");
    }

    @Test
    public void search_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> driveItemUnderTest.search(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.search(StringUtils.EMPTY)),
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> driveItemUnderTest.search("SearchQuery"));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> driveItemUnderTest.search("SearchQuery"));
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
                        .execute(requestCaptor.capture(), isA(DriveItemPageParser.class)),
                () -> assertEquals(apiUrl, requestCaptor.getAllValues().get(0).url().toString()),
                () -> assertEquals("GET", requestCaptor.getAllValues().get(0).method()),
                () -> assertEquals(NEXT_LINK_URL, requestCaptor.getAllValues().get(1).url().toString()),
                () -> assertEquals("GET", requestCaptor.getAllValues().get(1).method()));
    }
}
