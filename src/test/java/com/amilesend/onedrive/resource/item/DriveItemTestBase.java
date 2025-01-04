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

import com.amilesend.onedrive.connection.OneDriveConnection;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public abstract class DriveItemTestBase {
    protected static final String BASE_URL = "http://localhost/me";
    protected static final String DRIVE_ITEM_ID = "DriveItemId";
    protected static final String DRIVE_ITEM_NAME = "DriveItemName";
    protected static final long DRIVE_ITEM_SIZE = 1024L;

    @Mock
    protected OneDriveConnection mockConnection;
    protected DriveItem driveItemUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        final Request.Builder requestBuilder = new Request.Builder();
        lenient().when(mockConnection.newSignedForRequestBuilder()).thenReturn(requestBuilder);
        lenient().when(mockConnection.newSignedForApiRequestBuilder()).thenReturn(requestBuilder);
        lenient().when(mockConnection.newSignedForApiWithBodyRequestBuilder()).thenReturn(requestBuilder);

        driveItemUnderTest = spy(DriveItem.builder()
                .connection(mockConnection)
                .id(DRIVE_ITEM_ID)
                .name(DRIVE_ITEM_NAME)
                .size(DRIVE_ITEM_SIZE)
                .build());
    }
}
