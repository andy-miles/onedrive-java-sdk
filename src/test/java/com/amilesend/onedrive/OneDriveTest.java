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
package com.amilesend.onedrive;

import com.amilesend.client.connection.auth.AuthManager;
import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.auth.OneDriveAuthInfo;
import com.amilesend.onedrive.parse.resource.parser.ListResponseBodyParser;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.identity.Identity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import okhttp3.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OneDriveTest {
    static final String BASE_URL = "http://localhost/me";

    @Mock
    private OneDriveConnection mockConnection;
    @InjectMocks
    @Spy
    private OneDrive oneDriveUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newRequestBuilder()).thenReturn(new Request.Builder());
    }

    @Test
    public void getUserDrive_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive driveToReturn = newMockDrive();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(driveToReturn);

        final Drive actual = oneDriveUnderTest.getUserDrive();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals("DriveId1", actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/me/drive", requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getAvailableDrives_shouldReturnDriveList() {
        final List<com.amilesend.onedrive.resource.drive.Drive> drivesToReturn = newMockDrives();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(drivesToReturn);

        final List<Drive> actual = oneDriveUnderTest.getAvailableDrives();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(2, actual.size()),
                () -> assertEquals("DriveId1", actual.get(0).getId()),
                () -> assertEquals("DriveId2", actual.get(1).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(ListResponseBodyParser.class)),
                () -> assertEquals("http://localhost/me/drives", requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDrive_withValidDriveId_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive driveToReturn = newMockDrive();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(driveToReturn);

        final Drive actual = oneDriveUnderTest.getDrive(driveToReturn.getId());

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(driveToReturn.getId(), actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                () -> assertEquals("http://localhost/me/drives/DriveId1",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDrive_withInvalidInput_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getDrive(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> oneDriveUnderTest.getDrive(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrive(RandomStringUtils.secure().next(1000))));
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
        final OneDriveAuthInfo expected = mock(OneDriveAuthInfo.class);
        final AuthManager mockAuthManager = mock(AuthManager.class);
        when(mockAuthManager.getAuthInfo()).thenReturn(expected);
        when(mockConnection.getAuthManager()).thenReturn(mockAuthManager);

        assertEquals(expected, oneDriveUnderTest.getAuthInfo());
    }

    static com.amilesend.onedrive.resource.drive.Drive newMockDrive() {
        return newMockDrive(1);
    }

    static com.amilesend.onedrive.resource.drive.Drive newMockDrive(final int idSuffix) {
        final com.amilesend.onedrive.resource.drive.Drive mockDrive =
                mock(com.amilesend.onedrive.resource.drive.Drive.class);
        when(mockDrive.getId()).thenReturn("DriveId" + idSuffix);
        return mockDrive;
    }

    static List<com.amilesend.onedrive.resource.drive.Drive> newMockDrives() {
        return List.of(newMockDrive(1), newMockDrive(2));
    }
}
