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
 * Describes a thumbnail for item with a bitmap representation.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/thumbnail">API Documentation.</a>
 */
@Builder
@Data
public class Thumbnail {
    /** The image hight in pixels. */
    private final int height;
    /** The associated item identifier. */
    private final String sourceItemId;
    /** The URL used to fetch the thumbnail content. */
    private final String url;
    /** The image width in pixels. */
    private final int width;
    // Content is ignored as it's stream based. TODO: Implement if there are use-cases to use it by consumers.
}
