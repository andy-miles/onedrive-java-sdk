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
package com.amilesend.onedrive.crypto;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CryptoHelperTest {
    private static final String DESCRIPTION = "Description";
    private static final String CIPHER_ALGORITHM = "AES";
    private static final byte[] ENCODED_KEY = { 1, 2, 3, 4 };
    private static final int IV_SIZE = 16;

    @Mock
    private SecretKey mockSecretKey;

    private CryptoHelper helperUnderTest;

    @BeforeEach
    public void setUp() {
        when(mockSecretKey.getEncoded()).thenReturn(ENCODED_KEY);
        when(mockSecretKey.getAlgorithm()).thenReturn(CIPHER_ALGORITHM);

        helperUnderTest = spy(new CryptoHelper(CIPHER_ALGORITHM, mockSecretKey));
    }

    @Test
    public void ctor_withInvalidParameters_shouldThrowExecption() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CryptoHelper(StringUtils.EMPTY, mockSecretKey)),
                () -> assertThrows(NullPointerException.class,
                        () -> new CryptoHelper(null, mockSecretKey)),
                () -> assertThrows(NullPointerException.class,
                        () -> new CryptoHelper(CIPHER_ALGORITHM, null)));
    }

    //////////////
    // encrypt
    //////////////

    @SneakyThrows
    @Test
    public void encrypt_withValidContent_shouldReturnEnvelope() {
        final byte[] content = { 4, 3, 2, 1 };
        final byte[] encryptedContext = { 10, 9, 8, 7 };
        doReturn(encryptedContext)
                .when(helperUnderTest)
                .invokeCipher(anyInt(), any(byte[].class), any(IvParameterSpec.class));

        final EncryptedEnvelope actual = helperUnderTest.encrypt(content, DESCRIPTION);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(encryptedContext, actual.getEncryptedContent()),
                () -> assertEquals(IV_SIZE, actual.getIv().length),
                () -> assertEquals(DESCRIPTION, actual.getDescription()));
    }

    @SneakyThrows
    @Test
    public void encrypt_withGeneralSecurityException_shouldThrowException() {
        final byte[] content = { 4, 3, 2, 1 };
        doThrow(new NoSuchPaddingException("Exception"))
                .when(helperUnderTest)
                .invokeCipher(anyInt(), any(byte[].class), any(IvParameterSpec.class));

        final Throwable thrown = assertThrows(CryptoHelperException.class,
                () -> helperUnderTest.encrypt(content, DESCRIPTION));
        assertInstanceOf(GeneralSecurityException.class, thrown.getCause());
    }

    @Test
    public void encrypt_withNullContent_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> helperUnderTest.encrypt(null, DESCRIPTION));
    }

    //////////////
    // decrypt
    //////////////

    @SneakyThrows
    @Test
    public void decrypt_withValidEnvelope_shouldReturnDecryptedContents() {
        final byte[] decryptedContent = { 4, 3, 2, 1 };
        doReturn(decryptedContent)
                .when(helperUnderTest)
                .invokeCipher(anyInt(), any(byte[].class), any(IvParameterSpec.class));
        final byte[] encryptedContent = { 10, 9, 8, 7 };
        final byte[] iv = { 4, 5, 6, 7 };
        final EncryptedEnvelope envelope = new EncryptedEnvelope(encryptedContent, iv, DESCRIPTION);

        final byte[] actual = helperUnderTest.decrypt(envelope);

        assertTrue(Arrays.equals(decryptedContent, actual));
    }

    @SneakyThrows
    @Test
    public void decrypt_withGeneralSecurityException_shouldThrowException() {
        doThrow(new NoSuchAlgorithmException("Exception"))
                .when(helperUnderTest)
                .invokeCipher(anyInt(), any(byte[].class), any(IvParameterSpec.class));
        final byte[] encryptedContent = { 10, 9, 8, 7 };
        final byte[] iv = { 4, 5, 6, 7 };
        final EncryptedEnvelope envelope = new EncryptedEnvelope(encryptedContent, iv, DESCRIPTION);

        final Throwable thrown = assertThrows(CryptoHelperException.class, () -> helperUnderTest.decrypt(envelope));
        assertInstanceOf(GeneralSecurityException.class, thrown.getCause());
    }

    @Test
    public void decrypt_withNullEnvelope_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> helperUnderTest.decrypt(null));
    }

    //////////////////
    // invokeCipher
    //////////////////

    @SneakyThrows
    @Test
    public void invokeCipher_withValidParameters_shouldInvokeCipherAndReturnBytes() {
        final byte[] expected = { 1, 2, 3, 4 };
        final Cipher mockCipher = mock(Cipher.class);
        when(mockCipher.doFinal(any(byte[].class))).thenReturn(expected);
        final byte[] input = { 4, 3, 2, 1 };
        final IvParameterSpec mockIvSpec = mock(IvParameterSpec.class);

        try (final MockedStatic<Cipher> cipherMockedStatic = mockStatic(Cipher.class)) {
            cipherMockedStatic.when(() -> Cipher.getInstance(anyString())).thenReturn(mockCipher);

            final byte[] actual = helperUnderTest.invokeCipher(Cipher.DECRYPT_MODE, input, mockIvSpec);

            assertAll(
                    () -> assertTrue(Arrays.equals(expected, actual)),
                    () -> verify(mockCipher).init(eq(Cipher.DECRYPT_MODE), isA(SecretKeySpec.class), eq(mockIvSpec)));
        }
    }

    @Test
    public void invokeCipher_withGeneralSecurityException_shouldThrowException() {
        final byte[] input = { 4, 3, 2, 1 };
        final IvParameterSpec mockIvSpec = mock(IvParameterSpec.class);
        try (final MockedStatic<Cipher> cipherMockedStatic = mockStatic(Cipher.class)) {
            cipherMockedStatic.when(() -> Cipher.getInstance(anyString()))
                    .thenThrow(new NoSuchPaddingException("Exception"));

            assertThrows(GeneralSecurityException.class,
                    () -> helperUnderTest.invokeCipher(Cipher.DECRYPT_MODE, input, mockIvSpec));
        }
    }
}
