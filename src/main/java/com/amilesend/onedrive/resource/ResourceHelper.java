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

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResourceHelper {
    /**
     * Used for {@code equals()} implementation to determine if a given object is mutually defined or not.
     *
     * @param thisObj the base object to compare to
     * @param thatObj the object to compare with
     * @return {@code true} if both objects are defined (not {@code null}); else, {@code false}
     */
    public static boolean objectDefinedEquals(final Object thisObj, final Object thatObj) {
        if (thisObj == null && thatObj == null) {
            return true;
        }

        if (thisObj != null && thatObj != null) {
            return true;
        }

        return false;
    }
}
