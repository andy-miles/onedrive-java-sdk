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

import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthManagerTest {
    private static final String AUTH_CODE = "TestAuthCode";
    private static final String CLIENT_ID = "TestClientId";
    private static final String CLIENT_SECRET = "TestClientSecret";
    private static final String REDIRECT_URL = "http://TestRedirectUrl";
    private static final List<String> SCOPES = List.of("Permission1", "Permission2");
    private static final String BASE_AUTH_URL = "http://TestBaseUrl/token";

    @Mock
    private OkHttpClient mockHttpClient;
    @Mock
    private Gson mockGson;
    @Mock
    private GsonFactory mockGsonFactory;
    private AuthManager authManagerUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGsonFactory.newInstanceForAuthManager()).thenReturn(mockGson);
    }

    //////////////////////////
    // builderWithAuthCode
    //////////////////////////

    @Test
    public void builderWithAuthCode_withValidParameters_shouldRedeemToken() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);

            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
                    .buildWithAuthCode();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)));
        }
    }

    @Test
    public void builderWithAuthCode_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .buildWithAuthCode());
    }

    @Test
    public void builderWithAuthCode_withJsonSyntaxException_shouldThrowException() {
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString()))
                    .thenThrow(new JsonSyntaxException("Exception"));

            final Throwable thrown = assertThrows(AuthManagerException.class,
                    () -> authManagerUnderTest = AuthManager.builderWithAuthCode()
                            .authCode(AUTH_CODE)
                            .clientId(CLIENT_ID)
                            .clientSecret(CLIENT_SECRET)
                            .gsonFactory(mockGsonFactory)
                            .httpClient(mockHttpClient)
                            .redirectUrl(REDIRECT_URL)
                            .baseTokenUrl(BASE_AUTH_URL)
                            .buildWithAuthCode());

            assertInstanceOf(JsonSyntaxException.class, thrown.getCause());
        }
    }

    @Test
    public void builderWithAuthCode_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(null) // Null authCode
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(StringUtils.EMPTY) // Blank authCode
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(StringUtils.EMPTY) // Blank clientId
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(null) // Null gsonFactory
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(StringUtils.EMPTY) // Blank redirectUrl
                        .buildWithAuthCode()));
    }

    //////////////////////////
    // builderWithAuthInfo
    //////////////////////////

    @Test
    public void builderWithAuthInfo_withValidParameters_shouldRefreshToken() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);

            authManagerUnderTest = AuthManager.builderWithAuthInfo()
                    .authInfo(expected)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
                    .buildWithAuthInfo();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)));
        }
    }

    @Test
    public void builderWithAuthInfo_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = AuthManager.builderWithAuthInfo()
                        .authInfo(newAuthInfo())
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .buildWithAuthInfo());
    }

    @Test
    public void builderWithAuthInfo_withInvalidParameters_shouldThrowException() {
        final AuthInfo authInfo = newAuthInfo();
        assertAll(
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(null) // Null authInfo
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(StringUtils.EMPTY) // Blank clientId
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(null) // Null gsonFactory
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class, () ->AuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .baseTokenUrl(BASE_AUTH_URL)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .gsonFactory(mockGsonFactory)
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);
            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
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
        final AuthInfo expected = newAuthInfo(isExpired);
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);
            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
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
        final AuthInfo expected = newAuthInfo(isExpired);
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);
            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);
            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
                    .buildWithAuthCode();

            assertEquals("bearer accessTokenValue", authManagerUnderTest.refreshIfExpiredAndFetchFullToken());
        }
    }

    //////////////////////////
    // getAuthInfo
    //////////////////////////

    @Test
    public void getAuthInfo_withValidAuth_shouldReturnAuthInfo() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(eq(mockGson), anyString())).thenReturn(expected);
            authManagerUnderTest = AuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .gsonFactory(mockGsonFactory)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .baseTokenUrl(BASE_AUTH_URL)
                    .buildWithAuthCode();

            assertEquals(expected, authManagerUnderTest.getAuthInfo());
        }
    }

    @SneakyThrows
    private Response newResponse(final boolean isSuccessful) {
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

    @SneakyThrows
    private void setUpCall(final Response responseToReturn) {
        final Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(responseToReturn);
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
    }

    private AuthInfo newAuthInfo() {
        return newAuthInfo(false);
    }

    private AuthInfo newAuthInfo(final boolean isExpired) {
        final long expires = isExpired
                ? System.currentTimeMillis() - Duration.ofHours(2L).toMillis()
                : System.currentTimeMillis() + Duration.ofHours(2L).toMillis();
        return AuthInfo.builder()
                .accessToken("accessTokenValue")
                .refreshToken("refreshTokenValue")
                .expiresIn(expires)
                .extExpiresIn(expires)
                .scopes(SCOPES)
                .build();

    }
}
