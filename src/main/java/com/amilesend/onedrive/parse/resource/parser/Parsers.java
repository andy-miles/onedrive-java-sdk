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
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public class Parsers {
    public static final GsonParser<AsyncJobStatus> ASYNC_JOB_STATUS_PARSER = new BasicParser<>(AsyncJobStatus.class);
    public static final GsonParser<List<DriveItem>> DRIVE_ITEM_LIST_PARSER = new ListResponseBodyParser<>(DriveItem.class);
    public static final GsonParser<DriveItem> DRIVE_ITEM_PARSER = new BasicParser<>(DriveItem.class);
    public static final GsonParser<DriveItemPage> DRIVE_ITEM_PAGE_PARSER = new BasicParser<>(DriveItemPage.class);
    public static final GsonParser<Drive> DRIVE_PARSER = new BasicParser<>(Drive.class);
    public static final GsonParser<List<Drive>> DRIVE_LIST_PARSER = new ListResponseBodyParser<>(Drive.class);
    public static final GsonParser<Map<String, Object>> FIELD_VALUE_SET_PARSER = new MapParser<>(String.class, Object.class);
    public static final GsonParser<GetColumnValuesResponse> GET_COLUMN_VALUES_RESPONSE_PARSER = new BasicParser<>(GetColumnValuesResponse.class);
    public static final GsonParser<List<ItemActivity>> ITEM_ACTIVITY_LIST_PARSER = new ListResponseBodyParser<>(ItemActivity.class);
    public static final GsonParser<Site> SITE_PARSER = new BasicParser<>(Site.class);
    public static final GsonParser<List<Site>> SITE_LIST_PARSER = new ListResponseBodyParser<>(Site.class);
    public static final GsonParser<List<ThumbnailSet>> THUMBNAIL_SET_LIST_PARSER = new ListResponseBodyParser<>(ThumbnailSet.class);

    public static GsonParser<List<DriveItemVersion>> newDriveItemVersionListParser(
            final String driveItemId,
            final String name) {
        return new DriveItemVersionListParser(driveItemId, name);
    }

    public static GsonParser<ListItem> newListItemParser(final String siteId, final String listId) {
        return new ListItemParser(siteId, listId);
    }

    public static GsonParser<ListItemVersion> newListItemVersionParser(
            final String siteId,
            final String listId,
            final String listItemId) {
        return new ListItemVersionParser(siteId, listId, listItemId);
    }

    public static GsonParser<List<ListItemVersion>> newListItemVersionListParser(
            final String siteId,
            final String listId,
            final String listItemId) {
        return new ListItemVersionListParser(siteId, listId, listItemId);
    }

    public static GsonParser<List<com.amilesend.onedrive.resource.site.List>> newListListParser(final String siteId) {
        return new ListListParser(siteId);
    }

    public static GsonParser<List<Permission>> newPermissionListParser(final String driveItemId) {
        return new PermissionListParser(driveItemId);
    }

    public static GsonParser<Permission> newPermissionParser(final String driveItemId) {
        return new PermissionParser(driveItemId);
    }

    public static GsonParser<Preview> newPreviewParser(final String driveItemId) {
        return new PreviewParser(driveItemId);
    }

    public static GsonParser<SpecialDriveItem> newSpecialDriveItemParser(final SpecialFolder.Type specialFolderType) {
        return new SpecialDriveItemParser(specialFolderType);
    }
}
