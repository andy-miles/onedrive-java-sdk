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
import com.amilesend.onedrive.resource.item.type.Folder;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;

/** A wrapper around a {@link DriveItem} that represents a folder. */
public class DriveFolder extends DriveItemFolderType {

    /**
     * Creates a new {@code DriveFolder} that wraps the given {@code delegate} {@link DriveItem}.
     *
     * @param delegate the drive item to wrap
     */
    public DriveFolder(final DriveItem delegate) {
        super(delegate);
    }

    /**
     * Describes if this folder is the root of the drive or not.
     *
     * @return {@code true} if this folder is the root folder of the drive; else, {@code false}.
     */
    public boolean isRoot() {
        return getDelegate().getRoot() != null;
    }

    /**
     * Gets the attributes associated with this folder.
     *
     * @return the folder attributes
     * @see Folder
     */
    public Folder getFolderAttributes() {
        return getDelegate().getFolder();
    }

    /**
     * Describes if this folder is a special folder.
     *
     * @return {@code true} if this folder is special; else, {@code false}.
     * @see SpecialFolder
     * @see SpecialFolder.Type
     */
    public boolean isSpecialFolder() {
        return getSpecialFolder() != null;
    }

    /**
     * Gets the special folder attributes. Can be {@code null}.
     *
     * @return the special folder attributes
     */
    public SpecialFolder getSpecialFolder() {
        return getDelegate().getSpecialFolder();
    }

    @Override
    public String toString() {
        return new StringBuilder("DriveFolder(name=")
                .append(getName())
                .append(", id=")
                .append(getId())
                .append(", isRoot=")
                .append(isRoot())
                .append(", isSpecialFolder=")
                .append(isSpecialFolder())
                .append(", isDeleted=")
                .append(isDeleted())
                .append(", isRemote=")
                .append(isRemote())
                .append(")")
                .toString();
    }
}
