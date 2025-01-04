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

import lombok.Builder;
import lombok.Data;

/**
 * Describes a list
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/defaultcolumnvalue">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.site.List
 */
@Builder
@Data
public class ListInfo {
    /** Indicates that content types are enabled for the list. */
    private final boolean contentTypesEnabled;
    /** Indicates if the list is hidden from view. */
    private final boolean hidden;
    /**
     * Represents the base list template used for the list. Example values (but not limited to):
     * <ul>
     *     <li>{@literal documentLibrary}</li>
     *     <li>{@literal genericList}</li>
     *     <li>{@literal task}</li>
     *     <li>{@literal survey}</li>
     *     <li>{@literal announcements}</li>
     *     <li>{@literal contacts}</li>
     * </ul>
     */
    private final String template;
}
