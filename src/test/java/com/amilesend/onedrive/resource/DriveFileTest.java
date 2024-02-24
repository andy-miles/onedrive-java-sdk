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

import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.AsyncJob;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import com.amilesend.onedrive.resource.item.type.Audio;
import com.amilesend.onedrive.resource.item.type.Deleted;
import com.amilesend.onedrive.resource.item.type.FileSystemInfo;
import com.amilesend.onedrive.resource.item.type.GeoCoordinates;
import com.amilesend.onedrive.resource.item.type.Image;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.Photo;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.RemoteItem;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.item.type.Video;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import com.amilesend.onedrive.resource.request.PreviewRequest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveFileTest {
    @Mock
    private DriveItem mockDelegate;
    @InjectMocks
    private DriveFile driveFileUnderTest;

    ////////////////////
    // DriveItemType
    ////////////////////

    @Test
    public void getCreatedBy_shouldReturnIdentifySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getCreatedBy()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getCreatedBy());
    }

    @Test
    public void getCreatedDateTime_shouldReturnTimestamp() {
        final String expected = "CreatedTimestampValue";
        when(mockDelegate.getCreatedDateTime()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getCreatedDateTime());
    }

    @Test
    public void getETag_shouldReturnETag() {
        final String expected = "ETagValue";
        when(mockDelegate.getETag()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getETag());
    }

    @Test
    public void getId_shouldReturnId() {
        final String expected = "DriveItemId";
        when(mockDelegate.getId()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getId());
    }

    @Test
    public void getLastModifiedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getLastModifiedBy()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getLastModifiedBy());
    }

    @Test
    public void getLastModifiedDateTime_shouldReturnTimestamp() {
        final String expected = "LastModifiedTimestampValue";
        when(mockDelegate.getLastModifiedDateTime()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getLastModifiedDateTime());
    }

    @Test
    public void getName_shouldReturName() {
        final String expected = "NameValue";
        when(mockDelegate.getName()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getName());
    }

    @Test
    public void getParentReference_shouldReturnItemReference() {
        final ItemReference expected = mock(ItemReference.class);
        when(mockDelegate.getParentReference()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getParentReference());
    }

    @Test
    public void isDeleted_whenDeletedDefined_shouldReturnTrue() {
        when(mockDelegate.getDeleted()).thenReturn(mock(Deleted.class));
        assertTrue(driveFileUnderTest.isDeleted());
    }

    @Test
    public void isDeleted_whenDeletedNotDefined_shouldReturnFalse() {
        when(mockDelegate.getDeleted()).thenReturn(null);
        assertFalse(driveFileUnderTest.isDeleted());
    }

    @Test
    public void isRemote_whenRemoteItemDefined_shouldReturnTrue() {
        when(mockDelegate.getRemoteItem()).thenReturn(mock(RemoteItem.class));
        assertTrue(driveFileUnderTest.isRemote());
    }

    @Test
    public void isRemote_whenRemoteItemNotDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isRemote());
    }

    @Test
    public void getRemoteItem_shouldReturnRemoteItem() {
        final RemoteItem expected = mock(RemoteItem.class);
        when(mockDelegate.getRemoteItem()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getRemoteItem());
    }

    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final List<ItemActivity> expected = List.of(mock(ItemActivity.class), mock(ItemActivity.class));
        when(mockDelegate.getActivities()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getActivities());
    }

    @Test
    public void getPermissions_shouldReturnPermissionList() {
        final List<Permission> expected = List.of(mock(Permission.class), mock(Permission.class));
        when(mockDelegate.getPermissions()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getPermissions());
    }

    @Test
    public void getThumbnails_shouldReturnThumbnailSetList() {
        final List<ThumbnailSet> expected = List.of(mock(ThumbnailSet.class), mock(ThumbnailSet.class));
        when(mockDelegate.getThumbnails()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getThumbnails());
    }

    @Test
    public void getVersions_shouldReturnDriveItemVersionList() {
        final List<DriveItemVersion> expected = List.of(mock(DriveItemVersion.class), mock(DriveItemVersion.class));
        when(mockDelegate.getVersions()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getVersions());
    }

    @Test
    public void isFile_withFolderNotSet_shouldReturnTrue() {
        when(mockDelegate.getFolder()).thenReturn(null);
        assertAll(
                () -> assertTrue(driveFileUnderTest.isFile()),
                () -> assertFalse(driveFileUnderTest.isFolder()));
    }

    @Test
    public void getFileSystemInfo_shouldReturnFileSystemInfo() {
        final FileSystemInfo expected = mock(FileSystemInfo.class);
        when(mockDelegate.getFileSystemInfo()).thenReturn(expected);

        final FileSystemInfo actual = driveFileUnderTest.getFileSystemInfo();

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).getFileSystemInfo());
    }

    @Test
    public void setFileSystemInfo_withValidValue_shouldSetInDelegate() {
        doNothing().when(mockDelegate).setFileSystemInfo(any(FileSystemInfo.class));
        final FileSystemInfo expected = mock(FileSystemInfo.class);

        driveFileUnderTest.setFileSystemInfo(expected);

        verify(mockDelegate).setFileSystemInfo(eq(expected));
    }

    @Test
    public void setFileSystemInfo_withNullValue_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFileUnderTest.setFileSystemInfo(null));
    }

    ////////////////////
    // DriveFile
    ////////////////////

    @Test
    public void isAudio_withAudioDefined_shouldReturnTrue() {
        when(mockDelegate.getAudio()).thenReturn(mock(Audio.class));
        assertTrue(driveFileUnderTest.isAudio());
    }

    @Test
    public void isAudio_withNoAudioDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isAudio());
    }

    @Test
    public void getAudioAttributes_shouldReturnAudio() {
        final Audio expected = mock(Audio.class);
        when(mockDelegate.getAudio()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getAudioAttributes());
    }

    @Test
    public void isImage_withImageDefined_shouldReturnTrue() {
        when(mockDelegate.getImage()).thenReturn(mock(Image.class));
        assertTrue(driveFileUnderTest.isImage());
    }

    @Test
    public void isImage_withNoImageDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isImage());
    }

    @Test
    public void getImageAttributes_shouldReturnImage() {
        final Image expected = mock(Image.class);
        when(mockDelegate.getImage()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getImageAttributes());
    }

    @Test
    public void isLocationAvailable_withLocationDefined_shouldReturnTrue() {
        when(mockDelegate.getLocation()).thenReturn(mock(GeoCoordinates.class));
        assertTrue(driveFileUnderTest.isLocationAvailable());
    }

    @Test
    public void isLocationAvailable_withNoLocationDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isLocationAvailable());
    }

    @Test
    public void getLocationAttributes_shouldReturnGeoCoordinates() {
        final GeoCoordinates expected = mock(GeoCoordinates.class);
        when(mockDelegate.getLocation()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getLocationAttributes());
    }

    @Test
    public void isPhoto_withPhotoDefined_shouldReturnTrue() {
        when(mockDelegate.getPhoto()).thenReturn(mock(Photo.class));
        assertTrue(driveFileUnderTest.isPhoto());
    }

    @Test
    public void isPhoto_withNoPhotoDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isPhoto());
    }

    @Test
    public void getPhotoAttributes_shouldReturnPhoto() {
        final Photo expected = mock(Photo.class);
        when(mockDelegate.getPhoto()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getPhotoAttributes());
    }

    @Test
    public void isVideo_withVideoDefined_shouldReturnTrue() {
        when(mockDelegate.getVideo()).thenReturn(mock(Video.class));
        assertTrue(driveFileUnderTest.isVideo());
    }

    @Test
    public void isVideo_withNoVideoDefined_shouldReturnFalse() {
        assertFalse(driveFileUnderTest.isVideo());
    }

    @Test
    public void getVideoAttributes_shouldReturnVideo() {
        final Video expected = mock(Video.class);
        when(mockDelegate.getVideo()).thenReturn(expected);
        assertEquals(expected, driveFileUnderTest.getVideoAttributes());
    }

    @Test
    public void toString_shouldReturnStringValue() {
        when(mockDelegate.getName()).thenReturn("FileName");
        when(mockDelegate.getId()).thenReturn("FileId");

        assertEquals("DriveFile(name=FileName, id=FileId, isDeleted=false, " +
                        "isRemote=false, isAudio=false, isImage=false, isVideo=false)",
                driveFileUnderTest.toString());
    }

    //////////////////////
    // Download
    //////////////////////

    @Test
    public void download_withFolderPath_shouldInvokeDelegate() {
        final Path mockFolderPath = newMockFolderPath();

        driveFileUnderTest.download(mockFolderPath);

        verify(mockDelegate).download(eq(mockFolderPath), isA(LogProgressCallback.class));
    }

    @Test
    public void download_withFolderPathAndCallback_shouldInvokeDelegate() {
        final Path mockFolderPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        driveFileUnderTest.download(mockFolderPath, mockCallback);

        verify(mockDelegate).download(eq(mockFolderPath), eq(mockCallback));
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withFolderPath_shouldReturnExecution() {
        final Path mockFolderPath = newMockFolderPath();
        final CompletableFuture<Long> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(1024L);
        when(mockDelegate.downloadAsync(any(Path.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);

        final DriveFileDownloadExecution actual = driveFileUnderTest.downloadAsync(mockFolderPath);

        assertAll(
                () -> assertEquals(1024L, actual.get()),
                () -> verify(mockDelegate).downloadAsync(eq(mockFolderPath), isA(LogProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void downloadAsync_withFolderPathAndCallback_shouldReturnExecution() {
        final Path mockFolderPath = mock(Path.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);
        final CompletableFuture<Long> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(1024L);
        when(mockDelegate.downloadAsync(any(Path.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);

        final DriveFileDownloadExecution actual = driveFileUnderTest.downloadAsync(mockFolderPath, mockCallback);

        assertAll(
                () -> assertEquals(1024L, actual.get()),
                () -> verify(mockDelegate).downloadAsync(eq(mockFolderPath), eq(mockCallback)));
    }

    //////////////////////
    // Upload
    //////////////////////

    @SneakyThrows
    @Test
    public void upload_withFile_shouldReturnDriveFile() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        when(mockDelegate.upload(any(File.class), any(TransferProgressCallback.class)))
                .thenReturn(mockUploadedDriveItem);
        final File mockFile = newMockFile();

        final DriveFile actual = driveFileUnderTest.upload(mockFile);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.getId()),
                () -> verify(mockDelegate).upload(eq(mockFile), isA(LogProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void upload_withFileAndCallback_shouldReturnDriveFile() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        when(mockDelegate.upload(any(File.class), any(TransferProgressCallback.class)))
                .thenReturn(mockUploadedDriveItem);
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final DriveFile actual = driveFileUnderTest.upload(mockFile, mockCallback);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.getId()),
                () -> verify(mockDelegate).upload(eq(mockFile), eq(mockCallback)));
    }

    @SneakyThrows
    @Test
    public void upload_withIOException_shouldThrowException() {
        when(mockDelegate.upload(any(File.class), any(TransferProgressCallback.class)))
                .thenThrow(new IOException("Exception"));
        final File mockFile = newMockFile();

        assertAll(
                () -> assertThrows(IOException.class, () -> driveFileUnderTest.upload(mockFile)),
                () -> assertThrows(IOException.class,
                        () -> driveFileUnderTest.upload(mockFile, mock(TransferProgressCallback.class))));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withFile_shouldReturnExecution() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(mockUploadedDriveItem);
        when(mockDelegate.uploadAsync(any(File.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);
        final File mockFile = newMockFile();

        final DriveFileUploadExecution actual = driveFileUnderTest.uploadAsync(mockFile);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.get().getId()),
                () -> verify(mockDelegate).uploadAsync(eq(mockFile), isA(LogProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withFileAndCallback_shouldReturnExecution() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(mockUploadedDriveItem);
        when(mockDelegate.uploadAsync(any(File.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final DriveFileUploadExecution actual = driveFileUnderTest.uploadAsync(mockFile, mockCallback);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.get().getId()),
                () -> verify(mockDelegate).uploadAsync(eq(mockFile), eq(mockCallback)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withIOException_shouldThrowException() {
        when(mockDelegate.uploadAsync(any(File.class), any(TransferProgressCallback.class)))
                .thenThrow(new IOException("Exception"));
        final File mockFile = newMockFile();

        assertAll(
                () -> assertThrows(IOException.class, () -> driveFileUnderTest.uploadAsync(mockFile)),
                () -> assertThrows(IOException.class,
                        () -> driveFileUnderTest.uploadAsync(mockFile, mock(TransferProgressCallback.class))));
    }

    //////////////////////
    // Operations
    //////////////////////

    @Test
    public void addPermission_withRequest_shouldReturnListOfPermissions() {
        final List<Permission> expected = List.of(mock(Permission.class));
        when(mockDelegate.addPermission(any(AddPermissionRequest.class))).thenReturn(expected);
        final AddPermissionRequest request = mock(AddPermissionRequest.class);

        final List<Permission> actual = driveFileUnderTest.addPermission(request);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).addPermission(eq(request)));
    }

    @Test
    public void createSharingLink_withRequest_shouldReturnPermission() {
        final Permission expected = mock(Permission.class);
        when(mockDelegate.createSharingLink(any(CreateSharingLinkRequest.class))).thenReturn(expected);
        final CreateSharingLinkRequest request = mock (CreateSharingLinkRequest.class);

        final Permission actual = driveFileUnderTest.createSharingLink(request);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).createSharingLink(eq(request)));
    }

    @Test
    public void preview_withRequest_shouldReturnPreview() {
        final Preview expected = mock(Preview.class);
        when(mockDelegate.previewItem(any(PreviewRequest.class))).thenReturn(expected);
        final PreviewRequest request = mock(PreviewRequest.class);

        final Preview actual = driveFileUnderTest.preview(request);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).previewItem(eq(request)));
    }

    @Test
    public void copy_withFolderAndName_shouldReturnAsyncJob() {
        final String newName = "NewName";
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("FolderId");
        final AsyncJobStatus mockStatus = mock(AsyncJobStatus.class);
        final AsyncJob expected = mock(AsyncJob.class);
        when(expected.getStatus()).thenReturn(mockStatus);
        when(mockDelegate.copy(anyString(), anyString())).thenReturn(expected);

        final AsyncJob actual = driveFileUnderTest.copy(mockFolder, newName);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(mockStatus, actual.getStatus()),
                () -> verify(mockDelegate).copy(eq("FolderId"), eq(newName)));
    }

    @Test
    public void copy_withNullFolderAndName_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFileUnderTest.copy((DriveFolder) null, "NewName"));
    }

    @Test
    public void copy_withFolderOnly_shouldReturnAsyncJob() {
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("FolderId");
        final AsyncJobStatus mockStatus = mock(AsyncJobStatus.class);
        final AsyncJob expected = mock(AsyncJob.class);
        when(expected.getStatus()).thenReturn(mockStatus);
        doReturn(expected).when(mockDelegate).copy(anyString(), anyString());

        final AsyncJob actual = driveFileUnderTest.copy(mockFolder);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(mockStatus, actual.getStatus()),
                () -> verify(mockDelegate).copy(eq("FolderId"), eq(StringUtils.EMPTY)));
    }

    @Test
    public void copy_withNullFolderOnly_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFileUnderTest.copy((DriveFolder) null));
    }

    @Test
    public void copy_withNewNameOnly_shouldReturnAsyncJob() {
        final AsyncJobStatus mockStatus = mock(AsyncJobStatus.class);
        final AsyncJob expected = mock(AsyncJob.class);
        when(expected.getStatus()).thenReturn(mockStatus);
        doReturn(expected).when(mockDelegate).copy(anyString(), anyString());

        final AsyncJob actual = driveFileUnderTest.copy("NewNameValue");

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(mockStatus, actual.getStatus()),
                () -> verify(mockDelegate).copy(anyString(), eq("NewNameValue")));
    }

    @Test
    public void update_withFileSystemInfo_shouldReturnDriveFile() {
        final FileSystemInfo updatedFileSystemInfo = mock(FileSystemInfo.class);
        final DriveItem updatedDriveItem = mock(DriveItem.class);
        when(updatedDriveItem.getFileSystemInfo()).thenReturn(updatedFileSystemInfo);
        when(mockDelegate.update()).thenReturn(updatedDriveItem);

        final DriveFile actual = driveFileUnderTest.update();

        assertAll(
                () -> assertEquals(updatedFileSystemInfo, actual.getFileSystemInfo()),
                () -> verify(mockDelegate).update());
    }

    @Test
    public void move_withFolderAndName_shouldReturnDriveFile() {
        final DriveItem updatedDriveItem = mock(DriveItem.class);
        when(updatedDriveItem.getId()).thenReturn("ItemId");
        when(mockDelegate.move(anyString(), anyString())).thenReturn(updatedDriveItem);
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("DestinationId");

        final DriveFile actual = driveFileUnderTest.move(mockFolder, "NewName");

        assertAll(
                () -> assertEquals("ItemId", actual.getId()),
                () -> verify(mockDelegate).move(eq("DestinationId"), eq("NewName")));
    }

    @Test
    public void move_withNullFolderAndNewName_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFileUnderTest.move((DriveFolder) null, "Name"));
    }

    @Test
    public void move_withFolderOnly_shouldReturnDriveFile() {
        final DriveItem updatedDriveItem = mock(DriveItem.class);
        when(updatedDriveItem.getId()).thenReturn("ItemId");
        when(mockDelegate.move(anyString(), anyString())).thenReturn(updatedDriveItem);
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("DestinationId");

        final DriveFile actual = driveFileUnderTest.move(mockFolder);

        assertAll(
                () -> assertEquals("ItemId", actual.getId()),
                () -> verify(mockDelegate).move(eq("DestinationId"), eq(StringUtils.EMPTY)));
    }

    @Test
    public void move_withNullFolderOnly_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->driveFileUnderTest.move(null));
    }

    @Test
    public void delete_shouldInvokeDelegate() {
        driveFileUnderTest.delete();
        verify(mockDelegate).delete();
    }

    //////////////////////
    // equalsAndHashCode
    //////////////////////

    @Test
    public void equalsAndHashCode_withSameDriveFile_shouldReturnTrue() {
        assertAll(
                () -> assertTrue(driveFileUnderTest.equals(driveFileUnderTest)),
                () -> assertEquals(driveFileUnderTest.hashCode(), driveFileUnderTest.hashCode()));
    }

    @Test
    public void equalsAndHashCode_withDriveFolder_shouldReturnFalse() {
        final DriveItem driveItemFolder = mock(DriveItem.class);
        final DriveFolder folder = new DriveFolder(driveItemFolder);

        assertAll(
                () -> assertFalse(driveFileUnderTest.equals(folder)),
                () -> assertNotEquals(driveItemFolder.hashCode(), folder.hashCode()));
    }

    @Test
    public void equalsAndHashCode_withInequality_shouldReturnFalse() {
        final DriveItem differentFileDelegate = mock(DriveItem.class);
        final DriveFile differentFile = new DriveFile(differentFileDelegate);

        assertAll(
                () -> assertFalse(driveFileUnderTest.equals(differentFile)),
                () -> assertNotEquals(driveFileUnderTest.hashCode(), differentFile.hashCode()));
    }

    public static File newMockFile() {
        final File mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("FilenameValue");
        return mockFile;
    }

    public static Path newMockFolderPath() {
        final File mockFile = newMockFile();
        final Path mockFolderPath = mock(Path.class);
        when(mockFolderPath.toFile()).thenReturn(mockFile);

        return mockFolderPath;
    }
}
