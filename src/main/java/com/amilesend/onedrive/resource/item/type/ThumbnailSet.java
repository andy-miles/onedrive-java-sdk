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
package com.amilesend.onedrive.resource.item.type;

import lombok.Builder;
import lombok.Data;

/**
 * A collection of thumbnails associated with a drive item or identity set.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/thumbnailset">
 * API Documentation.</a>
 * @see com.amilesend.onedrive.resource.item.DriveItem
 * @see com.amilesend.onedrive.resource.identity.Identity
 */
@Builder
@Data
public class ThumbnailSet {
    /** The item identifier. */
    private final String id;
    /** A 1920x1920 scaled thumbnail. */
    private final Thumbnail large;
    /** A 176x176 scaled thumbnail. */
    private final Thumbnail medium;
    /** A 48x48 scopped thumbnail. */
    private final Thumbnail small;
    /** Source used to generate the other thumbnail sizes. */
    private final Thumbnail source;
}
