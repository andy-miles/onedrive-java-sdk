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

import com.amilesend.client.connection.file.ProgressReportingRequestBody;
import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.parse.parser.BasicParser;
import com.amilesend.client.parse.parser.GsonParser;
import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.Request;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.amilesend.client.connection.Connection.Headers.CONTENT_TYPE;
import static com.amilesend.onedrive.resource.DriveFileTest.newMockFilePath;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DriveItemUploadTest extends DriveItemTestBase {
    private static final String FILENAME = "SomeFile.zip";
    private static final String FILE_CONTENT_TYPE = "application/zip";

    @SneakyThrows
    @Test
    public void upload_withValidFileAndCallback_shouldReturnDriveItem() {
        final ProgressReportingRequestBody mockRequestBody = newMockRequestBody();
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(mockRequestBody);
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            final DriveItem actual = driveItemUnderTest.upload(newMockFilePath(), mock(TransferProgressCallback.class));

            final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            assertAll(
                    () -> assertEquals(expected, actual),
                    () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                    () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                            requestCaptor.getValue().url().toString()),
                    () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                    () -> assertEquals("PUT", requestCaptor.getValue().method()),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, requestCaptor.getValue().body()));
        }
    }

    @SneakyThrows
    @Test
    public void upload_withIOException_shouldThrowException() {
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(new IOException("Exception"));

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            assertThrows(IOException.class,
                    () -> driveItemUnderTest.upload(newMockFilePath(), mock(TransferProgressCallback.class)));
        }
    }

    @SneakyThrows
    @Test
    public void upload_withInvalidParameters_shouldThrowException() {
        final Path mockFilePath = newMockFilePath();
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.upload((Path) null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.upload(mockFilePath, null)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withValidFileAndCallback_shouldReturnFuture() {
        final ProgressReportingRequestBody mockRequestBody = newMockRequestBody();
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(mockRequestBody);
        final DriveItem expected = mock(DriveItem.class);
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(expected);
        when(mockConnection.executeAsync(any(Request.class), any(GsonParser.class))).thenReturn(mockFuture);

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);
            final CompletableFuture<DriveItem> actual =
                    driveItemUnderTest.uploadAsync(newMockFilePath(), mock(TransferProgressCallback.class));

            final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            assertAll(
                    () -> assertEquals(expected, actual.get()),
                    () -> verify(mockConnection).executeAsync(requestCaptor.capture(), isA(BasicParser.class)),
                    () -> assertEquals("http://localhost/me/drive/items/DriveItemId/content",
                            requestCaptor.getValue().url().toString()),
                    () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                    () -> assertEquals("PUT", requestCaptor.getValue().method()),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, requestCaptor.getValue().body()));
        }
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withIOException_shouldThrowException() {
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(new IOException("Exception"));

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            assertThrows(IOException.class,
                    () -> driveItemUnderTest.uploadAsync(newMockFilePath(), mock(TransferProgressCallback.class)));
        }
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withInvalidParameters_shouldThrowException() {
        final Path mockFilePath = newMockFilePath();
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadAsync((Path) null, mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadAsync(mockFilePath, null)));
    }

    @SneakyThrows
    @Test
    public void uploadNew_withValidFileAndCallback_shouldReturnDriveItem() {
        final ProgressReportingRequestBody mockRequestBody = newMockRequestBody();
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(mockRequestBody);
        final DriveItem expected = mock(DriveItem.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            final DriveItem actual =
                    driveItemUnderTest.uploadNew(newMockFilePath(), mock(TransferProgressCallback.class));

            final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            assertAll(
                    () -> assertEquals(expected, actual),
                    () -> verify(mockConnection).execute(requestCaptor.capture(), isA(BasicParser.class)),
                    () -> assertEquals("http://localhost/me/drive/items/DriveItemId:/"
                                    + FILENAME + ":/content",
                            requestCaptor.getValue().url().toString()),
                    () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                    () -> assertEquals("PUT", requestCaptor.getValue().method()),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, requestCaptor.getValue().body()));
        }
    }

    @SneakyThrows
    @Test
    public void uploadNew_withIOException_shouldThrowException() {
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(new IOException("Exception"));

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            assertThrows(IOException.class,
                    () -> driveItemUnderTest.uploadNew(newMockFilePath(), mock(TransferProgressCallback.class)));
        }
    }

    @SneakyThrows
    @Test
    public void uploadNew_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNew((Path) null, mock(TransferProgressCallback.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNew(mock(Path.class), null)));
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withValidFileAndCallback_shouldReturnDriveItem() {
        final ProgressReportingRequestBody mockRequestBody = newMockRequestBody();
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(mockRequestBody);
        final DriveItem expected = mock(DriveItem.class);
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(expected);
        when(mockConnection.executeAsync(any(Request.class), any(GsonParser.class))).thenReturn(mockFuture);

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            final CompletableFuture<DriveItem> actual =
                    driveItemUnderTest.uploadNewAsync(newMockFilePath(), mock(TransferProgressCallback.class));

            final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
            assertAll(
                    () -> assertEquals(expected, actual.get()),
                    () -> verify(mockConnection).executeAsync(requestCaptor.capture(), isA(BasicParser.class)),
                    () -> assertEquals("http://localhost/me/drive/items/DriveItemId:/" + FILENAME + ":/content",
                            requestCaptor.getValue().url().toString()),
                    () -> assertEquals(FILE_CONTENT_TYPE, requestCaptor.getValue().header(CONTENT_TYPE)),
                    () -> assertEquals("PUT", requestCaptor.getValue().method()),
                    () -> assertInstanceOf(ProgressReportingRequestBody.class, requestCaptor.getValue().body()));
        }
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withIOException_shouldThrowException() {
        final ProgressReportingRequestBody.Builder mockBuilder =
                newRequestBodyBuilderMock(new IOException("Exception"));

        try (final MockedStatic<ProgressReportingRequestBody> bodyMockedStatic =
                     mockStatic(ProgressReportingRequestBody.class)) {
            bodyMockedStatic.when(() -> ProgressReportingRequestBody.builder()).thenReturn(mockBuilder);

            assertThrows(IOException.class,
                    () -> driveItemUnderTest.uploadNewAsync(newMockFilePath(), mock(TransferProgressCallback.class)));
        }
    }

    @SneakyThrows
    @Test
    public void uploadNewAsync_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNewAsync((Path) null, mock(TransferProgressCallback.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> driveItemUnderTest.uploadNewAsync(mock(Path.class), null)));
    }

    private ProgressReportingRequestBody newMockRequestBody() {
        final ProgressReportingRequestBody mockRequestBody = mock(ProgressReportingRequestBody.class);
        when(mockRequestBody.contentType()).thenReturn(MediaType.parse(FILE_CONTENT_TYPE));
        return mockRequestBody;
    }

    @SneakyThrows
    private ProgressReportingRequestBody.Builder newRequestBodyBuilderMock(
            final ProgressReportingRequestBody mockBody) {
        final ProgressReportingRequestBody.Builder mockBuilder = mock(ProgressReportingRequestBody.Builder.class);
        when(mockBuilder.file(any(Path.class))).thenReturn(mockBuilder);
        when(mockBuilder.callback(any(TransferProgressCallback.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockBody);

        return mockBuilder;
    }

    @SneakyThrows
    private ProgressReportingRequestBody.Builder newRequestBodyBuilderMock(final IOException ex) {
        final ProgressReportingRequestBody.Builder mockBuilder = mock(ProgressReportingRequestBody.Builder.class);
        when(mockBuilder.file(any(Path.class))).thenReturn(mockBuilder);
        when(mockBuilder.callback(any(TransferProgressCallback.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenThrow(ex);

        return mockBuilder;
    }
}
