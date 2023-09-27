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
package com.amilesend.onedrive.resource.item.type;

import lombok.Builder;
import lombok.Data;

/**
 * Describes the recommended view of a folder.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/folderview">
 * API Documentation</a>.
 * @see Folder
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class FolderView {
    /**
     * How the contents should be sorted. Valid values include:
     * <ul>
     *     <li>{@literal default}</li>
     *     <li>{@literal name}</li>
     *     <li>{@literal type}</li>
     *     <li>{@literal size}</li>
     *     <li>{@literal takenOrCreatedDateTime}</li>
     *     <li>{@literal lastModifiedDateTime}</li>
     *     <li>{@literal sequence}</li>
     * </ul>
     */
    private String sortBy;
    /* Valid values: ascending | descending */
    /**
     * Describes the associated sort order. If undefined, {@literal ascending} is assumed. Value values include:
     * <ul>
     *     <li>{@literal ascending}</li>
     *     <li>{@literal descending}</li>
     * </ul>
     */
    private String sortOrder;
    /**
     * Describes the type of view that should be used for the folder. Valid values include:
     * <ul>
     *     <li>{@literal default }</li>
     *     <li>{@literal icons}</li>
     *     <li>{@literal details}</li>
     *     <li>{@literal thumbnails}</li>
     * </ul>
     */
    private String viewType;
}
