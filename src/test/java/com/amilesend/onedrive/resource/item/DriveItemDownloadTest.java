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

import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DriveItemDownloadTest extends DriveItemTestBase {
    @Test
    public void download_withValidPathAndCallback_shouldInvokeConnection() {
        when(mockConnection.download(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(DRIVE_ITEM_SIZE);
        final Path mockFolderPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        driveItemUnderTest.download(mockFolderPath, mockCallback);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> verify(mockConnection).download(
                        requestCaptor.capture(),
                        eq(mockFolderPath),
                        eq(DRIVE_ITEM_NAME),
                        eq(DRIVE_ITEM_SIZE),
                        eq(mockCallback)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void download_withInvalidParameters_shouldThrowException() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.download(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.download(mockPath, null)));
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withValidPathAndCallback_shouldReturnFuture() {
        final CompletableFuture<Long> future = mock(CompletableFuture.class);
        when(future.get()).thenReturn(DRIVE_ITEM_SIZE);
        when(mockConnection.downloadAsync(
                any(Request.class),
                any(Path.class),
                anyString(),
                anyLong(),
                any(TransferProgressCallback.class)))
                .thenReturn(future);
        final Path mockFolderPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final CompletableFuture<Long> actual = driveItemUnderTest.downloadAsync(mockFolderPath, mockCallback);

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(DRIVE_ITEM_SIZE, actual.get()),
                () -> verify(mockConnection).downloadAsync(
                        requestCaptor.capture(),
                        eq(mockFolderPath),
                        eq(DRIVE_ITEM_NAME),
                        eq(DRIVE_ITEM_SIZE),
                        eq(mockCallback)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void downloadAsync_withInvalidParameters_shouldThrowException() {
        final Path mockPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.downloadAsync(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.downloadAsync(mockPath, null)));
    }
}
