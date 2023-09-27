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
import com.amilesend.onedrive.connection.auth.AuthManager;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.connection.parse.resource.parser.SerializedResource;
import com.amilesend.onedrive.parse.GsonFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.AUTH_CODE;
import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.CLIENT_ID;
import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.CLIENT_SECRET;
import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.REDIRECT_URL;
import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.TOKEN_JSON_RESPONSE;
import static com.amilesend.onedrive.connection.auth.AuthManagerFunctionalTest.TOKEN_URL_PATH;

public class FunctionalTestBase {
    protected static final int SUCCESS_STATUS_CODE = 200;
    protected static final int SUCCESS_ASYNC_JOB_CODE = 202;
    protected static final int ERROR_STATUS_CODE = 404;
    protected static final int SERVICE_ERROR_STATUS_CODE = 503;
    protected static final TransferProgressCallback NO_OP_TRANSFER_PROGRESS_CALLBACK =
            new NoOpTransferProgressCallback();

    private MockWebServer mockWebServer = new MockWebServer();
    private OkHttpClient httpClient;
    private AuthManager authManager;
    @Getter
    private OneDriveConnection oneDriveConnection;
    @Getter
    private OneDrive oneDriveUnderTest;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        httpClient = new OkHttpClientBuilder().isForTest(true).build();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        setUpAuthManager();
        setUpOneDrive();
    }

    @SneakyThrows
    @AfterEach
    public void cleanUp() {
        mockWebServer.shutdown();
    }

    protected void setUpMockResponse(final int responseCode) {
        setUpMockResponse(responseCode, (SerializedResource) null);
    }

    @SneakyThrows
    protected void setUpMockResponse(final int responseCode, final SerializedResource responseBodyResource) {
        if (responseBodyResource == null) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(responseCode));
            return;
        }

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(responseCode)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(new Buffer().write(responseBodyResource.toGzipCompressedBytes())));
    }

    @SneakyThrows
    protected void setUpMockResponse(final int responseCode, final String locationHeaderValue) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(responseCode)
                .addHeader("Location", locationHeaderValue));
    }

    protected void setUpMockResponse(final int responseCode, final byte[] bytesForDownload) {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(responseCode)
                .setBody(new Buffer().write(bytesForDownload)));
    }

    @SneakyThrows
    protected File createFile(final Path tempDir) {
        final Path filePathToUpload = tempDir.resolve("testFileToUpload.txt");
        Files.write(filePathToUpload, "TestFileContents".getBytes(StandardCharsets.UTF_8));
        return filePathToUpload.toFile();
    }

    protected String getMockWebServerUrl() {
        return String.format("http://%s:%d", mockWebServer.getHostName(), mockWebServer.getPort());
    }

    private void setUpAuthManager() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(SUCCESS_STATUS_CODE)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(TOKEN_JSON_RESPONSE));
        final String authUrl = mockWebServer.url(TOKEN_URL_PATH).toString();

        authManager = AuthManager.builderWithAuthCode()
                .authCode(AUTH_CODE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .gson(GsonFactory.getInstance().getInstanceForAuthManager())
                .httpClient(httpClient)
                .redirectUrl(REDIRECT_URL)
                .baseTokenUrl(authUrl)
                .buildWithAuthCode();
    }

    private void setUpOneDrive() {
        oneDriveConnection = new OneDriveConnection(
                httpClient,
                authManager,
                GsonFactory.getInstance(),
                getMockWebServerUrl());
        oneDriveUnderTest = new OneDrive(oneDriveConnection);
    }

    public static class NoOpTransferProgressCallback implements TransferProgressCallback {
        @Override
        public void onUpdate(final long currentBytes, final long totalBytes) {
            // NoOp
        }

        @Override
        public void onFailure(final Throwable cause) {
            // NoOp
        }

        @Override
        public void onComplete(final long bytesTransferred) {
            // NoOp
        }
    }
}
