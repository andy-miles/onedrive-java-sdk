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
package com.amilesend.onedrive.resource.item.type;

import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.RequestException;
import com.amilesend.onedrive.connection.ResponseException;
import com.amilesend.onedrive.data.SerializedResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PermissionFunctionalTest extends FunctionalTestBase {
    private Permission permissionUnderTest;

    @BeforeEach
    public void configurePermission() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ROOT_FOLDER);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION_LIST);
        permissionUnderTest = getOneDriveUnderTest()
                .getUserDrive()
                .getRootFolder()
                .getPermissions()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to retrieve Permission to test"));
    }

    @Test
    public void deletePermission_withValidRequest_shouldNotThrowException() {
        setUpMockResponse(SUCCESS_STATUS_CODE);
        permissionUnderTest.deletePermission();
    }

    @Test
    public void deletePermission_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> permissionUnderTest.deletePermission());
    }

    @Test
    public void deletePermission_withServiceErrorResposne_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> permissionUnderTest.deletePermission());
    }
}
