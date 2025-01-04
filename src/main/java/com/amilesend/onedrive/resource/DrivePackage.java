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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.resource.item.DriveItem;

/** A wrapper around a {@link DriveItem} that represents a package (OneNote). */
public class DrivePackage extends DriveItemFolderType {
    /**
     * Creates a new {@code DrivePackage} that wraps the given {@code getDelegate()} {@link DriveItem}.
     *
     * @param delegate the drive item to wrap
     */
    public DrivePackage(final DriveItem delegate) {
        super(delegate);
    }

    @Override
    public String toString() {
        return new StringBuilder("DrivePackage(name=")
                .append(getName())
                .append(", id=")
                .append(getId())
                .append(", isDeleted=")
                .append(isDeleted())
                .append(", isRemote=")
                .append(isRemote())
                .append(")")
                .toString();
    }
}
