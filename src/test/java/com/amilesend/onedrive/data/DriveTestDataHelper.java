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
package com.amilesend.onedrive.data;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import lombok.experimental.UtilityClass;

import java.util.List;

import static com.amilesend.onedrive.data.TypeTestDataHelper.newAudio;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newDeleted;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newFile;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newFileSystemInfo;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newFolder;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newGeoCoordinates;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newIdentitySet;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newImage;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPackage;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newParentReference;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPhoto;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPublicationFacet;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newQuota;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newRemoteItem;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newSearchResult;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newSharePointIds;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newShared;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newSpecialFolder;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newVideo;

@UtilityClass
public class DriveTestDataHelper {
    public static Drive newDrive(final OneDriveConnection connection) {
        return newDrive(connection, new Object());
    }

    public static Drive newDrive(final OneDriveConnection connection, final Object system) {
        return Drive.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimestampValue")
                .description("DriveDescription")
                .driveType("personal")
                .eTag("eTagValue")
                .id("DriveIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                .name("DriveName")
                .owner(newIdentitySet())
                .parentReference(newParentReference())
                .quota(newQuota())
                .sharepointIds(newSharePointIds())
                .system(system)
                .webUrl("WebUrlValue")
                .build();
    }

    public static DriveItem newDriveItem(final OneDriveConnection connection, final int suffix) {
        return newDriveItem(connection, suffix, new Object(), new Object());
    }

    public static DriveItem newDriveItem(
            final OneDriveConnection connection,
            final int suffix,
            final Object malware,
            final Object root) {
        return DriveItem.builder()
                .audio(newAudio())
                .conflictBehavior("ConflictBehaviorValue")
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimestampValue")
                .cTag("cTagValue")
                .deleted(newDeleted())
                .description("DriveItemName" + suffix + " description")
                .downloadUrl("DownloadUrlValue")
                .eTag("eTagValue")
                .file(newFile())
                .fileSystemInfo(newFileSystemInfo())
                .folder(newFolder())
                .id("DriveItemId" + suffix)
                .image(newImage())
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                .location(newGeoCoordinates())
                .malware(malware)
                .name("DriveItemName" + suffix)
                ._package(newPackage())
                .parentReference(newParentReference())
                .photo(newPhoto())
                .publication(newPublicationFacet(String.valueOf(suffix)))
                .remoteItem(newRemoteItem())
                .root(root)
                .searchResult(newSearchResult())
                .shared(newShared())
                .sharepointIds(newSharePointIds())
                .size(1024L)
                .specialFolder(newSpecialFolder())
                .sourceUrl("SourceUrlValue")
                .video(newVideo())
                .webUrl("WebUrlValue")
                .build();
    }

    public static DriveItem newDriveItemFolder(final OneDriveConnection connection) {
        return DriveItem.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimeStampValue")
                .description("SomeFolder")
                .folder(newFolder())
                .id("FolderIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("FolderName")
                .build();
    }

    public static DriveItem newDriveItemPackage(final OneDriveConnection connection) {
        return DriveItem.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimeStampValue")
                .description("SomePackage")
                ._package(newPackage())
                .id("PackageIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("PackageName")
                .build();
    }



    public static DriveItem newDriveItemZipFile(final OneDriveConnection connection, final int suffix) {
        return DriveItem.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimeStampValue")
                .description("SomeDescription")
                .file(newFile())
                .fileSystemInfo(newFileSystemInfo())
                .id("ZipFileIdValue" + suffix)
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("SomeFile" + suffix + ".zip")
                .parentReference(newParentReference())
                .size(4096L)
                .build();
    }

    public static DriveItemPage newDriveItemPage(final OneDriveConnection connection) {
        return DriveItemPage.builder()
                .nextLink("NextUrlValue")
                .value(List.of(newDriveItemZipFile(connection, 1), newDriveItemFolder(connection)))
                .build();
    }

    public static DriveItemVersion newDriveItemVersion(
            final OneDriveConnection connection,
            final String versionIdSuffix) {
        final String versionId = "VersionIdValue" + versionIdSuffix;
        return DriveItemVersion.builder()
                .connection(connection)
                .driveItemId("FileIdValue")
                .id(versionId)
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("SomeFileVersion")
                .publication(newPublicationFacet(versionId))
                .size(4096L)
                .build();
    }

    public static Drive newDrive(final OneDriveConnection connection, final int suffix) {
        return Drive.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimeStampValue")
                .description("TestDriveDescription")
                .driveType("personal")
                .id("DriveIdValue" + suffix)
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("TestDrive" + suffix)
                .owner(newIdentitySet())
                .quota(newQuota())
                .build();
    }

    public static DriveItem newRootDriveItemFolder(final OneDriveConnection connection) {
        return DriveItem.builder()
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimeStampValue")
                .description("SomeFolder")
                .folder(newFolder())
                .id("FolderIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .name("FolderName")
                .root(new Object())
                .build();
    }

    public static SpecialDriveItem newSpecialDriveItem(final OneDriveConnection connection) {
        return SpecialDriveItem.builder()
                .audio(newAudio())
                .conflictBehavior("ConflictBehaviorValue")
                .connection(connection)
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimestampValue")
                .cTag("cTagValue")
                .deleted(newDeleted())
                .description("DriveItemName description")
                .downloadUrl("DownloadUrlValue")
                .eTag("eTagValue")
                .file(newFile())
                .fileSystemInfo(newFileSystemInfo())
                .folder(newFolder())
                .id("DriveItemId")
                .image(newImage())
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                .location(newGeoCoordinates())
                .malware( new Object())
                .name("DriveItemName")
                .parentReference(newParentReference())
                .photo(newPhoto())
                .publication(newPublicationFacet("1"))
                ._package(newPackage())
                .remoteItem(newRemoteItem())
                .root(new Object())
                .searchResult(newSearchResult())
                .shared(newShared())
                .sharepointIds(newSharePointIds())
                .size(1024L)
                .sourceUrl("SourceUrlValue")
                .specialFolder(newSpecialFolder())
                .specialFolderType(SpecialFolder.Type.MUSIC)
                .video(newVideo())
                .webUrl("WebUrlValue")
                .build();
    }
}
