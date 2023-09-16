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

import com.amilesend.onedrive.resource.identity.Identity;
import lombok.Data;

/**
 * Describes a shared link to a resource.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/sharinglink">
 * API Documentation</a>.
 */
@Data
public class SharingLink {
    /** The associated application identity. */
    private Identity application;
    /**
     * The type of link. Valid values include:
     * <ul>
     *     <li>{@literal view}</li>
     *     <li>{@literal edit}</li>
     *     <li>{@literal embed}</li>
     * </ul>
     */
    private String type;
    /**
     * Describes the visibility scope of the shared link. Valid values include:
     * <ul>
     *     <li>{@literal anonymous}</li>
     *     <li>{@literal organization}</li>
     * </ul>
     */
    private String scope;
    /** The embedded HTML for an {@literal <iframe>} to include in a webpage. */
    private String webHtml;
    /** URL for the resource shown in a browser. */
    private String webUrl;
}
