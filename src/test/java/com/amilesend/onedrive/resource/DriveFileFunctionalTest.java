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

import com.amilesend.onedrive.FunctionalTestBase;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.RequestException;
import com.amilesend.onedrive.connection.ResponseException;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.activities.ItemActivity;
import com.amilesend.onedrive.resource.identity.DriveRecipient;
import com.amilesend.onedrive.resource.item.AsyncJob;
import com.amilesend.onedrive.resource.item.DriveItemVersion;
import com.amilesend.onedrive.resource.item.type.Permission;
import com.amilesend.onedrive.resource.item.type.Preview;
import com.amilesend.onedrive.resource.item.type.ThumbnailSet;
import com.amilesend.onedrive.resource.request.AddPermissionRequest;
import com.amilesend.onedrive.resource.request.CreateSharingLinkRequest;
import com.amilesend.onedrive.resource.request.PreviewRequest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemVersion;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItemZipFile;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newItemActivity;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPermission;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPreview;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newThumbnailSet;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DriveFileFunctionalTest extends FunctionalTestBase {
    private DriveFile fileUnderTest;

    @BeforeEach
    public void configureFile() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ROOT_FOLDER);
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_LIST);
        fileUnderTest = getOneDriveUnderTest()
                .getUserDrive()
                .getRootFolder()
                .getChildFiles()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to retrieve DriveFile to test"));
    }

    //////////////////
    // download
    //////////////////

    @SneakyThrows
    @Test
    public void download_withValidRequest_shouldDownloadFile(@TempDir final Path tempDir) {
        final String expectedContents = "TestFileContents";
        setUpMockResponse(SUCCESS_STATUS_CODE, expectedContents.getBytes(StandardCharsets.UTF_8));

        fileUnderTest.download(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK);

        final Path downloadedFile = tempDir.resolve(fileUnderTest.getName());
        final String contents = Files.readString(downloadedFile);
        assertEquals(expectedContents, contents);
    }

    @Test
    public void download_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.download(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    @Test
    public void download_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.download(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    //////////////////
    // downloadAsync
    //////////////////

    @SneakyThrows
    @Test
    public void downloadAsync_withValidRequest_shouldDownloadFiles(@TempDir final Path tempDir) {
        final String expectedContents = "TestFileContentsForAsync";
        setUpMockResponse(SUCCESS_STATUS_CODE, expectedContents.getBytes(StandardCharsets.UTF_8));

        final Long bytesDownloaded =
                fileUnderTest.downloadAsync(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK).get();

        final Path downloadedFile = tempDir.resolve(fileUnderTest.getName());
        final String contents = Files.readString(downloadedFile);

        assertAll(
                () -> assertEquals(expectedContents.getBytes(StandardCharsets.UTF_8).length,
                        bytesDownloaded.longValue()),
                () -> assertEquals(expectedContents, contents));
    }

    @Test
    public void downloadAsync_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.download(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    @Test
    public void downloadAsync_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.download(tempDir, NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    //////////////////
    // upload
    //////////////////

    @SneakyThrows
    @Test
    public void upload_withFile_shouldReturnDriveFile(@TempDir final Path tempDir) {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ZIP_FILE);
        final DriveFile expected = new DriveFile(newDriveItemZipFile(getOneDriveConnection(), 1));

        final DriveFile actual = fileUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK);

        assertEquals(expected, actual);
    }

    @Test
    public void upload_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class,
                () -> fileUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    @Test
    public void upload_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class,
                () -> fileUnderTest.upload(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK));
    }

    @Test
    public void upload_withIOException_shouldThrowException(@TempDir final Path tempDir) {
        final Path filePathToUpload = tempDir.resolve("testFileToUpload.txt");
        assertThrows(IOException.class,
                () -> fileUnderTest.upload(filePathToUpload.toFile(), NO_OP_TRANSFER_PROGRESS_CALLBACK));
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
                fileUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get();

        assertEquals(expected, actual);
    }

    @Test
    public void uploadAsync_withErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(ERROR_STATUS_CODE);
        final Throwable thrown = assertThrows(
                ExecutionException.class,
                () -> fileUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get());
        assertInstanceOf(RequestException.class, thrown.getCause());
    }

    @Test
    public void uploadAsync_withServiceErrorResponse_shouldThrowException(@TempDir final Path tempDir) {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        final Throwable thrown = assertThrows(
                ExecutionException.class,
                () -> fileUnderTest.uploadAsync(createFile(tempDir), NO_OP_TRANSFER_PROGRESS_CALLBACK).get());
        assertInstanceOf(ResponseException.class, thrown.getCause());
    }

    @Test
    public void uploadAsync_withIOException_shouldThrowException(@TempDir final Path tempDir) {
        final Path filePathToUpload = tempDir.resolve("testFileToUpload.txt");
        final Throwable thrown = assertThrows(IOException.class,
                () -> fileUnderTest.uploadAsync(filePathToUpload.toFile(), NO_OP_TRANSFER_PROGRESS_CALLBACK).get());
    }

    ///////////////////
    // update
    ///////////////////

    @Test
    public void update_withValidRequest_shouldReturnDriveFolder() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_ZIP_FILE);
        final DriveFile expected = new DriveFile(newDriveItemZipFile(getOneDriveConnection(), 1));

        final DriveFile actual = fileUnderTest.update();

        assertEquals(expected, actual);
    }

    @Test
    public void update_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.update());
    }

    @Test
    public void update_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.update());
    }

    ///////////////////
    // preview
    ///////////////////

    @Test
    public void preview_withValidRequest_shouldReturnPreview() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PREVIEW);
        final Preview expected = newPreview(fileUnderTest.getId());

        final Preview actual = fileUnderTest.preview(PreviewRequest.builder().build());

        assertEquals(expected, actual);
    }

    @Test
    public void preview_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.preview(PreviewRequest.builder().build()));
    }

    @Test
    public void preview_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.preview(PreviewRequest.builder().build()));
    }

    ///////////////////
    // move
    ///////////////////

    @Test
    public void move_withValidRequest_shouldReturnUpdatedDriveFile() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.UPDATED_DRIVE_ITEM_ZIP_FILE);

        final DriveFile actual = fileUnderTest.move("ParentIdValue", "NewName");

        assertEquals(fileUnderTest, actual);
    }

    @Test
    public void move_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.move("ParentIdValue", "NewName"));
    }

    @Test
    public void move_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.move("ParentIdValue", "NewName"));
    }

    ///////////////////
    // getActivities
    ///////////////////

    @Test
    public void getActivities_withValidRequest_shouldReturnItemActivityList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.ITEM_ACTIVITY_LIST);
        final List<ItemActivity> expected = List.of(newItemActivity(1), newItemActivity(2));

        final List<ItemActivity> actual = fileUnderTest.getActivities();

        assertEquals(expected, actual);
    }

    @Test
    public void getActivities_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.getActivities());
    }

    @Test
    public void getActivities_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.getActivities());
    }

    ///////////////////
    // getPermissions
    ///////////////////

    @Test
    public void getPermissions_withValidRequest_shouldReturnPermissionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final Permission expectedPermission =
                newPermission(oneDriveConnection, "driveItemIdValue", "ZipFileIdValue1");
        final List<Permission> expected = List.of(expectedPermission, expectedPermission);

        final List<Permission> actual = fileUnderTest.getPermissions();

        assertEquals(expected, actual);
    }

    @Test
    public void getPermissions_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.getPermissions());
    }

    @Test
    public void getPermissions_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.getPermissions());
    }

    ///////////////////
    // getThumbnails
    ///////////////////

    @Test
    public void getThumbnails_withValidRequest_shouldReturnThumbnailSetList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.THUMBNAIL_SET_LIST);
        final List<ThumbnailSet> expected = List.of(newThumbnailSet(), newThumbnailSet());

        final List<ThumbnailSet> actual = fileUnderTest.getThumbnails();

        assertEquals(expected, actual);
    }

    @Test
    public void getThumbnails_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.getThumbnails());
    }

    @Test
    public void getThumbnails_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.getThumbnails());
    }

    ///////////////////
    // getVersions
    ///////////////////

    @Test
    public void getVersions_withValidRequest_shouldReturnDriveItemVersionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_ITEM_VERSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final String id = fileUnderTest.getId();
        final String name = fileUnderTest.getName();
        final List<DriveItemVersion> expected = List.of(
                copyWithNameAndDriveItemId(newDriveItemVersion(oneDriveConnection, "1"), name, id),
                copyWithNameAndDriveItemId(newDriveItemVersion(oneDriveConnection, "2"), name, id));

        final List<DriveItemVersion> actual = fileUnderTest.getVersions();

        assertEquals(expected, actual);
    }



    @Test
    public void getVersions_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.getVersions());
    }

    @Test
    public void getVersions_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.getVersions());
    }

    ///////////////////
    // addPermission
    ///////////////////

    @Test
    public void addPermission_withValidRequest_shouldReturnPermissionList() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION_LIST);
        final OneDriveConnection oneDriveConnection = getOneDriveConnection();
        final Permission expectedPermission =
                newPermission(oneDriveConnection, "driveItemIdValue", "ZipFileIdValue1");
        final List<Permission> expected = List.of(expectedPermission, expectedPermission);

        final List<Permission> actual = fileUnderTest.addPermission(AddPermissionRequest.builder()
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
                () -> fileUnderTest.addPermission(AddPermissionRequest.builder().build()));
    }

    @Test
    public void addPermission_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class,
                () -> fileUnderTest.addPermission(AddPermissionRequest.builder().build()));
    }

    ///////////////////////
    // createSharingLink
    ///////////////////////

    @Test
    public void createSharingLink_withValidRequest_shouldReturnPermission() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.PERMISSION);
        final Permission expected =
                newPermission(getOneDriveConnection(), "driveItemIdValue", "ZipFileIdValue1");

        final Permission actual = fileUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
                .scope("anonymous")
                .type("view")
                .build());

        assertEquals(expected, actual);
    }

    @Test
    public void createSharingLink_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
                .scope("anonymous")
                .type("view")
                .build()));
    }

    @Test
    public void createSharingLink_withServiceError_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.createSharingLink(CreateSharingLinkRequest.builder()
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

        final AsyncJob actual = fileUnderTest.copy("DestinationIdValue", "NewNameValue");

        assertEquals(expected, actual);
    }

    @Test
    public void copy_withNonExpectedSuccessResponseCode_shouldThrowException() {
        setUpMockResponse(SUCCESS_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    @Test
    public void copy_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    @Test
    public void copy_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.copy("DestinationIdValue", "NewNameValue"));
    }

    ///////////////////////
    // delete
    ///////////////////////

    @Test
    public void delete_withValidRequest_shouldUpdateDeletedState() {
        setUpMockResponse(SUCCESS_STATUS_CODE);

        fileUnderTest.delete();

        assertTrue(fileUnderTest.isDeleted());
    }

    @Test
    public void delete_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> fileUnderTest.delete());
    }

    @Test
    public void delete_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> fileUnderTest.delete());
    }

    static DriveItemVersion copyWithNameAndDriveItemId(
            final DriveItemVersion orig,
            final String name,
            final String driveItemId) {
        return DriveItemVersion.builder()
                .connection(orig.getConnection())
                .driveItemId(driveItemId)
                .id(orig.getId())
                .lastModifiedBy(orig.getLastModifiedBy())
                .lastModifiedDateTime(orig.getLastModifiedDateTime())
                .name(name)
                .publication(orig.getPublication())
                .size(orig.getSize())
                .build();
    }
}
