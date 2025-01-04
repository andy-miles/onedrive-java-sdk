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
package com.amilesend.onedrive.connection;

import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.connection.auth.PersonalAccountAuthManager;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OneDriveConnectionBuilderTest {
    private static final String CLIENT_ID = "ClientId";
    private static final String CLIENT_SECRET = "ClientSecret";
    private static final String REDIRECT_URL = "RedirectUrl";
    private static final String AUTH_CODE = "AuthCode";

    @Mock
    private OkHttpClient mockClient;
    @Mock
    private Gson mockGson;
    @Mock
    private GsonFactory mockGsonFactory;
    @Mock
    private PersonalAccountAuthManager mockAuthManager;
    private OneDriveConnectionBuilder builderUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGsonFactory.newInstanceForConnection(any(OneDriveConnection.class))).thenReturn(mockGson);
        lenient().when(mockGsonFactory.getInstanceForAuthManager()).thenReturn(mockGson);
        builderUnderTest = new OneDriveConnectionBuilder(mockGsonFactory);
    }

    @Test
    public void builder_withAuthCode_shouldReturnConnection() {
        final PersonalAccountAuthManager.BuilderWithAuthCode mockBuilder =
                setUpAuthManagerBuilderWithAuthCode(mockAuthManager);
        try (final MockedStatic<PersonalAccountAuthManager> authManagerMockedStatic =
                     mockStatic(PersonalAccountAuthManager.class)) {
            authManagerMockedStatic.when(
                    () -> PersonalAccountAuthManager.builderWithAuthCode()).thenReturn(mockBuilder);
            final OneDriveConnection actual = builderUnderTest
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUrl(REDIRECT_URL)
                    .httpClient(mockClient)
                    .build(AUTH_CODE);
            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(mockAuthManager, actual.getAuthManager()),
                    () -> assertEquals(mockGson, actual.getGson()));
        }
    }

    @Test
    public void builder_withDefaultGsonFactoryAndAuthCode_shouldReturnConnection() {
        builderUnderTest = OneDriveConnectionBuilder.newInstance();
        final PersonalAccountAuthManager.BuilderWithAuthCode mockBuilder =
                setUpAuthManagerBuilderWithAuthCode(mockAuthManager);
        try (final MockedStatic<PersonalAccountAuthManager> authManagerMockedStatic =
                     mockStatic(PersonalAccountAuthManager.class)) {
            authManagerMockedStatic.when(
                    () -> PersonalAccountAuthManager.builderWithAuthCode()).thenReturn(mockBuilder);
            final OneDriveConnection actual = builderUnderTest
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUrl(REDIRECT_URL)
                    .httpClient(mockClient)
                    .build(AUTH_CODE);
            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(mockAuthManager, actual.getAuthManager()),
                    () -> assertNotNull(actual.getGson()));
        }
    }

    @Test
    public void builder_withAuthInfo_shouldReturnConnection() {
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        final PersonalAccountAuthManager.BuilderWithAuthInfo mockBuilder =
                setUpAuthManagerbuilderWithAuthInfo(mockAuthManager);
        try (final MockedStatic<PersonalAccountAuthManager> authManagerMockedStatic =
                     mockStatic(PersonalAccountAuthManager.class)) {
            authManagerMockedStatic.when(
                    () -> PersonalAccountAuthManager.builderWithAuthInfo()).thenReturn(mockBuilder);

            final OneDriveConnection actual = builderUnderTest
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUrl(REDIRECT_URL)
                    .httpClient(mockClient)
                    .build(mockAuthInfo);
            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(mockAuthManager, actual.getAuthManager()),
                    () -> assertEquals(mockGson, actual.getGson()));
        }
    }

    @Test
    public void builder_withNoHttpClientDefined_shouldReturnConnection() {
        final PersonalAccountAuthManager.BuilderWithAuthCode mockBuilder =
                setUpAuthManagerBuilderWithAuthCode(mockAuthManager);
        try (final MockedStatic<PersonalAccountAuthManager> authManagerMockedStatic =
                     mockStatic(PersonalAccountAuthManager.class)) {
            authManagerMockedStatic.when(
                    () -> PersonalAccountAuthManager.builderWithAuthCode()).thenReturn(mockBuilder);

            final OneDriveConnection actual = builderUnderTest
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUrl(REDIRECT_URL)
                    .build(AUTH_CODE);
            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(mockAuthManager, actual.getAuthManager()),
                    () -> assertEquals(mockGson, actual.getGson()));
        }
    }

    @Test
    public void builder_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> builderUnderTest
                                .clientId(null) // Null clientId
                                .clientSecret(CLIENT_SECRET)
                                .redirectUrl(REDIRECT_URL)
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> builderUnderTest
                                .clientId(StringUtils.EMPTY) // Empty clientId
                                .clientSecret(CLIENT_SECRET)
                                .redirectUrl(REDIRECT_URL)
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(NullPointerException.class,
                        () -> builderUnderTest
                                .clientId(CLIENT_ID)
                                .clientSecret(null) // Null clientSecret
                                .redirectUrl(REDIRECT_URL)
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> builderUnderTest
                                .clientId(CLIENT_ID)
                                .clientSecret(StringUtils.EMPTY) // Empty clientSecret
                                .redirectUrl(REDIRECT_URL)
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(NullPointerException.class,
                        () -> builderUnderTest
                                .clientId(CLIENT_ID)
                                .clientSecret(CLIENT_SECRET)
                                .redirectUrl(null) // Null redirectUrl
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> builderUnderTest
                                .clientId(CLIENT_ID)
                                .clientSecret(CLIENT_SECRET)
                                .redirectUrl(StringUtils.EMPTY) // Empty redirectUrl
                                .httpClient(mockClient)
                                .build(AUTH_CODE)),
                () -> assertThrows(NullPointerException.class,
                        () -> new OneDriveConnectionBuilder(null)));

    }

    private static PersonalAccountAuthManager.BuilderWithAuthCode setUpAuthManagerBuilderWithAuthCode(
            final PersonalAccountAuthManager managerToReturn) {
        final PersonalAccountAuthManager.BuilderWithAuthCode mockBuilder =
                mock(PersonalAccountAuthManager.BuilderWithAuthCode.class);
        when(mockBuilder.httpClient(any(OkHttpClient.class))).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.authCode(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.buildWithAuthCode()).thenReturn(managerToReturn);
        return mockBuilder;
    }

    private static PersonalAccountAuthManager.BuilderWithAuthInfo setUpAuthManagerbuilderWithAuthInfo(
            final PersonalAccountAuthManager managerToReturn) {
        final PersonalAccountAuthManager.BuilderWithAuthInfo mockBuilder =
                mock(PersonalAccountAuthManager.BuilderWithAuthInfo.class);
        when(mockBuilder.httpClient(any(OkHttpClient.class))).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.clientSecret(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.redirectUrl(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.authInfo(any(AuthInfo.class))).thenReturn(mockBuilder);
        when(mockBuilder.buildWithAuthInfo()).thenReturn(managerToReturn);
        return mockBuilder;
    }
}
