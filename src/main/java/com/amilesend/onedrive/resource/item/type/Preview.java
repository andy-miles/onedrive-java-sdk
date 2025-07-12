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

import com.amilesend.client.parse.strategy.GsonExclude;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a response to preview an item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_preview">
 * API Documentation</a>.
 */
@Builder
@Data
public class Preview {
    /** The URL for embedding using HTTP get (e.g., iFrames).*/
    private final String getUrl;
    /** The HTTP POST parameters to include if using {@link #getPostUrl()}. */
    private final String postParameters;
    /** The URL suiting for embedding using HTTP POST (e.g., post, javascript, etc.).*/
    private final String postUrl;
    /** The associated drive item identifier that this preview applies to. */
    @GsonExclude
    private final String driveItemId;
}
