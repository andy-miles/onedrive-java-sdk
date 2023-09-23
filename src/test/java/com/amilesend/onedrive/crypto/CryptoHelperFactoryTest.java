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
package com.amilesend.onedrive.crypto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoHelperFactoryTest {
    private static final String ALIAS = "onedrive-auth-crypto-key";
    private static final String KEY_ALGORITHM = "AES";
    private static final char[] KEY_PASSWORD = "password".toCharArray();
    private static final byte[] ENCODED_KEY = { 1, 2, 3, 4 };

    @Mock
    private KeyStoreHelper mockKeyStoreHelper;

    private CryptoHelperFactory factoryUnderTest;

    @BeforeEach
    public void setUp() {
        factoryUnderTest = new CryptoHelperFactory(mockKeyStoreHelper, KEY_PASSWORD);
    }

    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> new CryptoHelperFactory(null, KEY_PASSWORD)),
                () -> assertThrows(NullPointerException.class,
                        () -> new CryptoHelperFactory(mockKeyStoreHelper, null)));
    }

    @SneakyThrows
    @Test
    public void newInstance_withNoExistingKey_shouldGenerateAndSaveNewKey() {
        final SecretKey mockSecretKey = mockSecretKey();
        final KeyGenerator mockKeyGenerator = mock(KeyGenerator.class);
        when(mockKeyGenerator.generateKey()).thenReturn(mockSecretKey);

        try (final MockedStatic<KeyGenerator> keyGeneratorMockedStatic = mockStatic(KeyGenerator.class)) {
            keyGeneratorMockedStatic.when(() -> KeyGenerator.getInstance(anyString())).thenReturn(mockKeyGenerator);

            final CryptoHelper actual = factoryUnderTest.newInstance();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> verify(mockKeyStoreHelper).saveSecretKey(eq(ALIAS), eq(mockSecretKey), eq(KEY_PASSWORD)),
                    () -> verify(mockKeyGenerator).generateKey());
        }
    }

    @SneakyThrows
    @Test
    public void newInstance_withExistingKey_shouldReturnCrypto() {
        final SecretKey mockSecretKey = mockSecretKey();
        when(mockKeyStoreHelper.getSecretKey(anyString(), any(char[].class))).thenReturn(mockSecretKey);

        assertNotNull(factoryUnderTest.newInstance());
    }

    @SneakyThrows
    @Test
    public void newInstance_withKeyStoreHelperException_shouldThrowException() {
        when(mockKeyStoreHelper.getSecretKey(anyString(), any(char[].class)))
                .thenThrow(new KeyStoreHelperException("Exception"));

        final Throwable thrown = assertThrows(CryptoHelperException.class, () -> factoryUnderTest.newInstance());
        assertInstanceOf(KeyStoreHelperException.class, thrown.getCause());
    }

    @Test
    public void newInstance_withNoSuchAlgorithmException_shouldThrowExecption() {
        try (final MockedStatic<KeyGenerator> keyGeneratorMockedStatic = mockStatic(KeyGenerator.class)) {
            keyGeneratorMockedStatic.when(() -> KeyGenerator.getInstance(anyString()))
                    .thenThrow(new NoSuchAlgorithmException("Exception"));

            final Throwable thrown = assertThrows(CryptoHelperException.class, () -> factoryUnderTest.newInstance());
            assertInstanceOf(NoSuchAlgorithmException.class, thrown.getCause());
        }
    }

    private SecretKey mockSecretKey() {
        final SecretKey mockSecretKey = mock(SecretKey.class);
        when(mockSecretKey.getEncoded()).thenReturn(ENCODED_KEY);
        when(mockSecretKey.getAlgorithm()).thenReturn(KEY_ALGORITHM);
        return mockSecretKey;
    }
}
