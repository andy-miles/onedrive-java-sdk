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
package com.amilesend.onedrive.resource.item;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.amilesend.onedrive.resource.DriveFileTest.newMockFolderPath;
import static com.amilesend.onedrive.resource.item.DriveItemVersion.NO_CONTENT_RESPONSE_HTTP_CODE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveItemVersionTest {
    private static final String BASE_URL = "http://localhost/me";
    private static final String VERSION_ID = "VersionId";
    private static final String DRIVE_ITEM_ID = "DriveItemId";
    private static final String NAME = "DriveItemVersionName";
    private static final long SIZE = 1024L;

    @Mock
    private OneDriveConnection mockConnection;
    private DriveItemVersion versionUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BASE_URL);
        lenient().when(mockConnection.newSignedForRequestBuilder()).thenReturn(new Request.Builder());
        versionUnderTest = DriveItemVersion.builder()
                .connection(mockConnection)
                .driveItemId(DRIVE_ITEM_ID)
                .id(VERSION_ID)
                .name(NAME)
                .size(SIZE)
                .build();
    }

    /////////////
    // download
    /////////////

    @Test
    public void download_withValidFolderPathAndCallback_shouldInvokeApi() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);
        when(mockConnection.download(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(SIZE);

        versionUnderTest.download(mockPath, mockCallback);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).download(
                        requestCaptor.capture(),
                        eq(mockPath),
                        eq(NAME),
                        eq(SIZE),
                        eq(mockCallback)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/versions/VersionId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void download_withFolderPathOnly_shouldInvokeApiWithDefaultCallback() {
        final Path mockPath = newMockFolderPath();
        when(mockConnection.download(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(SIZE);

        versionUnderTest.download(mockPath);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).download(
                        requestCaptor.capture(),
                        eq(mockPath),
                        eq(NAME),
                        eq(SIZE),
                        isA(LogProgressCallback.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/versions/VersionId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void download_withInvalidParameters_shouldThrowException() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> versionUnderTest.download(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> versionUnderTest.download(mockPath, null)));
    }

    //////////////////
    // downloadAsync
    //////////////////

    @Test
    public void downloadAsync_withValidFolderPathAndCallback_shouldInvokeApi() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);
        when(mockConnection.downloadAsync(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(CompletableFuture.completedFuture(SIZE));

        versionUnderTest.downloadAsync(mockPath, mockCallback);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).downloadAsync(
                        requestCaptor.capture(),
                        eq(mockPath),
                        eq(NAME),
                        eq(SIZE),
                        eq(mockCallback)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/versions/VersionId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void downloadAsync_withFolderPathOnly_shouldInvokeApiWithDefaultCallback() {
        final Path mockPath = newMockFolderPath();
        when(mockConnection.downloadAsync(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(CompletableFuture.completedFuture(SIZE));

        versionUnderTest.downloadAsync(mockPath);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).downloadAsync(
                        requestCaptor.capture(),
                        eq(mockPath),
                        eq(NAME),
                        eq(SIZE),
                        isA(LogProgressCallback.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/versions/VersionId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void downloadAsync_withInvalidParameters_shouldThrowException() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> versionUnderTest.downloadAsync(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> versionUnderTest.downloadAsync(mockPath, null)));
    }

    ////////////
    // restore
    ////////////

    @Test
    public void restore_withValidResponseCode_shouldReturnTrue() {
        when(mockConnection.execute(any(Request.class))).thenReturn(NO_CONTENT_RESPONSE_HTTP_CODE);

        final boolean actual = versionUnderTest.restore();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertTrue(actual),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId" +
                                "/versions/VersionId/restoreVersion",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()));
    }

    @Test
    public void restore_withInvalidResponseCode_shouldReturnFalse() {
        when(mockConnection.execute(any(Request.class))).thenReturn(400);

        final boolean actual = versionUnderTest.restore();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertFalse(actual),
                () -> verify(mockConnection).execute(requestCaptor.capture()),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId" +
                                "/versions/VersionId/restoreVersion",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("POST", requestCaptor.getValue().method()));
    }
}
