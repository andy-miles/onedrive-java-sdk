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
package com.amilesend.onedrive.parse.resource.parser;

import com.amilesend.onedrive.resource.item.type.Permission;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;

/**
 * Parses a response body that contains a {@link Permission}.
 * @see Permission
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PermissionParser implements GsonParser<Permission> {
    private static final BasicParser<Permission> BASIC_PARSER = new BasicParser<>(Permission.class);

    /** The permission associated with the permission to parse. */
    private final String driveItemId;

    @Override
    public Permission parse(@NonNull final Gson gson, @NonNull final InputStream jsonStream) {
        final Permission permission = BASIC_PARSER.parse(gson, jsonStream);
        return copyWithDriveItemId(permission, driveItemId);
    }

    /**
     * Copies the given {@code permission} and injects the {@code driveItemId}.
     *
     * @param permission the permission to copy
     * @param driveItemId the driveItemId to set
     * @return the copied permission
     */
    static Permission copyWithDriveItemId(final Permission permission, final String driveItemId) {
        return Permission.builder()
                .connection(permission.getConnection())
                .driveItemId(driveItemId)
                .grantedTo(permission.getGrantedTo())
                .grantedToIdentities(permission.getGrantedToIdentities())
                .id(permission.getId())
                .inheritedFrom(permission.getInheritedFrom())
                .invitation(permission.getInvitation())
                .link(permission.getLink())
                .roles(permission.getRoles())
                .shareId(permission.getShareId())
                .build();
    }
}
