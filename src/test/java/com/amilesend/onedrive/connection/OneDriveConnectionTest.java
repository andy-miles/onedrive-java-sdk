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

import okhttp3.Request;
import org.junit.jupiter.api.Test;

import static com.amilesend.client.connection.Connection.Headers.ACCEPT;
import static com.amilesend.client.connection.Connection.Headers.ACCEPT_ENCODING;
import static com.amilesend.client.connection.Connection.Headers.AUTHORIZATION;
import static com.amilesend.client.connection.Connection.Headers.CONTENT_TYPE;
import static com.amilesend.client.connection.Connection.JSON_CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;


public class OneDriveConnectionTest extends OneDriveConnectionTestBase {
    @Test
    public void newRequestBuilder_shouldReturnBuilderWithHeadersDefined() {
        final Request actual = connectionUnderTest.newRequestBuilder().url(REQUEST_URL).build();

        assertAll(
                () -> assertEquals("FullAuthToken", actual.header(AUTHORIZATION)),
                () -> assertEquals("gzip", actual.header(ACCEPT_ENCODING)),
                () -> assertEquals(JSON_CONTENT_TYPE, actual.header(ACCEPT)),
                () -> verify(mockAuthManager).addAuthentication(isA(Request.Builder.class)));
    }

    @Test
    public void newWithBodyRequestBuilder_shouldReturnBuilderWithHeadersDefined() {
        final Request actual = connectionUnderTest.newWithBodyRequestBuilder()
                .url(REQUEST_URL)
                .build();

        assertAll(
                () -> assertEquals("FullAuthToken", actual.header(AUTHORIZATION)),
                () -> assertEquals("gzip", actual.header(ACCEPT_ENCODING)),
                () -> assertEquals(JSON_CONTENT_TYPE, actual.header(ACCEPT)),
                () -> assertEquals(JSON_CONTENT_TYPE, actual.header(CONTENT_TYPE)),
                () -> verify(mockAuthManager).addAuthentication(isA(Request.Builder.class)));
    }
}
