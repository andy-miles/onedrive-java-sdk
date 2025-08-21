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
package com.amilesend.onedrive.connection;

import com.amilesend.client.connection.ConnectionException;
import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.file.TransferFileWriter;
import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.util.StringUtils;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OneDriveConnectionDownloadTest extends OneDriveConnectionTestBase {

    ////////////////////////////////////
    // download
    ////////////////////////////////////

    @SneakyThrows
    @Test
    public void download_withValidRequestAndPath_shouldProcessDownloadResponse() {
        doReturn(BYTES_TRANSFERRED)
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        setUpHttpClientMock(mockResponse);

        final long bytesDownloaded = connectionUnderTest.download(
                mock(Request.class),
                mock(Path.class),
                "filename",
                BYTES_TRANSFERRED,
                mock(TransferProgressCallback.class));

        assertAll(
                () -> assertEquals(BYTES_TRANSFERRED, bytesDownloaded),
                () -> verify(connectionUnderTest).processDownloadResponse(
                        eq(mockResponse),
                        eq(mockDownloadPath),
                        eq(BYTES_TRANSFERRED),
                        isA(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void download_withIOExceptionGettingDownloadPath_shouldThrowException() {
        doThrow(new IOException("Exception"))
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.download(
                        mock(Request.class),
                        mock(Path.class),
                        "filename",
                        BYTES_TRANSFERRED,
                        mockCallback));

        assertAll(
                () -> assertInstanceOf(IOException.class, thrown.getCause()),
                () -> verify(mockCallback).onFailure(eq(thrown.getCause())));
    }

    @SneakyThrows
    @Test
    public void download_withIOExceptionDuringProcess_shouldThrowException() {
        doThrow(new IOException("Exception"))
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        setUpHttpClientMock(mockResponse);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.download(
                        mock(Request.class),
                        mock(Path.class),
                        "filename",
                        BYTES_TRANSFERRED,
                        mockCallback));

        assertAll(
                () -> assertInstanceOf(IOException.class, thrown.getCause()),
                () -> verify(mockCallback, never()).onFailure(any(Throwable.class)));
    }

    @SneakyThrows
    @Test
    public void download_withOneDriveConnectionException_shouldThrowException() {
        doThrow(new ConnectionException("Exception"))
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        setUpHttpClientMock(mockResponse);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        assertThrows(ConnectionException.class, () -> connectionUnderTest.download(
                mock(Request.class),
                mock(Path.class),
                "filename",
                BYTES_TRANSFERRED,
                mockCallback));

        verify(mockCallback).onFailure(any(Throwable.class));
    }

    @SneakyThrows
    @Test
    public void download_withInvalidParameters_shouldThrowException() {
        final Request mockRequest = mock(Request.class);
        final Path mockPath = mock(Path.class);
        final long sizeBytes = BYTES_TRANSFERRED;
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.download(
                                null, // Null Request
                                mockPath,
                                "FileName",
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.download(
                                mockRequest,
                                null, // Null folderPath
                                "FileName",
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.download(
                                mockRequest,
                                mockPath,
                                null, // Null name
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> connectionUnderTest.download(
                                mockRequest,
                                mockPath,
                                StringUtils.EMPTY, // Empty name
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.download(
                                mockRequest,
                                mockPath,
                                "FileName",
                                sizeBytes,
                                null))); // Null callback
    }

    ////////////////////////////////////
    // downloadAsync
    ////////////////////////////////////

    @SneakyThrows
    @Test
    public void downloadAsync_withValidRequestAndPath_shouldProcessDownloadResponse() {
        doReturn(BYTES_TRANSFERRED)
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Call mockCall = setUpHttpClientMockAsync();
        // Obtain future and get the callback reference
        final CompletableFuture<Long> future = connectionUnderTest.downloadAsync(
                mock(Request.class),
                mock(Path.class),
                "Filename",
                BYTES_TRANSFERRED,
                mock(TransferProgressCallback.class));
        final Callback callback = getCallbackFromCallMock(mockCall);
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);

        callback.onResponse(mockCall, mockResponse);

        assertAll(
                () -> assertEquals(BYTES_TRANSFERRED, future.get()),
                () -> verify(connectionUnderTest).processDownloadResponse(
                        eq(mockResponse),
                        eq(mockDownloadPath),
                        eq(BYTES_TRANSFERRED),
                        isA(TransferProgressCallback.class)));
    }

    @Test
    public void downloadSync_withFailure_shouldThrowException() {
        final Call mockCall = setUpHttpClientMockAsync();
        final TransferProgressCallback mockTransferProgressCallback = mock(TransferProgressCallback.class);
        // Obtain future and get the callback reference
        final CompletableFuture<Long> future = connectionUnderTest.downloadAsync(
                mock(Request.class),
                mock(Path.class),
                "Filename",
                BYTES_TRANSFERRED,
                mockTransferProgressCallback);
        final Callback callback = getCallbackFromCallMock(mockCall);

        callback.onFailure(mockCall, new IOException("Exception"));
        verify(mockTransferProgressCallback).onFailure(isA(IOException.class));

        final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withIOExceptionGettingDownloadPath_shouldThrowException() {
        doThrow(new IOException("Exception"))
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Call mockCall = setUpHttpClientMockAsync();
        final TransferProgressCallback mockTransferProgressCallback = mock(TransferProgressCallback.class);
        // Obtain future and get the callback reference
        final CompletableFuture<Long> future = connectionUnderTest.downloadAsync(
                mock(Request.class),
                mock(Path.class),
                "Filename",
                BYTES_TRANSFERRED,
                mockTransferProgressCallback);
        final Callback callback = getCallbackFromCallMock(mockCall);

        final Throwable thrownFromCallback =
                assertThrows(RequestException.class, () -> callback.onResponse(mockCall, mock(Response.class)));

        assertAll(
                () -> assertInstanceOf(IOException.class, thrownFromCallback.getCause()),
                () -> verify(mockTransferProgressCallback).onFailure(eq(thrownFromCallback.getCause())));

        final Throwable thrownFromFuture = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IOException.class, thrownFromFuture.getCause());
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withOneDriveConnectionException_shouldThrowException() {
        doThrow(new ConnectionException("Exception"))
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Call mockCall = setUpHttpClientMockAsync();
        final TransferProgressCallback mockTransferProgressCallback = mock(TransferProgressCallback.class);
        // Obtain future and get the callback reference
        final CompletableFuture<Long> future = connectionUnderTest.downloadAsync(
                mock(Request.class),
                mock(Path.class),
                "Filename",
                BYTES_TRANSFERRED,
                mockTransferProgressCallback);
        final Callback callback = getCallbackFromCallMock(mockCall);

        assertThrows(ConnectionException.class, () -> callback.onResponse(mockCall, mock(Response.class)));

        verify(mockTransferProgressCallback).onFailure(isA(ConnectionException.class));

        final Throwable thrownFromFuture = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(ConnectionException.class, thrownFromFuture.getCause());
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withIOExceptionDuringProcess_shouldThrowException() {
        doThrow(new IOException("Exception"))
                .when(connectionUnderTest)
                .processDownloadResponse(
                        any(Response.class),
                        any(Path.class),
                        anyLong(),
                        any(TransferProgressCallback.class));
        final Path mockDownloadPath = mock(Path.class);
        doReturn(mockDownloadPath)
                .when(connectionUnderTest)
                .checkFolderAndGetDestinationPath(any(Path.class), anyString());
        final Call mockCall = setUpHttpClientMockAsync();
        final TransferProgressCallback mockTransferProgressCallback = mock(TransferProgressCallback.class);
        // Obtain future and get the callback reference
        final CompletableFuture<Long> future = connectionUnderTest.downloadAsync(
                mock(Request.class),
                mock(Path.class),
                "Filename",
                BYTES_TRANSFERRED,
                mockTransferProgressCallback);
        final Callback callback = getCallbackFromCallMock(mockCall);

        assertThrows(IOException.class, () -> callback.onResponse(mockCall, mock(Response.class)));
        verify(mockTransferProgressCallback, never()).onFailure(any(Throwable.class));

        final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withInvalidParameters_shouldThrowException() {
        final Request mockRequest = mock(Request.class);
        final Path mockPath = mock(Path.class);
        final long sizeBytes = BYTES_TRANSFERRED;
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.downloadAsync(
                                null, // Null Request
                                mockPath,
                                "FileName",
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.downloadAsync(
                                mockRequest,
                                null, // Null folderPath
                                "FileName",
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.downloadAsync(
                                mockRequest,
                                mockPath,
                                null, // Null name
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> connectionUnderTest.downloadAsync(
                                mockRequest,
                                mockPath,
                                StringUtils.EMPTY, // Empty name
                                sizeBytes,
                                mockCallback)),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.downloadAsync(
                                mockRequest,
                                mockPath,
                                "FileName",
                                sizeBytes,
                                null))); // Null callback
    }

    ////////////////////////////////////
    // processDownloadResponse
    ////////////////////////////////////

    @SneakyThrows
    @Test
    public void processDownloadResponse_withSuccessfulResponse_shouldWriteFile() {
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        final TransferFileWriter mockWriter = mock(TransferFileWriter.class);
        when(mockWriter.write(any(BufferedSource.class), anyLong())).thenReturn(BYTES_TRANSFERRED);
        final TransferFileWriter.TransferFileWriterBuilder mockBuilder = setUpTransferFileWriterBuilderMock(mockWriter);

        try (final MockedStatic<TransferFileWriter> writerMockedStatic = mockStatic(TransferFileWriter.class)) {
            writerMockedStatic.when(() -> TransferFileWriter.builder()).thenReturn(mockBuilder);

            final long actual = connectionUnderTest.processDownloadResponse(
                    mockResponse,
                    mock(Path.class),
                    BYTES_TRANSFERRED,
                    mock(TransferProgressCallback.class));

            assertAll(
                    () -> assertEquals(BYTES_TRANSFERRED, actual),
                    () -> verify(mockWriter).write(isA(BufferedSource.class), eq(BYTES_TRANSFERRED)));
        }
    }

    @SneakyThrows
    @Test
    public void processDownloadResponse_withIOException_shouldThrowException() {
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        final TransferFileWriter mockWriter = mock(TransferFileWriter.class);
        when(mockWriter.write(any(BufferedSource.class), anyLong())).thenThrow(new IOException("Exception"));
        final TransferFileWriter.TransferFileWriterBuilder mockBuilder = setUpTransferFileWriterBuilderMock(mockWriter);

        try (final MockedStatic<TransferFileWriter> writerMockedStatic = mockStatic(TransferFileWriter.class)) {
            writerMockedStatic.when(() -> TransferFileWriter.builder()).thenReturn(mockBuilder);

            assertThrows(IOException.class, () -> connectionUnderTest.processDownloadResponse(
                    mockResponse,
                    mock(Path.class),
                    BYTES_TRANSFERRED,
                    mock(TransferProgressCallback.class)));
        }
    }

    @SneakyThrows
    @Test
    public void processDownloadResponse_withServerErrorResponse_shouldThrowException() {
        final Response mockResponse = newMockedResponse(SERVER_ERROR_RESPONSE_CODE);
        assertThrows(ResponseException.class, () -> connectionUnderTest.processDownloadResponse(
                mockResponse,
                mock(Path.class),
                BYTES_TRANSFERRED,
                mock(TransferProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void processDownloadResponse_withRequestErrorResponse_shouldThrowException() {
        final Response mockResponse = newMockedResponse(REQUEST_ERROR_CODE);
        assertThrows(RequestException.class, () -> connectionUnderTest.processDownloadResponse(
                mockResponse,
                mock(Path.class),
                BYTES_TRANSFERRED,
                mock(TransferProgressCallback.class)));
    }

    ////////////////////////////////////
    // checkFolderAndGetDestinationPath
    ////////////////////////////////////

    @SneakyThrows
    @Test
    public void checkFolderAndGetDestinationPath_withValidPathAndName_shouldReturnPath() {
        final Path mockFolderPath = newFolderPathMock();
        final Path createdFoldersPath = mock(Path.class);
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            filesMockedStatic.when(() -> Files.isDirectory(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.createDirectories(any(Path.class))).thenReturn(createdFoldersPath);

            final Path actual = connectionUnderTest.checkFolderAndGetDestinationPath(mockFolderPath, "name");

            assertEquals(mockFolderPath, actual);
        }
    }

    @SneakyThrows
    @Test
    public void checkFolderAndGetDestinationPath_withIOException_shouldThrowException() {
        final Path mockFolderPath = newFolderPathMock();
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            filesMockedStatic.when(() -> Files.isDirectory(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.createDirectories(any(Path.class)))
                    .thenThrow(new IOException("Exception"));

            assertThrows(IOException.class,
                    () -> connectionUnderTest.checkFolderAndGetDestinationPath(mockFolderPath, "name"));
        }
    }

    @SneakyThrows
    @Test
    public void checkFolderAndGetDestinationPath_withInvalidFolderPath_shouldThrowException() {
        final Path mockFolderPath = newFolderPathMock();
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isDirectory(any(Path.class))).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> connectionUnderTest.checkFolderAndGetDestinationPath(mockFolderPath, "name"));
        }
    }

    private Path newFolderPathMock() {
        final Path mockPath = mock(Path.class);
        when(mockPath.toAbsolutePath()).thenReturn(mockPath);
        when(mockPath.normalize()).thenReturn(mockPath);
        lenient().when(mockPath.resolve(anyString())).thenReturn(mockPath);
        return mockPath;
    }

    private TransferFileWriter.TransferFileWriterBuilder setUpTransferFileWriterBuilderMock(
            final TransferFileWriter mockWriter) {
        final TransferFileWriter.TransferFileWriterBuilder mockBuilder =
                mock(TransferFileWriter.TransferFileWriterBuilder.class);
        when(mockBuilder.output(any(Path.class))).thenReturn(mockBuilder);
        when(mockBuilder.callback(any(TransferProgressCallback.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockWriter);

        return mockBuilder;
    }
}
