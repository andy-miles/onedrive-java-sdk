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
package com.amilesend.onedrive.resource.request;

import com.amilesend.onedrive.resource.identity.DriveRecipient;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Represents a request to share the item to one or more recipients.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_invite">
 * API Documentation</a>.
 */
@Builder
@Data
public class AddPermissionRequest {
    /** The list of recipients. */
    private final List<DriveRecipient> recipients;
    /** A message to include in the invite. */
    private final String message;
    /** Indicates if accessing the shared item requires sign-in. */
    private final boolean requireSignIn;
    /** Indicates if an invitation should be sent to the recipient, or just grant the permission. */
    private final boolean sendInvitation;
    /** The allowed roles granted to the recipients. Valid values include:
     * <ul>
     *     <li>{@literal read}</li>
     *     <li>{@literal write}</li>
     * </ul>
     */
    private final List<String> roles;
}
