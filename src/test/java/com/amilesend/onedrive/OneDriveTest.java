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
package com.amilesend.onedrive;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.connection.auth.AuthManager;
import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.DriveListParser;
import com.amilesend.onedrive.parse.resource.parser.DriveParser;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.identity.Identity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OneDriveTest {
    private static final String BASE_URL = "http://localhost";

    @Mock
    private OneDriveConnection mockConnection;
    @InjectMocks
    @Spy
    private OneDrive oneDriveUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newSignedForApiRequestBuilder()).thenReturn(new Request.Builder());
    }

    @Test
    public void getUserDrive_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive driveToReturn = newDriveMock(1);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(driveToReturn);

        final Drive actual = oneDriveUnderTest.getUserDrive();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals("DriveId1", actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveParser.class)),
                () -> assertEquals("http://localhost/me/drive", requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getAvailableDrives_shouldReturnDriveList() {
        final List<com.amilesend.onedrive.resource.drive.Drive> drives =
                List.of(newDriveMock(1), newDriveMock(2));
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(drives);

        final List<Drive> actual = oneDriveUnderTest.getAvailableDrives();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(drives.size(), actual.size()),
                () -> assertEquals("DriveId1", actual.get(0).getId()),
                () -> assertEquals("DriveId2", actual.get(1).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveListParser.class)),
                () -> assertEquals("http://localhost/me/drives", requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getUserDisplayName_shouldReturnDisplayName() {
        final Identity mockUserIdentity = mock(Identity.class);
        when(mockUserIdentity.getDisplayName()).thenReturn("DisplayName");
        final IdentitySet mockOwner = mock(IdentitySet.class);
        when(mockOwner.getUser()).thenReturn(mockUserIdentity);
        final Drive mockDrive = mock(Drive.class);
        when(mockDrive.getOwner()).thenReturn(mockOwner);
        doReturn(mockDrive).when(oneDriveUnderTest).getUserDrive();

        assertEquals("DisplayName", oneDriveUnderTest.getUserDisplayName());
    }

    @Test
    public void getAuthInfo_shouldReturnAuthInfo() {
        final AuthInfo expected = mock(AuthInfo.class);
        final AuthManager mockAuthManager = mock(AuthManager.class);
        when(mockAuthManager.getAuthInfo()).thenReturn(expected);
        when(mockConnection.getAuthManager()).thenReturn(mockAuthManager);

        assertEquals(expected, oneDriveUnderTest.getAuthInfo());
    }

    private com.amilesend.onedrive.resource.drive.Drive newDriveMock(final int idSuffix) {
        final com.amilesend.onedrive.resource.drive.Drive mockDrive =
                mock(com.amilesend.onedrive.resource.drive.Drive.class);
        when(mockDrive.getId()).thenReturn("DriveId" + idSuffix);
        return mockDrive;
    }
}
