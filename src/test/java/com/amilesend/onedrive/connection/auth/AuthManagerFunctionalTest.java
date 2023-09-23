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
package com.amilesend.onedrive.connection.auth;

import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.parse.GsonFactory;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthManagerFunctionalTest {
    private static final String TOKEN_URL_PATH = "/common/oauth2/v2.0/token";
    private static final String AUTH_CODE = "TestAuthCode";
    private static final String CLIENT_ID = "TestClientId";
    private static final String CLIENT_SECRET = "TestClientSecret";
    private static final String REDIRECT_URL = "http://TestRedirectUrl";
    static final long EXPIRES_TIME_MILLIS = System.currentTimeMillis() + Duration.ofDays(2L).toMillis();
    static final String TOKEN_JSON_RESPONSE =
            "{" +
                "\"access_token\": \"AccessToken\"," +
                "\"expires_in\": " + EXPIRES_TIME_MILLIS + "," +
                "\"ext_expires_in\": " + EXPIRES_TIME_MILLIS + "," +
                "\"refresh_token\": \"RefreshToken\"," +
                "\"scope\": \"Scope1 Scope2 Scope3\"," +
                "\"token_type\": \"bearer\"" +
            "}";

    private MockWebServer mockWebServer;
    private OkHttpClient httpClient;
    private String baseUrl;

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(TOKEN_JSON_RESPONSE));
        mockWebServer.start();

        httpClient = new OkHttpClientBuilder().isForTest(true).build();
        baseUrl = mockWebServer.url(TOKEN_URL_PATH).toString();
    }

    @SneakyThrows
    @AfterEach
    public void cleanUp() {
        mockWebServer.shutdown();
    }

    @SneakyThrows
    @Test
    public void builderWithAuthCode_withValidParameters_shouldRedeemToken() {
        final AuthManager authManager = AuthManager.builderWithAuthCode()
                .authCode(AUTH_CODE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .gson(GsonFactory.getInstance().getInstanceForAuthManager())
                .httpClient(newHttpClient())
                .redirectUrl(REDIRECT_URL)
                .baseTokenUrl(baseUrl)
                .buildWithAuthCode();

        validateAuthInfo(authManager.getAuthInfo());
    }

    @Test
    public void builderWithAuthInfo_withValidParameters_shouldRefreshToken() {
        final AuthManager authManager =  AuthManager.builderWithAuthInfo()
                .authInfo(AuthInfo.builder()
                        .accessToken("StaleAccessToken")
                        .expiresIn(EXPIRES_TIME_MILLIS)
                        .extExpiresIn(EXPIRES_TIME_MILLIS)
                        .refreshToken("OldRefreshToken")
                        .scopes(List.of("Scope1", "Scope2", "Scope3"))
                        .build())
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .gson(GsonFactory.getInstance().getInstanceForAuthManager())
                .httpClient(newHttpClient())
                .redirectUrl(REDIRECT_URL)
                .baseTokenUrl(baseUrl)
                .buildWithAuthInfo();

        validateAuthInfo(authManager.getAuthInfo());
    }

    private static void validateAuthInfo(final AuthInfo actual) {
        assertAll(
                () -> assertEquals("AccessToken", actual.getAccessToken()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExpiresIn()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExtExpiresIn()),
                () -> assertEquals("bearer AccessToken", actual.getFullToken()),
                () -> assertEquals("RefreshToken", actual.getRefreshToken()),
                () -> assertEquals(List.of("Scope1", "Scope2", "Scope3"), actual.getScopes()),
                () -> assertEquals("bearer", actual.getTokenType()));
    }

    private OkHttpClient newHttpClient() {
        return new OkHttpClientBuilder().isForTest(true).build();
    }
}
