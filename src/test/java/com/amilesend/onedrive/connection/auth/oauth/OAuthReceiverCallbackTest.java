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
package com.amilesend.onedrive.connection.auth.oauth;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.URI;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OAuthReceiverCallbackTest {
    private OAuthReceiverCallback callbackUnderTest = OAuthReceiverCallback.builder()
            .callbackPath("/Callback")
            .build();

    @SneakyThrows
    @Test
    public void handle_withCodeReturned_shouldSetCode() {
        final HttpExchange mockHttpExchange = newMockHttpExchangeFor("code=CodeValue&error=", "/Callback");
        callbackUnderTest.handle(mockHttpExchange);
        assertAll(
                () -> assertEquals("CodeValue", callbackUnderTest.waitForCode()),
                () -> assertTrue(StringUtils.isBlank(callbackUnderTest.getError())));
    }

    @SneakyThrows
    @Test
    public void handle_withErrorReturned_shouldSetError() {
        final HttpExchange mockHttpExchange = newMockHttpExchangeFor("code&error=ErrorValue", "/Callback");
        callbackUnderTest.handle(mockHttpExchange);
        assertAll(
                () -> assertEquals("ErrorValue", callbackUnderTest.getError()),
                () -> assertTrue(StringUtils.isBlank(callbackUnderTest.getCode())));
    }

    @SneakyThrows
    @Test
    public void handle_withNoCodesReturned_shouldSetBothErrorAndCodeAsEmpty() {
        final HttpExchange mockHttpExchange = newMockHttpExchangeFor(null, "/Callback");
        callbackUnderTest.handle(mockHttpExchange);
        assertAll(
                () -> assertTrue(StringUtils.isBlank(callbackUnderTest.getError())),
                () -> assertTrue(StringUtils.isBlank(callbackUnderTest.getCode())));
    }

    @SneakyThrows
    @Test
    public void handle_withNoErrorAndSuccessLandingPageUrl_shouldSetResponseHeaders() {
        callbackUnderTest = OAuthReceiverCallback.builder()
                .callbackPath("/Callback")
                .successLandingPageUrl("SuccessUrl")
                .build();
        final Headers mockHeaders = mock(Headers.class);
        final HttpExchange mockHttpExchange =
                newMockHttpExchangeFor("code=CodeValue&error=", "/Callback", mockHeaders);

        callbackUnderTest.handle(mockHttpExchange);

        assertAll(
                () -> verify(mockHeaders).add(eq("Location"), eq("SuccessUrl")),
                () -> verify(mockHttpExchange).sendResponseHeaders(eq(HTTP_MOVED_TEMP), eq(-1L)));
    }

    @SneakyThrows
    @Test
    public void handle_withErrorAndFailureLandingPageUrl_shouldSetResponseHeaders() {
        callbackUnderTest = OAuthReceiverCallback.builder()
                .callbackPath("/Callback")
                .failureLandingPageUrl("FailureUrl")
                .build();
        final Headers mockHeaders = mock(Headers.class);
        final HttpExchange mockHttpExchange =
                newMockHttpExchangeFor("code&error=ErrorValue", "/Callback", mockHeaders);

        callbackUnderTest.handle(mockHttpExchange);

        assertAll(
                () -> verify(mockHeaders).add(eq("Location"), eq("FailureUrl")),
                () -> verify(mockHttpExchange).sendResponseHeaders(eq(HTTP_MOVED_TEMP), eq(-1L)));
    }

    @SneakyThrows
    @Test
    public void waitForCode_withError_shouldThrowException() {
        final HttpExchange mockHttpExchange = newMockHttpExchangeFor("code&error=ErrorValue", "/Callback");
        callbackUnderTest.handle(mockHttpExchange);
        assertThrows(OAuthReceiverException.class, () -> callbackUnderTest.waitForCode());
    }

    @SneakyThrows
    private HttpExchange newMockHttpExchangeFor(final String query, final String path) {
        return newMockHttpExchangeFor(query, path, mock(Headers.class));
    }

    @SneakyThrows
    private HttpExchange newMockHttpExchangeFor(
            final String query,
            final String path,
            final Headers mockResponseHeaders) {
        final URI mockUri = mock(URI.class);
        when(mockUri.getQuery()).thenReturn(query);
        when(mockUri.getPath()).thenReturn(path);
        final OutputStream mockResponseBody = mock(OutputStream.class);
        final HttpExchange mockHttpExchange = mock(HttpExchange.class);
        when(mockHttpExchange.getRequestURI()).thenReturn(mockUri);
        when(mockHttpExchange.getResponseHeaders()).thenReturn(mockResponseHeaders);
        lenient().when(mockHttpExchange.getResponseBody()).thenReturn(mockResponseBody);

        return mockHttpExchange;
    }
}
