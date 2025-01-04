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

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * Helper that encrypts and decrypts content for the configured cipher algorithm and key.
 * <p>
 * Note: See
 * <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html">
 * Java Security Standard Algorithm Names</a> for a list of available ciphers and be aware of which
 * are applicable to comply with your local geographic laws and/or policies.
 * @see EncryptedEnvelope
 */
@Slf4j
public class CryptoHelper {
    private static final int IV_SIZE = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String cipherAlgorithm;
    private final SecretKeySpec keySpec;

    /**
     * Creates a new {@code CryptoHelper} instance.
     *
     * @param cipherAlgorithm the cipher algorithm to use
     * @param key the key used to encrypt/decrypt content
     */
    public CryptoHelper(final String cipherAlgorithm, @NonNull final SecretKey key) {
        Validate.notBlank(cipherAlgorithm, "cipherAlgorithm must not be blank");
        this.cipherAlgorithm = cipherAlgorithm;
        keySpec = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
    }

    /**
     * Encrypts the given bytes and returns an {@link EncryptedEnvelope}.
     *
     * @param content the content to encrypt
     * @param description a description of the content
     * @return the {@link EncryptedEnvelope} containing the encrypted bytes
     * @throws CryptoHelperException if an error occurred while encrypting the contents
     */
    public EncryptedEnvelope encrypt(@NonNull final byte[] content, final String description)
            throws CryptoHelperException {
        log.debug("Encrypting {}", description);
        final IvParameterSpec ivSpec = generativeIv();
        try {
            final byte[] encryptedContent = invokeCipher(Cipher.ENCRYPT_MODE, content, ivSpec);
            return new EncryptedEnvelope(encryptedContent, ivSpec.getIV(), description);
        } catch (final GeneralSecurityException ex) {
            throw new CryptoHelperException("Unable to encrypt content: " + ex.getMessage(), ex);
        }
    }

    /**
     * Decrypts the given {@link EncryptedEnvelope} and returns the contents as a byte array.
     *
     * @param envelope the envelope to decrypt
     * @return the decrypted contents
     * @throws CryptoHelperException if an error occurred while decrypting the envelope's contents
     */
    @SuppressFBWarnings("STATIC_IV")
    public byte[] decrypt(@NonNull final EncryptedEnvelope envelope) throws CryptoHelperException {
        log.debug("Decrypting {}", envelope.getDescription());
        final IvParameterSpec ivSpec = new IvParameterSpec(envelope.getIv());
        try {
            return invokeCipher(Cipher.DECRYPT_MODE, envelope.getEncryptedContent(), ivSpec);
        } catch (final GeneralSecurityException ex) {
            throw new CryptoHelperException("Unable to decrypt contnet: " + ex.getMessage(), ex);
        }
    }

    @VisibleForTesting
    byte[] invokeCipher(final int mode, final byte[] input, final IvParameterSpec ivSpec)
            throws GeneralSecurityException {
        final Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(mode, keySpec, ivSpec);
        return cipher.doFinal(input);
    }

    private IvParameterSpec generativeIv() {
        final byte[] iv = new byte[IV_SIZE];
        RANDOM.nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}
