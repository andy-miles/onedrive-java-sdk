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
package com.amilesend.onedrive.resource.request;

import com.amilesend.onedrive.resource.identity.DriveRecipient;
import lombok.Data;

import java.util.List;

/**
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_invite">
 * API Documentation</a>.
 */
@Data
public class AddPermissionRequest {
    private List<DriveRecipient> recipients;
    private String message;
    private boolean requireSignIn;
    private boolean sendInvitation;
    private List<String> roles;
}
