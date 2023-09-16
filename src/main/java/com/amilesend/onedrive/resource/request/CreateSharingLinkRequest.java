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
package com.amilesend.onedrive.resource.request;

import lombok.Data;

/**
 * A request to create a new sharing link for an item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_createlink">
 * API Documentation</a>.
 */
@Data
public class CreateSharingLinkRequest {
    /**
     * The type of sharing link to create. Valid values include:
     * <ul>
     *     <li>{@literal view} - Read only access</li>
     *     <li>{@literal edit} - Read/write access</li>
     *     <li>{@literal embed} - Embeddable link to the item.  Only applies to personal accounts</li>
     * </ul>
     */
    private String type;
    /**
     * The scope of access to the link. Valid values include:
     * <ul>
     *     <li>{@literal anonymous} - Anyone can access without sign-in.</li>
     *     <li>{@literal organization} - Only those in your organization (tenant) can use the link</li>
     * </ul>
     */
    private String scope;
}
