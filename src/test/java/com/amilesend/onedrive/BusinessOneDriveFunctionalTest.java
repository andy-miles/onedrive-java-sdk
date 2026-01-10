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
package com.amilesend.onedrive;

import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.retry.NoRetryStrategy;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.connection.auth.BusinessAccountAuthManager;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.parse.GsonFactory;
import com.amilesend.onedrive.resource.Drive;
import com.amilesend.onedrive.resource.Site;
import com.amilesend.onedrive.resource.discovery.Service;
import lombok.SneakyThrows;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

import static com.amilesend.client.connection.Connection.Headers.CONTENT_ENCODING;
import static com.amilesend.onedrive.connection.auth.BusinessAccountAuthManagerFunctionalTest.TOKEN_JSON_RESPONSE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.AUTH_CODE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.CLIENT_ID;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.CLIENT_SECRET;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.REDIRECT_URL;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.TOKEN_URL_PATH;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDrive;
import static com.amilesend.onedrive.data.SiteTestDataHelper.newSite;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BusinessOneDriveFunctionalTest {
    protected static final int SUCCESS_STATUS_CODE = 200;
    protected static final int ERROR_STATUS_CODE = 404;
    protected static final int SERVICE_ERROR_STATUS_CODE = 503;

    private MockWebServer mockWebServer = new MockWebServer();
    private OkHttpClient httpClient;
    private BusinessAccountAuthManager authManager;
    private OneDriveConnection oneDriveConnection;
    private BusinessOneDrive oneDriveUnderTest;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        httpClient = new OkHttpClientBuilder().isForTest(true).build();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        setUpOneDrive(setUpAuthManager());
    }

    @SneakyThrows
    @AfterEach
    public void cleanUp() {
        mockWebServer.close();
    }

    //////////////////////
    // getRootSite
    //////////////////////
    @Test
    public void getRootSite_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SITE);
        final Site expected = new Site(newSite(oneDriveConnection));

        final Site actual = oneDriveUnderTest.getRootSite();

        assertEquals(expected, actual);
    }

    @Test
    public void getRootSite_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getRootSite());
    }

    @Test
    public void getRootSite_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getRootSite());
    }

    ////////////
    // getSite
    ////////////

    @Test
    public void getSite_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SITE);
        final Site expected = new Site(newSite(oneDriveConnection));

        final Site actual = oneDriveUnderTest.getSite("SiteIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getSite_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getSite("SiteIdValue"));
    }

    @Test
    public void getSite_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getSite("SiteIdValue"));
    }

    ////////////////////
    // getSiteForGroup
    ////////////////////

    @Test
    public void getSiteForGroup_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SITE);
        final Site expected = new Site(newSite(oneDriveConnection));

        final Site actual = oneDriveUnderTest.getSiteForGroup("GroupIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getSiteForGroup_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getSiteForGroup("GroupIdValue"));
    }

    @Test
    public void getSiteForGroup_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getSiteForGroup("GroupIdValue"));
    }

    ////////////////////
    // getRootSites
    ////////////////////

    @Test
    public void getRootSites_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SITE_LIST);
        final List<Site> expected =
                List.of(new Site(newSite(oneDriveConnection)), new Site(newSite(oneDriveConnection)));

        final List<Site> actual = oneDriveUnderTest.getRootSites();

        assertEquals(expected, actual);
    }

    @Test
    public void getRootSites_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getRootSites());
    }

    @Test
    public void getRootSites_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getRootSites());
    }

    ////////////////////
    // searchForSite
    ////////////////////

    @Test
    public void searchForSite_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.SITE_LIST);
        final List<Site> expected =
                List.of(new Site(newSite(oneDriveConnection)), new Site(newSite(oneDriveConnection)));

        final List<Site> actual = oneDriveUnderTest.searchForSite("SearchQuery");

        assertEquals(expected, actual);
    }

    @Test
    public void searchForSite_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.searchForSite("SearchQuery"));
    }

    @Test
    public void searchForSite_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.searchForSite("SearchQuery"));
    }

    ////////////////////////////
    // getDefaultDriveForGroup
    ////////////////////////////

    @Test
    public void getDefaultDriveForGroup_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        final Drive expected = new Drive(newDrive(oneDriveConnection, 1));

        final Drive actual = oneDriveUnderTest.getDefaultDriveForGroup("GroupIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getDefaultDriveForGroup_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getDefaultDriveForGroup("GroupIdValue"));
    }

    @Test
    public void getDefaultDriveForGroup_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getDefaultDriveForGroup("GroupIdValue"));
    }

    ////////////////////////////
    // getDefaultDriveForSite
    ////////////////////////////

    @Test
    public void getDefaultDriveForSite_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE);
        final Drive expected = new Drive(newDrive(oneDriveConnection, 1));

        final Drive actual = oneDriveUnderTest.getDefaultDriveForSite("SiteIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getDefaultDriveForSite_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getDefaultDriveForSite("SiteIdValue"));
    }

    @Test
    public void getDefaultDriveForSite_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getDefaultDriveForSite("SiteIdValue"));
    }

    /////////////////////
    // getDrivesForSite
    /////////////////////

    @Test
    public void getDrivesForSite_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_LIST);
        final List<Drive> expected = List.of(
                new Drive(newDrive(oneDriveConnection, 1)),
                new Drive(newDrive(oneDriveConnection, 2)));

        final List<Drive> actual = oneDriveUnderTest.getDrivesForSite("SiteIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getDrivesForSite_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getDrivesForSite("SiteIdValue"));
    }

    @Test
    public void getDrivesForSite_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getDrivesForSite("SiteIdValue"));
    }

    //////////////////////
    // getDrivesForGroup
    //////////////////////

    @Test
    public void getDrivesForGroup_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_LIST);
        final List<Drive> expected = List.of(
                new Drive(newDrive(oneDriveConnection, 1)),
                new Drive(newDrive(oneDriveConnection, 2)));

        final List<Drive> actual = oneDriveUnderTest.getDrivesForGroup("GroupIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getDrivesForGroup_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getDrivesForGroup("GroupIdValue"));
    }

    @Test
    public void getDrivesForGroup_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getDrivesForGroup("GroupIdValue"));
    }

    //////////////////////
    // getDrivesForUser
    //////////////////////

    @Test
    public void getDrivesForUser_withValidRequest_shouldReturnSite() {
        setUpMockResponse(SUCCESS_STATUS_CODE, SerializedResource.DRIVE_LIST);
        final List<Drive> expected = List.of(
                new Drive(newDrive(oneDriveConnection, 1)),
                new Drive(newDrive(oneDriveConnection, 2)));

        final List<Drive> actual = oneDriveUnderTest.getDrivesForUser("UserIdValue");

        assertEquals(expected, actual);
    }

    @Test
    public void getDrivesForUser_withErrorResponse_shouldThrowException() {
        setUpMockResponse(ERROR_STATUS_CODE);
        assertThrows(RequestException.class, () -> oneDriveUnderTest.getDrivesForUser("UserIdValue"));
    }

    @Test
    public void getDrivesForUser_withServiceErrorResponse_shouldThrowException() {
        setUpMockResponse(SERVICE_ERROR_STATUS_CODE);
        assertThrows(ResponseException.class, () -> oneDriveUnderTest.getDrivesForUser("UserIdValue"));
    }

    @SneakyThrows
    private BusinessAccountAuthManager setUpAuthManager() {
        // Initial auth for discovery
        setUpSuccessfulMockResponse(TOKEN_JSON_RESPONSE);
        final String authUrl = mockWebServer.url(TOKEN_URL_PATH).toString();
        authManager = BusinessAccountAuthManager.builderWithAuthCode()
                .authBaseTokenUrl(authUrl)
                .authCode(AUTH_CODE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .discoveryBaseTokenUrl(authUrl)
                .httpClient(httpClient)
                .redirectUrl(REDIRECT_URL)
                .buildWithAuthCode();

        // Service discovery
        setUpSuccessfulMockResponse(new String(
                SerializedResource.DISCOVER_SERVICE_RESPONSE.getResource().readAllBytes(),
                StandardCharsets.UTF_8));
        final List<Service> services = authManager.getServices();

        // Refresh tokens for the specific resource
        setUpSuccessfulMockResponse(TOKEN_JSON_RESPONSE);
        authManager.authenticateService(services.get(0));

        return authManager;
    }

    private void setUpOneDrive(final BusinessAccountAuthManager authManager) {
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

        oneDriveUnderTest = new BusinessOneDrive(oneDriveConnection);
    }

    private String getMockWebServerUrl() {
        return String.format("http://%s:%d", mockWebServer.getHostName(), mockWebServer.getPort());
    }

    private void setUpSuccessfulMockResponse(final String responseBodyJson) {
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(SUCCESS_STATUS_CODE)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .body(responseBodyJson)
                .build());
    }

    private void setUpMockResponse(final int responseCode) {
        setUpMockResponse(responseCode, null);
    }

    @SneakyThrows
    private void setUpMockResponse(final int responseCode, final SerializedResource responseBodyResource) {
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
}
