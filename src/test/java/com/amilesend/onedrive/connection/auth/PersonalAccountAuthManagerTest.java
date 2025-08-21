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
package com.amilesend.onedrive.connection.auth;

import com.amilesend.client.util.StringUtils;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalAccountAuthManagerTest {
    public static final String AUTH_CODE = "TestAuthCode";
    public static final String CLIENT_ID = "TestClientId";
    public static final String CLIENT_SECRET = "TestClientSecret";
    public static final String REDIRECT_URL = "http://TestRedirectUrl";
    private static final List<String> SCOPES = List.of("Permission1", "Permission2");

    @Mock
    private OkHttpClient mockHttpClient;
    private PersonalAccountAuthManager authManagerUnderTest;


    //////////////////////////
    // builderWithAuthCode
    //////////////////////////

    @Test
    public void builderWithAuthCode_withValidParameters_shouldRedeemToken() {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);

            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)));
        }
    }

    @Test
    public void builderWithAuthCode_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(mockHttpClient, newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode());
    }

    @Test
    public void builderWithAuthCode_withJsonSyntaxException_shouldThrowException() {
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString()))
                    .thenThrow(new JsonSyntaxException("Exception"));

            final Throwable thrown = assertThrows(AuthManagerException.class,
                    () -> authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                            .authCode(AUTH_CODE)
                            .clientId(CLIENT_ID)
                            .clientSecret(CLIENT_SECRET)
                            .httpClient(mockHttpClient)
                            .redirectUrl(REDIRECT_URL)
                            .buildWithAuthCode());

            assertInstanceOf(JsonSyntaxException.class, thrown.getCause());
        }
    }

    @Test
    public void builderWithAuthCode_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(null) // Null authCode
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthCode()
                                .authCode(StringUtils.EMPTY) // Blank authCode
                                .clientId(CLIENT_ID)
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthCode()
                                .authCode(AUTH_CODE)
                                .clientId(StringUtils.EMPTY) // Blank clientId
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthCode()
                                .authCode(AUTH_CODE)
                                .clientId(CLIENT_ID)
                                .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () -> PersonalAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(StringUtils.EMPTY) // Blank redirectUrl
                        .buildWithAuthCode()));
    }

    //////////////////////////
    // builderWithAuthInfo
    //////////////////////////

    @Test
    public void builderWithAuthInfo_withValidParameters_shouldRefreshToken() {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);

            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthInfo()
                    .authInfo(expected)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthInfo();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)));
        }
    }

    @Test
    public void builderWithAuthInfo_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(mockHttpClient, newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(newAuthInfo())
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo());
    }

    @Test
    public void builderWithAuthInfo_withInvalidParameters_shouldThrowException() {
        final OneDriveAuthInfo authInfo = newAuthInfo();
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(null) // Null authInfo
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthInfo()
                                .authInfo(authInfo)
                                .clientId(StringUtils.EMPTY) // Blank clientId
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthInfo()
                                .authInfo(authInfo)
                                .clientId(CLIENT_ID)
                                .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> PersonalAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> PersonalAccountAuthManager.builderWithAuthInfo()
                                .authInfo(authInfo)
                                .clientId(CLIENT_ID)
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(StringUtils.EMPTY) // Blank redirectUrl
                                .buildWithAuthInfo()));
    }

    //////////////////////////
    // isAuthenticated
    //////////////////////////

    @Test
    public void isAuthenticated_withAuthInfoSet_shouldReturnTrue() {
        isAuthenticated_shouldReturn(true);
    }

    @Test
    public void isAuthenticated_withNoAuthInfoSet_shouldReturnFalse() {
        isAuthenticated_shouldReturn(false);
    }

    private void isAuthenticated_shouldReturn(final boolean isAuthenticated) {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            if (isAuthenticated) {
                assertTrue(authManagerUnderTest.isAuthenticated());
            } else {
                authManagerUnderTest.setAuthInfo(null);
                assertFalse(authManagerUnderTest.isAuthenticated());
            }
        }
    }

    //////////////////////////
    // isExpired
    //////////////////////////

    @Test
    public void isExpired_withExpiredAuthInfo_shouldReturnTrue() {
        isExpired_shouldReturn(true, true);
    }

    @Test
    public void isExpired_withValidAuthInfo_shouldReturnFalse() {
        isExpired_shouldReturn(false, true);
    }

    @Test
    public void isExpired_withNoAuthInfo_shouldThrowException() {
        isExpired_shouldReturn(false /* ignored */, false);
    }

    private void isExpired_shouldReturn(final boolean isExpired, final boolean isAuthenticated) {
        final OneDriveAuthInfo expected = newAuthInfo(isExpired);
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            if (!isAuthenticated) {
                authManagerUnderTest.setAuthInfo(null);
                assertThrows(AuthManagerException.class, () -> authManagerUnderTest.isExpired());
                return;
            }

            if (isExpired) {
                assertTrue(authManagerUnderTest.isExpired());
            } else {
                assertFalse(authManagerUnderTest.isExpired());
            }
        }
    }

    //////////////////////////
    // refreshIfExpired
    //////////////////////////

    @Test
    public void refreshIfExpired_withExpiredAuth_shouldRefresh() {
        refreshIfExpired_shouldRefresh(true);
    }

    @Test
    public void refreshIfExpired_withValidAuth_shouldNotRefresh() {
        refreshIfExpired_shouldRefresh(false);
    }

    private void refreshIfExpired_shouldRefresh(final boolean isExpired) {
        final OneDriveAuthInfo expected = newAuthInfo(isExpired);
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            authManagerUnderTest.refreshIfExpired();

            if (isExpired) {
                verify(mockHttpClient, times(2)).newCall(isA(Request.class));
            } else {
                verify(mockHttpClient).newCall(isA(Request.class));
            }
        }
    }

    //////////////////////////////////////////
    // refreshIfExpiredAndFetchFullToken
    //////////////////////////////////////////

    @Test
    public void refreshIfExpiredAndFetchFullToken_withValidAuth_shouldReturnFullToken() {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertEquals("Bearer accessTokenValue", authManagerUnderTest.refreshIfExpiredAndFetchFullToken());
        }
    }

    //////////////////////////
    // getAuthInfo
    //////////////////////////

    @Test
    public void getAuthInfo_withValidAuth_shouldReturnAuthInfo() {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertEquals(expected, authManagerUnderTest.getAuthInfo());
        }
    }

    //////////////////////////////
    // getAuthenticatedEndpoint
    //////////////////////////////

    @Test
    public void getAuthenticatedEndpoint_shouldReturnPersonalEndpointUrl() {
        final OneDriveAuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<OneDriveAuthInfo> authInfoMockedStatic = mockStatic(OneDriveAuthInfo.class)) {
            authInfoMockedStatic.when(() -> OneDriveAuthInfo.fromJson(anyString())).thenReturn(expected);
            authManagerUnderTest = PersonalAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertEquals("https://graph.microsoft.com/v1.0/me", authManagerUnderTest.getAuthenticatedEndpoint());
        }
    }

    @SneakyThrows
    static void setUpCall(final OkHttpClient httpClient, final Response responseToReturn) {
        final Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(responseToReturn);
        when(httpClient.newCall(any(Request.class))).thenReturn(mockCall);
    }


    @SneakyThrows
    static Response newResponse(final boolean isSuccessful) {
        final ResponseBody mockResponseBody = mock(ResponseBody.class);
        if (isSuccessful) {
            when(mockResponseBody.string()).thenReturn("JsonBody");
        }

        final Response mockResponse = mock(Response.class);
        if (isSuccessful) {
            when(mockResponse.body()).thenReturn(mockResponseBody);
        }
        when(mockResponse.isSuccessful()).thenReturn(isSuccessful);

        return mockResponse;
    }

    static OneDriveAuthInfo newAuthInfo() {
        return newAuthInfo(false);
    }

    static OneDriveAuthInfo newAuthInfo(final boolean isExpired) {
        final long expires = isExpired
                ? System.currentTimeMillis() - Duration.ofHours(2L).toMillis()
                : System.currentTimeMillis() + Duration.ofHours(2L).toMillis();
        return OneDriveAuthInfo.builder()
                .accessToken("accessTokenValue")
                .expiresIn(expires)
                .extExpiresIn(expires)
                .refreshToken("refreshTokenValue")
                .resourceId("https://api.office.com/discovery/")
                .scopes(SCOPES)
                .build();

    }
}
