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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.drive.Quota;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.Folder;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveTest {
    @Mock
    private com.amilesend.onedrive.resource.drive.Drive mockDelegate;
    @InjectMocks
    private Drive driveUnderTest;

    @Test
    public void getId_shouldReturnId() {
        when(mockDelegate.getId()).thenReturn("DriveId");
        assertEquals("DriveId", driveUnderTest.getId());
    }

    @Test
    public void getCreatedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getCreatedBy()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getCreatedBy());
    }

    @Test
    public void getCreatedDateTime_shouldReturnTimestamp() {
        final String expected = "CreatedTimestampValue";
        when(mockDelegate.getCreatedDateTime()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getCreatedDateTime());
    }

    @Test
    public void getDescription_shouldReturnDescription() {
        final String expected = "DescriptionValue";
        when(mockDelegate.getDescription()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getDescription());
    }

    @Test
    public void getETag_shouldReturnETag() {
        final String expected = "ETagValue";
        when(mockDelegate.getETag()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getETag());
    }

    @Test
    public void getLastModifiedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getLastModifiedBy()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getLastModifiedBy());
    }

    @Test
    public void getLastModifiedDateTime_shouldReturnTimestamp() {
        final String expected = "LastModifiedTimestampValue";
        when(mockDelegate.getLastModifiedDateTime()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getLastModifiedDateTime());
    }

    @Test
    public void getName_shouldReturnName() {
        final String expected = "DriveNameValue";
        when(mockDelegate.getName()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getName());
    }

    @Test
    public void getParentReference_shouldReturnItemReference() {
        final ItemReference expected = mock(ItemReference.class);
        when(mockDelegate.getParentReference()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getParentReference());
    }

    @Test
    public void getWebUrl_shouldReturnUrl() {
        final String expected = "http://some.url";
        when(mockDelegate.getWebUrl()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getWebUrl());
    }

    @Test
    public void getDriveType_shouldReturnType() {
        final String expected = "personal";
        when(mockDelegate.getDriveType()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getDriveType());
    }

    @Test
    public void getOwner_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getOwner()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getOwner());
    }

    @Test
    public void getQuota_shouldReturnQuota() {
        final Quota expected = mock(Quota.class);
        when(mockDelegate.getQuota()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getQuota());
    }

    @Test
    public void getSharepointIds_shouldReturnSharePointIds() {
        final SharePointIds expected = mock(SharePointIds.class);
        when(mockDelegate.getSharepointIds()).thenReturn(expected);
        assertEquals(expected, driveUnderTest.getSharepointIds());
    }

    @Test
    public void isSystemManaged_withSystemDefined_shouldReturnTrue() {
        when(mockDelegate.getSystem()).thenReturn(new Object());
        assertTrue(driveUnderTest.isSystemManaged());
    }

    @Test
    public void isSystemManaged_withNoSystemDefined_shouldReturnFalse() {
        when(mockDelegate.getSystem()).thenReturn(null);
        assertFalse(driveUnderTest.isSystemManaged());
    }

    @Test
    public void toString_shouldReturnStringValue() {
        when(mockDelegate.getName()).thenReturn("DriveNameValue");
        when(mockDelegate.getId()).thenReturn("DriveId");
        assertEquals("Drive(name=DriveNameValue, id=DriveId)", driveUnderTest.toString());
    }

    @Test
    public void getRootFolder_shouldReturnRootFolder() {
        final DriveItem rootFolderDriveItem = mock(DriveItem.class);
        when(rootFolderDriveItem.getId()).thenReturn("RootFolderId");
        when(rootFolderDriveItem.getName()).thenReturn("RootFolderName");
        when(rootFolderDriveItem.getRoot()).thenReturn(new Object());
        when(mockDelegate.getRootFolder()).thenReturn(rootFolderDriveItem);

        final DriveFolder actual = driveUnderTest.getRootFolder();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("RootFolderId", actual.getId()),
                () -> assertEquals("RootFolderName", actual.getName()),
                () -> assertTrue(actual.isRoot()));
    }

    @Test
    public void search_withQuery_shouldReturnDriveItemTypeList() {
        final List<DriveItem> queryResponse = List.of(newDriveItem(true), newDriveItem(false));
        when(mockDelegate.search(anyString())).thenReturn(queryResponse);

        final List<? extends DriveItemType> actual = driveUnderTest.search("Query");

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(DriveFile.class, actual.get(0)),
                () -> assertInstanceOf(DriveFolder.class, actual.get(1)),
                () -> assertEquals("FileName", actual.get(0).getName()),
                () -> assertEquals("FolderName", actual.get(1).getName()));
    }

    @Test
    public void getChanges_shouldReturnDriveItemTypeList() {
        final List<DriveItem> queryResponse = List.of(newDriveItem(true), newDriveItem(false));
        when(mockDelegate.getChanges()).thenReturn(queryResponse);

        final List<? extends DriveItemType> actual = driveUnderTest.getChanges();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(2, actual.size()),
                () -> assertInstanceOf(DriveFile.class, actual.get(0)),
                () -> assertInstanceOf(DriveFolder.class, actual.get(1)),
                () -> assertEquals("FileName", actual.get(0).getName()),
                () -> assertEquals("FolderName", actual.get(1).getName()));
    }

    @Test
    public void getSpecialFolder_shouldReturnDriveFolder() {
        final SpecialDriveItem specialDriveItem = mock(SpecialDriveItem.class);
        when(specialDriveItem.getSpecialFolder()).thenReturn(mock(SpecialFolder.class));
        when(mockDelegate.getSpecialFolder(any(SpecialFolder.Type.class))).thenReturn(specialDriveItem);

        final DriveFolder actual = driveUnderTest.getSpecialFolder(SpecialFolder.Type.DOCUMENTS);

        assertAll(
                () -> assertNotNull(actual),
                () -> assertTrue(actual.isSpecialFolder()));
    }

    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final List<ItemActivity> expected = List.of(mock(ItemActivity.class), mock(ItemActivity.class));
        when(mockDelegate.getActivities()).thenReturn(expected);

        final List<ItemActivity> actual = driveUnderTest.getActivities();

        assertEquals(expected, actual);
    }

    private DriveItem newDriveItem(final boolean isFile) {
        final DriveItem item = mock(DriveItem.class);
        if (isFile) {
            when(item.getName()).thenReturn("FileName");
            when(item.getFolder()).thenReturn(null);
        } else {
            when(item.getName()).thenReturn("FolderName");
            when(item.getFolder()).thenReturn(mock(Folder.class));
        }

        return item;
    }

}
