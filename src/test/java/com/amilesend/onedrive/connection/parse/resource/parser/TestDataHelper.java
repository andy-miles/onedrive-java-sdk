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
        final AsyncJobStatus status = new AsyncJobStatus();
        status.setStatus("completed");
        status.setOperation("ItemCopy");
        status.setPercentageComplete(100.0D);
        status.setStatusDescription("Description");
        status.setResourceId("identifier");

        return status;
    }

    public static Drive newDrive(final OneDriveConnection connection) {
        final Drive drive = new Drive(connection);
        drive.setId("DriveIdValue");
        drive.setCreatedBy(newIdentitySet());
        drive.setCreatedDateTime("CreatedTimestampValue");
        drive.setDescription("DriveDescription");
        drive.setETag("eTagValue");
        drive.setLastModifiedBy(newIdentitySet());
        drive.setLastModifiedDateTime("LastModifiedTimestampValue");
        drive.setName("DriveName");
        drive.setParentReference(newParentReference());
        drive.setWebUrl("WebUrlValue");

        drive.setDriveType("personal");
        drive.setOwner(newIdentitySet());
        drive.setQuota(newQuota());
        drive.setSharepointIds(newSharePointIds());
        drive.setSystem(new Object());
        return drive;
    }

    public static DriveItem newDriveItemFolder(final OneDriveConnection connection) {
        final DriveItem item = new DriveItem(connection);
        item.setFolder(newFolder());
        item.setName("FolderName");
        item.setDescription("SomeFolder");
        item.setCreatedDateTime("CreatedTimeStampValue");
        item.setLastModifiedDateTime("LastModifiedTimeStampValue");
        item.setCreatedBy(newIdentitySet());
        item.setLastModifiedBy(newIdentitySet());
        item.setId("FolderIdValue");
        return item;
    }

    public static DriveItem newRootDriveItemFolder(final OneDriveConnection connection) {
        final DriveItem item = newDriveItemFolder(connection);
        item.setRoot(new Object());
        return item;
    }

    public static DriveItem newDriveItemZipFile(final OneDriveConnection connection, final int suffix) {
        final DriveItem item = new DriveItem(connection);
        item.setFile(newFile());
        item.setName("SomeFile" + suffix + ".zip");
        item.setFileSystemInfo(newFileSystemInfo());
        item.setParentReference(newParentReference());
        item.setLastModifiedDateTime("LastModifiedTimeStampValue");
        item.setSize(4096L);
        item.setCreatedDateTime("CreatedTimeStampValue");
        item.setDescription("SomeDescription");
        item.setLastModifiedBy(newIdentitySet());
        item.setId("ZipFileIdValue" + suffix);
        item.setCreatedBy(newIdentitySet());
        return item;
    }

    public static DriveItem newDriveItem(final OneDriveConnection connection, final int suffix) {
        final DriveItem item = new DriveItem(connection);
        item.setId("DriveItemId" + suffix);
        item.setCreatedBy(newIdentitySet());
        item.setCreatedDateTime("CreatedTimestampValue");
        item.setDescription("DriveItemName" + suffix + " description");
        item.setETag("eTagValue");
        item.setLastModifiedBy(newIdentitySet());
        item.setLastModifiedDateTime("LastModifiedTimestampValue");
        item.setName("DriveItemName" + suffix);
        item.setParentReference(newParentReference());
        item.setWebUrl("WebUrlValue");
        item.setAudio(newAudio());
        item.setCTag("cTagValue");
        item.setDeleted(newDeleted());
        item.setFile(newFile());
        item.setFileSystemInfo(newFileSystemInfo());
        item.setFolder(newFolder());
        item.setImage(newImage());
        item.setLocation(newGeoCoordinates());
        item.setMalware(new Object());
        item.set_package(newPackage());
        item.setPhoto(newPhoto());
        item.setPublication(newPublicationFacet(String.valueOf(suffix)));
        item.setRemoteItem(newRemoteItem());
        item.setRoot(new Object());
        item.setSearchResult(newSearchResult());
        item.setShared(newShared());
        item.setSharepointIds(newSharePointIds());
        item.setSize(1024L);
        item.setSpecialFolder(newSpecialFolder());
        item.setVideo(newVideo());
        item.setConflictBehavior("ConflictBehaviorValue");
        item.setDownloadUrl("DownloadUrlValue");
        item.setSourceUrl("SourceUrlValue");
        return item;
    }

    public static DriveItemPage newDriveItemPage(final OneDriveConnection connection) {
        final DriveItemPage page = new DriveItemPage();
        page.setValue(List.of(newDriveItemZipFile(connection, 1), newDriveItemFolder(connection)));
        page.setNextLink("NextUrlValue");
        return page;
    }

    public static DriveItemVersion newDriveItemVersion(final OneDriveConnection connection,
                                                       final String versionIdSuffix) {
        final String versionId = "VersionIdValue" + versionIdSuffix;
        final DriveItemVersion version = new DriveItemVersion(connection);
        version.setDriveItemId("FileIdValue");
        version.setName("SomeFileVersion");
        version.setSize(4096L);
        version.setId(versionId);
        version.setLastModifiedBy(newIdentitySet());
        version.setLastModifiedDateTime("LastModifiedTimeStampValue");
        version.setPublication(newPublicationFacet(versionId));
        return version;
    }

    public static Drive newDrive(final OneDriveConnection connection, final int suffix) {
        final Drive drive = new Drive(connection);
        drive.setDriveType("personal");
        drive.setQuota(newQuota());
        drive.setId("DriveIdValue" + suffix);
        drive.setName("TestDrive" + suffix);
        drive.setOwner(newIdentitySet());
        drive.setCreatedBy(newIdentitySet());
        drive.setLastModifiedBy(newIdentitySet());
        drive.setLastModifiedDateTime("LastModifiedTimeStampValue");
        drive.setDescription("TestDriveDescription");
        drive.setCreatedDateTime("CreatedTimeStampValue");
        return drive;
    }

    public static ItemActivity newItemActivity(final int suffix) {
        final ItemActivity activity = new ItemActivity();
        activity.setId("ItemActivityIdValue" + suffix);
        activity.setActor(new IdentitySet());
        activity.setAction(newItemActionSet());
        activity.setTimes(newItemActivityTimeSet());
        return activity;
    }

    public static SpecialDriveItem newSpecialDriveItem(final OneDriveConnection connection) {
        final SpecialDriveItem item = new SpecialDriveItem(connection);
        item.setSpecialFolderType(SpecialFolder.Type.MUSIC);
        item.setId("DriveItemId");
        item.setCreatedBy(newIdentitySet());
        item.setCreatedDateTime("CreatedTimestampValue");
        item.setDescription("DriveItemName description");
        item.setETag("eTagValue");
        item.setLastModifiedBy(newIdentitySet());
        item.setLastModifiedDateTime("LastModifiedTimestampValue");
        item.setName("DriveItemName");
        item.setParentReference(newParentReference());
        item.setWebUrl("WebUrlValue");
        item.setAudio(newAudio());
        item.setCTag("cTagValue");
        item.setDeleted(newDeleted());
        item.setFile(newFile());
        item.setFileSystemInfo(newFileSystemInfo());
        item.setFolder(newFolder());
        item.setImage(newImage());
        item.setLocation(newGeoCoordinates());
        item.setMalware(new Object());
        item.set_package(newPackage());
        item.setPhoto(newPhoto());
        item.setPublication(newPublicationFacet("1"));
        item.setRemoteItem(newRemoteItem());
        item.setRoot(new Object());
        item.setSearchResult(newSearchResult());
        item.setShared(newShared());
        item.setSharepointIds(newSharePointIds());
        item.setSize(1024L);
        item.setSpecialFolder(newSpecialFolder());
        item.setVideo(newVideo());
        item.setConflictBehavior("ConflictBehaviorValue");
        item.setDownloadUrl("DownloadUrlValue");
        item.setSourceUrl("SourceUrlValue");
        return item;
    }

    public static Permission newPermission(final OneDriveConnection connection, final String driveItemId) {
        final Permission permission = new Permission(connection);
        permission.setDriveItemId(driveItemId);
        permission.setId("PermissionId-" + driveItemId);
        permission.setLink(newSharingLink());
        permission.setInvitation(newSharingInvitation());
        permission.setRoles(List.of("Role1", "Role2", "Role3"));
        permission.setGrantedTo(newIdentitySet());
        permission.setShareId("ShareIdValue");
        permission.setGrantedToIdentities(List.of(newIdentitySet()));
        permission.setInheritedFrom(newParentReference());
        return permission;
    }

    public static Preview newPreview(final String driveItemId) {
        final Preview preview = new Preview();
        preview.setDriveItemId(driveItemId);
        preview.setGetUrl("PreviewGetUrlValue");
        preview.setPostUrl("PreviewPostUrlValue");
        preview.setPostParameters("PreviewPostParametersValue");
        return preview;
    }


    public static ThumbnailSet newThumbnailSet() {
        final ThumbnailSet set = new ThumbnailSet();
        set.setId("ThumbnailId");
        set.setLarge(newThumbnail("Large"));
        set.setMedium(newThumbnail("Medium"));
        set.setSmall(newThumbnail("Small"));
        set.setSource(newThumbnail("Source"));
        return set;
    }

    private static ItemActivityTimeSet newItemActivityTimeSet() {
        final ItemActivityTimeSet set = new ItemActivityTimeSet();
        set.setObservedDateTime("ObservedTimeStampValue");
        set.setRecordedDateTime("RecordedTimeStampValue");
        return set;
    }

    private static Deleted newDeleted() {
        final Deleted deleted = new Deleted();
        deleted.setState("StateValue");
        return deleted;
    }

    private static Audio newAudio() {
        final Audio audio = new Audio();
        audio.setAlbum("AlbumValue");
        audio.setAlbumArtist("AlbumArtistValue");
        audio.setArtist("ArtistValue");
        audio.setBitrate(128L);
        audio.setComposers("ComposersValue");
        audio.setCopyright("CopyrightValue");
        audio.setDisc((short) 1);
        audio.setDiscCount((short) 2);
        audio.setDuration(120L);
        audio.setGenre("GenreValue");
        audio.setHasDrm(true);
        audio.setVariableBitrate(true);
        audio.setTitle("TitleValue");
        audio.setTrack(3);
        audio.setTrackCount(12);
        audio.setYear(2000);
        return audio;
    }

    private static Video newVideo() {
        final Video video = new Video();
        video.setAudioBitsPerSample(16);
        video.setAudioChannels(2);
        video.setAudioFormat("AAC");
        video.setAudioSamplesPerSecond(44100);
        video.setBitrate(39101896);
        video.setDuration(8053L);
        video.setFourCC("H264");
        video.setFrameRate(23.9877D);
        video.setHeight(1280);
        video.setWidth(720);
        return video;
    }

    private static Image newImage() {
        final Image image = new Image();
        image.setHeight(200);
        image.setWidth(300);
        return image;
    }

    private static Photo newPhoto() {
        final Photo photo = new Photo();
        photo.setCameraMake("CameraMakeValue");
        photo.setIso(500);
        photo.setCameraModel("CameraModelValue");
        photo.setFNumber(4);
        photo.setFocalLength(3);
        photo.setExposureNumerator(150.0D);
        photo.setExposureDenominator(300.0D);
        photo.setTakenDateTime("TakenTimestampValue");
        return photo;
    }

    private static GeoCoordinates newGeoCoordinates() {
        final GeoCoordinates location = new GeoCoordinates();
        location.setAltitude(500.0D);
        location.setLatitude(100.0D);
        location.setLongitude(200.0D);
        return location;
    }

    private static SearchResult newSearchResult() {
        final SearchResult result = new SearchResult();
        result.setOnClickTelemetryUrl("TelemetryUrlValue");
        return result;
    }

    private static RemoteItem newRemoteItem() {
        final RemoteItem item = new RemoteItem();
        item.set_package(newPackage());
        item.setId("RemoteIdValue");
        item.setFolder(newFolder());
        item.setFile(newFile());
        item.setSize(2048L);
        item.setCreatedBy(newIdentitySet());
        item.setCreatedDateTime("CreatedTimestampValue");
        item.setFileSystemInfo(newFileSystemInfo());
        item.setLastModifiedBy(newIdentitySet());
        item.setLastModifiedDateTime("LastModifiedTimestampValue");
        item.setParentReference(newParentReference());
        item.setShared(newShared());
        item.setSharepointIds(newSharePointIds());
        item.setSpecialFolder(newSpecialFolder());
        item.setWebDavUrl("WebDavUrlValue");
        item.setWebUrl("WebUrlValue");
        return item;
    }

    private static SpecialFolder newSpecialFolder() {
        final SpecialFolder folder = new SpecialFolder();
        folder.setName("SpecialFolderName");
        return folder;
    }

    private static SharePointIds newSharePointIds() {
        final SharePointIds ids = new SharePointIds();
        ids.setListId("ListIdValue");
        ids.setSiteId("SiteIdValue");
        ids.setSiteUrl("SiteUrlValue");
        ids.setListItemId("ListItemIdValue");
        ids.setTenantId("TenantIdValue");
        ids.setWebId("WebIdValue");
        ids.setListItemUniqueId("ListItemUniqueIdValue");
        return ids;
    }

    private static Shared newShared() {
        final Shared shared = new Shared();
        shared.setSharedBy(newIdentitySet());
        shared.setOwner(newIdentitySet());
        shared.setScope("users");
        shared.setSharedDateTime("SharedTimestampValue");
        return shared;
    }

    private static Package newPackage() {
        final Package p = new Package();
        p.setType("PackageTypeValue");
        return p;
    }

    private static SharingInvitation newSharingInvitation() {
        final SharingInvitation invitation = new SharingInvitation();
        invitation.setInvitedBy(newIdentitySet());
        invitation.setEmail("emailValue");
        invitation.setSignInRequired(true);
        return invitation;
    }
    private static SharingLink newSharingLink() {
        final SharingLink link = new SharingLink();
        link.setApplication(newIdentity());
        link.setType("view");
        link.setScope("anonymous");
        link.setWebUrl("webUrlValue");
        link.setWebHtml("webHtmlValue");
        return link;
    }

    // Note: Not all attributes are set in a real-world scenario. Populating all of the attributes
    // for test coverage
    private static ItemActionSet newItemActionSet() {
        final ItemActionSet set = new ItemActionSet();
        set.setComment(newCommentAction());
        set.setCreate(new Object());
        set.setDelete(newDeleteAction());
        set.setEdit(new Object());
        set.setMention(newMentionAction());
        set.setMove(newMoveAction());
        set.setRename(newRenameAction());
        set.setRestore(new Object());
        set.setShare(newShareAction());
        set.setVersion(newVersionAction());
        return set;
    }

    private static VersionAction newVersionAction() {
        final VersionAction action = new VersionAction();
        action.setNewVersion("NewVersion");
        return action;
    }

    private static ShareAction newShareAction() {
        final ShareAction action = new ShareAction();
        action.setRecipients(List.of(newIdentitySet()));
        return action;
    }

    private static MoveAction newMoveAction() {
        final MoveAction action = new MoveAction();
        action.setFrom("FromValue");
        action.setTo("ToValue");
        return action;
    }

    private static RenameAction newRenameAction() {
        final RenameAction action = new RenameAction();
        action.setNewName("NewNameValue");
        action.setOldName("OldNameValue");
        return action;
    }

    private static MentionAction newMentionAction() {
        final MentionAction action = new MentionAction();
        action.setMentionees(List.of(newIdentitySet()));
        return action;
    }

    private static DeleteAction newDeleteAction() {
        final DeleteAction action = new DeleteAction();
        action.setName("DeleteActionName");
        action.setObjectType("File");
        return action;
    }

    private static CommentAction newCommentAction() {
        final CommentAction action = new CommentAction();
        action.setReply(false);
        action.setParentAuthor(newIdentitySet());
        action.setParticipants(List.of(newIdentitySet()));
        return action;
    }

    private static Quota newQuota() {
        final Quota quota = new Quota();
        quota.setUsed(1000L);
        quota.setRemaining(8000L);
        quota.setState("normal");
        quota.setTotal(10000L);
        quota.setDeleted(1000L);
        return quota;
    }


    private static PublicationFacet newPublicationFacet(final String versionId) {
        final PublicationFacet facet = new PublicationFacet();
        facet.setLevel("published");
        facet.setVersionId(versionId);
        return facet;
    }

    private static IdentitySet newIdentitySet() {
        final IdentitySet set = new IdentitySet();
        set.setUser(newIdentity());
        return set;
    }

    private static Identity newIdentity() {
        final Identity id = new Identity();
        id.setId("IdentityId");
        id.setDisplayName("IdentityName");
        id.setThumbnails(newThumbnailSet());
        return id;
    }



    private static Thumbnail newThumbnail(final String urlPrefix) {
        final Thumbnail thumbnail = new Thumbnail();
        thumbnail.setHeight(100);
        thumbnail.setUrl(urlPrefix + "ThumbnailUrlValue");
        thumbnail.setWidth(100);
        thumbnail.setSourceItemId("SourceItemIdValue");
        return thumbnail;
    }

    private static Folder newFolder() {
        final Folder folder = new Folder();
        folder.setFolderView(newFolderView());
        folder.setChildCount(3);
        return folder;
    }

    private static FolderView newFolderView() {
        final FolderView folderView = new FolderView();
        folderView.setViewType("details");
        folderView.setSortOrder("ascending");
        folderView.setSortBy("name");
        return folderView;
    }

    private static File newFile() {
        final File fileContents = new File();
        fileContents.setHashes(newHashes());
        fileContents.setMimeType("application/zip");
        fileContents.setProcessingMetadata(false);
        return fileContents;
    }

    private static ItemReference newParentReference() {
        final ItemReference ref = new ItemReference();
        ref.setId("ParentIdValue");
        ref.setName("ParentFolderName");
        ref.setPath("ParentFolderPath");
        ref.setDriveId("DriveIdValue");
        ref.setDriveType("personal");
        return ref;
    }

    private static FileSystemInfo newFileSystemInfo() {
        final FileSystemInfo info = new FileSystemInfo();
        info.setCreatedDateTime("CreatedTimeStampValue");
        info.setLastAccessedDateTime("LastModifiedTimeStampValue");
        info.setLastAccessedDateTime("LastAccessedTimeStampValue");
        return info;
    }

    private static Hashes newHashes() {
        final Hashes hashes = new Hashes();
        hashes.setCrc32Hash("crcHashValue");
        hashes.setSha1Hash("sha1Value");
        hashes.setQuickXorHash("quickXorHashValue");
        return hashes;
    }
}
