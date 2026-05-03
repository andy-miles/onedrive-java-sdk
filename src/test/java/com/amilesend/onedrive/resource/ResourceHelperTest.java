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
package com.amilesend.onedrive.resource;

import org.junit.jupiter.api.Test;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;
import static com.amilesend.onedrive.resource.ResourceHelper.validateFilename;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceHelperTest {
    @Test
    public void objectDefinedEquals_withBothNull_shouldReturnTrue() {
        assertTrue(objectDefinedEquals(null, null));
    }

    @Test
    public void objectDefinedEquals_withBothDefined_shouldReturnTrue() {
        assertTrue(objectDefinedEquals(new Object(), new Object()));
    }

    @Test
    public void objectDefinedEquals_withOneDefinedOnly_shouldReturnFalse() {
        assertAll(
                () -> assertFalse(objectDefinedEquals(new Object(), null)),
                () -> assertFalse(objectDefinedEquals(null, new Object())));
    }

    @Test
    public void validateFilename_withInvalidFilenames_shouldThrowException() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("filename\".txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("filenam*e.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("filen:ame.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("filenam<e.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("filen>ame.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("file?name.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("file/name.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("file\\name.txt")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> validateFilename("file|name.txt")));
    }
}
