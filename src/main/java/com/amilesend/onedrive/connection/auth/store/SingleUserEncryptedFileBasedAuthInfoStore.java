/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
import com.amilesend.client.util.StringUtils;
import com.amilesend.onedrive.connection.auth.OneDriveAuthInfo;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An {@link AuthInfoStore} implementation that stores and retrieves encrypted user authentication information
 * to the filesystem. This assumes a single user associated with the specified file path, which means
 * that any provided keyed identifiers are ignored.
 * @see OneDriveAuthInfo
 * @see AuthInfoStore
 */
@RequiredArgsConstructor
public class SingleUserEncryptedFileBasedAuthInfoStore implements AuthInfoStore {
    /** The path to save and read from. */
    @NonNull
    private final Path stateFilePath;
    /** The crypto helper instance used to encrypt and decrypt the auth info file state contents. */
    @NonNull
    private final CryptoHelper cryptoHelper;

    /**
     * Saves the given {@code authInfo} to the file system.
     *
     * @param id is ignored for this implementation
     * @param authInfo the authentication information
     * @throws AuthInfoStoreException if an error occurred while saving the authentication info to the file system
     */
    @Override
    public void store(final String id, @NonNull final OneDriveAuthInfo authInfo) throws AuthInfoStoreException {
        try {
            final EncryptedEnvelope encryptedEnvelope = cryptoHelper.encrypt(
                    authInfo.toJson().getBytes(StandardCharsets.UTF_8),
                    "OneDriveConnection AuthInfo");
            final Gson gson = GsonFactory.getInstanceForAuthManager();
            Files.write(stateFilePath, gson.toJson(encryptedEnvelope).getBytes(StandardCharsets.UTF_8));
        } catch (final CryptoHelperException ex) {
            throw new AuthInfoStoreException(
                    "An error occurred while encrypting the auth info: " + ex.getMessage(), ex);
        } catch (final IOException ex) {
            throw new AuthInfoStoreException("Unable to store AuthInfo: " + ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves the {@link OneDriveAuthInfo} from the file system.
     *
     * @param id is ignored for this implementation
     * @return the authentication information, or {@code null}
     * @throws AuthInfoStoreException if an error occurred while retrieving the authentication info from the file system
     */
    @Override
    public OneDriveAuthInfo retrieve(final String id) throws AuthInfoStoreException {
        if (!Files.exists(stateFilePath) || !Files.isReadable(stateFilePath)) {
            return null;
        }

        try {
            final String encryptedEnvelopeJson = Files.readString(stateFilePath);
            if (StringUtils.isBlank(encryptedEnvelopeJson)) {
                return null;
            }

            final Gson gson = GsonFactory.getInstanceForAuthManager();
            final EncryptedEnvelope encryptedEnvelope = gson.fromJson(encryptedEnvelopeJson, EncryptedEnvelope.class);
            final String decryptedAuthInfoJson =
                    new String(cryptoHelper.decrypt(encryptedEnvelope), StandardCharsets.UTF_8);
            return OneDriveAuthInfo.fromJson(decryptedAuthInfoJson);
        } catch (final CryptoHelperException ex) {
            throw new AuthInfoStoreException("Unable to decrypt AuthInfo: " + ex.getMessage(), ex);
        } catch (final IOException ex) {
            throw new AuthInfoStoreException("Unable to retrieve AuthInfo: " + ex.getMessage(), ex);
        }
    }
}
