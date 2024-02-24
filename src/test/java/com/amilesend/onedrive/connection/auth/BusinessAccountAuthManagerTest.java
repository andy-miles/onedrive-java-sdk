/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2024 Andy Miles (andy.miles@amilesend.com)
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
import com.amilesend.onedrive.resource.discovery.DiscoverServiceResponse;
import com.amilesend.onedrive.resource.discovery.Service;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.AUTH_CODE;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.CLIENT_ID;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.CLIENT_SECRET;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.REDIRECT_URL;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.newAuthInfo;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.newResponse;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerTest.setUpCall;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BusinessAccountAuthManagerTest {
    @Mock
    private OkHttpClient mockHttpClient;
    private BusinessAccountAuthManager authManagerUnderTest;

    //////////////////////////
    // builderWithAuthCode
    //////////////////////////

    @Test
    public void builderWithAuthCode_withValidParameters_shouldRedeemToken() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();

            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)),
                    () -> assertEquals("https://api.office.com/discovery/",
                            authManagerUnderTest.getResourceId()));
        }
    }

    @Test
    public void builderWithAuthCode_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(mockHttpClient, newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString()))
                    .thenThrow(new JsonSyntaxException("Exception"));

            final Throwable thrown = assertThrows(AuthManagerException.class,
                    () -> authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
                        .authCode(null) // Null authCode
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthCode()
                                .authCode(StringUtils.EMPTY) // Blank authCode
                                .clientId(CLIENT_ID)
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthCode()
                                .authCode(AUTH_CODE)
                                .clientId(StringUtils.EMPTY) // Blank clientId
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthCode()
                                .authCode(AUTH_CODE)
                                .clientId(CLIENT_ID)
                                .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthCode()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
                        .authCode(AUTH_CODE)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthCode()),
                () -> assertThrows(IllegalArgumentException.class, () -> BusinessAccountAuthManager.builderWithAuthCode()
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();

            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthInfo()
                    .authInfo(expected)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthInfo();

            assertAll(
                    () -> assertEquals(expected, authManagerUnderTest.getAuthInfo()),
                    () -> verify(mockHttpClient).newCall(isA(Request.class)),
                    () -> assertEquals(expected.getResourceId(), authManagerUnderTest.getResourceId()));
        }
    }

    @Test
    public void builderWithAuthInfo_withUnsuccessfulResponse_shouldThrowException() {
        setUpCall(mockHttpClient, newResponse(false));

        assertThrows(AuthManagerException.class,
                () -> authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(newAuthInfo())
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo());
    }

    @Test
    public void builderWithAuthInfo_withInvalidParameters_shouldThrowException() {
        final AuthInfo authInfo = newAuthInfo();
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(null) // Null authInfo
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(null) // Null clientId
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthInfo()
                                .authInfo(authInfo)
                                .clientId(StringUtils.EMPTY) // Blank clientId
                                .clientSecret(CLIENT_SECRET)
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(null) // Null clientSecret
                        .httpClient(mockHttpClient)
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthInfo()
                                .authInfo(authInfo)
                                .clientId(CLIENT_ID)
                                .clientSecret(StringUtils.EMPTY) // Blank clientSecret
                                .httpClient(mockHttpClient)
                                .redirectUrl(REDIRECT_URL)
                                .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(null) // Null httpClient
                        .redirectUrl(REDIRECT_URL)
                        .buildWithAuthInfo()),
                () -> assertThrows(NullPointerException.class, () -> BusinessAccountAuthManager.builderWithAuthInfo()
                        .authInfo(authInfo)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .httpClient(mockHttpClient)
                        .redirectUrl(null) // Null redirectUrl
                        .buildWithAuthInfo()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> BusinessAccountAuthManager.builderWithAuthInfo()
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
        final AuthInfo expected = newAuthInfo(isExpired);
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
        final AuthInfo expected = newAuthInfo(isExpired);
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
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
    public void getAuthenticatedEndpoint_withValidResourceId_shouldReturnEndpointUrl() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();
            authManagerUnderTest.setResourceId("http://mySite");

            assertEquals("http://mySite/_api/v2.0", authManagerUnderTest.getAuthenticatedEndpoint());
        }
    }

    @Test
    public void getAuthenticatedEndpoint_withInvalidResourceId_shouldThrowException() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();

            assertAll(
                    // The case for initial valid auth before discovering and setting a service
                    () -> assertThrows(AuthManagerException.class,
                            () -> authManagerUnderTest.getAuthenticatedEndpoint()),
                    () -> {
                        authManagerUnderTest.setResourceId(null);
                        assertThrows(AuthManagerException.class, () -> authManagerUnderTest.getAuthenticatedEndpoint());
                    },
                    () -> {
                        authManagerUnderTest.setResourceId(StringUtils.EMPTY);
                        assertThrows(AuthManagerException.class, () -> authManagerUnderTest.getAuthenticatedEndpoint());
                    });
        }
    }

    //////////////////////////////
    // getServices
    //////////////////////////////

    @Test
    public void getServices_withAuthenticatedManager_shouldReturnServiceList() {
        final AuthInfo authInfo = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        final DiscoverServiceResponse response = DiscoverServiceResponse.builder()
                .services(List.of(Service.builder().serviceResourceId("http://mySite").build()))
                .build();
        final Gson mockGson = mock(Gson.class);
        when(mockGson.fromJson(anyString(), eq(DiscoverServiceResponse.class))).thenReturn(response);
        final GsonFactory mockGsonFactory = mock(GsonFactory.class);
        when(mockGsonFactory.getInstanceForServiceDiscovery()).thenReturn(mockGson);

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class);
            final MockedStatic<GsonFactory> gsonMockedStatic = mockStatic(GsonFactory.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(authInfo);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            gsonMockedStatic.when(() -> GsonFactory.getInstance()).thenReturn(mockGsonFactory);
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();
            setUpCall(mockHttpClient, newResponse(true));

            final List<Service> actual = authManagerUnderTest.getServices();

            assertEquals(response.getServices(), actual);
        }
    }

    @Test
    public void getServices_withUnsuccessfulResponse_shouldThrowException() {
        final AuthInfo authInfo = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(authInfo);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();
            setUpCall(mockHttpClient, newResponse(false));

            assertThrows(AuthManagerException.class, () -> authManagerUnderTest.getServices());
        }
    }

    @Test
    public void getServices_withJsonSyntaxException_shouldThrowException() {
        final AuthInfo authInfo = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        final DiscoverServiceResponse response = DiscoverServiceResponse.builder()
                .services(List.of(Service.builder().serviceResourceId("http://mySite").build()))
                .build();
        final Gson mockGson = mock(Gson.class);
        when(mockGson.fromJson(anyString(), eq(DiscoverServiceResponse.class)))
                .thenThrow(new JsonSyntaxException("Exception"));
        final GsonFactory mockGsonFactory = mock(GsonFactory.class);
        when(mockGsonFactory.getInstanceForServiceDiscovery()).thenReturn(mockGson);

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class);
            final MockedStatic<GsonFactory> gsonMockedStatic = mockStatic(GsonFactory.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(authInfo);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            gsonMockedStatic.when(() -> GsonFactory.getInstance()).thenReturn(mockGsonFactory);
            authManagerUnderTest = BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode();
            setUpCall(mockHttpClient, newResponse(true));

            final Throwable thrown = assertThrows(AuthManagerException.class, () -> authManagerUnderTest.getServices());
            assertInstanceOf(JsonSyntaxException.class, thrown.getCause());
        }
    }

    //////////////////////////////
    // authenticateService
    //////////////////////////////

    @Test
    public void authenticateService_withValidService_shouldSetResourceIdAndRefreshToken() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = spy(BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode());
            doReturn(expected).when(authManagerUnderTest).refreshToken();

            authManagerUnderTest.authenticateService(Service.builder().serviceResourceId("http://mySite").build());

            assertAll(
                    () -> assertEquals("http://mySite", authManagerUnderTest.getResourceId()),
                    () -> assertEquals("http://mySite/_api/v2.0", authManagerUnderTest.getAuthenticatedEndpoint()),
                    () -> verify(authManagerUnderTest).refreshToken());
        }
    }

    @Test
    public void authenticateService_withInvalidService_shouldThrowException() {
        final AuthInfo expected = newAuthInfo();
        setUpCall(mockHttpClient, newResponse(true));

        try(final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(anyString())).thenReturn(expected);
            authInfoMockedStatic.when(() -> AuthInfo.builder()).thenCallRealMethod();
            authManagerUnderTest = spy(BusinessAccountAuthManager.builderWithAuthCode()
                    .authCode(AUTH_CODE)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .httpClient(mockHttpClient)
                    .redirectUrl(REDIRECT_URL)
                    .buildWithAuthCode());

            assertAll(
                    () -> assertThrows(NullPointerException.class,
                            () -> authManagerUnderTest.authenticateService(null)),
                    () -> assertThrows(NullPointerException.class,
                            () -> authManagerUnderTest.authenticateService(Service.builder().build())),
                    () -> assertThrows(IllegalArgumentException.class,
                            () -> authManagerUnderTest.authenticateService(Service.builder()
                                    .serviceResourceId(StringUtils.EMPTY)
                                    .build())));
        }
    }
}
