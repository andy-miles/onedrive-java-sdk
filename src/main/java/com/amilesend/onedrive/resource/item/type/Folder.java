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
package com.amilesend.onedrive.resource.item.type;

import lombok.Builder;
import lombok.Data;

/**
 * Describes if a given drive item is a folder resource type.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/folder">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class Folder {
    /** The number of child items associated with the drive item. */
    private final int childCount;
    /** The properties that describe the recommended view for the folder. */
    private FolderView folderView;
}
