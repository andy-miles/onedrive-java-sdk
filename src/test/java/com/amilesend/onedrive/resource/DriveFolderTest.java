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

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.file.LogProgressCallback;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.AsyncJob;
import com.amilesend.onedrive.resource.item.DriveItem;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import com.amilesend.onedrive.resource.item.type.Deleted;
import com.amilesend.onedrive.resource.item.type.FileSystemInfo;
import com.amilesend.onedrive.resource.item.type.Folder;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import com.amilesend.onedrive.resource.item.type.Package;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.RemoteItem;
import com.amilesend.onedrive.resource.item.type.SpecialFolder;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.amilesend.onedrive.resource.DriveFileTest.newMockFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DriveFolderTest {
    @Mock
    private DriveItem mockDelegate;
    @InjectMocks
    private DriveFolder driveFolderUnderTest;

    ////////////////////
    // DriveItemType
    ////////////////////

    @Test
    public void getCreatedBy_shouldReturnIdentifySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getCreatedBy()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getCreatedBy());
    }

    @Test
    public void getCreatedDateTime_shouldReturnTimestamp() {
        final String expected = "CreatedTimestampValue";
        when(mockDelegate.getCreatedDateTime()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getCreatedDateTime());
    }

    @Test
    public void getETag_shouldReturnETag() {
        final String expected = "ETagValue";
        when(mockDelegate.getETag()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getETag());
    }

    @Test
    public void getId_shouldReturnId() {
        final String expected = "DriveItemId";
        when(mockDelegate.getId()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getId());
    }

    @Test
    public void getLastModifiedBy_shouldReturnIdentitySet() {
        final IdentitySet expected = mock(IdentitySet.class);
        when(mockDelegate.getLastModifiedBy()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getLastModifiedBy());
    }

    @Test
    public void getLastModifiedDateTime_shouldReturnTimestamp() {
        final String expected = "LastModifiedTimestampValue";
        when(mockDelegate.getLastModifiedDateTime()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getLastModifiedDateTime());
    }

    @Test
    public void getName_shouldReturnName() {
        final String expected = "NameValue";
        when(mockDelegate.getName()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getName());
    }

    @Test
    public void getParentReference_shouldReturnItemReference() {
        final ItemReference expected = mock(ItemReference.class);
        when(mockDelegate.getParentReference()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getParentReference());
    }

    @Test
    public void isDeleted_whenDeletedDefined_shouldReturnTrue() {
        when(mockDelegate.getDeleted()).thenReturn(mock(Deleted.class));
        assertTrue(driveFolderUnderTest.isDeleted());
    }

    @Test
    public void isDeleted_whenDeletedNotDefined_shouldReturnFalse() {
        when(mockDelegate.getDeleted()).thenReturn(null);
        assertFalse(driveFolderUnderTest.isDeleted());
    }

    @Test
    public void isRemote_whenRemoteItemDefined_shouldReturnTrue() {
        when(mockDelegate.getRemoteItem()).thenReturn(mock(RemoteItem.class));
        assertTrue(driveFolderUnderTest.isRemote());
    }

    @Test
    public void isRemote_whenRemoteItemNotDefined_shouldReturnFalse() {
        when(mockDelegate.getRemoteItem()).thenReturn(null);
        assertFalse(driveFolderUnderTest.isRemote());
    }

    @Test
    public void getRemoteItem_shouldReturnRemoteItem() {
        final RemoteItem expected = mock(RemoteItem.class);
        when(mockDelegate.getRemoteItem()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getRemoteItem());
    }

    @Test
    public void getActivities_shouldReturnItemActivityList() {
        final List<ItemActivity> expected = List.of(mock(ItemActivity.class), mock(ItemActivity.class));
        when(mockDelegate.getActivities()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getActivities());
    }

    @Test
    public void getPermissions_shouldReturnPermissionList() {
        final List<Permission> expected = List.of(mock(Permission.class), mock(Permission.class));
        when(mockDelegate.getPermissions()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getPermissions());
    }

    @Test
    public void getThumbnails_shouldReturnThumbnailSetList() {
        final List<ThumbnailSet> expected = List.of(mock(ThumbnailSet.class), mock(ThumbnailSet.class));
        when(mockDelegate.getThumbnails()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getThumbnails());
    }

    @Test
    public void getVersions_shouldReturnDriveItemVersionList() {
        final List<DriveItemVersion> expected = List.of(mock(DriveItemVersion.class), mock(DriveItemVersion.class));
        when(mockDelegate.getVersions()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getVersions());
    }

    @Test
    public void isFile_withFolderSet_shouldReturnTrue() {
        when(mockDelegate.getFolder()).thenReturn(mock(Folder.class));
        assertAll(
                () -> assertTrue(driveFolderUnderTest.isFolder()),
                () -> assertFalse(driveFolderUnderTest.isFile()));
    }

    @Test
    public void getFileSystemInfo_shouldReturnFileSystemInfo() {
        final FileSystemInfo expected = mock(FileSystemInfo.class);
        when(mockDelegate.getFileSystemInfo()).thenReturn(expected);

        final FileSystemInfo actual = driveFolderUnderTest.getFileSystemInfo();

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).getFileSystemInfo());
    }

    @Test
    public void setFileSystemInfo_withValidValue_shouldSetInDelegate() {
        doNothing().when(mockDelegate).setFileSystemInfo(any(FileSystemInfo.class));
        final FileSystemInfo expected = mock(FileSystemInfo.class);

        driveFolderUnderTest.setFileSystemInfo(expected);

        verify(mockDelegate).setFileSystemInfo(eq(expected));
    }

    @Test
    public void setFileSystemInfo_withNullValue_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFolderUnderTest.setFileSystemInfo(null));
    }

    ////////////////////
    // DriveFolder
    ////////////////////

    @Test
    public void isRoot_whenRootDefined_shouldReturnTrue() {
        when(mockDelegate.getRoot()).thenReturn(new Object());
        assertTrue(driveFolderUnderTest.isRoot());
    }

    @Test
    public void isRoot_whenNoRootDefined_shouldReturnFalse() {
        assertFalse(driveFolderUnderTest.isRoot());
    }

    @Test
    public void getFolderAttributes_shouldReturnFolder() {
        final Folder expected = mock(Folder.class);
        when(mockDelegate.getFolder()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getFolderAttributes());
    }

    @Test
    public void isSpecialFolder_whenSpecialFolderDefined_shouldReturnTrue() {
        when(mockDelegate.getSpecialFolder()).thenReturn(mock(SpecialFolder.class));
        assertTrue(driveFolderUnderTest.isSpecialFolder());
    }

    @Test
    public void isSpecialFolder_whenNoSpecialFolderDefined_shouldReturnFalse() {
        assertFalse(driveFolderUnderTest.isSpecialFolder());
    }

    @Test
    public void getSpecialFolder_shouldReturnSpecialFolder() {
        final SpecialFolder expected = mock(SpecialFolder.class);
        when(mockDelegate.getSpecialFolder()).thenReturn(expected);
        assertEquals(expected, driveFolderUnderTest.getSpecialFolder());
    }

    @Test
    public void toString_shouldReturnStringValue() {
        when(mockDelegate.getName()).thenReturn("FolderName");
        when(mockDelegate.getId()).thenReturn("FolderId");
        when(mockDelegate.getRoot()).thenReturn(new Object());

        assertEquals("DriveFolder(name=FolderName, id=FolderId, isRoot=true, " +
                        "isSpecialFolder=false, isDeleted=false, isRemote=false)",
                driveFolderUnderTest.toString());
    }

    //////////////////////
    // Upload
    //////////////////////

    @SneakyThrows
    @Test
    public void upload_withFile_shouldReturnDriveFile() {
        final DriveItem uploadedItem = mock(DriveItem.class);
        when(uploadedItem.getId()).thenReturn("UploadedFileId");
        when(mockDelegate.uploadNew(any(File.class), any(TransferProgressCallback.class))).thenReturn(uploadedItem);
        final File mockFile = newMockFile();

        final DriveFile actual = driveFolderUnderTest.upload(mockFile);

        assertAll(
                () -> assertEquals("UploadedFileId", actual.getId()),
                () -> verify(mockDelegate).uploadNew(eq(mockFile), isA(LogProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void upload_withFileAndCallback_shouldReturnDriveFile() {
        final DriveItem uploadedItem = mock(DriveItem.class);
        when(uploadedItem.getId()).thenReturn("UploadedFileId");
        when(mockDelegate.uploadNew(any(File.class), any(TransferProgressCallback.class))).thenReturn(uploadedItem);
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final DriveFile actual = driveFolderUnderTest.upload(mockFile, mockCallback);

        assertAll(
                () -> assertEquals("UploadedFileId", actual.getId()),
                () -> verify(mockDelegate).uploadNew(eq(mockFile), eq(mockCallback)));
    }

    @SneakyThrows
    @Test
    public void upload_withIOException_shouldThrowException() {
        when(mockDelegate.uploadNew(any(File.class), any(TransferProgressCallback.class)))
                .thenThrow(new IOException("Exception"));
        final File mockFile = newMockFile();

        assertAll(
                () -> assertThrows(IOException.class, () -> driveFolderUnderTest.upload(mockFile)),
                () -> assertThrows(IOException.class,
                        () -> driveFolderUnderTest.upload(mockFile, mock(TransferProgressCallback.class))));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withFile_shouldReturnExecution() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(mockUploadedDriveItem);
        when(mockDelegate.uploadNewAsync(any(File.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);
        final File mockFile = newMockFile();

        final DriveFileUploadExecution actual = driveFolderUnderTest.uploadAsync(mockFile);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.get().getId()),
                () -> verify(mockDelegate).uploadNewAsync(eq(mockFile), isA(LogProgressCallback.class)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withFileAndCallback_shouldReturnExecution() {
        final DriveItem mockUploadedDriveItem = mock(DriveItem.class);
        when(mockUploadedDriveItem.getId()).thenReturn("UploadedDriveItemId");
        final CompletableFuture<DriveItem> mockFuture = mock(CompletableFuture.class);
        when(mockFuture.get()).thenReturn(mockUploadedDriveItem);
        when(mockDelegate.uploadNewAsync(any(File.class), any(TransferProgressCallback.class))).thenReturn(mockFuture);
        final File mockFile = mock(File.class);
        final TransferProgressCallback mockCallback = mock(TransferProgressCallback.class);

        final DriveFileUploadExecution actual = driveFolderUnderTest.uploadAsync(mockFile, mockCallback);

        assertAll(
                () -> assertEquals("UploadedDriveItemId", actual.get().getId()),
                () -> verify(mockDelegate).uploadNewAsync(eq(mockFile), eq(mockCallback)));
    }

    @SneakyThrows
    @Test
    public void uploadAsync_withIOException_shouldThrowException() {
        when(mockDelegate.uploadNewAsync(any(File.class), any(TransferProgressCallback.class)))
                .thenThrow(new IOException("Exception"));
        final File mockFile = newMockFile();

        assertAll(
                () -> assertThrows(IOException.class, () -> driveFolderUnderTest.uploadAsync(mockFile)),
                () -> assertThrows(IOException.class,
                        () -> driveFolderUnderTest.uploadAsync(mockFile, mock(TransferProgressCallback.class))));
    }

    //////////////////////
    // Operations
    //////////////////////

    @Test
    public void addPermission_withRequest_shouldReturnListOfPermissions() {
        final List<Permission> expected = List.of(mock(Permission.class));
        when(mockDelegate.addPermission(any(AddPermissionRequest.class))).thenReturn(expected);
        final AddPermissionRequest request = mock(AddPermissionRequest.class);

        final List<Permission> actual = driveFolderUnderTest.addPermission(request);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).addPermission(eq(request)));
    }

    @Test
    public void createSharingLink_withRequest_shouldReturnPermission() {
        final Permission expected = mock(Permission.class);
        when(mockDelegate.createSharingLink(any(CreateSharingLinkRequest.class))).thenReturn(expected);
        final CreateSharingLinkRequest request = mock (CreateSharingLinkRequest.class);

        final Permission actual = driveFolderUnderTest.createSharingLink(request);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockDelegate).createSharingLink(eq(request)));
    }

    @Test
    public void createFolder_withValidName_shouldReturnDriveFolder() {
        final DriveItem createdDriveItem = mock(DriveItem.class);
        when(createdDriveItem.getId()).thenReturn("CreatedFolderId");
        when(mockDelegate.create(any(DriveItem.class))).thenReturn(createdDriveItem);
        when(mockDelegate.getConnection()).thenReturn(mock(OneDriveConnection.class));

        final DriveFolder actual = driveFolderUnderTest.createFolder("NewFolderName");

        final ArgumentCaptor<DriveItem> attributesCaptor = ArgumentCaptor.forClass(DriveItem.class);
        assertAll(
                () -> assertEquals("CreatedFolderId", actual.getId()),
                () -> verify(mockDelegate).create(attributesCaptor.capture()),
                () -> assertEquals("NewFolderName", attributesCaptor.getValue().getName()),
                () -> assertNotNull(attributesCaptor.getValue().getFolder()));
    }

    @Test
    public void createFolder_withInvalidName_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> driveFolderUnderTest.createFolder(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> driveFolderUnderTest.createFolder(StringUtils.EMPTY)));
    }

    @Test
    public void getChildPackages_shouldReturnDrivePackageList() {
        setUpChildren();

        final List<DrivePackage> actual = driveFolderUnderTest.getChildPackages();

        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals("PackageId", actual.get(0).getId()));
    }

    @Test
    public void getChildFolders_shouldReturnDriveFolderList() {
        setUpChildren();

        final List<DriveFolder> actual = driveFolderUnderTest.getChildFolders();

        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals("FolderId", actual.get(0).getId()));
    }

    @Test
    public void getChildFiles_shouldReturnDriveFileList() {
        setUpChildren();

        final List<DriveFile> actual = driveFolderUnderTest.getChildFiles();

        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals("FileId", actual.get(0).getId()));
    }

    @Test
    public void getChildren_shouldReturnDriveItemTypeList() {
        setUpChildren();

        final List<? extends DriveItemType> actual = driveFolderUnderTest.getChildren();

        assertAll(
                () -> assertEquals(3, actual.size()),
                () -> assertEquals("FileId", actual.get(0).getId()),
                () -> assertEquals("FolderId", actual.get(1).getId()),
                () -> assertEquals("PackageId", actual.get(2).getId()));
    }

    @Test
    public void search_withQuery_shouldReturnDriveItemTypeList() {
        setUpSearch();

        final List<? extends DriveItemType> actual = driveFolderUnderTest.search("Query");

        assertAll(
                () -> assertEquals(3, actual.size()),
                () -> assertEquals("FileId", actual.get(0).getId()),
                () -> assertEquals("FolderId", actual.get(1).getId()),
                () -> assertEquals("PackageId", actual.get(2).getId()));
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

        final AsyncJob actual = driveFolderUnderTest.copy(mockFolder, newName);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(mockStatus, actual.getStatus()),
                () -> verify(mockDelegate).copy(eq("FolderId"), eq(newName)));
    }

    @Test
    public void copy_withNullFolderAndName_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFolderUnderTest.copy((DriveFolder) null, "NewName"));
    }

    @Test
    public void copy_withFolderOnly_shouldReturnAsyncJob() {
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("FolderId");
        final AsyncJobStatus mockStatus = mock(AsyncJobStatus.class);
        final AsyncJob expected = mock(AsyncJob.class);
        when(expected.getStatus()).thenReturn(mockStatus);
        doReturn(expected).when(mockDelegate).copy(anyString(), anyString());

        final AsyncJob actual = driveFolderUnderTest.copy(mockFolder);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(mockStatus, actual.getStatus()),
                () -> verify(mockDelegate).copy(eq("FolderId"), eq(StringUtils.EMPTY)));
    }

    @Test
    public void copy_withNullFolderOnly_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFolderUnderTest.copy((DriveFolder) null));
    }

    @Test
    public void copy_withNewNameOnly_shouldReturnAsyncJob() {
        final AsyncJobStatus mockStatus = mock(AsyncJobStatus.class);
        final AsyncJob expected = mock(AsyncJob.class);
        when(expected.getStatus()).thenReturn(mockStatus);
        doReturn(expected).when(mockDelegate).copy(anyString(), anyString());

        final AsyncJob actual = driveFolderUnderTest.copy("NewNameValue");

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

        final DriveFolder actual = driveFolderUnderTest.update();

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

        final DriveFolder actual = driveFolderUnderTest.move(mockFolder, "NewName");

        assertAll(
                () -> assertEquals("ItemId", actual.getId()),
                () -> verify(mockDelegate).move(eq("DestinationId"), eq("NewName")));
    }

    @Test
    public void move_withNullFolderAndName_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> driveFolderUnderTest.move((DriveFolder) null, "Name"));
    }

    @Test
    public void move_withFolderOnly_shouldReturnDriveFile() {
        final DriveItem updatedDriveItem = mock(DriveItem.class);
        when(updatedDriveItem.getId()).thenReturn("ItemId");
        when(mockDelegate.move(anyString(), anyString())).thenReturn(updatedDriveItem);
        final DriveFolder mockFolder = mock(DriveFolder.class);
        when(mockFolder.getId()).thenReturn("DestinationId");

        final DriveFolder actual = driveFolderUnderTest.move(mockFolder);

        assertAll(
                () -> assertEquals("ItemId", actual.getId()),
                () -> verify(mockDelegate).move(eq("DestinationId"), eq(StringUtils.EMPTY)));
    }

    @Test
    public void move_withNullFolderOnly_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->driveFolderUnderTest.move(null));
    }

    @Test
    public void delete_shouldInvokeDelegate() {
        driveFolderUnderTest.delete();
        verify(mockDelegate).delete();
    }

    private void setUpChildren() {
        final List<DriveItem> children = newDriveItemList();
        when(mockDelegate.getChildren()).thenReturn(children);
    }

    private void setUpSearch() {
        final List<DriveItem> searchResults = newDriveItemList();
        when(mockDelegate.search(anyString())).thenReturn(searchResults);
    }

    private List<DriveItem> newDriveItemList() {
        final DriveItem mockFile = mock(DriveItem.class);
        lenient().when(mockFile.getFile()).thenReturn(mock(com.amilesend.onedrive.resource.item.type.File.class));
        lenient().when(mockFile.getId()).thenReturn("FileId");

        final DriveItem mockFolder = mock(DriveItem.class);
        lenient().when(mockFolder.getFolder()).thenReturn(mock(Folder.class));
        lenient().when(mockFolder.getId()).thenReturn("FolderId");

        final DriveItem mockPackage = mock(DriveItem.class);
        lenient().when(mockPackage.get_package()).thenReturn(mock(Package.class));
        lenient().when(mockPackage.getId()).thenReturn("PackageId");

        return List.of(mockFile, mockFolder, mockPackage);
    }

    //////////////////////
    // equalsAndHashCode
    //////////////////////

    @Test
    public void equalsAndHashCode_withSameDriveFile_shouldReturnTrue() {
        assertAll(
                () -> assertTrue(driveFolderUnderTest.equals(driveFolderUnderTest)),
                () -> assertEquals(driveFolderUnderTest.hashCode(), driveFolderUnderTest.hashCode()));
    }

    @Test
    public void equalsAndHashCode_withDriveFile_shouldReturnFalse() {
        final DriveItem driveItemFile = mock(DriveItem.class);
        final DriveFile file = new DriveFile(driveItemFile);

        assertAll(
                () -> assertFalse(driveFolderUnderTest.equals(file)),
                () -> assertNotEquals(driveFolderUnderTest.hashCode(), file.hashCode()));
    }

    @Test
    public void equalsAndHashCode_withInequality_shouldReturnFalse() {
        final DriveItem differentFolderDelegate = mock(DriveItem.class);
        final DriveFolder differentFolder = new DriveFolder(differentFolderDelegate);

        assertAll(
                () -> assertFalse(driveFolderUnderTest.equals(differentFolder)),
                () -> assertNotEquals(driveFolderUnderTest.hashCode(), differentFolder.hashCode()));
    }
}
