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
package com.amilesend.onedrive;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.DriveListParser;
import com.amilesend.onedrive.parse.resource.parser.DriveParser;
import com.amilesend.onedrive.parse.resource.parser.SiteListParser;
import com.amilesend.onedrive.parse.resource.parser.SiteParser;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.Site;
import okhttp3.Request;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amilesend.onedrive.OneDriveTest.newMockDrive;
import static com.amilesend.onedrive.OneDriveTest.newMockDrives;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BusinessOneDriveTest {
    private static final String BUSINESS_ENDPOINT_URL = "http://mysite/_api/v2.0";
    private static final int INVALID_ID_LENGTH = 513;
    private static final int INVALID_QUERY_LENGTH = 1001;

    @Mock
    private OneDriveConnection mockConnection;
    @InjectMocks
    @Spy
    private BusinessOneDrive oneDriveUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.getBaseUrl()).thenReturn(BUSINESS_ENDPOINT_URL);
        lenient().when(mockConnection.newSignedForApiRequestBuilder()).thenReturn(new Request.Builder());
    }

    //////////////////////
    // getRootSite
    //////////////////////

    @Test
    public void getRootSite_shouldReturnSite() {
        final com.amilesend.onedrive.resource.site.Site siteToReturn = newMockSite();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(siteToReturn);

        final Site actual = oneDriveUnderTest.getRootSite();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("SiteIdValue", actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SiteParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/sites/root",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    //////////////////////
    // getSite
    //////////////////////

    @Test
    public void getSite_withValidSiteId_shouldReturnSite() {
        final com.amilesend.onedrive.resource.site.Site siteToReturn = newMockSite();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(siteToReturn);

        final Site actual = oneDriveUnderTest.getSite("SiteIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("SiteIdValue", actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SiteParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/sites/SiteIdValue",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getSite_withInvalidSiteId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getSite(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> oneDriveUnderTest.getSite(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getSite(RandomStringUtils.random(INVALID_ID_LENGTH))));
    }

    //////////////////////
    // getSiteForGroup
    //////////////////////

    @Test
    public void getSiteForGroup_withValidGroupId_shouldReturnSite() {
        final com.amilesend.onedrive.resource.site.Site siteToReturn = newMockSite();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(siteToReturn);

        final Site actual = oneDriveUnderTest.getSiteForGroup("GroupIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals("SiteIdValue", actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SiteParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/groups/GroupIdValue/sites/root",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getSiteForGroup_withInvalidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getSiteForGroup(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getSiteForGroup(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getSiteForGroup(RandomStringUtils.random(INVALID_ID_LENGTH))));
    }

    //////////////////////
    // getRootSites
    //////////////////////

    @Test
    public void getRootSites_shouldReturnSiteList() {
        final List<com.amilesend.onedrive.resource.site.Site> sitesToReturn = newMockSites();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(sitesToReturn);

        final List<Site> actual = oneDriveUnderTest.getRootSites();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertFalse(actual.isEmpty()),
                () -> assertEquals("SiteIdValue", actual.get(0).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SiteListParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/" +
                                "sites?select=siteCollection,webUrl&filter=siteCollection/root%20ne%20null",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    //////////////////////
    // searchForSite
    //////////////////////

    @Test
    public void searchForSite_withValidQuery_shouldReturnSiteList() {
        final List<com.amilesend.onedrive.resource.site.Site> sitesToReturn = newMockSites();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(sitesToReturn);

        final List<Site> actual = oneDriveUnderTest.searchForSite("SearchQuery");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertNotNull(actual),
                () -> assertFalse(actual.isEmpty()),
                () -> assertEquals("SiteIdValue", actual.get(0).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(SiteListParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/sites?search=SearchQuery",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void searchForSite_withInvalidQuery_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.searchForSite(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.searchForSite(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.searchForSite(RandomStringUtils.random(INVALID_QUERY_LENGTH))));
    }

    ////////////////////////////
    // getDefaultDriveForGroup
    ////////////////////////////

    @Test
    public void getDefaultDriveForGroup_withValidGroupId_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive driveToReturn = newMockDrive();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(driveToReturn);

        final Drive actual = oneDriveUnderTest.getDefaultDriveForGroup("GroupIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(driveToReturn.getId(), actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/groups/GroupIdValue/drive",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDefaultDriveForGroup_withInvalidValidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForGroup(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForGroup(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForGroup(RandomStringUtils.random(INVALID_ID_LENGTH))));
    }

    ////////////////////////////
    // getDefaultDriveForSite
    ////////////////////////////

    @Test
    public void getDefaultDriveForSite_withValidGroupId_shouldReturnDrive() {
        final com.amilesend.onedrive.resource.drive.Drive driveToReturn = newMockDrive();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(driveToReturn);

        final Drive actual = oneDriveUnderTest.getDefaultDriveForSite("SiteIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(driveToReturn.getId(), actual.getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/sites/SiteIdValue/drive",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDefaultDriveForSite_withInvalidValidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForSite(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForSite(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDefaultDriveForSite(RandomStringUtils.random(INVALID_ID_LENGTH))));
    }

    //////////////////////
    // getDrivesForGroup
    //////////////////////

    @Test
    public void getDrivesForGroup_withValidGroupId_shouldReturnDriveList() {
        final List<com.amilesend.onedrive.resource.drive.Drive> drives = newMockDrives();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(drives);

        final List<Drive> actual = oneDriveUnderTest.getDrivesForGroup("GroupIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(drives.size(), actual.size()),
                () -> assertEquals("DriveId1", actual.get(0).getId()),
                () -> assertEquals("DriveId2", actual.get(1).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveListParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/groups/GroupIdValue/drives",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDrivesForGroup_withInvalidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getDrivesForGroup(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForGroup(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForGroup(RandomStringUtils.random(512))));
    }

    //////////////////////
    // getDrivesForSite
    //////////////////////

    @Test
    public void getDrivesForSite_withValidSiteId_shouldReturnDriveList() {
        final List<com.amilesend.onedrive.resource.drive.Drive> drives = newMockDrives();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(drives);

        final List<com.amilesend.onedrive.resource.Drive> actual =
                oneDriveUnderTest.getDrivesForSite("SiteIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(drives.size(), actual.size()),
                () -> assertEquals("DriveId1", actual.get(0).getId()),
                () -> assertEquals("DriveId2", actual.get(1).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveListParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/sites/SiteIdValue/drives",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDrivesForSite_withInvalidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getDrivesForSite(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForSite(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForSite(RandomStringUtils.random(512))));
    }

    //////////////////////
    // getDrivesForUser
    //////////////////////

    @Test
    public void getDrivesForUser_withValidUserId_shouldReturnDriveList() {
        final List<com.amilesend.onedrive.resource.drive.Drive> drives = newMockDrives();
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(drives);

        final List<com.amilesend.onedrive.resource.Drive> actual =
                oneDriveUnderTest.getDrivesForUser("UserIdValue");

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(drives.size(), actual.size()),
                () -> assertEquals("DriveId1", actual.get(0).getId()),
                () -> assertEquals("DriveId2", actual.get(1).getId()),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(DriveListParser.class)),
                () -> assertEquals("http://mysite/_api/v2.0/users/UserIdValue/drives",
                        requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }

    @Test
    public void getDrivesForUser_withInvalidGroupId_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> oneDriveUnderTest.getDrivesForUser(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForUser(StringUtils.EMPTY)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> oneDriveUnderTest.getDrivesForUser(RandomStringUtils.random(512))));
    }

    private static com.amilesend.onedrive.resource.site.Site newMockSite() {
        final com.amilesend.onedrive.resource.site.Site site =
                mock(com.amilesend.onedrive.resource.site.Site.class);
        when(site.getId()).thenReturn("SiteIdValue");
        return site;
    }

    private static List<com.amilesend.onedrive.resource.site.Site> newMockSites() {
        return List.of(newMockSite());
    }
}
