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

import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.RequestException;
import com.amilesend.onedrive.connection.ResponseException;
import com.amilesend.onedrive.connection.parse.resource.parser.SerializedResource;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.amilesend.onedrive.connection.parse.resource.parser.TypeTestDataHelper.newAsyncJobStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AsyncJobFunctionalTest extends FunctionalTestBase {
    private AsyncJob jobUnderTest;

    @BeforeEach
    public void configureAsyncJob() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ROOT_FOLDER);
        setUpMockResponse(SUCCESS_ASYNC_JOB_CODE, getMockWebServerUrl());
        jobUnderTest = getOneDriveUnderTest()
                .getUserDrive()
                .getRootFolder()
                .copy("DestinationId", "NewName");
    }

    @Test
    public void getStatus_withValidRequest_shouldReturnAsyncJobStatus() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.ASYNC_JOB_STATUS);
        final AsyncJobStatus expected = newAsyncJobStatus();

        final AsyncJobStatus actual = jobUnderTest.getStatus();

        assertEquals(expected, actual);
    }

    @Test
    public void getStatus_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> jobUnderTest.getStatus());
    }

    @Test
    public void getStatus_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> jobUnderTest.getStatus());
    }
}
