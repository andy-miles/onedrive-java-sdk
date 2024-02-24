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
package com.amilesend.onedrive.resource.item;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.parser.DriveItemListParser;
import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Objects;

import static com.amilesend.onedrive.resource.drive.Drive.DRIVE_BASE_URL_PATH;

/**
 * Represent a special drive folder.
 * <p>
 * a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/drive_get_specialfolder">
 * API Documentation</a>.
 * <p> Note: Documentation mentions to reference special folder by name, but seems to be by id instead (e.g., "approot"
 * instead of "App+Root" in the path).</p>
 * @see DriveItem
 */

@SuperBuilder
@ToString(callSuper = true)
public class SpecialDriveItem extends DriveItem {
    /** The special folder type. */
    @Getter
    @GsonExclude
    @Setter
    private SpecialFolder.Type specialFolderType;

    @Override
    public List<DriveItem> getChildren() {
        Validate.notNull(specialFolderType, "specialFolderType must not be null");

        final OneDriveConnection connection = getConnection();
        return connection.execute(
                connection.newSignedForApiRequestBuilder()
                        .url(new StringBuilder(connection.getBaseUrl())
                                .append(DRIVE_BASE_URL_PATH)
                                .append("special/")
                                .append(specialFolderType.getId())
                                .append("/children")
                                .toString())
                        .build(),
                new DriveItemListParser());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }

        final SpecialDriveItem that = (SpecialDriveItem) obj;
        return getSpecialFolderType() == that.getSpecialFolderType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSpecialFolderType());
    }
}
