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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.RequestException;
import com.amilesend.onedrive.connection.ResponseException;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemFolder;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemZipFile;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newRootDriveItemFolder;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newSpecialDriveItem;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newItemActivity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DriveFunctionalTest extends FunctionalTestBase {
    private Drive driveUnderTest;

    @BeforeEach
    public void configureDrive() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        driveUnderTest = getOneDriveUnderTest().getUserDrive();
    }

    ///////////////////
    // getActivities
    ///////////////////

    @Test
    public void getActivities_withValidRequest_shouldReturnItemActivityList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.ITEM_ACTIVITY_LIST);
        final List<ItemActivity> expected = List.of(newItemActivity(1), newItemActivity(2));

        final List<ItemActivity> actual = driveUnderTest.getActivities();

        assertEquals(expected, actual);
    }

    @Test
    public void getActivities_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> driveUnderTest.getActivities());
    }

    @Test
    public void getActivities_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> driveUnderTest.getActivities());
    }

    ///////////////////
    // getRootFolder
    ///////////////////

    @Test
    public void getRootFolder_withValidRequest_shouldReturnDriveFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ROOT_FOLDER);
        final DriveFolder expected = new DriveFolder(newRootDriveItemFolder(getOneDriveConnection()));

        final DriveFolder actual = driveUnderTest.getRootFolder();

        assertEquals(expected, actual);
    }

    @Test
    public void getRootFolder_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> driveUnderTest.getRootFolder());
    }

    @Test
    public void getRootFolder_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> driveUnderTest.getRootFolder());
    }

    ///////////////////
    // getChanges
    ///////////////////

    @Test
    public void getChanges_withValidRequest_shouldReturnListOfDriveItemTypes() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SINGLE_DRIVE_ITEM_PAGE);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<? extends DriveItemType> expected = List.of(
                new DriveFile(newDriveItemZipFile(oneDriveConnection, 1)),
                new DriveFolder(newDriveItemFolder(oneDriveConnection)));

        final List<? extends DriveItemType> actual = driveUnderTest.getChanges();

        assertEquals(expected, actual);
    }

    @Test
    public void getChanges_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> driveUnderTest.getChanges());
    }

    @Test
    public void getChanges_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> driveUnderTest.getChanges());
    }

    ///////////////////
    // search
    ///////////////////

    @Test
    public void search_withValidRequest_shouldReturnListOfDriveItemTypes() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SINGLE_DRIVE_ITEM_PAGE);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<? extends DriveItemType> expected = List.of(
                new DriveFile(newDriveItemZipFile(oneDriveConnection, 1)),
                new DriveFolder(newDriveItemFolder(oneDriveConnection)));

        final List<? extends DriveItemType> actual = driveUnderTest.search("SearchQuery");

        assertEquals(expected, actual);
    }

    @Test
    public void search_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> driveUnderTest.search("SearchQuery"));
    }

    @Test
    public void search_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> driveUnderTest.search("SearchQuery"));
    }

    //////////////////////
    // getSpecialFolder
    //////////////////////

    @Test
    public void getSpecialFolder_withValidRequest_shouldReturnDriveFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SPECIAL_DRIVE_ITEM);
        final DriveFolder expected = new DriveFolder(newSpecialDriveItem(getOneDriveConnection()));

        final DriveFolder actual = driveUnderTest.getSpecialFolder(SpecialFolder.Type.MUSIC);

        assertEquals(expected, actual);
    }

    @Test
    public void getSpecialFolder_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> driveUnderTest.getSpecialFolder(SpecialFolder.Type.MUSIC));
    }

    @Test
    public void getSpecialFolder_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> driveUnderTest.getSpecialFolder(SpecialFolder.Type.MUSIC));
    }
}
