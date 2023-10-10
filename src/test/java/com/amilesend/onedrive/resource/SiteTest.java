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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.site.List;
import com.amilesend.onedrive.resource.site.SiteCollection;
import com.amilesend.onedrive.resource.site.type.ColumnDefinition;
import com.amilesend.onedrive.resource.site.type.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SiteTest {
    @Mock
    private com.amilesend.onedrive.resource.site.Site mockDelegate;
    @InjectMocks
    private Site siteUnderTest;

    @Test
    public void getId_shouldReturnId() {
        when(mockDelegate.getId()).thenReturn("DriveId");
        assertEquals("DriveId", siteUnderTest.getId());
    }

    @Test
    public void getCreatedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getCreatedBy()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getCreatedBy());
    }

    @Test
    public void getCreatedDateTime_shouldReturnTimestamp() {
        final String expected = "CreatedTimestampValue";
        when(mockDelegate.getCreatedDateTime()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getCreatedDateTime());
    }

    @Test
    public void getDescription_shouldReturnDescription() {
        final String expected = "DescriptionValue";
        when(mockDelegate.getDescription()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getDescription());
    }

    @Test
    public void getETag_shouldReturnETag() {
        final String expected = "ETagValue";
        when(mockDelegate.getETag()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getETag());
    }

    @Test
    public void getLastModifiedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getLastModifiedBy()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getLastModifiedBy());
    }

    @Test
    public void getLastModifiedDateTime_shouldReturnTimestamp() {
        final String expected = "LastModifiedTimestampValue";
        when(mockDelegate.getLastModifiedDateTime()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getLastModifiedDateTime());
    }

    @Test
    public void getName_shouldReturnName() {
        final String expected = "DriveNameValue";
        when(mockDelegate.getName()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getName());
    }

    @Test
    public void getParentReference_shouldReturnItemReference() {
        final ItemReference expected = mock(ItemReference.class);
        when(mockDelegate.getParentReference()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getParentReference());
    }

    @Test
    public void getWebUrl_shouldReturnUrl() {
        final String expected = "http://some.url";
        when(mockDelegate.getWebUrl()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getWebUrl());
    }

    @Test
    public void getDisplayName_shouldReturnDisplayName() {
        final String expected = "DisplayNameValue";
        when(mockDelegate.getDisplayName()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getDisplayName());
    }

    @Test
    public void isRoot_whenDefined_shouldReturnTrue() {
        final Object expected = new Object();
        when(mockDelegate.getRoot()).thenReturn(expected);
        assertTrue(siteUnderTest.isRoot());
    }

    @Test
    public void isRoot_whenNotDefined_shouldReturnFalse() {
        when(mockDelegate.getRoot()).thenReturn(null);
        assertFalse(siteUnderTest.isRoot());
    }

    @Test
    public void getSharepointIds_shouldReturnIds() {
        final SharePointIds expected = mock(SharePointIds.class);
        when(mockDelegate.getSharepointIds()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getSharepointIds());
    }

    @Test
    public void getSiteCollection_shouldReturnSiteCollection() {
        final SiteCollection expected = mock(SiteCollection.class);
        when(mockDelegate.getSiteCollection()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getSiteCollection());
    }

    @Test
    public void getContentTypes_shouldReturnContentTypes() {
        final java.util.List<ContentType> expected = java.util.List.of(mock(ContentType.class));
        when(mockDelegate.getContentTypes()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getContentTypes());
    }

    @Test
    public void getColumns_shouldReturnColumnDefinitions() {
        final java.util.List<ColumnDefinition> expected = java.util.List.of(mock(ColumnDefinition.class));
        when(mockDelegate.getColumns()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getColumns());
    }

    @Test
    public void getItems_shouldReturnItems() {
        final java.util.List<BaseItem> expected = java.util.List.of(mock(BaseItem.class));
        when(mockDelegate.getItems()).thenReturn(expected);
        assertEquals(expected, siteUnderTest.getItems());
    }

    @Test
    public void toString_shouldReturnStringValue() {
        when(mockDelegate.getName()).thenReturn("SiteNameValue");
        when(mockDelegate.getId()).thenReturn("SiteIdValue");
        assertEquals("Site(name=SiteNameValue, id=SiteIdValue)", siteUnderTest.toString());
    }

    @Test
    public void getDefaultDocumentLibrary_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive drive =
                mock(com.amilesend.onedrive.resource.drive.Drive.class);
        when(drive.getId()).thenReturn("DocumentLibraryId");
        when(drive.getName()).thenReturn("DefaultDocumentLibrary");
        when(mockDelegate.getDefaultDocumentLibrary()).thenReturn(drive);

        final Drive actual = siteUnderTest.getDefaultDocumentLibrary();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("DocumentLibraryId", actual.getId()),
                () -> assertEquals("DefaultDocumentLibrary", actual.getName()));
    }

    @Test
    public void getDocumentLibraries_shouldReturnDrives() {
        final com.amilesend.onedrive.resource.drive.Drive drive =
                mock(com.amilesend.onedrive.resource.drive.Drive.class);
        when(drive.getId()).thenReturn("DocumentLibraryId");
        when(drive.getName()).thenReturn("DefaultDocumentLibrary");
        final java.util.List<com.amilesend.onedrive.resource.drive.Drive> drives = java.util.List.of(drive);
        when(mockDelegate.getDocumentLibraries()).thenReturn(drives);

        final java.util.List<Drive> actual = siteUnderTest.getDocumentLibraries();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(1, actual.size()),
                () -> assertEquals("DocumentLibraryId", actual.get(0).getId()),
                () -> assertEquals("DefaultDocumentLibrary", actual.get(0).getName()));
    }

    @Test
    public void getLists_shouldReturnLists() {
        final List list = mock(List.class);
        when(list.getName()).thenReturn("ListNameValue");
        when(list.getId()).thenReturn("ListIdValue");
        when(list.getSiteId()).thenReturn("SiteIdValue");
        final java.util.List<List> lists = java.util.List.of(list);
        when(mockDelegate.getLists()).thenReturn(lists);

        final java.util.List<List> actual = siteUnderTest.getLists();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(1, actual.size()),
                () -> assertEquals("ListIdValue", actual.get(0).getId()),
                () -> assertEquals("ListNameValue", actual.get(0).getName()),
                () -> assertEquals("SiteIdValue", actual.get(0).getSiteId()));
    }
}
