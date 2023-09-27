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
 * Describes the identifiers on how to reference a drive item resource.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/itemreference">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class ItemReference {
    /** The drive identifier. */
    private final String driveId;
    /**
     * The drive type descriptor.  Valid types are:
     * <ul>
     *     <li>{@literal personal} - Personal drive</li>
     *     <li>{@literal business} - Busieness drive</li>
     *     <li>{@literal documentLibrary} - Sharepoint document library</li>
     * </ul>
     */
    private final String driveType;
    /** The drive item identifier. */
    private final String id;
    /** The list identifier. */
    private final String listId;
    /** The name of the referenced item. */
    private final String name;
    /** The path of the referenced item. */
    private final String path;
    /** The shared resource identifier of the referenced item. */
    private final String shareId;
    /** The SharePoint identifiers of the referenced item. */
    private final SharePointIds sharepointIds;
    /** The site identifier of the referenced item. */
    private final String siteId;
}
