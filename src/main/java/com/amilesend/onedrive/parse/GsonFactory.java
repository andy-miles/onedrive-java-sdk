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
package com.amilesend.onedrive.parse;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.creator.DriveInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemVersionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.PermissionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.SpecialDriveItemInstanceCreator;
import com.amilesend.onedrive.parse.strategy.AnnotationBasedExclusionStrategy;
import com.amilesend.onedrive.parse.strategy.AnnotationBasedSerializationExclusionStrategy;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

/** Factory that vends new pre-configured {@link Gson} instances. */
public class GsonFactory {
    /**
     * Gets a new {@link Gson} instance that is configured for use by
     * {@link com.amilesend.onedrive.connection.auth.AuthManager}.
     *
     * @return the pre-configured Gson instance
     */
    public Gson newInstanceForAuthManager() {
        return new GsonBuilder()
                .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    /**
     * Gets a new {@link Gson} instance that is configured for use by
     * {@link com.amilesend.onedrive.OneDriveFactoryStateManager}.
     *
     * @return the pre-configured Gson instance
     */
    public Gson newInstanceForStateManager() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Gets a new {@link Gson} instance that is configured for use by {@link OneDriveConnection}.
     *
     * @return the pre-configured Gson instance
     */
    public Gson newInstanceForConnection(final OneDriveConnection oneDriveConnection) {
        return new GsonBuilder()
                .setExclusionStrategies(new AnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new AnnotationBasedSerializationExclusionStrategy())
                // Resource types with methods that interact with the API
                .registerTypeAdapter(Drive.class, new DriveInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(DriveItem.class, new DriveItemInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(SpecialDriveItem.class, new SpecialDriveItemInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(DriveItemVersion.class, new DriveItemVersionInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(Permission.class, new PermissionInstanceCreator(oneDriveConnection))
                .create();
    }
}
