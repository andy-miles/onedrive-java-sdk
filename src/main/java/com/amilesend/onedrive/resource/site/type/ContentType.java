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
package com.amilesend.onedrive.resource.site.type;

import com.amilesend.onedrive.resource.item.type.ItemReference;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Describes the SharePoint content type. This enables the definition of a set of required columns that
 * are present on every item in a list.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/contenttype">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.site.Site
 */
@Builder
@Data
public class ContentType {
    /** The item description. */
    private final String description;
    /** The group name that this type belongs to. */
    private final String group;
    /** Indicates if the content type is hidden. */
    private final boolean hidden;
    /** The content type identifier. */
    private final String id;
    /** Describes if this type is inherited from another type (like a site that contains the type).*/
    private final ItemReference inheritedFrom;
    /** The content type name. */
    private final String name;
    /** Describes the order that the content type appears in the UI. */
    private final ContentTypeOrder order;
    /** The parent content type identifier. */
    private final String parentId;
    /** Indicates if this content type is mutable. */
    private final boolean readOnly;
    /** Indicates if the content type is only mutable by administrators. */
    private final boolean sealed;
    /** The list of columns that are required by this content type. */
    private final List<ColumnLink> columnLinks;
}
