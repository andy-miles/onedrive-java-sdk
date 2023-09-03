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
package com.amilesend.onedrive.resource.item.type;

import com.amilesend.onedrive.connection.OneDriveConnection;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionTest {
    private static final String BASE_URL = "http://localhost";
    private static final String DRIVE_ITEM_ID = "DriveItemId";
    private static final String PERMISSION_ID = "PermissionId";
    private static final int SUCCESS_RESPONSE_CODE = 200;

    @Mock
    private OneDriveConnection mockConnection;
    @InjectMocks
    private Permission permissionUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newSignedForRequestBuilder()).thenReturn(new Request.Builder());
        permissionUnderTest.setId(PERMISSION_ID);
        permissionUnderTest.setDriveItemId(DRIVE_ITEM_ID);
    }

    @Test
    public void deletePermissions_shouldInvokeApi() {
        when(mockConnection.execute(any(Request.class))).thenReturn(SUCCESS_RESPONSE_CODE);

        permissionUnderTest.deletePermission();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals(BASE_URL + "/me/drive/items/DriveItemId/permissions/PermissionId",
                        requestCaptor.getValue().url().toString()));
    }

    @Test
    public void deletePermissions_withInvalidIds_shouldThrowException() {
        assertAll(
                () -> {
                    permissionUnderTest.setDriveItemId(null);
                    permissionUnderTest.setId(PERMISSION_ID);
                    assertThrows(NullPointerException.class, () -> permissionUnderTest.deletePermission());
                },
                () -> {
                    permissionUnderTest.setDriveItemId(StringUtils.EMPTY);
                    permissionUnderTest.setId(PERMISSION_ID);
                    assertThrows(IllegalArgumentException.class, () -> permissionUnderTest.deletePermission());
                },
                () -> {
                    permissionUnderTest.setDriveItemId(DRIVE_ITEM_ID);
                    permissionUnderTest.setId(null);
                    assertThrows(NullPointerException.class, () -> permissionUnderTest.deletePermission());
                },
                () -> {
                    permissionUnderTest.setDriveItemId(DRIVE_ITEM_ID);
                    permissionUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class, () -> permissionUnderTest.deletePermission());
                }
        );
    }
}
