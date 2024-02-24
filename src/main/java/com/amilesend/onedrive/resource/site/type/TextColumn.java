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
 * Describes that the column stores text as its value.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/textcolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class TextColumn {
    /** Indicates if multiple lines of text are allowed. */
    private final boolean allowMultipleLines;
    /** Indicates that updates should append existing text, or to replace it. */
    private final boolean appendChangesToExistingText;
    /** The size of the text box. */
    private final int linesForEditing;
    /** The maximum number of characters permitted. */
    private final int maxLength;
    /**
     * The type of text. Valid values:
     * <ul>
     *     <li>{@literal plain}</li>
     *     <li>{@literal richText}</li>
     * </ul>
     */
    private final String textType;
}
