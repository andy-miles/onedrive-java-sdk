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
package com.amilesend.onedrive.connection.parse.resource.parser;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.activities.ItemActionSet;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.activities.ItemActivityTimeSet;
import com.amilesend.onedrive.resource.activities.action.CommentAction;
import com.amilesend.onedrive.resource.activities.action.DeleteAction;
import com.amilesend.onedrive.resource.activities.action.MentionAction;
import com.amilesend.onedrive.resource.activities.action.MoveAction;
import com.amilesend.onedrive.resource.activities.action.RenameAction;
import com.amilesend.onedrive.resource.activities.action.ShareAction;
import com.amilesend.onedrive.resource.activities.action.VersionAction;
import com.amilesend.onedrive.resource.drive.Drive;
import com.amilesend.onedrive.resource.drive.Quota;
import com.amilesend.onedrive.resource.identity.Identity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemPage;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.SpecialDriveItem;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import com.amilesend.onedrive.resource.item.type.Audio;
import com.amilesend.onedrive.resource.item.type.Deleted;
import com.amilesend.onedrive.resource.item.type.File;
import com.amilesend.onedrive.resource.item.type.FileSystemInfo;
import com.amilesend.onedrive.resource.item.type.Folder;
import com.amilesend.onedrive.resource.item.type.FolderView;
import com.amilesend.onedrive.resource.item.type.GeoCoordinates;
import com.amilesend.onedrive.resource.item.type.Hashes;
import com.amilesend.onedrive.resource.item.type.Image;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.Package;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.Photo;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.PublicationFacet;
import com.amilesend.onedrive.resource.item.type.RemoteItem;
import com.amilesend.onedrive.resource.item.type.SearchResult;
import com.amilesend.onedrive.resource.item.type.SharePointIds;
import com.amilesend.onedrive.resource.item.type.Shared;
import com.amilesend.onedrive.resource.item.type.SharingInvitation;
import com.amilesend.onedrive.resource.item.type.SharingLink;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import com.amilesend.onedrive.resource.item.type.Thumbnail;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.item.type.Video;

import java.util.List;

public class TestDataHelper {

    public static AsyncJobStatus newAsyncJobStatus() {
        return AsyncJobStatus.builder()
                .operation("ItemCopy")
                .percentageComplete(100.0D)
                .resourceId("identifier")
                .status("completed")
                .statusDescription("Description")
                .build();
    }

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

    public static DriveItem newDriveItem(final OneDriveConnection connection, final int suffix) {
        return newDriveItem(connection, suffix, new Object(), new Object());
    }

    public static DriveItem newDriveItem(final OneDriveConnection connection,
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

    public static DriveItemPage newDriveItemPage(final OneDriveConnection connection) {
        return DriveItemPage.builder()
                .nextLink("NextUrlValue")
                .value(List.of(newDriveItemZipFile(connection, 1), newDriveItemFolder(connection)))
                .build();
    }

    public static DriveItemVersion newDriveItemVersion(final OneDriveConnection connection,
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

    public static ItemActivity newItemActivity(final int suffix) {
        return ItemActivity.builder()
                .action(newItemActionSet())
                .actor(newIdentitySet())
                .id("ItemActivityIdValue" + suffix)
                .times(newItemActivityTimeSet())
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

    public static Permission newPermission(final OneDriveConnection connection, final String driveItemId) {
        return newPermission(connection, driveItemId, driveItemId);
    }

    public static Permission newPermission(final OneDriveConnection connection,
                                           final String idSuffix,
                                           final String driveItemId) {
        return Permission.builder()
                .connection(connection)
                .driveItemId(driveItemId)
                .grantedTo(newIdentitySet())
                .grantedToIdentities(List.of(newIdentitySet()))
                .id("PermissionId-" + idSuffix)
                .inheritedFrom(newParentReference())
                .invitation(newSharingInvitation())
                .link(newSharingLink())
                .roles(List.of("Role1", "Role2", "Role3"))
                .shareId("ShareIdValue")
                .build();
    }

    public static Preview newPreview(final String driveItemId) {
        return Preview.builder()
                .driveItemId(driveItemId)
                .getUrl("PreviewGetUrlValue")
                .postUrl("PreviewPostUrlValue")
                .postParameters("PreviewPostParametersValue")
                .build();
    }


    public static ThumbnailSet newThumbnailSet() {
        return ThumbnailSet.builder()
                .id("ThumbnailId")
                .large(newThumbnail("Large"))
                .medium(newThumbnail("Medium"))
                .small(newThumbnail("Small"))
                .source(newThumbnail("Source"))
                .build();
    }

    private static ItemActivityTimeSet newItemActivityTimeSet() {
        return ItemActivityTimeSet.builder()
                .observedDateTime("ObservedTimeStampValue")
                .recordedDateTime("RecordedTimeStampValue")
                .build();
    }

    private static Deleted newDeleted() {
        return Deleted.builder().state("StateValue").build();
    }

    private static Audio newAudio() {
        return Audio.builder()
                .album("AlbumValue")
                .albumArtist("AlbumArtistValue")
                .artist("ArtistValue")
                .bitrate(128L)
                .composers("ComposersValue")
                .copyright("CopyrightValue")
                .disc((short) 1)
                .discCount((short) 2)
                .duration(120L)
                .genre("GenreValue")
                .hasDrm(true)
                .isVariableBitrate(true)
                .title("TitleValue")
                .track(3)
                .trackCount(12)
                .year(2000)
                .build();
    }

    private static Video newVideo() {
        return Video.builder()
                .audioBitsPerSample(16)
                .audioChannels(2)
                .audioFormat("AAC")
                .audioSamplesPerSecond(44100)
                .bitrate(39101896)
                .duration(8053L)
                .fourCC("H264")
                .frameRate(23.9877D)
                .height(1280)
                .width(720)
                .build();
    }

    private static Image newImage() {
        return Image.builder()
                .height(200)
                .width(300)
                .build();
    }

    private static Photo newPhoto() {
        return Photo.builder()
                .cameraMake("CameraMakeValue")
                .cameraModel("CameraModelValue")
                .exposureDenominator(300.0D)
                .exposureNumerator(150.0D)
                .focalLength(3)
                .fNumber(4.0D)
                .iso(500)
                .takenDateTime("TakenTimestampValue")
                .build();
    }

    private static GeoCoordinates newGeoCoordinates() {
        return GeoCoordinates.builder()
                .altitude(500.0D)
                .latitude(100.0D)
                .longitude(200.0D)
                .build();
    }

    private static SearchResult newSearchResult() {
        return SearchResult.builder().onClickTelemetryUrl("TelemetryUrlValue").build();
    }

    private static RemoteItem newRemoteItem() {
        return RemoteItem.builder()
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedTimestampValue")
                .file(newFile())
                .fileSystemInfo(newFileSystemInfo())
                .folder(newFolder())
                .id("RemoteIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                ._package(newPackage())
                .parentReference(newParentReference())
                .shared(newShared())
                .sharepointIds(newSharePointIds())
                .size(2048L)
                .specialFolder(newSpecialFolder())
                .webDavUrl("WebDavUrlValue")
                .webUrl("WebUrlValue")
                .build();
    }

    private static SpecialFolder newSpecialFolder() {
        return SpecialFolder.builder().name("SpecialFolderName").build();
    }

    private static SharePointIds newSharePointIds() {
        return SharePointIds.builder()
                .listId("ListIdValue")
                .listItemId("ListItemIdValue")
                .listItemUniqueId("ListItemUniqueIdValue")
                .siteId("SiteIdValue")
                .siteUrl("SiteUrlValue")
                .tenantId("TenantIdValue")
                .webId("WebIdValue")
                .build();
    }

    private static Shared newShared() {
        return Shared.builder()
                .owner(newIdentitySet())
                .scope("users")
                .sharedBy(newIdentitySet())
                .sharedDateTime("SharedTimestampValue")
                .build();
    }

    private static Package newPackage() {
        return Package.builder().type("PackageTypeValue").build();
    }

    private static SharingInvitation newSharingInvitation() {
        return SharingInvitation.builder()
                .email("emailValue")
                .invitedBy(newIdentitySet())
                .signInRequired(true)
                .build();
    }
    private static SharingLink newSharingLink() {
        return SharingLink.builder()
                .application(newIdentity())
                .scope("anonymous")
                .type("view")
                .webHtml("webHtmlValue")
                .webUrl("webUrlValue")
                .build();
    }

    // Note: Not all attributes are set in a real-world scenario. Populating all of the attributes
    // for test coverage
    private static ItemActionSet newItemActionSet() {
        return ItemActionSet.builder()
                .comment(CommentAction.builder()
                        .parentAuthor(newIdentitySet())
                        .participants(List.of(newIdentitySet()))
                        .isReply(false)
                        .build())
                .create(new Object())
                .delete(DeleteAction.builder()
                        .name("DeleteActionName")
                        .objectType("File")
                        .build())
                .edit(new Object())
                .mention(MentionAction.builder().mentionees(List.of(newIdentitySet())).build())
                .move(MoveAction.builder()
                        .from("FromValue")
                        .to("ToValue")
                        .build())
                .rename(RenameAction.builder()
                        .newName("NewNameValue")
                        .oldName("OldNameValue")
                        .build())
                .restore(new Object())
                .share(ShareAction.builder().recipients(List.of(newIdentitySet())).build())
                .version(VersionAction.builder().newVersion("NewVersion").build())
                .build();
    }

    private static Quota newQuota() {
        return Quota.builder()
                .deleted(1000L)
                .fileCount(0L)
                .remaining(8000L)
                .state("normal")
                .total(10000L)
                .used(1000L)
                .build();
    }

    private static PublicationFacet newPublicationFacet(final String versionId) {
        return PublicationFacet.builder()
                .level("published")
                .versionId(versionId)
                .build();
    }

    private static IdentitySet newIdentitySet() {
        return IdentitySet.builder().user(newIdentity()).build();
    }

    private static Identity newIdentity() {
        return Identity.builder()
                .displayName("IdentityName")
                .id("IdentityId")
                .thumbnails(newThumbnailSet())
                .build();
    }

    private static Thumbnail newThumbnail(final String urlPrefix) {
        return Thumbnail.builder()
                .height(100)
                .sourceItemId("SourceItemIdValue")
                .url(urlPrefix + "ThumbnailUrlValue")
                .width(100)
                .build();
    }

    private static Folder newFolder() {
        return Folder.builder()
                .childCount(3)
                .folderView(newFolderView())
                .build();
    }

    private static FolderView newFolderView() {
        return FolderView.builder()
                .sortBy("name")
                .sortOrder("ascending")
                .viewType("details")
                .build();
    }

    private static File newFile() {
        return File.builder()
                .hashes(newHashes())
                .mimeType("application/zip")
                .processingMetadata(false)
                .build();
    }

    private static ItemReference newParentReference() {
        return ItemReference.builder()
                .driveId("DriveIdValue")
                .driveType("personal")
                .id("ParentIdValue")
                .name("ParentFolderName")
                .path("ParentFolderPath")
                .build();
    }

    private static FileSystemInfo newFileSystemInfo() {
        return FileSystemInfo.builder()
                .createdDateTime("CreatedTimeStampValue")
                .lastAccessedDateTime("LastAccessedTimeStampValue")
                .lastModifiedDateTime("LastModifiedTimeStampValue")
                .build();
    }

    private static Hashes newHashes() {
        return Hashes.builder()
                .crc32Hash("crcHashValue")
                .sha1Hash("sha1Value")
                .quickXorHash("quickXorHashValue")
                .build();
    }
}
