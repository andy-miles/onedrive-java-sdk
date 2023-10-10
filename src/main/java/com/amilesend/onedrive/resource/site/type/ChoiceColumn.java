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

import java.util.List;

/**
 * Indicates that a column value can be selected from a list of choices.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/choicecolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class ChoiceColumn {
    /** Indicates if custom values are allowed. */
    private final boolean allowTextEntry;
    /** The list of values available for the column. */
    private final List<String> choices;
    /**
     * How choices are presented in the UI. Valid values:
     * <ul>
     *     <li>{@literal checkBoxes}</li>
     *     <li>{@literal dropDownMenu}</li>
     *     <li>{@literal radioButtons}</li>
     * </ul>
     */
    private final String displayAs;
}
