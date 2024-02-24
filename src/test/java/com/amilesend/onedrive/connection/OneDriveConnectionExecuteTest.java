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
package com.amilesend.onedrive.connection;

import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OneDriveConnectionExecuteTest extends OneDriveConnectionTestBase {

    ////////////////////////////////////
    // Execute (with response body)
    ////////////////////////////////////

    @Test
    public void execute_withValidRequestAndResponse_shouldReturnParsedObject() {
        final DriveItem mockDriveItem = mock(DriveItem.class);
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenReturn(mockDriveItem);
        setUpHttpClientMock(newMockedResponse(SUCCESS_RESPONSE_CODE));

        try (final MockedConstruction<GZIPInputStream> streamCons = mockConstruction(GZIPInputStream.class)) {
            final DriveItem actual = connectionUnderTest.execute(mock(Request.class), mockParser);

            assertAll(
                    () -> assertEquals(mockDriveItem, actual),
                    () -> verify(mockParser).parse(isA(Gson.class), isA(InputStream.class)));
        }
    }

    @Test
    public void execute_withThrottledResponse_shouldThrowException() {
        final long expected = 60L;
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, expected));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));

        assertEquals(expected, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void execute_withThrottledResponseAndNullRetryAfterHeader_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, (Long) null));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));

        assertNull(((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void execute_withParseException_shouldThrowException() {
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenThrow(new JsonParseException("Exception"));
        setUpHttpClientMock(newMockedResponse(SUCCESS_RESPONSE_CODE));

        try (final MockedConstruction<GZIPInputStream> streamCons = mockConstruction(GZIPInputStream.class)) {
            final Throwable thrown = assertThrows(ResponseParseException.class,
                    () -> connectionUnderTest.execute(mock(Request.class), mockParser));
            assertInstanceOf(JsonParseException.class, thrown.getCause());
        }
    }

    @Test
    public void execute_withServerErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(SERVER_ERROR_RESPONSE_CODE));

        assertThrows(ResponseException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));
    }

    @Test
    public void execute_withRequestErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(REQUEST_ERROR_CODE));

        assertThrows(RequestException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));
    }

    @Test
    public void execute_withIOException_shouldThrowException() {
        setUpHttpClientMock(new IOException("Exception"));

        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.execute(mock(Request.class), mock(GsonParser.class)));

        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    public void execute_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.execute(null, mock(GsonParser.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.execute(mock(Request.class), null)));
    }

    ////////////////////////////////////
    // Execute (with no response body)
    ////////////////////////////////////

    @Test
    public void executeNoResponse_withValidRequest_shouldReturnResponseCode() {
        setUpHttpClientMock(newMockedResponse(SUCCESS_RESPONSE_CODE));
        assertEquals(SUCCESS_RESPONSE_CODE, connectionUnderTest.execute(mock(Request.class)));
    }

    @Test
    public void executeNoResponse_withThrottledResponse_shouldThrowException() {
        final long expected = 60L;
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, expected));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.execute(mock(Request.class)));

        assertEquals(expected, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void executeNoResponse_withThrottledResponseAndNullRetryAfterHeader_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, (Long) null));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.execute(mock(Request.class)));

        assertNull(((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void executeNoResponse_withServerErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(SERVER_ERROR_RESPONSE_CODE));
        assertThrows(ResponseException.class, () -> connectionUnderTest.execute(mock(Request.class)));
    }

    @Test
    public void executeNoResponse_withRequestErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(REQUEST_ERROR_CODE));
        assertThrows(RequestException.class, () -> connectionUnderTest.execute(mock(Request.class)));
    }

    @Test
    public void executeNoResponse_withIOException_shouldThrowException() {
        setUpHttpClientMock(new IOException("Exception"));
        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.execute(mock(Request.class)));
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    public void executeNoResponse_withNullRequest_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> connectionUnderTest.execute(null));
    }

    ////////////////////////////////////
    // executeRemoteAsync
    ////////////////////////////////////

    @Test
    public void executeRemoteAsync_withValidRequest_shouldReturnLocationUrl() {
        setUpHttpClientMock(newMockedResponse(REMOTE_SUCCESS_RESPONSE_CODE, "LocationUrlValue"));

        final String actual = connectionUnderTest.executeRemoteAsync(mock(Request.class));

        assertEquals("LocationUrlValue", actual);
    }

    @Test
    public void executeRemoteAsync_withUnrecognizedResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(SUCCESS_RESPONSE_CODE, "LocationUrlValue"));
        assertThrows(ResponseException.class, () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));
    }

    @Test
    public void executeRemoteAsync_withThrottledResponse_shouldThrowException() {
        final long expected = 60L;
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, expected));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));

        assertEquals(expected, ((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void executeRemoteAsync_withThrottledResponseAndNullRetryAfterHeader_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(THROTTLED_ERROR_CODE, (Long) null));

        final Throwable thrown = assertThrows(ThrottledException.class,
                () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));

        assertNull(((ThrottledException) thrown).getRetryAfterSeconds());
    }

    @Test
    public void executeRemoteAsync_withServerErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(SERVER_ERROR_RESPONSE_CODE, "LocationUrlValue"));
        assertThrows(ResponseException.class, () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));
    }

    @Test
    public void executeRemoteAsync_withRequestErrorResponseCode_shouldThrowException() {
        setUpHttpClientMock(newMockedResponse(REQUEST_ERROR_CODE, "LocationUrlValue"));
        assertThrows(RequestException.class, () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));
    }

    @Test
    public void executeRemoteAsync_withIOException_shouldThrowException() {
        setUpHttpClientMock(new IOException("Exception"));

        final Throwable thrown = assertThrows(RequestException.class,
                () -> connectionUnderTest.executeRemoteAsync(mock(Request.class)));

        assertInstanceOf(IOException.class, thrown.getCause());
    }

    ////////////////////////////////////
    // executeAsync
    ////////////////////////////////////

    @SneakyThrows
    @Test
    public void executeAsync_withValidRequest_shouldReturnFuture() {
        final Response mockResponse = newMockedResponse(SUCCESS_RESPONSE_CODE);
        final Call mockCall = setUpHttpClientMockAsync();
        final DriveItem mockDriveItem = mock(DriveItem.class);
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);
        when(mockParser.parse(any(Gson.class), any(InputStream.class))).thenReturn(mockDriveItem);

        // Obtain future and get the callback reference
        final CompletableFuture<DriveItem> future =
                connectionUnderTest.executeAsync(mock(Request.class), mockParser);
        final Callback callback = getCallbackFromCallMock(mockCall);

        try (final MockedConstruction<GZIPInputStream> streamCons = mockConstruction(GZIPInputStream.class)) {
            // Simulate callback onResponse invocation
            callback.onResponse(mockCall, mockResponse);

            final DriveItem actual = future.get();

            assertAll(
                    () -> assertEquals(mockDriveItem, actual),
                    () -> verify(mockParser).parse(isA(Gson.class), isA(InputStream.class)));
        }
    }

    @SneakyThrows
    @Test
    public void executeAsync_withServerErrorResponseCode_shouldThrowException() {
        executeAsync_withErrorCode_shouldThrowException(SERVER_ERROR_RESPONSE_CODE, ResponseException.class);
    }

    @SneakyThrows
    @Test
    public void executeAsync_withRequestErrorResponseCode_shouldThrowException() {
        executeAsync_withErrorCode_shouldThrowException(REQUEST_ERROR_CODE, RequestException.class);
    }

    @SneakyThrows
    @Test
    public void executeAsync_withThrottledResponse_shouldThrowException() {
        final Long expected = 1000L;
        final Response mockResponse = newMockedResponse(THROTTLED_ERROR_CODE, expected);
        final Call mockCall = setUpHttpClientMockAsync();
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);

        // Obtain future and get the callback reference
        final CompletableFuture<DriveItem> future = connectionUnderTest.executeAsync(mock(Request.class), mockParser);
        // Simulate callback onResponse invocation
        getCallbackFromCallMock(mockCall).onResponse(mockCall, mockResponse);

        final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
        assertAll(
                () -> assertInstanceOf(ThrottledException.class, thrown.getCause()),
                () -> assertEquals(expected, ((ThrottledException) thrown.getCause()).getRetryAfterSeconds()));
    }

    @SneakyThrows
    @Test
    public void executeAsync_withThrottledResponseAndNullRetryAfterHeader_shouldThrowException() {
        final Response mockResponse = newMockedResponse(THROTTLED_ERROR_CODE, (Long) null);
        final Call mockCall = setUpHttpClientMockAsync();
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);

        // Obtain future and get the callback reference
        final CompletableFuture<DriveItem> future = connectionUnderTest.executeAsync(mock(Request.class), mockParser);
        // Simulate callback onResponse invocation
        getCallbackFromCallMock(mockCall).onResponse(mockCall, mockResponse);

        final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
        assertAll(
                () -> assertInstanceOf(ThrottledException.class, thrown.getCause()),
                () -> assertNull(((ThrottledException) thrown.getCause()).getRetryAfterSeconds()));
    }

    @SneakyThrows
    private <T extends Throwable> void executeAsync_withErrorCode_shouldThrowException(
            final int code, final Class<T> expectedExceptionType) {
        final Response mockResponse = newMockedResponse(code);
        final Call mockCall = setUpHttpClientMockAsync();
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);

        // Obtain future and get the callback reference
        final CompletableFuture<DriveItem> future = connectionUnderTest.executeAsync(mock(Request.class), mockParser);
        // Simulate callback onResponse invocation
        getCallbackFromCallMock(mockCall).onResponse(mockCall, mockResponse);

        try (final MockedConstruction<GZIPInputStream> streamCons = mockConstruction(GZIPInputStream.class)) {
            final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
            assertInstanceOf(expectedExceptionType, thrown.getCause());
        }
    }

    @Test
    public void executeAsync_withFailure_shouldThrowException() {
        final Call mockCall = setUpHttpClientMockAsync();
        final GsonParser<DriveItem> mockParser = mock(GsonParser.class);

        // Obtain future and get the callback reference
        final CompletableFuture<DriveItem> future =
                connectionUnderTest.executeAsync(mock(Request.class), mockParser);
        final Callback callback = getCallbackFromCallMock(mockCall);

        callback.onFailure(mockCall, new IOException("Exception"));

        final Throwable thrown = assertThrows(ExecutionException.class, () -> future.get());
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    public void executeAsync_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.executeAsync(null, mock(GsonParser.class))),
                () -> assertThrows(NullPointerException.class,
                        () -> connectionUnderTest.executeAsync(mock(Request.class), null)));
    }

    @SneakyThrows
    private void setUpHttpClientMock(final IOException ioException) {
        final Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenThrow(ioException);
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
    }
}
