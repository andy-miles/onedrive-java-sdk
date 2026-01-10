/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
package com.amilesend.onedrive.connection.auth.oauth;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OAuthReceiverTest {
    private static final int PORT = 9000;

    @Mock
    private HttpServer mockServer;
    private OAuthReceiver receiverUnderTest;

    @BeforeEach
    public void setUp() {
        receiverUnderTest = OAuthReceiver.defaultOAuthReceiverBuilder()
                .port(PORT)
                .callbackPath("/Callback")
                .build();
    }

    @SneakyThrows
    @Test
    public void builder_withNoPortDefined_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> receiverUnderTest = OAuthReceiver.defaultOAuthReceiverBuilder().build());
    }

    /////////////////////
    // start()
    /////////////////////

    @SneakyThrows
    @Test
    public void start_withValidConfig_shouldStartServer() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);

            receiverUnderTest.start();

            verify(mockServer).start();
        }
    }

    @SneakyThrows
    @Test
    public void start_withMultipleInvocations_shouldOnlyStartServiceOnce() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);

            receiverUnderTest.start();
            receiverUnderTest.start();

            verify(mockServer).start();
        }
    }

    @SneakyThrows
    @Test
    public void start_withIOException_shouldThrowException() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);
            doThrow(new IllegalStateException("Exception")).when(mockServer).start();

            assertThrows(OAuthReceiverException.class, () -> receiverUnderTest.start());
        }
    }

    /////////////////////
    // stop()
    /////////////////////

    @SneakyThrows
    @Test
    public void stop_withStartedServer_shouldStopServer() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);
            receiverUnderTest.start();

            receiverUnderTest.stop();

            verify(mockServer).stop(anyInt());
        }
    }

    @SneakyThrows
    @Test
    public void stop_withNoServerStarted_shouldNotStopServer() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);

            receiverUnderTest.stop();

            verify(mockServer, never()).stop(anyInt());
        }
    }

    @SneakyThrows
    @Test
    public void stop_withExceptionThrown_shouldThrowException() {
        try (final MockedStatic<HttpServer> mockServerStatic = mockStatic(HttpServer.class)) {
            mockServerStatic.when(() -> HttpServer.create(any(InetSocketAddress.class), anyInt()))
                    .thenReturn(mockServer);
            doThrow(new IllegalStateException("Exception")).when(mockServer).stop(anyInt());
            receiverUnderTest.start();

            assertThrows(OAuthReceiverException.class, () -> receiverUnderTest.stop());
        }
    }

    /////////////////////
    // close()
    /////////////////////

    @SneakyThrows
    @Test
    public void close_withNoException_shouldInvokeStop() {
        receiverUnderTest = spy(OAuthReceiver.defaultOAuthReceiverBuilder()
                .port(PORT)
                .build());
        doNothing().when(receiverUnderTest).stop();

        receiverUnderTest.close();

        verify(receiverUnderTest).stop();
    }

    @SneakyThrows
    @Test
    public void close_whenExceptionThrown_shouldThrowException() {
        receiverUnderTest = spy(OAuthReceiver.defaultOAuthReceiverBuilder()
                .port(PORT)
                .build());
        doThrow(new OAuthReceiverException("Exception")).when(receiverUnderTest).stop();

        assertThrows(OAuthReceiverException.class, () -> receiverUnderTest.close());
    }

    /////////////////////
    // getRedirectUri()
    /////////////////////

    @SneakyThrows
    @Test
    public void getRedirectUri_shouldReturnUri() {
        final String actual = receiverUnderTest.getRedirectUri();
        assertEquals("http://localhost:9000/Callback", actual);
    }

    /////////////////////
    // waitForCode()
    /////////////////////

    @SneakyThrows
    @Test
    public void waitForCode_withCodeSet_shouldReturnCode() {
        final Future<String> future = Executors.newSingleThreadExecutor().submit(() -> receiverUnderTest.waitForCode());
        assertFalse(future.isDone());
        receiverUnderTest.getCallback().setCode("AuthCode");
        receiverUnderTest.getCallback().releaseLock();
        final String actual = future.get();


        assertEquals("AuthCode", actual);
    }

    @SneakyThrows
    @Test
    public void waitForCode_withErrorCodeSet_shouldThrowException() {
        final Future<String> future = Executors.newSingleThreadExecutor().submit(() -> receiverUnderTest.waitForCode());
        assertFalse(future.isDone());
        receiverUnderTest.getCallback().setError("Error");
        receiverUnderTest.getCallback().releaseLock();
        final Exception actual = assertThrows(ExecutionException.class, () -> future.get());
        assertAll(
                () -> assertNotNull(actual),
                () -> assertInstanceOf(OAuthReceiverException.class, actual.getCause()));
    }
}
