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
package com.amilesend.onedrive.resource;

import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.DriveRecipient;
import com.amilesend.onedrive.resource.item.AsyncJob;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemFolder;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemVersion;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemZipFile;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newItemActivity;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPermission;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newThumbnailSet;
import static com.amilesend.onedrive.resource.DriveFileFunctionalTest.copyWithNameAndDriveItemId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DrivePackageFunctionalTest extends FunctionalTestBase {
    private DrivePackage drivePackageUnderTest;

    @BeforeEach
    public void configureDrivePackage() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ROOT_FOLDER);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_PACKAGE);
        drivePackageUnderTest = getOneDriveUnderTest()
                .getUserDrive()
                .getRootFolder()
                .getChildPackages()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to retrieve DrivePackage to test"));
    }

    //////////////////
    // upload
    //////////////////

    @SneakyThrows
    @Test
    public void upload_withFile_shouldReturnDriveFile(@TempDir final Path tempDir) {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ZIP_FILE);
        final DriveFile expected = new DriveFile(newDriveItemZipFile(getOneDriveConnection(), 1));

        final DriveFile actual = drivePackageUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK);

        assertEquals(expected, actual);
    }

    @Test
    public void upload_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class,
                () -> drivePackageUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    @Test
    public void upload_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class,
                () -> drivePackageUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    //////////////////
    // uploadAsync
    //////////////////

    @SneakyThrows
    @Test
    public void uploadAsync_withFile_shouldReturnDriveFile(@TempDir final Path tempDir) {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ZIP_FILE);
        final DriveFile expected = new DriveFile(newDriveItemZipFile(getOneDriveConnection(), 1));

        final DriveFile actual =
                drivePackageUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get();

        assertEquals(expected, actual);
    }

    @Test
    public void uploadAsync_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        final Throwable thrown = assertThrows(
                ExecutionException.class,
                () -> drivePackageUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get());
        assertInstanceOf(RequestException.class, thrown.getCause());
    }

    @Test
    public void uploadAsync_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        final Throwable thrown = assertThrows(
                ExecutionException.class,
                () -> drivePackageUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get());
        assertInstanceOf(ResponseException.class, thrown.getCause());
    }

    //////////////////
    // createFolder
    //////////////////

    @Test
    public void createFolder_withValidRequest_shouldReturnFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_FOLDER);
        final DriveFolder expected = new DriveFolder(newDriveItemFolder(getOneDriveConnection()));

        final DriveFolder actual = drivePackageUnderTest.createFolder("NewFolder");

        assertEquals(expected, actual);
    }

    @Test
    public void createFolder_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.createFolder("NewFolder"));
    }

    @Test
    public void createFolder_withServiceRepsonse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.createFolder("NewFolder"));
    }

    /////////////////////
    // getChildFolders
    /////////////////////

    @Test
    public void getChildFolders_withValidRequest_shouldReturnDriveFolderList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_LIST);
        final List<DriveFolder> expected = List.of(new DriveFolder(newDriveItemFolder(getOneDriveConnection())));

        final List<DriveFolder> actual = drivePackageUnderTest.getChildFolders();

        assertEquals(expected, actual);
    }

    @Test
    public void getChildFolders_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getChildFolders());
    }

    @Test
    public void getChildFolders_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getChildFolders());
    }

    /////////////////////
    // getChildFiles
    /////////////////////

    @Test
    public void getChildFiles_withValidRequest_shouldReturnDriveFileList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_LIST);
        final List<DriveFile> expected = List.of(new DriveFile(newDriveItemZipFile(getOneDriveConnection(), 1)));

        final List<DriveFile> actual = drivePackageUnderTest.getChildFiles();

        assertEquals(expected, actual);
    }

    @Test
    public void getChildFiles_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getChildFiles());
    }

    @Test
    public void getChildFiles_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getChildFiles());
    }

    /////////////////////
    // getChildren
    /////////////////////

    @Test
    public void getChildren_withValidRequest_shouldReturnDriveItemTypeList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<? extends DriveItemType> expected = List.of(
                new DriveFile(newDriveItemZipFile(oneDriveConnection, 1)),
                new DriveFolder(newDriveItemFolder(oneDriveConnection)));

        final List<? extends DriveItemType> actual = drivePackageUnderTest.getChildren();

        assertEquals(expected, actual);
    }

    @Test
    public void getChildren_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getChildren());
    }

    @Test
    public void getChildren_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getChildren());
    }

    ///////////////////
    // search
    ///////////////////

    @Test
    public void search_withValidRequest_shouldReturnListOfDriveItemTypes() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SINGLE_DRIVE_ITEM_PAGE);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final List<? extends DriveItemType> expected = List.of(
                new DriveFile(newDriveItemZipFile(oneDriveConnection, 1)),
                new DriveFolder(newDriveItemFolder(oneDriveConnection)));

        final List<? extends DriveItemType> actual = drivePackageUnderTest.search("SearchQuery");

        assertEquals(expected, actual);
    }

    @Test
    public void search_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.search("SearchQuery"));
    }

    @Test
    public void search_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.search("SearchQuery"));
    }

    ///////////////////
    // update
    ///////////////////

    @Test
    public void update_withValidRequest_shouldReturnDriveFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_FOLDER);
        final DriveFolder expected = new DriveFolder(newDriveItemFolder(getOneDriveConnection()));

        final DriveFolder actual = drivePackageUnderTest.update();

        assertEquals(expected, actual);
    }

    @Test
    public void update_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.update());
    }

    @Test
    public void update_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.update());
    }

    ///////////////////
    // move
    ///////////////////

    @Test
    public void move_withValidRequest_shouldReturnDriveFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_FOLDER);
        final DriveFolder expected = new DriveFolder(newDriveItemFolder(getOneDriveConnection()));

        final DriveFolder actual = drivePackageUnderTest.move("newParentId", "newName");

        assertEquals(expected, actual);
    }

    @Test
    public void move_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.move("newParentId", "newName"));
    }

    @Test
    public void move_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.move("newParentId", "newName"));
    }

    ///////////////////
    // getActivities
    ///////////////////

    @Test
    public void getActivities_withValidRequest_shouldReturnItemActivityList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.ITEM_ACTIVITY_LIST);
        final List<ItemActivity> expected = List.of(newItemActivity(1), newItemActivity(2));

        final List<ItemActivity> actual = drivePackageUnderTest.getActivities();

        assertEquals(expected, actual);
    }

    @Test
    public void getActivities_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getActivities());
    }

    @Test
    public void getActivities_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getActivities());
    }

    ///////////////////
    // getPermissions
    ///////////////////

    @Test
    public void getPermissions_withValidRequest_shouldReturnPermissionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final Permission expectedPermission =
                newPermission(oneDriveConnection, "driveItemIdValue", "PackageIdValue");
        final List<Permission> expected = List.of(expectedPermission, expectedPermission);

        final List<Permission> actual = drivePackageUnderTest.getPermissions();

        assertEquals(expected, actual);
    }

    @Test
    public void getPermissions_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getPermissions());
    }

    @Test
    public void getPermissions_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getPermissions());
    }

    ///////////////////
    // getThumbnails
    ///////////////////

    @Test
    public void getThumbnails_withValidRequest_shouldReturnThumbnailSetList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.THUMBNAIL_SET_LIST);
        final List<ThumbnailSet> expected = List.of(newThumbnailSet(), newThumbnailSet());

        final List<ThumbnailSet> actual = drivePackageUnderTest.getThumbnails();

        assertEquals(expected, actual);
    }

    @Test
    public void getThumbnails_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getThumbnails());
    }

    @Test
    public void getThumbnails_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getThumbnails());
    }

    ///////////////////
    // getVersions
    ///////////////////

    @Test
    public void getVersions_withValidRequest_shouldReturnDriveItemVersionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_VERSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final DriveItemVersion firstEntry = newDriveItemVersion(oneDriveConnection, "1");
        final String id = drivePackageUnderTest.getId();
        final String name = drivePackageUnderTest.getName();
        final List<DriveItemVersion> expected = List.of(
                copyWithNameAndDriveItemId(newDriveItemVersion(oneDriveConnection, "1"), name, id),
                copyWithNameAndDriveItemId(newDriveItemVersion(oneDriveConnection, "2"), name, id));

        final List<DriveItemVersion> actual = drivePackageUnderTest.getVersions();

        assertEquals(expected, actual);
    }

    @Test
    public void getVersions_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.getVersions());
    }

    @Test
    public void getVersions_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.getVersions());
    }

    ///////////////////
    // addPermission
    ///////////////////

    @Test
    public void addPermission_withValidRequest_shouldReturnPermissionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final Permission expectedPermission =
                newPermission(oneDriveConnection, "driveItemIdValue", "PackageIdValue");;
        final List<Permission> expected = List.of(expectedPermission, expectedPermission);

        final List<Permission> actual = drivePackageUnderTest.addPermission(AddPermissionRequest.builder()
                .message("Message")
                .roles(List.of("Role1"))
                .requireSignIn(false)
                .sendInvitation(false)
                .recipients(List.of(DriveRecipient.builder()
                        .alias("Alias")
                        .email("email@someemail.com")
                        .objectId("ObjectId")
                        .build()))
                .build());

        assertEquals(expected, actual);
    }

    @Test
    public void addPermission_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class,
                () -> drivePackageUnderTest.addPermission(AddPermissionRequest.builder().build()));
    }

    @Test
    public void addPermission_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class,
                () -> drivePackageUnderTest.addPermission(AddPermissionRequest.builder().build()));
    }

    ///////////////////////
    // createSharingLink
    ///////////////////////

    @Test
    public void createSharingLink_withValidRequest_shouldReturnPermission() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION);
        final Permission expected =
                newPermission(getOneDriveConnection(), "driveItemIdValue", "PackageIdValue");

        final Permission actual = drivePackageUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
                .scope("anonymous")
                .type("view")
                .build());

        assertEquals(expected, actual);
    }

    @Test
    public void createSharingLink_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
                .scope("anonymous")
                .type("view")
                .build()));
    }

    @Test
    public void createSharingLink_withServiceError_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
                .scope("anonymous")
                .type("view")
                .build()));
    }

    ///////////////////////
    // copy
    ///////////////////////

    @Test
    public void copy_withValidRequest_shouldReturnAsyncJob() {
        setUpMockResponse(SUCCESS_ASYNC_JOB_CODE, "MonitoringUrlValue");
        final AsyncJob expected = new AsyncJob("MonitoringUrlValue", getOneDriveConnection());

        final AsyncJob actual = drivePackageUnderTest.copy("DestinationIdValue", "NewNameValue");

        assertEquals(expected, actual);
    }

    @Test
    public void copy_withNonExpectedSuccessResponseCode_shouldThrowException() {
        setUpMockResponse(SUCCESS_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    @Test
    public void copy_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    @Test
    public void copy_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    ///////////////////////
    // delete
    ///////////////////////

    @Test
    public void delete_withValidRequest_shouldUpdateDeletedState() {
        setUpMockResponse(SUCCESS_STATUS_CODE);

        drivePackageUnderTest.delete();

        assertTrue(drivePackageUnderTest.isDeleted());
    }

    @Test
    public void delete_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> drivePackageUnderTest.delete());
    }

    @Test
    public void delete_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> drivePackageUnderTest.delete());
    }
}
