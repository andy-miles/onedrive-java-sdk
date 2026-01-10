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
package com.amilesend.onedrive.connection.auth;

import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.data.SerializedResource;
import com.amilesend.onedrive.resource.discovery.Service;
import lombok.SneakyThrows;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.AUTH_CODE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.CLIENT_ID;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.CLIENT_SECRET;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.EXPIRES_TIME_MILLIS;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.REDIRECT_URL;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.TOKEN_URL_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BusinessAccountAuthManagerFunctionalTest {
    public static final String TOKEN_JSON_RESPONSE =
            "{" +
                "\"access_token\": \"AccessToken\"," +
                "\"expires_in\": " + EXPIRES_TIME_MILLIS + "," +
                "\"refresh_token\": \"RefreshToken\"," +
                "\"token_type\": \"Bearer\"" +
            "}";
    public static final String DISCOVERY_URL_PATH = "/discovery";

    private MockWebServer mockWebServer;
    private OkHttpClient httpClient;
    private String authUrl;
    private String discoveryUrl;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        mockWebServer = new MockWebServer();
        enqueueSuccessfulResponse(TOKEN_JSON_RESPONSE);
        mockWebServer.start();

        httpClient = new OkHttpClientBuilder().isForTest(true).build();
        authUrl = mockWebServer.url(TOKEN_URL_PATH).toString();
        discoveryUrl = mockWebServer.url(DISCOVERY_URL_PATH).toString();
    }

    @SneakyThrows
    @AfterEach
    public void cleanUp() {
        mockWebServer.close();
    }

    @SneakyThrows
    @Test
    public void authenticateFlowWithAuthCode_shouldRedeemAndRefreshTokens() {
        // Initial auth flow to be able to query for services
        final BusinessAccountAuthManager authManager = BusinessAccountAuthManager.builderWithAuthCode()
                .authCode(AUTH_CODE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .httpClient(httpClient)
                .redirectUrl(REDIRECT_URL)
                .authBaseTokenUrl(authUrl)
                .discoveryBaseTokenUrl(discoveryUrl)
                .buildWithAuthCode();
        // Validate that the current auth info is for discovery
        assertEquals(
                OneDriveAuthInfo.builder()
                        .accessToken("AccessToken")
                        .expiresIn(EXPIRES_TIME_MILLIS)
                        .refreshToken("RefreshToken")
                        .resourceId(discoveryUrl)
                        .build(),
                authManager.getAuthInfo());

        // Mock service discovery
        enqueueSuccessfulResponse(new String(
                SerializedResource.DISCOVER_SERVICE_RESPONSE.getResource().readAllBytes(),
                StandardCharsets.UTF_8));
        final List<Service> services = authManager.getServices();

        // Mock refreshing the tokens for the specific resource
        enqueueSuccessfulResponse(TOKEN_JSON_RESPONSE);
        authManager.authenticateService(services.get(0));

        // Validate that current auth info is now associated with the resource
        assertEquals(
                OneDriveAuthInfo.builder()
                        .accessToken("AccessToken")
                        .expiresIn(EXPIRES_TIME_MILLIS)
                        .refreshToken("RefreshToken")
                        .resourceId(services.get(0).getServiceResourceId())
                        .build(),
                authManager.getAuthInfo());
     }

    @Test
    public void authenticateFlowWithAuthInfo_shouldRefreshTokensForExistingResource() {
        final OneDriveAuthInfo authInfoForResource = OneDriveAuthInfo.builder()
                .accessToken("AccessToken")
                .expiresIn(EXPIRES_TIME_MILLIS)
                .refreshToken("RefreshToken")
                .resourceId("http://mysite")
                .build();
        final BusinessAccountAuthManager authManager = BusinessAccountAuthManager.builderWithAuthInfo()
                .authInfo(authInfoForResource)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .httpClient(httpClient)
                .redirectUrl(REDIRECT_URL)
                .authBaseTokenUrl(authUrl)
                .discoveryBaseTokenUrl(discoveryUrl)
                .buildWithAuthInfo();

        assertEquals(
                OneDriveAuthInfo.builder()
                        .accessToken("AccessToken")
                        .expiresIn(EXPIRES_TIME_MILLIS)
                        .refreshToken("RefreshToken")
                        .resourceId("http://mysite")
                        .build(),
                authManager.getAuthInfo());
    }

    private void enqueueSuccessfulResponse(final String responseBodyJson) {
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .body(responseBodyJson)
                .build());
    }
}
