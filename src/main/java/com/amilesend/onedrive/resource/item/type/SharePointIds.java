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
 * SharePoint resource identifiers for SharePoint and Business account items. This is not used for personal accounts.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/sharepointids">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.drive.Drive
 * @see com.amilesend.onedrive.resource.item.DriveItem
 */
@Builder
@Data
public class SharePointIds {
    /** Item list identifier. */
    private final String listId;
    /** Item identifier for an element contained within the list (as an integer). */
    private final String listItemId;
    /** The unique identifier for an element contained within the list. */
    private final String listItemUniqueId;
    /** The site collection identifier. */
    private final String siteId;
    /** The SharePointURL for the site that contains the item. */
    private final String siteUrl;
    /** The tenant identiifer. */
    private final String tenantId;
    /** The item's site identifier for SharePoint web. */
    private final String webId;
}
