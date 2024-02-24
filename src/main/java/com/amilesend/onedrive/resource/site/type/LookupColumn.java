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
package com.amilesend.onedrive.resource.site.type;

import lombok.Builder;
import lombok.Data;

/**
 * Describes how the column's values are looked up from another source.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/lookupcolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class LookupColumn {
    /** Indicates when multiple values are selectable from the source. */
    private final boolean allowMultipleValues;
    /** Indicates if values should be able to exceed the 255-character limit. */
    private final boolean allowUnlimitedLength;
    /** The name of the source column. */
    private final String columnName;
    /** The identifier for the lookup source list. */
    private final String listId;
    /** When specified, this specified column becomes the primary lookup over this column. */
    private final String primaryLookupColumnId;
}
