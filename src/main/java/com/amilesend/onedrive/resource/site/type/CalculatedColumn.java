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
package com.amilesend.onedrive.resource.site.type;

import lombok.Builder;
import lombok.Data;

/**
 * Indicates that a column value is calculated based on other columns.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/calculatedcolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class CalculatedColumn {
    /**
     * The column format. Valid values:
     * <ul>
     *     <li>{@literal dateOnly}</li>
     *     <li>{@literal dateTime}</li>
     * </ul>
     */
    private final String format;
    /** The formula used to calculate the value for the column. */
    private final String formula;
    /** The output type used for format values in the column. Valid values:
     * <ul>
     *     <li>{@literal boolean}</li>
     *     <li>{@literal currency}</li>
     *     <li>{@literal dateTime}</li>
     *     <li>{@literal number}</li>
     *     <li>{@literal text}</li>
     * </ul>
     */
    private final String outputType;
}