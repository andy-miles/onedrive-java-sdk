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

import com.amilesend.onedrive.resource.identity.IdentitySet;
import lombok.Builder;
import lombok.Data;

/**
 * Indicates that a drive item has been shared with others.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/shared">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class Shared {
    /** The owner of the drive item. */
    private final IdentitySet owner;
    /**
     * The scope of how the item is shared. Valid values include:
     * <ul>
     *     <li>{@literal anonymous}</li>
     *     <li>{@literal organization}</li>
     *     <li>{@literal users}</li>
     * </ul>
     */
    private final String scope;
    /** Who shared the drive item. */
    private final IdentitySet sharedBy;
    /** When the drive item was shared (in UTC). */
    private final String sharedDateTime;
}
