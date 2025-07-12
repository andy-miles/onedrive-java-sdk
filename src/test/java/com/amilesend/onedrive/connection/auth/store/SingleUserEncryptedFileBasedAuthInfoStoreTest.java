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
package com.amilesend.onedrive.connection.auth.store;

import com.amilesend.client.crypto.CryptoHelper;
import com.amilesend.client.crypto.CryptoHelperException;
import com.amilesend.client.crypto.EncryptedEnvelope;
import com.amilesend.onedrive.connection.auth.OneDriveAuthInfo;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SingleUserEncryptedFileBasedAuthInfoStoreTest {
    private static final String ID = "DoesNotMatter";

    @Mock
    private Path mockStateFilePath;
    @Mock
    private CryptoHelper mockCryptoHelper;
    @InjectMocks
    private SingleUserEncryptedFileBasedAuthInfoStore storeUnderTest;

    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new SingleUserEncryptedFileBasedAuthInfoStore(null, mockCryptoHelper)),
                () -> assertThrows(NullPointerException.class,
                        () -> new SingleUserEncryptedFileBasedAuthInfoStore(mockStateFilePath, null)));
    }

    ////////////////
    // store
    ////////////////

    @SneakyThrows
    @Test
    public void store_withValidAuthInfo_shouldEncryptAndStore() {
        final OneDriveAuthInfo mockAuthInfo = mock(OneDriveAuthInfo.class);
        when(mockAuthInfo.toJson()).thenReturn("StateContents");
        final EncryptedEnvelope mockEnvelope = mock(EncryptedEnvelope.class);
        when(mockCryptoHelper.encrypt(any(byte[].class), anyString())).thenReturn(mockEnvelope);
        final Gson mockGson = mock(Gson.class);
        when(mockGson.toJson(any(EncryptedEnvelope.class))).thenReturn("EncryptedStateContents");

        try (final MockedStatic<GsonFactory> gsonFactoryMockedStatic = mockStatic(GsonFactory.class);
             final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            gsonFactoryMockedStatic.when(() -> GsonFactory.getInstanceForAuthManager()).thenReturn(mockGson);

            storeUnderTest.store(ID, mockAuthInfo);

            assertAll(
                    () -> verify(mockCryptoHelper).encrypt(isA(byte[].class), isA(String.class)),
                    () -> filesMockedStatic.verify(() -> Files.write(eq(mockStateFilePath), isA(byte[].class))));
        }
    }

    @SneakyThrows
    @Test
    public void store_withCryptoHelperException_shouldThrowException() {
        final OneDriveAuthInfo mockAuthInfo = mock(OneDriveAuthInfo.class);
        when(mockAuthInfo.toJson()).thenReturn("StateContents");
        final EncryptedEnvelope mockEnvelope = mock(EncryptedEnvelope.class);
        when(mockCryptoHelper.encrypt(any(byte[].class), anyString()))
                .thenThrow(new CryptoHelperException("Exception"));

        final Throwable thrown =
                assertThrows(AuthInfoStoreException.class, () -> storeUnderTest.store(ID, mockAuthInfo));
        assertInstanceOf(CryptoHelperException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void store_withIOException_shouldThrowException() {
        final OneDriveAuthInfo mockAuthInfo = mock(OneDriveAuthInfo.class);
        when(mockAuthInfo.toJson()).thenReturn("StateContents");
        final EncryptedEnvelope mockEnvelope = mock(EncryptedEnvelope.class);
        when(mockCryptoHelper.encrypt(any(byte[].class), anyString())).thenReturn(mockEnvelope);
        final Gson mockGson = mock(Gson.class);
        when(mockGson.toJson(any(EncryptedEnvelope.class))).thenReturn("EncryptedStateContents");

        try (final MockedStatic<GsonFactory> gsonFactoryMockedStatic = mockStatic(GsonFactory.class);
             final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            gsonFactoryMockedStatic.when(() -> GsonFactory.getInstanceForAuthManager()).thenReturn(mockGson);
            filesMockedStatic.when(() -> Files.write(any(Path.class), any(byte[].class)))
                            .thenThrow(new IOException("Exception)"));

            final Throwable thrown =
                    assertThrows(AuthInfoStoreException.class, () -> storeUnderTest.store(ID, mockAuthInfo));
            assertInstanceOf(IOException.class, thrown.getCause());
        }
    }

    @Test
    public void store_withNullAuthInfo_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> storeUnderTest.store(ID, null));
    }

    ////////////////
    // retrieve
    ////////////////

    @SneakyThrows
    @Test
    public void retrieve_withValidStateFile_shouldReturnAuthInfo() {
        final OneDriveAuthInfo expected = mock(OneDriveAuthInfo.class);
        final EncryptedEnvelope mockEnvelope = mock(EncryptedEnvelope.class);
        when(mockCryptoHelper.decrypt(any(EncryptedEnvelope.class)))
                .thenReturn("DecryptedStateContents".getBytes(StandardCharsets.UTF_8));
        final Gson mockGson = mock(Gson.class);
        when(mockGson.fromJson(anyString(), eq(EncryptedEnvelope.class))).thenReturn(mockEnvelope);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class);
             final MockedStatic<GsonFactory> gsonFactoryMockedStatic = mockStatic(GsonFactory.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn("EncryptedJsonState");
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            gsonFactoryMockedStatic.when(() -> GsonFactory.getInstanceForAuthManager()).thenReturn(mockGson);

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

    @SneakyThrows
    @Test
    public void retrieve_withCryptoHelperException_shouldThrowException() {
        final EncryptedEnvelope mockEnvelope = mock(EncryptedEnvelope.class);
        when(mockCryptoHelper.decrypt(any(EncryptedEnvelope.class)))
                .thenThrow(new CryptoHelperException("Exception"));
        final Gson mockGson = mock(Gson.class);
        when(mockGson.fromJson(anyString(), eq(EncryptedEnvelope.class))).thenReturn(mockEnvelope);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<GsonFactory> gsonFactoryMockedStatic = mockStatic(GsonFactory.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn("EncryptedJsonState");
            gsonFactoryMockedStatic.when(() -> GsonFactory.getInstanceForAuthManager()).thenReturn(mockGson);

            final Throwable thrown = assertThrows(AuthInfoStoreException.class, () -> storeUnderTest.retrieve(ID));
            assertInstanceOf(CryptoHelperException.class, thrown.getCause());
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
