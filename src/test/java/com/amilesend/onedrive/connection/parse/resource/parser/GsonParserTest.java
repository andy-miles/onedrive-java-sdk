/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
package com.amilesend.onedrive.connection.parse.resource.parser;

import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.client.parse.strategy.AnnotationBasedExclusionStrategy;
import com.amilesend.client.parse.strategy.AnnotationBasedSerializationExclusionStrategy;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.parse.resource.creator.DriveInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.DriveItemVersionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.ListItemInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.ListItemVersionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.PermissionInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.SiteInstanceCreator;
import com.amilesend.onedrive.parse.resource.creator.SpecialDriveItemInstanceCreator;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.site.ListItem;
import com.amilesend.onedrive.resource.site.ListItemVersion;
import com.amilesend.onedrive.resource.site.Site;
import com.amilesend.onedrive.resource.site.response.GetColumnValuesResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDrive;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItem;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemFolder;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemPage;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemVersion;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemZipFile;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newSpecialDriveItem;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newFieldValueSet;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newGetColumnValuesResponse;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newList;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newListItem;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newListItemVersion;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newSite;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newAsyncJobStatus;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newItemActivity;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPermission;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPreview;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newThumbnailSet;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.ASYNC_JOB_STATUS_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PAGE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_ITEM_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.DRIVE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.FIELD_VALUE_SET_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.GET_COLUMN_VALUES_RESPONSE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.ITEM_ACTIVITY_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.SITE_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.SITE_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.THUMBNAIL_SET_LIST_PARSER;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newDriveItemVersionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemVersionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListItemVersionParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newListListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPermissionListParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPermissionParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newPreviewParser;
import static com.amilesend.onedrive.parse.resource.parser.Parsers.newSpecialDriveItemParser;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class GsonParserTest {
    final OneDriveConnection connection = mock(OneDriveConnection.class);
    private Gson gson = newInstanceForTest(connection);

    ///////////////////////////////
    // AsyncJobStatusParser
    ///////////////////////////////

    @Test
    public void asyncJobStatusParser_withValidInputStream_shouldReturnAsyncJobStatus() {
        final AsyncJobStatus expected = newAsyncJobStatus();
        final AsyncJobStatus actual =
                ASYNC_JOB_STATUS_PARSER.parse(gson, SerializedResource.ASYNC_JOB_STATUS.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void asyncJobStatusParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(ASYNC_JOB_STATUS_PARSER);
    }

    ///////////////////////////////
    // DriveItemParser
    ///////////////////////////////

    @Test
    public void driveItemParser_withValidInputStream_shouldReturnDriveItem() {
        final DriveItem expected = newDriveItem(connection, 1);
        final DriveItem actual =
                DRIVE_ITEM_PARSER.parse(gson, SerializedResource.DRIVE_ITEM.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveItemParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(DRIVE_ITEM_PARSER);
    }

    ///////////////////////////////
    // DriveItemListParser
    ///////////////////////////////

    @Test
    public void driveItemListParser_withValidInputStream_shouldReturnDriveItemList() {
        final List<DriveItem> expected =
                List.of(newDriveItemZipFile(connection, 1), newDriveItemFolder(connection));
        final List<DriveItem> actual =
                DRIVE_ITEM_LIST_PARSER.parse(gson, SerializedResource.DRIVE_ITEM_LIST.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveItemListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(DRIVE_ITEM_LIST_PARSER);
    }

    ///////////////////////////////
    // DriveItemPageParser
    ///////////////////////////////

    @Test
    public void driveItemPageParser_withValidInputStream_shouldReturnDriveItemPage() {
        final DriveItemPage expected = newDriveItemPage(connection);
        final DriveItemPage actual =
                DRIVE_ITEM_PAGE_PARSER.parse(gson, SerializedResource.DRIVE_ITEM_PAGE.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveItemPageParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(DRIVE_ITEM_PAGE_PARSER);
    }

    ///////////////////////////////
    // DriveItemVersionListParser
    ///////////////////////////////

    @Test
    public void driveItemVersionListParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final List<DriveItemVersion> expected = List.of(
                newDriveItemVersion(connection, "1"),
                newDriveItemVersion(connection, "2"));
        final List<DriveItemVersion> actual =
                newDriveItemVersionListParser(
                        expected.get(0).getDriveItemId(),
                        expected.get(0).getName())
                .parse(gson, SerializedResource.DRIVE_ITEM_VERSION_LIST.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveItemVersionListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(
                newDriveItemVersionListParser("VersionIdValue", "Filename"));

    }

    ///////////////////////////////
    // DriveParser
    ///////////////////////////////

    @Test
    public void driveParser_withValidInputStream_shouldReturnDrive() {
        final Drive expected = newDrive(connection, 1);
        final Drive actual = DRIVE_PARSER.parse(gson, SerializedResource.DRIVE.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(DRIVE_PARSER);
    }

    ///////////////////////////////
    // DriveListParser
    ///////////////////////////////

    @Test
    public void driveListParser_withValidInputStream_shouldReturnDriveList() {
        final List<Drive> expected = List.of(newDrive(connection, 1), newDrive(connection, 2));
        final List<Drive> actual = DRIVE_LIST_PARSER.parse(gson, SerializedResource.DRIVE_LIST.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void driveListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(DRIVE_LIST_PARSER);
    }

    ///////////////////////////////
    // FieldValueSetParser
    ///////////////////////////////

    @Test
    public void fieldValueSetParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final Map<String, Object> expected = newFieldValueSet();
        final Map<String, Object> actual =
                FIELD_VALUE_SET_PARSER.parse(gson, SerializedResource.FIELD_VALUE_SET.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void fieldValueSetParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(FIELD_VALUE_SET_PARSER);
    }

    //////////////////////////////////
    // GetColumnValuesResponseParser
    //////////////////////////////////

    @Test
    public void getColumnValuesResponseParser_withValidInputStream_shouldReturnResponse() {
        final GetColumnValuesResponse expected = newGetColumnValuesResponse();
        final GetColumnValuesResponse actual = GET_COLUMN_VALUES_RESPONSE_PARSER
                .parse(gson, SerializedResource.GET_COLUMN_VALUES_RESPONSE.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void getColumnValuesResponseParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(GET_COLUMN_VALUES_RESPONSE_PARSER);
    }

    ///////////////////////////////
    // ItemActivityListParser
    ///////////////////////////////

    @Test
    public void itemActivityListParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final List<ItemActivity> expected = List.of(newItemActivity(1), newItemActivity(2));
        final List<ItemActivity> actual =
                ITEM_ACTIVITY_LIST_PARSER.parse(gson, SerializedResource.ITEM_ACTIVITY_LIST.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void itemActivityListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(ITEM_ACTIVITY_LIST_PARSER);
    }

    ///////////////////////////////
    // ListItemParser
    ///////////////////////////////

    @Test
    public void listItemParser_withValidInputStream_shouldReturnListItem() {
        final String siteId = "SiteIdValue";
        final String listId = "ListIdValue";
        final ListItem expected = newListItem(connection, siteId, listId);
        final ListItem actual =
                newListItemParser(siteId, listId).parse(gson, SerializedResource.LIST_ITEM.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void listItemParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newListItemParser("SiteIdValue", "ListIdValue"));
    }

    ///////////////////////////////
    // ListItemVersionListParser
    ///////////////////////////////

    @Test
    public void listItemVersionListParser_withValidInputStream_shouldReturnListItemVersionList() {
        final String siteId = "SiteIdValue";
        final String listId = "ListIdValue";
        final String listItemId = "ListItemIdValue";
        final List<ListItemVersion> expected = List.of(
                newListItemVersion(connection, siteId, listId, listItemId),
                newListItemVersion(connection, siteId, listId, listItemId));
        final List<ListItemVersion> actual =
                newListItemVersionListParser(siteId, listId, listItemId)
                        .parse(gson, SerializedResource.LIST_ITEM_VERSION_LIST.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void listItemVersionListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(
                newListItemVersionListParser("SiteIdValue", "ListIdValue", "ListItemIdValue"));
    }

    ///////////////////////////////
    // ListItemVersionParser
    ///////////////////////////////

    @Test
    public void listItemVersionParser_withValidInputStream_shouldReturnListItemVersion() {
        final String siteId = "SiteIdValue";
        final String listId = "ListIdValue";
        final String listItemId = "ListItemIdValue";
        final ListItemVersion expected = newListItemVersion(connection, siteId, listId, listItemId);
        final ListItemVersion actual =
                newListItemVersionParser(siteId, listId, listItemId)
                        .parse(gson, SerializedResource.LIST_ITEM_VERSION.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void listItemVersionParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(
                newListItemVersionParser("SiteIdValue", "ListIdValue", "ListItemIdValue"));
    }

    ///////////////////////////////
    // ListListParser
    ///////////////////////////////

    @Test
    public void listListParser_withValidInputStream_shouldReturnListOfLists() {
        final String siteId = "SiteIdValue";
        final List<com.amilesend.onedrive.resource.site.List> expected = List.of(newList(connection, siteId));
        final List<com.amilesend.onedrive.resource.site.List> actual =
                newListListParser(siteId).parse(gson, SerializedResource.LIST_OF_LIST.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void listListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newListListParser("SiteIdValue"));
    }


    public Gson newInstanceForConnection(final OneDriveConnection oneDriveConnection) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setExclusionStrategies(new AnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new AnnotationBasedSerializationExclusionStrategy())
                // Resource types with methods that interact with the API
                .registerTypeAdapter(Drive.class, new DriveInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(DriveItem.class, new DriveItemInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(SpecialDriveItem.class, new SpecialDriveItemInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(DriveItemVersion.class, new DriveItemVersionInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(Permission.class, new PermissionInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(Site.class, new SiteInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(ListItem.class, new ListItemInstanceCreator(oneDriveConnection))
                .registerTypeAdapter(ListItemVersion.class, new ListItemVersionInstanceCreator(oneDriveConnection))
                .create();
    }


    ///////////////////////////////
    // PermissionParser
    ///////////////////////////////

    @Test
    public void permissionParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final Permission expected = newPermission(connection, "driveItemIdValue");
        final Permission actual = newPermissionParser("driveItemIdValue")
                .parse(gson, SerializedResource.PERMISSION.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void permissionParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newPermissionParser("driveItemIdValue"));
    }

    ///////////////////////////////
    // PermissionsListParser
    ///////////////////////////////

    @Test
    public void permissionsListParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final List<Permission> expected = List.of(
                newPermission(connection, "driveItemIdValue"),
                newPermission(connection, "driveItemIdValue"));
        final List<Permission> actual = newPermissionListParser("driveItemIdValue")
                .parse(gson, SerializedResource.PERMISSION_LIST.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void permissionsListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newPermissionListParser("driveItemIdValue"));
    }

    ///////////////////////////////
    // PreviewParser
    ///////////////////////////////

    @Test
    public void previewParser_withValidInputStream_shouldReturnPreview() {
        final Preview expected = newPreview("driveItemIdValue");
        final Preview actual = newPreviewParser("driveItemIdValue")
                .parse(gson, SerializedResource.PREVIEW.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void previewParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newPreviewParser("driveItemIdValue"));
    }

    ///////////////////////////////
    // SiteParser
    ///////////////////////////////

    @Test
    public void siteParser_withValidInputStream_shouldReturnSite() {
        final Site expected = newSite(connection);
        final Site actual = SITE_PARSER.parse(gson, SerializedResource.SITE.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void siteParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(SITE_PARSER);
    }

    ///////////////////////////////
    // SiteListParser
    ///////////////////////////////

    @Test
    public void setListParser_withValidInputStream_shouldReturnSiteList() {
        final java.util.List<Site> expected = List.of(newSite(connection), newSite(connection));
        final java.util.List<Site> actual =
                SITE_LIST_PARSER.parse(gson, SerializedResource.SITE_LIST.getResource());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void siteListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(SITE_LIST_PARSER);
    }

    ///////////////////////////////
    // SpecialDriveItemParser
    ///////////////////////////////

    @Test
    public void specialDriveItemParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final SpecialDriveItem expected = newSpecialDriveItem(connection);
        final SpecialDriveItem actual = newSpecialDriveItemParser(SpecialFolder.Type.MUSIC)
                .parse(gson, SerializedResource.SPECIAL_DRIVE_ITEM.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()),
                () -> assertEquals(expected.getSpecialFolderType(), actual.getSpecialFolderType()));
    }

    @Test
    public void specialDriveItemParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(newSpecialDriveItemParser(SpecialFolder.Type.MUSIC));
    }

    ///////////////////////////////
    // ThumbnailSetListParser
    ///////////////////////////////

    @Test
    public void thumbnailSetListParser_withValidInputStream_shouldReturnDriveItemVersionList() {
        final List<ThumbnailSet> expected = List.of(newThumbnailSet(), newThumbnailSet());
        final List<ThumbnailSet> actual = THUMBNAIL_SET_LIST_PARSER
                .parse(gson, SerializedResource.THUMBNAIL_SET_LIST.getResource());
        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected.hashCode(), actual.hashCode()));
    }

    @Test
    public void thumbnailSetListParser_withInvalidParameters_shouldThrowException() {
        parse_withInvalidParameters_shouldThrowException(THUMBNAIL_SET_LIST_PARSER);
    }

    private <T extends GsonParser<?>> void parse_withInvalidParameters_shouldThrowException(final T parserUnderTest) {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> parserUnderTest.parse(
                        null,
                        SerializedResource.DRIVE_ITEM_ZIP_FILE.getResource())),
                () -> assertThrows(NullPointerException.class, () -> parserUnderTest.parse(
                        gson,
                        (InputStream) null)));
    }

    private static Gson newInstanceForTest(final OneDriveConnection connection) {
        return new GsonBuilder()
                .setPrettyPrinting()
                .setExclusionStrategies(new AnnotationBasedExclusionStrategy())
                .addSerializationExclusionStrategy(new AnnotationBasedSerializationExclusionStrategy())
                // Resource types with methods that interact with the API
                .registerTypeAdapter(Drive.class, new DriveInstanceCreator(connection))
                .registerTypeAdapter(DriveItem.class, new DriveItemInstanceCreator(connection))
                .registerTypeAdapter(SpecialDriveItem.class, new SpecialDriveItemInstanceCreator(connection))
                .registerTypeAdapter(DriveItemVersion.class, new DriveItemVersionInstanceCreator(connection))
                .registerTypeAdapter(Permission.class, new PermissionInstanceCreator(connection))
                .create();
    }
}
