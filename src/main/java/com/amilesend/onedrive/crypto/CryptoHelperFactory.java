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

import lombok.Builder;
import lombok.NonNull;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

/**
 * Factory used to vend a configured {@link CryptoHelper} instance.
 * @see CryptoHelper
 */
@Builder
public class CryptoHelperFactory {
    private static final String ALIAS = "onedrive-auth-crypto-key";
    private static final String KEY_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int KEY_SIZE = 128;

    /** The key store helper used to store/retrieve keys. */
    @NonNull
    private final KeyStoreHelper keyStoreHelper;
    /** The password for the keystore. */
    @NonNull
    private final char[] keyPassword;
    /** The key alias that is stored in the keystore. */
    @Builder.Default
    @NonNull
    private final String keyAlias = ALIAS;

    /**
     * Creates a new {@link CryptoHelper} instance.
     *
     * @return the crypto helper instance
     * @throws CryptoHelperException if an error occurred while creating the instance
     */
    public CryptoHelper newInstance() throws CryptoHelperException {
        try {
            SecretKey cryptoKey = keyStoreHelper.getSecretKey(keyAlias, keyPassword);
            // Not using Optional#ofNullable() here so that exceptions are propagated
            if (cryptoKey == null) {
                cryptoKey = generateNewCryptoKey();
                keyStoreHelper.saveSecretKey(keyAlias, cryptoKey, keyPassword);
            }

            return new CryptoHelper(KEY_CIPHER_ALGORITHM, cryptoKey);
        } catch (final KeyStoreHelperException ex) {
            throw new CryptoHelperException("Unable to retrieve key from keystore: " + ex.getMessage(), ex);
        } catch (final NoSuchAlgorithmException ex) {
            throw new CryptoHelperException(
                    "Unable to generate a new crypto key (malformed algorithm): " + ex.getMessage(), ex);
        }
    }

    private SecretKey generateNewCryptoKey() throws NoSuchAlgorithmException {
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }
}
