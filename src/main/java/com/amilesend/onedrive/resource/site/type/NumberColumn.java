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
 * Describes that the column's values are numbers.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/numbercolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class NumberColumn {
    /**
     * How many decimal places to display. Value values:
     * <ul>
     *     <li>{@literal automatic} (default) - Display as needed</li>
     *     <li>{@literal none}</li>
     *     <li>{@literal one}</li>
     *     <li>{@literal two}</li>
     *     <li>{@literal three}</li>
     *     <li>{@literal four}</li>
     *     <li>{@literal five}</li>
     * </ul>
     */
    private final String decimalPlaces;
    /**
     * How the value should be shown to the user. Valid value:
     * <ul>
     *     <li>{@literal number} (default)</li>
     *     <li>{@literal percentage}</li>
     * </ul>
     */
    private final String displayAs;
    /** The maximum permitted value. */
    private final double maximum;
    /** The minimum permitted value. */
    private final double minimum;
}
