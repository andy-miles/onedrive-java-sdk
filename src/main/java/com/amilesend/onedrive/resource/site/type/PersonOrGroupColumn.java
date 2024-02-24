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
 * Indicates that the column's value represent a person or group from the directory.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/personorgroupcolumn">
 * API Documentation</a>.
 * @see ColumnDefinition
 */
@Builder
@Data
public class PersonOrGroupColumn {
    /** Indicates if multiple values can be selected. */
    private final boolean allowMultipleSelection;
    /**
     * How the information about the person or group should be shown. Valid values (but not limited to):
     * <ul>
     *     <li>{@literal account}</li>
     *     <li>{@literal department}</li>
     *     <li>{@literal firstName}</li>
     *     <li>{@literal id}</li>
     *     <li>{@literal lastName}</li>
     *     <li>{@literal mobilePhone}</li>
     *     <li>{@literal name}</li>
     *     <li>{@literal nameWithPictureAndDetails}</li>
     *     <li>{@literal nameWithPresence}</li>
     *     <li>{@literal office}</li>
     *     <li>{@literal pictureOnly36x36}</li>
     *     <li>{@literal pictureOnly48x48}</li>
     *     <li>{@literal pictureOnly72x72}</li>
     *     <li>{@literal sipAddress}</li>
     *     <li>{@literal title}</li>
     *     <li>{@literal userName}</li>
     *     <li>{@literal workEmail}</li>
     *     <li>{@literal workPhone}</li>
     * </ul>
     */
    private final String displayAs;
    /**
     * The permitted selection constraint. Valid values:
     * <ul>
     *     <li>{@literal peopleAndGroups}</li>
     *     <li>{@literal peopleOnly}</li>
     * </ul>
     */
    private final String chooseFromType;
}
