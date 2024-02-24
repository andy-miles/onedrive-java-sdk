/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2024 Andy Miles (andy.miles@amilesend.com)
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
package com.amilesend.onedrive;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.RequestException;
import com.amilesend.onedrive.connection.ResponseException;
import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.Drive;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.validateAuthInfo;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDrive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OneDriveFunctionalTest extends FunctionalTestBase {
    /////////////////////
    // getUserDrive
    /////////////////////

    @Test
    public void getUserDrive_withValidRequest_shouldReturnDrive() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        final Drive expected = new Drive(newDrive(getOneDriveConnection(), 1));

        final Drive actual = getOneDriveUnderTest().getUserDrive();

        assertEquals(expected, actual);
    }

    @Test
    public void getUserDrive_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> getOneDriveUnderTest().getUserDrive());
    }

    @Test
    public void getUserDrive_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> getOneDriveUnderTest().getUserDrive());
    }

    ////////////////////////
    // getAvailableDrives
    ////////////////////////

    @Test
    public void getAvailableDrives_withValidRequest_shouldReturnDrives() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<Drive> expected = List.of(
                new Drive(newDrive(oneDriveConnection, 1)),
                new Drive(newDrive(oneDriveConnection, 2)));

        final List<Drive> actual = getOneDriveUnderTest().getAvailableDrives();

        assertEquals(expected, actual);
    }

    @Test
    public void getAvailableDrives_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> getOneDriveUnderTest().getAvailableDrives());
    }

    @Test
    public void getAvailableDrives_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> getOneDriveUnderTest().getAvailableDrives());
    }

    ////////////////////////
    // getDrive
    ////////////////////////

    @Test
    public void getDrive_withValidDriveId_shouldReturnDrive() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        final Drive expected = new Drive(newDrive(getOneDriveConnection(), 1));

        final Drive actual = getOneDriveUnderTest().getDrive(expected.getId());

        assertEquals(expected, actual);
    }

    @Test
    public void getDrive_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> getOneDriveUnderTest().getDrive("SomeDriveId"));
    }

    @Test
    public void getDrive_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> getOneDriveUnderTest().getDrive("SomeDriveId"));
    }

    ////////////////////////
    // getUserDisplayName
    ////////////////////////

    @Test
    public void getUserDisplayName_withValidRequest_shouldReturnDisplayName() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        assertEquals("IdentityName", getOneDriveUnderTest().getUserDisplayName());
    }

    //////////////////
    // getAuthInfo
    //////////////////

    @Test
    public void getAuthInfo_shouldReturnAuthInfo() {
        final AuthInfo actual = getOneDriveUnderTest().getAuthInfo();
        validateAuthInfo(actual);
    }
}
