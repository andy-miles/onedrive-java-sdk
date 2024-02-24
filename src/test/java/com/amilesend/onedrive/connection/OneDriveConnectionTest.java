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

import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCEPT_ENCODING;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;


public class OneDriveConnectionTest extends OneDriveConnectionTestBase {
    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new OneDriveConnection(
                        null,
                        mockAuthManager,
                        mockGsonFactory,
                        StringUtils.EMPTY)),
                () -> assertThrows(NullPointerException.class, () -> new OneDriveConnection(
                        mockHttpClient,
                        null,
                        mockGsonFactory,
                        StringUtils.EMPTY)),
                () -> assertThrows(NullPointerException.class, () -> new OneDriveConnection(
                        mockHttpClient,
                        mockAuthManager,
                        null,
                        StringUtils.EMPTY)));
    }

    @Test
    public void ctor_withNoBaseUrl_shouldUseDefault() {
        final OneDriveConnection connection = new OneDriveConnection(
                mockHttpClient,
                mockAuthManager,
                mockGsonFactory,
                null);
        assertEquals("https://graph.microsoft.com/v1.0/me", connection.getBaseUrl());
    }

    @Test
    public void ctor_withBaseUrl_shouldUseConfiguredUrl() {
        final OneDriveConnection connection = new OneDriveConnection(
                mockHttpClient,
                mockAuthManager,
                mockGsonFactory,
                "http://localhost");
        assertEquals("http://localhost", connection.getBaseUrl());
    }

    @Test
    public void newSignedForRequestBuilder_shouldReturnBuilderWithHeaderDefined() {
        final Request actual = connectionUnderTest.newSignedForRequestBuilder().url(REQUEST_URL).build();

        assertAll(
                () -> assertEquals("FullAuthToken", actual.header(AUTHORIZATION)),
                () -> verify(mockAuthManager).refreshIfExpiredAndFetchFullToken());
    }

    @Test
    public void newSignedForApiRequestBuilder_shouldReturnBuilderWithHeadersDefined() {
        final Request actual = connectionUnderTest.newSignedForApiRequestBuilder().url(REQUEST_URL).build();

        assertAll(
                () -> assertEquals("FullAuthToken", actual.header(AUTHORIZATION)),
                () -> assertEquals("gzip", actual.header(ACCEPT_ENCODING)),
                () -> assertEquals(JSON_UTF_8.toString(), actual.header(ACCEPT)),
                () -> verify(mockAuthManager).refreshIfExpiredAndFetchFullToken());
    }

    @Test
    public void newSignedForApiWithBodyRequestBuilder_shouldReturnBuilderWithHeadersDefined() {
        final Request actual = connectionUnderTest.newSignedForApiWithBodyRequestBuilder()
                .url(REQUEST_URL)
                .build();

        assertAll(
                () -> assertEquals("FullAuthToken", actual.header(AUTHORIZATION)),
                () -> assertEquals("gzip", actual.header(ACCEPT_ENCODING)),
                () -> assertEquals(JSON_UTF_8.toString(), actual.header(ACCEPT)),
                () -> assertEquals(JSON_UTF_8.toString(), actual.header(CONTENT_TYPE)),
                () -> verify(mockAuthManager).refreshIfExpiredAndFetchFullToken());
    }
}
