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

import com.amilesend.onedrive.connection.file.ProgressReportingFileRequestBody;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.DriveItemParser;
import lombok.SneakyThrows;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DriveItemUploadTest extends DriveItemTestBase {
    private static final String FILENAME = "SomeFile.zip";
    private static final String FILE_CONTENT_TYPE = "application/zip";

    @SneakyThrows
    @Test
    public void upload_withValidFileAndCallback_shouldReturnDriveItem() {
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        final File mockFile = setUpFileMock();

        final DriveItem actual = driveItemUnderTest.upload(mockFile, mock(TransferProgressCallback.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                () -> assertEquals("PUT", requestCaptor.getValue().method()),
                () -> assertInstanceOf(ProgressReportingFileRequestBody.class, requestCaptor.getValue().body()));
    }

    @SneakyThrows
    @Test
    public void upload_withIOException_shouldThrowException() {
        final File mockFile = setUpFileMockToThrowIOException();
        assertThrows(IOException.class,
                () -> driveItemUnderTest.upload(mockFile, mock(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void upload_withInvalidParameters_shouldThrowException() {
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class,
                            () -> driveItemUnderTest.upload(mockFile, mockCallback));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class,
                            () -> driveItemUnderTest.upload(mockFile, mockCallback));
                },
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.upload(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.upload(mockFile, null)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withValidFileAndCallback_shouldReturnFuture() {
        final DriveItem expected = mock(DriveItem.class);
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(expected);
        when(mockConnection.executeAsync(any(Request.class), any(GsonParser.class))).thenReturn(mockFuture);
        final File mockFile = setUpFileMock();

        final CompletableFuture<DriveItem> actual =
                driveItemUnderTest.uploadAsync(mockFile, mock(TransferProgressCallback.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual.get()),
                () -> verify(mockConnection).executeAsync(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                () -> assertEquals("PUT", requestCaptor.getValue().method()),
                () -> assertInstanceOf(ProgressReportingFileRequestBody.class, requestCaptor.getValue().body()));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withIOException_shouldThrowException() {
        final File mockFile = setUpFileMockToThrowIOException();
        assertThrows(IOException.class,
                () -> driveItemUnderTest.uploadAsync(mockFile, mock(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withInvalidParameters_shouldThrowException() {
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class,
                            () -> driveItemUnderTest.uploadAsync(mockFile, mockCallback));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class,
                            () -> driveItemUnderTest.uploadAsync(mockFile, mockCallback));
                },
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadAsync(null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadAsync(mockFile, null)));
    }

    @SneakyThrows
    @Test
    public void uploadNew_withValidFileAndCallback_shouldReturnDriveItem() {
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);
        final File mockFile = setUpFileMock();

        final DriveItem actual = driveItemUnderTest.uploadNew(mockFile, mock(TransferProgressCallback.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId:/"
                                + FILENAME + ":/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                () -> assertEquals("PUT", requestCaptor.getValue().method()),
                () -> assertInstanceOf(ProgressReportingFileRequestBody.class, requestCaptor.getValue().body()));
    }

    @SneakyThrows
    @Test
    public void uploadNew_withIOException_shouldThrowException() {
        final File mockFile = setUpFileMockToThrowIOException();
        assertThrows(IOException.class,
                () -> driveItemUnderTest.uploadNew(mockFile, mock(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void uploadNew_withInvalidParameters_shouldThrowException() {
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class,
                            () -> driveItemUnderTest.uploadNew(mockFile, mockCallback));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class,
                            () -> driveItemUnderTest.uploadNew(mockFile, mockCallback));
                },
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNew(null, mock(TransferProgressCallback.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNew(mock(File.class), null)));
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withValidFileAndCallback_shouldReturnDriveItem() {
        final DriveItem expected = mock(DriveItem.class);
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(expected);
        when(mockConnection.executeAsync(any(Request.class), any(GsonParser.class))).thenReturn(mockFuture);
        final File mockFile = setUpFileMock();

        final CompletableFuture<DriveItem> actual =
                driveItemUnderTest.uploadNewAsync(mockFile, mock(TransferProgressCallback.class));

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual.get()),
                () -> verify(mockConnection).executeAsync(requestCaptor.capture(), isA(DriveItemParser.class)),
                () -> assertEquals("http://localhost/me/drive/items/DriveItemId:/" + FILENAME + ":/content",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                () -> assertEquals("PUT", requestCaptor.getValue().method()),
                () -> assertInstanceOf(ProgressReportingFileRequestBody.class, requestCaptor.getValue().body()));
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withIOException_shouldThrowException() {
        final File mockFile = setUpFileMockToThrowIOException();
        assertThrows(IOException.class,
                () -> driveItemUnderTest.uploadNewAsync(mockFile, mock(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withInvalidParameters_shouldThrowException() {
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> {
                    driveItemUnderTest.setId(null);
                    assertThrows(NullPointerException.class,
                            () -> driveItemUnderTest.uploadNewAsync(mockFile, mockCallback));
                },
                () -> {
                    driveItemUnderTest.setId(StringUtils.EMPTY);
                    assertThrows(IllegalArgumentException.class,
                            () -> driveItemUnderTest.uploadNewAsync(mockFile, mockCallback));
                },
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNewAsync(null, mock(TransferProgressCallback.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNewAsync(mock(File.class), null)));
    }

    @SneakyThrows
    private File setUpFileMock() {
        final URLConnection mockConnection = mock(URLConnection.class);
        when(mockConnection.getInputStream()).thenReturn(mock(InputStream.class));
        when(mockConnection.getContentType()).thenReturn(FILE_CONTENT_TYPE);

        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenReturn(mockConnection);

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final File mockFile = mock(File.class);
        when(mockFile.toURI()).thenReturn(mockURI);
        lenient().when(mockFile.getName()).thenReturn(FILENAME);

        return mockFile;
    }

    @SneakyThrows
    private File setUpFileMockToThrowIOException() {
        final URL mockURL = mock(URL.class);
        when(mockURL.openConnection()).thenThrow(new IOException("Exception"));

        final URI mockURI = mock(URI.class);
        when(mockURI.toURL()).thenReturn(mockURL);

        final File mockFile = mock(File.class);
        when(mockFile.toURI()).thenReturn(mockURI);
        lenient().when(mockFile.getName()).thenReturn(FILENAME);

        return mockFile;
    }
}
