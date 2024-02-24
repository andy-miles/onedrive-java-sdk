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

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The envelope that encapsulates encrypted content that is used to persist to the file system.
 */
@AllArgsConstructor
@Data
public class EncryptedEnvelope {
    /** The encrypted bytes. */
    private byte[] encryptedContent;
    /** The associated initialization vector. */
    private byte[] iv;
    /** The description of the contents. */
    private String description;
}
