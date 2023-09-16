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
package com.amilesend.onedrive.resource.identity;

import lombok.Data;

/**
 * Describes a person, group, or other recipient that a item was shared to using a share action.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/driverecipient">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.activities.action.ShareAction
 */
@Data
public class DriveRecipient {
    /** The recipient's email address. */
    private String email;
    /** The alias of the recipient (where email address is unavailable. */
    private String alias;
    /** The identifier for the recipient. */
    private String objectId;
}
