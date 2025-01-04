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
package com.amilesend.onedrive.crypto;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PublicKey;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KeyStoreHelperTest {
    private static final char[] KEYSTORE_PASSWORD = "KeyStorePassword".toCharArray();
    private static final char[] KEY_PASSWORD = "KeyPassword".toCharArray();
    private static final String KEY_ALIAS = "KeyAlias";

    @Mock
    private Path mockKeyStorePath;

    private KeyStoreHelper helperUnderTest;

    @BeforeEach
    public void setUp() {
        helperUnderTest = spy(new KeyStoreHelper(mockKeyStorePath, KEYSTORE_PASSWORD));
    }

    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new KeyStoreHelper(null, KEYSTORE_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> new KeyStoreHelper(mockKeyStorePath, null)));
    }

    //////////////////////
    // saveSecretKey
    //////////////////////

    @SneakyThrows
    @Test
    public void saveSecretKey_withValidAttributes_shouldSaveKey() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        doReturn(mockKeyStore).when(helperUnderTest).loadKeyStore();
        doNothing().when(mockKeyStore).setEntry(
                anyString(),
                any(KeyStore.SecretKeyEntry.class),
                any(KeyStore.ProtectionParameter.class));
        doNothing().when(helperUnderTest).saveKeyStore(any(KeyStore.class));
        final SecretKey key = mock(SecretKey.class);

        helperUnderTest.saveSecretKey(KEY_ALIAS, key, KEY_PASSWORD);

        final ArgumentCaptor<KeyStore.SecretKeyEntry> keyEntryCaptor =
                ArgumentCaptor.forClass(KeyStore.SecretKeyEntry.class);
        final ArgumentCaptor<KeyStore.PasswordProtection> passwordCaptor =
                ArgumentCaptor.forClass(KeyStore.PasswordProtection.class);
        assertAll(
                () -> verify(helperUnderTest).saveKeyStore(eq(mockKeyStore)),
                () -> verify(mockKeyStore).setEntry(
                        eq(KEY_ALIAS),
                        keyEntryCaptor.capture(),
                        passwordCaptor.capture()),
                () -> assertEquals(key, keyEntryCaptor.getValue().getSecretKey()),
                () -> assertTrue(Arrays.equals(KEY_PASSWORD, passwordCaptor.getValue().getPassword())));
    }

    @SneakyThrows
    @Test
    public void saveSecretKey_withGeneralSecurityException_shouldThrowException() {
        doThrow(new KeyStoreException("Exception")).when(helperUnderTest).loadKeyStore();

        final Throwable thrown = assertThrows(KeyStoreHelperException.class,
                () -> helperUnderTest.saveSecretKey(KEY_ALIAS, mock(SecretKey.class), KEY_PASSWORD));
        assertInstanceOf(GeneralSecurityException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void saveSecretKey_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(helperUnderTest).loadKeyStore();

        final Throwable thrown = assertThrows(KeyStoreHelperException.class,
                () -> helperUnderTest.saveSecretKey(KEY_ALIAS, mock(SecretKey.class), KEY_PASSWORD));
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    public void saveSecretKey_withInvalidParameters_shouldThrowException() {
        final SecretKey key = mock(SecretKey.class);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> helperUnderTest.saveSecretKey(StringUtils.EMPTY, key, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> helperUnderTest.saveSecretKey(null, key, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> helperUnderTest.saveSecretKey(KEY_ALIAS, null, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> helperUnderTest.saveSecretKey(KEY_ALIAS, key, null)));
    }

    //////////////////////
    // getSecretKey
    //////////////////////

    @SneakyThrows
    @Test
    public void getSecretKey_withValidParameters_shouldReturnSecretKey() {
        final SecretKey expected = mock(SecretKey.class);
        final KeyStore mockKeyStore = mock(KeyStore.class);
        when(mockKeyStore.getKey(anyString(), any(char[].class))).thenReturn(expected);
        doReturn(mockKeyStore).when(helperUnderTest).loadKeyStore();

        final SecretKey actual = helperUnderTest.getSecretKey(KEY_ALIAS, KEY_PASSWORD);

        assertEquals(expected, actual);
    }

    @SneakyThrows
    @Test
    public void getSecretKey_withNonSecretKeyType_shouldThrowException() {
        final PublicKey mockKey = mock(PublicKey.class);
        final KeyStore mockKeyStore = mock(KeyStore.class);
        when(mockKeyStore.getKey(anyString(), any(char[].class))).thenReturn(mockKey);
        doReturn(mockKeyStore).when(helperUnderTest).loadKeyStore();

        assertThrows(KeyStoreHelperException.class, () -> helperUnderTest.getSecretKey(KEY_ALIAS, KEY_PASSWORD));
    }

    @SneakyThrows
    @Test
    public void getSecretKey_withNullKeyReturned_shouldReturnNull() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        when(mockKeyStore.getKey(anyString(), any(char[].class))).thenReturn(null);
        doReturn(mockKeyStore).when(helperUnderTest).loadKeyStore();

        assertNull(helperUnderTest.getSecretKey(KEY_ALIAS, KEY_PASSWORD));
    }

    @SneakyThrows
    @Test
    public void getSecretKey_withGeneralSecurityException_shouldThrowException() {
        doThrow(new KeyStoreException("Exception")).when(helperUnderTest).loadKeyStore();

        final Throwable thrown = assertThrows(KeyStoreHelperException.class,
                () -> helperUnderTest.getSecretKey(KEY_ALIAS, KEY_PASSWORD));
        assertInstanceOf(GeneralSecurityException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void getSecretKey_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(helperUnderTest).loadKeyStore();

        final Throwable thrown = assertThrows(KeyStoreHelperException.class,
                () -> helperUnderTest.getSecretKey(KEY_ALIAS, KEY_PASSWORD));
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    public void getSecretKey_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> helperUnderTest.getSecretKey(StringUtils.EMPTY, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> helperUnderTest.getSecretKey(null, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> helperUnderTest.getSecretKey(KEY_ALIAS, null)));
    }

    //////////////////////
    // loadKeyStore
    //////////////////////

    @SneakyThrows
    @Test
    public void loadKeyStore_withExistingKeyStore_shouldReturnKeyStore() {
        doNothing().when(helperUnderTest).initKeyStoreFileIfNotExist();
        final KeyStore expected = mock(KeyStore.class);
        final InputStream mockInputStream = mock(InputStream.class);

        try (final MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class);
             final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            keyStoreMockedStatic.when(() -> KeyStore.getInstance(anyString())).thenReturn(expected);
            filesMockedStatic.when(() -> Files.newInputStream(any(Path.class))).thenReturn(mockInputStream);

            final KeyStore actual = helperUnderTest.loadKeyStore();

            assertAll(
                    () -> assertEquals(expected, actual),
                    () -> verify(expected).load(eq(mockInputStream), eq(KEYSTORE_PASSWORD)));
        }
    }

    @SneakyThrows
    @Test
    public void loadKeyStore_withGeneralSecurityException_shouldThrowException() {
        doThrow(new KeyStoreException("Exception")).when(helperUnderTest).initKeyStoreFileIfNotExist();
        assertThrows(GeneralSecurityException.class, () -> helperUnderTest.loadKeyStore());
    }

    @SneakyThrows
    @Test
    public void loadKeyStore_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(helperUnderTest).initKeyStoreFileIfNotExist();
        assertThrows(IOException.class, () -> helperUnderTest.loadKeyStore());
    }

    ////////////////////////////////
    // initKeyStoreFileIfNotExist
    ////////////////////////////////

    @SneakyThrows
    @Test
    public void initKeyStoreFileIfNotExist_withNonExistingKeyStorePath_shouldSaveKeyStore() {
        doNothing().when(helperUnderTest).saveKeyStore(any(KeyStore.class));
        final KeyStore mockKeyStore = mock(KeyStore.class);

        try(final MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class);
            final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            keyStoreMockedStatic.when(() -> KeyStore.getInstance(anyString())).thenReturn(mockKeyStore);
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            helperUnderTest.initKeyStoreFileIfNotExist();

            assertAll(
                    () -> verify(helperUnderTest).saveKeyStore(eq(mockKeyStore)),
                    () -> verify(mockKeyStore).load(eq(null), eq(null)));
        }
    }

    @SneakyThrows
    @Test
    public void initKeyStoreFileIfNotExist_withExistingKeyStorePath_shouldDoNothing() {
        try(final MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class);
            final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            helperUnderTest.initKeyStoreFileIfNotExist();

            keyStoreMockedStatic.verify(() -> KeyStore.getInstance(anyString()), never());
        }
    }

    @Test
    public void initKeyStoreFileIfNotExist_withGeneralSecurityException_shouldThrowException() {
        try(final MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class);
            final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            keyStoreMockedStatic.when(() -> KeyStore.getInstance(anyString()))
                    .thenThrow(new KeyStoreException("Exception"));
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertThrows(GeneralSecurityException.class, () -> helperUnderTest.initKeyStoreFileIfNotExist());
        }
    }

    @SneakyThrows
    @Test
    public void initKeyStoreFileIfNotExist_withIOException_shouldThrowException() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        doThrow(new IOException("Exception")).when(mockKeyStore).load(any(), any());

        try(final MockedStatic<KeyStore> keyStoreMockedStatic = mockStatic(KeyStore.class);
            final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            keyStoreMockedStatic.when(() -> KeyStore.getInstance(anyString())).thenReturn(mockKeyStore);
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertThrows(IOException.class, () -> helperUnderTest.initKeyStoreFileIfNotExist());
        }
    }

    //////////////////////
    // saveKeyStore
    //////////////////////

    @SneakyThrows
    @Test
    public void saveKeyStore_withValidKeyStoreAndPath_shouldStore() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        final OutputStream mockOutputStream = mock(OutputStream.class);

        try(final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.newOutputStream(any(Path.class))).thenReturn(mockOutputStream);

            helperUnderTest.saveKeyStore(mockKeyStore);

            verify(mockKeyStore).store(eq(mockOutputStream), eq(KEYSTORE_PASSWORD));
        }
    }

    @SneakyThrows
    @Test
    public void saveKeyStore_withGeneralSecurityException_shouldThrowException() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        doThrow(new KeyStoreException("Exception"))
                .when(mockKeyStore)
                .store(any(OutputStream.class), any(char[].class));
        final OutputStream mockOutputStream = mock(OutputStream.class);

        try(final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.newOutputStream(any(Path.class))).thenReturn(mockOutputStream);

            assertThrows(GeneralSecurityException.class, () -> helperUnderTest.saveKeyStore(mockKeyStore));
        }
    }

    @SneakyThrows
    @Test
    public void saveKeyStore_withIOException_shouldThrowException() {
        final KeyStore mockKeyStore = mock(KeyStore.class);
        doThrow(new IOException("Exception"))
                .when(mockKeyStore)
                .store(any(OutputStream.class), any(char[].class));
        final OutputStream mockOutputStream = mock(OutputStream.class);

        try(final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.newOutputStream(any(Path.class))).thenReturn(mockOutputStream);

            assertThrows(IOException.class, () -> helperUnderTest.saveKeyStore(mockKeyStore));
        }
    }
}
