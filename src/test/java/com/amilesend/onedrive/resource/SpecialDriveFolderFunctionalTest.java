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
package com.amilesend.onedrive.resource;

import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemFolder;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemZipFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SpecialDriveFolderFunctionalTest extends FunctionalTestBase {
    private DriveFolder folderUnderTest;

    @BeforeEach
    public void configureSpecialFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SPECIAL_DRIVE_ITEM);
        folderUnderTest = getOneDriveUnderTest().getUserDrive().getSpecialFolder(SpecialFolder.Type.MUSIC);
    }

    /////////////////////
    // getChildren
    /////////////////////

    @Test
    public void getChildren_withValidRequest_shouldReturnDriveItemTypeList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<? extends DriveItemType> expected = List.of(
                new DriveFile(newDriveItemZipFile(oneDriveConnection, 1)),
                new DriveFolder(newDriveItemFolder(oneDriveConnection)));

        final List<? extends DriveItemType> actual = folderUnderTest.getChildren();

        assertEquals(expected, actual);
    }

    @Test
    public void getChildren_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> folderUnderTest.getChildren());
    }

    @Test
    public void getChildren_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> folderUnderTest.getChildren());
    }
}
