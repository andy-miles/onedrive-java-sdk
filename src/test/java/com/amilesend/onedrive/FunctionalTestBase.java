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
package com.amilesend.onedrive;

import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.connection.retry.NoRetryStrategy;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.auth.OneDriveAuthManager;
import com.amilesend.onedrive.connection.auth.PersonalAccountAuthManager;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.parse.GsonFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import static com.amilesend.client.connection.Connection.Headers.CONTENT_ENCODING;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.TOKEN_JSON_RESPONSE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.TOKEN_URL_PATH;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.AUTH_CODE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.CLIENT_ID;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.CLIENT_SECRET;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.REDIRECT_URL;

public class FunctionalTestBase {
    public static final int SUCCESS_STATUS_CODE = 200;
    public static final int SUCCESS_ASYNC_JOB_CODE = 202;
    public static final int ERROR_STATUS_CODE = 404;
    public static final int SERVICE_ERROR_STATUS_CODE = 503;

    protected static final TransferProgressCallback NO_OP_TRANSFER_PROGRESS_CALLBACK =
            new NoOpTransferProgressCallback();

    private MockWebServer mockWebServer = new MockWebServer();
    private OkHttpClient httpClient;
    private OneDriveAuthManager authManager;
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
        mockWebServer.close();
    }

    protected void setUpMockResponse(final int responseCode) {
        setUpMockResponse(responseCode, (SerializedResource) null);
    }

    @SneakyThrows
    protected void setUpMockResponse(final int responseCode, final SerializedResource responseBodyResource) {
        if (responseBodyResource == null) {
            mockWebServer.enqueue(new MockResponse.Builder()
                    .code(responseCode)
                    .build());
            return;
        }

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(responseCode)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader(CONTENT_ENCODING, "gzip")
                .body(new Buffer().write(responseBodyResource.toGzipCompressedBytes()))
                .build());
    }

    @SneakyThrows
    protected void setUpMockResponse(final int responseCode, final String locationHeaderValue) {
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(responseCode)
                .addHeader("Location", locationHeaderValue)
                .build());
    }

    protected void setUpMockResponse(final int responseCode, final byte[] bytesForDownload) {
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(responseCode)
                .body(new Buffer().write(bytesForDownload))
                .build());
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
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(SUCCESS_STATUS_CODE)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .body(TOKEN_JSON_RESPONSE)
                .build());
        final String authUrl = mockWebServer.url(TOKEN_URL_PATH).toString();

        authManager = PersonalAccountAuthManager.builderWithAuthCode()
                .authCode(AUTH_CODE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .httpClient(httpClient)
                .redirectUrl(REDIRECT_URL)
                .baseTokenUrl(authUrl)
                .buildWithAuthCode();
    }

    private void setUpOneDrive() {
        oneDriveConnection = OneDriveConnection.builder()
                .httpClient(httpClient)
                .authManager(authManager)
                .gsonFactory(new GsonFactory())
                .baseUrl(getMockWebServerUrl())
                .userAgent("OneDriveTestJavaClient/1.0")
                .isGzipContentEncodingEnabled(true)
                .retryStrategy(new NoRetryStrategy())
                .threadPool(Executors.newSingleThreadExecutor())
                .build();

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
