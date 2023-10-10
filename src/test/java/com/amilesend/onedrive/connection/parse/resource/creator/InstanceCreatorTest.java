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
package com.amilesend.onedrive.connection.parse.resource.creator;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.resource.creator.DriveInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemVersionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.ListItemInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.ListItemVersionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.PermissionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.SiteInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.SpecialDriveItemInstanceCreator;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.site.ListItem;
import com.amilesend.onedrive.resource.site.ListItemVersion;
import com.amilesend.onedrive.resource.site.Site;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class InstanceCreatorTest {
    @Mock
    private OneDriveConnection mockConnection;
    @Mock
    private Type mockType;

    @Test
    public void newDriveInstanceCreator_withCreateInstance_shouldReturnDrive() {
        final Drive actual = new DriveInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newDriveItemInstanceCreator_withCreateInstance_shouldReturnDriveItem() {
        final DriveItem actual = new DriveItemInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newDriveItemVersionInstanceCreator_withCreateInstance_shouldReturnDriveItemVersion() {
        final DriveItemVersion actual = new DriveItemVersionInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newListItemInstanceCreator_withCreateInstance_shouldReturnListItem() {
        final ListItem actual = new ListItemInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newListItemVersionInstanceCreator_withCreateInstance_shouldReturnListItemVersion() {
        final ListItemVersion actual = new ListItemVersionInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newPermissionInstanceCreator_withCreateInstance_shouldReturnPermission() {
        final Permission actual = new PermissionInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newSiteInstanceCreator_withCreateInstance_shouldReturnSite() {
        final Site actual = new SiteInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }

    @Test
    public void newSpecialDriveItemInstanceCreator_withCreateInstance_shouldReturnSpecialDriveItem() {
        final SpecialDriveItem actual = new SpecialDriveItemInstanceCreator(mockConnection).createInstance(mockType);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(mockConnection, actual.getConnection()));
    }
}
