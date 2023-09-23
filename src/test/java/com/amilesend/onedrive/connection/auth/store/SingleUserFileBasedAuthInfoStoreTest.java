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
package com.amilesend.onedrive.connection.auth.store;

import com.amilesend.onedrive.connection.auth.AuthInfo;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SingleUserFileBasedAuthInfoStoreTest {
    private static final String ID = "DoesNotMatter";

    @Mock
    private Path mockStateFilePath;
    @InjectMocks
    private SingleUserFileBasedAuthInfoStore storeUnderTest;

    ////////////////
    // store
    ////////////////

    @SneakyThrows
    @Test
    public void store_withAuthInfo_shouldWrite() {
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        when(mockAuthInfo.toJson()).thenReturn("StateContents");

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            storeUnderTest.store(ID, mockAuthInfo);
            filesMockedStatic.verify(() -> Files.write(eq(mockStateFilePath), isA(byte[].class)));
        }
    }

    @Test
    public void store_withNullAuthInfo_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> storeUnderTest.store(ID, null));
    }

    @Test
    public void store_withIOException_shouldThrowException() {
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        when(mockAuthInfo.toJson()).thenReturn("StateContents");

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.write(any(Path.class), any(byte[].class)))
                    .thenThrow(new IOException("Exception"));
            final Throwable thrown =
                    assertThrows(AuthInfoStoreException.class, () -> storeUnderTest.store(ID, mockAuthInfo));
            assertInstanceOf(IOException.class, thrown.getCause());
        }
    }

    ////////////////
    // retrieve
    ////////////////

    @SneakyThrows
    @Test
    public void retrieve_withValidState_shouldReturnAuthInfo() {
        final AuthInfo expected = mock(AuthInfo.class);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn("JsonState");
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);

            assertEquals(expected, storeUnderTest.retrieve(ID));
        }
    }

    @SneakyThrows
    @Test
    public void retrieve_withNonExistentStateFile_shouldReturnNull() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertNull(storeUnderTest.retrieve(ID));
        }
    }

    @SneakyThrows
    @Test
    public void retrieve_withNonReadableFile_shouldReturnNull() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(false);

            assertNull(storeUnderTest.retrieve(ID));
        }
    }

    @SneakyThrows
    @Test
    public void retrieve_withBlankJsonState_shouldReturnNull() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn(null);

            assertNull(storeUnderTest.retrieve(ID));
        }
    }

    @Test
    public void retrieve_withIOException_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenThrow(new IOException("Exception"));

            final Throwable thrown =
                    assertThrows(AuthInfoStoreException.class, () ->  assertNull(storeUnderTest.retrieve(ID)));
            assertInstanceOf(IOException.class, thrown.getCause());
        }
    }
}
