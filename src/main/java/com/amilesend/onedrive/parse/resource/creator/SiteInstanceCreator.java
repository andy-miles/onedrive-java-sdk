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
package com.amilesend.onedrive.parse.resource.creator;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.site.Site;
import com.google.gson.InstanceCreator;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

/**
 * A custom {@link InstanceCreator} implementation that injects the {@link OneDriveConnection} to the
 * resource type so that method operations can be performed on the {@link Site} resource.
 */
@RequiredArgsConstructor
public class SiteInstanceCreator implements InstanceCreator<Site> {
    /** The current client connection instance. */
    private final OneDriveConnection connection;

    /** Creates a new {@code DriveInstanceCreator} with the current client connection instance. */
    @Override
    public Site createInstance(final Type type) {
        return Site.builder().connection(connection).build();
    }
}
