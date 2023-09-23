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

import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Validate;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;

/**
 * Helper that manages storage of symmetric keys to a key store file.
 */
@RequiredArgsConstructor
public class KeyStoreHelper {
    private static final String KEY_STORE_TYPE = "pkcs12";

    /** The path to the key store used to store the symmetric key used for encryption. */
    @NonNull
    private final Path keyStorePath;
    /** The password to access the key store. */
    @NonNull
    private final char[] keyStorePassword;

    /**
     * Saves the given {@code key} to the key store. Notes:
     * <ul>
     *     <li>The key is referenced by the {@code alias} and is individually protected via the given
     *     {@code keyPassword}.</li>
     *     <li>If the key store file does not exist, this method attempts
     *     to create a new key store to the defined {@code keyStorePath} prior to saving the key.</li>
     * </ul>
     *
     * @param alias the alias to store the key as
     * @param key the key itself
     * @param keyPassword the password that is specific to the key
     * @throws KeyStoreHelperException if an error occurred while saving the key to the key store
     */
    public void saveSecretKey(final String alias, @NonNull final SecretKey key, @NonNull final char[] keyPassword)
            throws KeyStoreHelperException {
        Validate.notBlank(alias, "alias must not be blank");

        try {
            final KeyStore keyStore = loadKeyStore();
            final KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
            final KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(keyPassword);
            keyStore.setEntry(alias, secretKeyEntry, protectionParameter);
            saveKeyStore(keyStore);
        } catch (final GeneralSecurityException ex) {
            throw new KeyStoreHelperException(
                    "An error occurred while accessing the keystore: " + ex.getMessage(), ex);
        } catch (final IOException ex) {
            throw new KeyStoreHelperException(
                    "An error occurred while accessing the keystore file: " + ex.getMessage(), ex);
        }
    }

    /**
     * Gets the key with the given {@code alias} and associated {@code keyPassword} from the key store. Notes:
     * <ul>
     *     <li>If the key store file does not exist, then this method attempts to create a new key store to the defined
     *     {@code keyStorePath}. In this case, {@code null} will be returned as the given {@code alias} references no
     *     existing key.</li>
     *     <li>If the key does not exist for the given {@code alias}, then {@code null} will be returned.</li>
     * </ul>
     *
     * @param alias the alias for the key
     * @param keyPassword the associated password that protects the key
     * @return the key, or {@code null}
     * @throws KeyStoreHelperException if an error occurred while retrieving the key
     */
    public SecretKey getSecretKey(final String alias, @NonNull final char[] keyPassword)
            throws KeyStoreHelperException {
        Validate.notBlank(alias, "alias must not be blank");

        try {
            final KeyStore keyStore = loadKeyStore();
            final Key key = keyStore.getKey(alias, keyPassword);
            if (key == null) {
                return null;
            }

            if (!SecretKey.class.isInstance(key)) {
                throw new KeyStoreHelperException("Retrieved key is not a SecretKey");
            }

            return (SecretKey) key;
        } catch (final GeneralSecurityException ex) {
            throw new KeyStoreHelperException(
                    "An error occurred while accessing the keystore: " + ex.getMessage(), ex);
        } catch (final IOException ex) {
            throw new KeyStoreHelperException(
                    "An error occurred while accessing the keystore file: " + ex.getMessage(), ex);
        }
    }

    @VisibleForTesting
    KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        initKeyStoreFileIfNotExist();

        final KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(Files.newInputStream(keyStorePath), keyStorePassword);
        return keyStore;
    }

    @VisibleForTesting
    void initKeyStoreFileIfNotExist() throws GeneralSecurityException, IOException {
        if (Files.exists(keyStorePath)) {
            return;
        }

        final KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
        keyStore.load(null, null);
        saveKeyStore(keyStore);
    }

    @VisibleForTesting
    void saveKeyStore(final KeyStore keyStore) throws GeneralSecurityException, IOException {
        try (final OutputStream os = Files.newOutputStream(keyStorePath)) {
            keyStore.store(os, keyStorePassword);
        }
    }
}
