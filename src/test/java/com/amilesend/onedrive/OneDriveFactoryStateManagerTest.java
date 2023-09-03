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
import com.amilesend.onedrive.connection.OneDriveConnectionBuilder;
import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.connection.auth.oauth.OAuthReceiver;
import com.amilesend.onedrive.connection.auth.oauth.OAuthReceiverException;
import com.amilesend.onedrive.connection.auth.oauth.OneDriveOAuthReceiver;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OneDriveFactoryStateManagerTest {
    private static final String REDIRECT_URL = "http://localhost";
    private static final String CALLBACK_PATH = "/Callback";
    private static final List<String> SCOPES = List.of("Scope1", "Scope2");
    private static final int PORT = 9000;

    @Mock
    private OkHttpClient mockHttpClient;
    @Mock
    private Gson mockGson;
    @Mock
    private OneDriveFactoryStateManager.CredentialConfig mockConfig;
    @Mock
    private Path mockStateFilePath;
    @Mock
    private OneDrive mockOneDrive;
    private OneDriveFactoryStateManager managerUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConfig.getClientId()).thenReturn("ClientId");
        lenient().when(mockConfig.getClientSecret()).thenReturn("ClientSecret");

        managerUnderTest = spy(OneDriveFactoryStateManager.builder()
                .httpClient(mockHttpClient)
                .receiverPort(PORT)
                .redirectUrl(REDIRECT_URL)
                .callbackPath(CALLBACK_PATH)
                .scopes(SCOPES)
                .stateGson(mockGson)
                .credentialConfig(mockConfig)
                .stateFile(mockStateFilePath)
                .build());
    }

    @Test
    public void builder_withDefaults_shouldBuildOneDrive() {
        managerUnderTest = OneDriveFactoryStateManager.builder()
                .stateFile(mockStateFilePath)
                .build();

        assertNotNull(managerUnderTest);
    }

    @Test
    public void builder_withNullStateFilePath_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> OneDriveFactoryStateManager.builder().build());
    }


    /////////////////////
    // saveState
    /////////////////////

    @SneakyThrows
    @Test
    public void saveState_withOneDrive_shouldWriteState() {
        managerUnderTest.setOnedrive(mockOneDrive);
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        when(mockAuthInfo.toJson(any(Gson.class))).thenReturn("StateContents");
        when(mockOneDrive.getAuthInfo()).thenReturn(mockAuthInfo);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            managerUnderTest.saveState();
            filesMockedStatic.verify(() -> Files.write(eq(mockStateFilePath), isA(byte[].class)));
        }
    }

    @SneakyThrows
    @Test
    public void saveState_withIOException_shouldThrowException() {
        managerUnderTest.setOnedrive(mockOneDrive);
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        when(mockAuthInfo.toJson(any(Gson.class))).thenReturn("StateContents");
        when(mockOneDrive.getAuthInfo()).thenReturn(mockAuthInfo);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.write(any(Path.class), any(byte[].class)))
                            .thenThrow(new IOException("Exception"));
            assertThrows(IOException.class, () -> managerUnderTest.saveState());
        }
    }

    @SneakyThrows
    @Test
    public void saveState_withNoOneDriveDefined_shouldNotWriteState() {
        managerUnderTest.saveState();
        verify(mockOneDrive, never()).getAuthInfo();
    }

    @SneakyThrows
    @Test
    public void close_shouldCallSaveState() {
        doNothing().when(managerUnderTest).saveState();
        managerUnderTest.close();
        verify(managerUnderTest).saveState();
    }

    /////////////////////
    // getInstance
    /////////////////////

    @SneakyThrows
    @Test
    public void getInstance_shouldReturnOneDrive() {
        managerUnderTest.setOnedrive(mockOneDrive);
        when(mockOneDrive.getUserDisplayName()).thenReturn("DisplayName");

        final OneDrive actual = managerUnderTest.getInstance();

        assertAll(
                () -> assertEquals(mockOneDrive, actual),
                () -> verify(mockOneDrive).getUserDisplayName());
    }

    @SneakyThrows
    @Test
    public void getInstance_withOAuthReceiverException_shouldThrowException() {
        doThrow(new OAuthReceiverException("Exception")).when(managerUnderTest).fetchOneDrive();
        final Throwable thrown = assertThrows(OneDriveException.class, () -> managerUnderTest.getInstance());
        assertInstanceOf(OAuthReceiverException.class, thrown.getCause());
    }

    @SneakyThrows
    @Test
    public void getInstance_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(managerUnderTest).fetchOneDrive();
        final Throwable thrown = assertThrows(OneDriveException.class, () -> managerUnderTest.getInstance());
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    /////////////////////
    // fetchOneDrive
    /////////////////////

    @SneakyThrows
    @Test
    public void fetchOneDrive_withAuthCode_shouldReturnOneDrive() {
        doReturn(mockConfig).when(managerUnderTest).loadCredentialConfig();
        doReturn(Optional.empty()).when(managerUnderTest).loadState();
        doReturn("AuthCode")
                .when(managerUnderTest)
                .authenticate(any(OneDriveFactoryStateManager.CredentialConfig.class));
        doNothing().when(managerUnderTest).saveState();
        final OneDriveConnectionBuilder builderMock = setUpOneDriveConnectionBuilderMock();

        try (final MockedStatic<OneDriveConnectionBuilder> builderMockedStatic =
                     mockStatic(OneDriveConnectionBuilder.class)) {
            builderMockedStatic.when(() -> OneDriveConnectionBuilder.newInstance()).thenReturn(builderMock);

            final OneDrive actual = managerUnderTest.fetchOneDrive();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> verify(builderMock).build(eq("AuthCode")),
                    () -> verify(managerUnderTest)
                            .authenticate(isA(OneDriveFactoryStateManager.CredentialConfig.class)),
                    () -> verify(managerUnderTest).loadCredentialConfig(),
                    () -> verify(managerUnderTest).loadState(),
                    () -> verify(managerUnderTest).saveState());
        }
    }

    @SneakyThrows
    @Test
    public void fetchOneDrive_withAuthInfo_shouldReturnOneDrive() {
        doReturn(mockConfig).when(managerUnderTest).loadCredentialConfig();
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);
        doReturn(Optional.of(mockAuthInfo)).when(managerUnderTest).loadState();
        doNothing().when(managerUnderTest).saveState();
        final OneDriveConnectionBuilder builderMock = setUpOneDriveConnectionBuilderMock();

        try (final MockedStatic<OneDriveConnectionBuilder> builderMockedStatic =
                     mockStatic(OneDriveConnectionBuilder.class)) {
            builderMockedStatic.when(() -> OneDriveConnectionBuilder.newInstance()).thenReturn(builderMock);

            final OneDrive actual = managerUnderTest.fetchOneDrive();

            assertAll(
                    () -> assertNotNull(actual),
                    () -> verify(builderMock).build(eq(mockAuthInfo)),
                    () -> verify(managerUnderTest, never())
                            .authenticate(any(OneDriveFactoryStateManager.CredentialConfig.class)),
                    () -> verify(managerUnderTest).loadCredentialConfig(),
                    () -> verify(managerUnderTest).loadState(),
                    () -> verify(managerUnderTest).saveState());
        }
    }

    @SneakyThrows
    @Test
    public void fetchOneDrive_withOAuthReceiverException_shouldThrowException() {
        doReturn(mockConfig).when(managerUnderTest).loadCredentialConfig();
        doReturn(Optional.empty()).when(managerUnderTest).loadState();
        doThrow(new OAuthReceiverException("Exception"))
                .when(managerUnderTest)
                .authenticate(any(OneDriveFactoryStateManager.CredentialConfig.class));
        final OneDriveConnectionBuilder builderMock = setUpOneDriveConnectionBuilderMock();

        try (final MockedStatic<OneDriveConnectionBuilder> builderMockedStatic =
                     mockStatic(OneDriveConnectionBuilder.class)) {
            builderMockedStatic.when(() -> OneDriveConnectionBuilder.newInstance()).thenReturn(builderMock);

            assertThrows(OAuthReceiverException.class, () -> managerUnderTest.fetchOneDrive());
        }
    }

    @SneakyThrows
    @Test
    public void fetchOnedrive_withIOException_shouldThrowException() {
        doThrow(new IOException("Exception")).when(managerUnderTest).loadCredentialConfig();
        assertThrows(IOException.class, () -> managerUnderTest.fetchOneDrive());
    }

    /////////////////////
    // loadState
    /////////////////////

    @SneakyThrows
    @Test
    public void loadState_withValidState_shouldReturnAuthInfo() {
        final AuthInfo mockAuthInfo = mock(AuthInfo.class);

        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class);
             final MockedStatic<AuthInfo> authInfoMockedStatic = mockStatic(AuthInfo.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn("JsonState");
            authInfoMockedStatic.when(() -> AuthInfo.fromJson(any(Gson.class), anyString())).thenReturn(mockAuthInfo);

            assertEquals(mockAuthInfo, managerUnderTest.loadState().get());
        }
    }

    @SneakyThrows
    @Test
    public void loadState_withNonExistentStateFile_shouldReturnEmptyOptional() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertFalse(managerUnderTest.loadState().isPresent());
        }
    }

    @SneakyThrows
    @Test
    public void loadState_withNonReadableFile_shouldReturnEmptyOptional() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(false);

            assertFalse(managerUnderTest.loadState().isPresent());
        }
    }

    @SneakyThrows
    @Test
    public void loadState_withBlankJsonState_shouldReturnEmptyOptional() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenReturn(null);

            assertFalse(managerUnderTest.loadState().isPresent());
        }
    }

    @SneakyThrows
    @Test
    public void loadStatus_withIOException_shouldThrowException() {
        try (final MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.isReadable(any(Path.class))).thenReturn(true);
            filesMockedStatic.when(() -> Files.readString(any(Path.class))).thenThrow(new IOException("Exception"));

            assertThrows(IOException.class, () -> managerUnderTest.loadState());
        }
    }

    ///////////////////////////
    // loadCredentialConfig
    ///////////////////////////

    @SneakyThrows
    @Test
    public void loadCredentialConfig_withNoConfigSet_shouldReturnConfig() {
        managerUnderTest.setCredentialConfig(null);
        try (final MockedStatic<OneDriveFactoryStateManager.CredentialConfig> configMockedStatic =
                mockStatic(OneDriveFactoryStateManager.CredentialConfig.class)) {
            configMockedStatic.when(() -> OneDriveFactoryStateManager.CredentialConfig
                    .loadDefaultCredentialConfigResource(any(Gson.class)))
                    .thenReturn(mockConfig);

            final OneDriveFactoryStateManager.CredentialConfig actual = managerUnderTest.loadCredentialConfig();

            assertAll(
                    () -> assertEquals(mockConfig, actual),
                    () -> configMockedStatic.verify(
                            () -> OneDriveFactoryStateManager.CredentialConfig
                                    .loadDefaultCredentialConfigResource(isA(Gson.class))));
        }
    }

    @SneakyThrows
    @Test
    public void loadCredentialConfig_withIOException_shouldThrowException() {
        managerUnderTest.setCredentialConfig(null);
        try (final MockedStatic<OneDriveFactoryStateManager.CredentialConfig> configMockedStatic =
                     mockStatic(OneDriveFactoryStateManager.CredentialConfig.class)) {
            configMockedStatic.when(() -> OneDriveFactoryStateManager.CredentialConfig
                            .loadDefaultCredentialConfigResource(any(Gson.class)))
                    .thenThrow(new IOException("Exception"));

            assertThrows(IOException.class, () ->  managerUnderTest.loadCredentialConfig());
        }
    }

    @SneakyThrows
    @Test
    public void loadCredentialConfig_withConfigSet_shouldReturnConfig() {
        assertEquals(mockConfig, managerUnderTest.loadCredentialConfig());
    }

    /////////////////////
    // authenticate
    /////////////////////

    @SneakyThrows
    @Test
    public void authenticate_withConfig_shouldReturnAuthCode() {
        final OneDriveOAuthReceiver mockReceiver = mock(OneDriveOAuthReceiver.class);
        when(mockReceiver.getAuthCodeUri()).thenReturn("AuthCodeUri");
        when(mockReceiver.waitForCode()).thenReturn("AuthCode");
        when(mockReceiver.start()).thenReturn(mockReceiver);
        final OneDriveOAuthReceiver.OneDriveOAuthReceiverBuilder receiverBuilder = setUpReceiverBuilder(mockReceiver);
        final OneDriveFactoryStateManager.CredentialConfig mockConfig =
                mock(OneDriveFactoryStateManager.CredentialConfig.class);
        when(mockConfig.getClientId()).thenReturn("ClientId");

        try (final MockedStatic<OneDriveOAuthReceiver> receiverMockedStatic = mockStatic(OneDriveOAuthReceiver.class);
             final MockedStatic<OAuthReceiver> receiverBaseMockedStatic = mockStatic(OAuthReceiver.class)) {
            receiverMockedStatic.when(() -> OneDriveOAuthReceiver.builder()).thenReturn(receiverBuilder);

            assertEquals("AuthCode", managerUnderTest.authenticate(mockConfig));
        }
    }

    @SneakyThrows
    @Test
    public void authenticate_withOAuthReceiverException_shouldThrowException() {
        final OneDriveOAuthReceiver mockReceiver = mock(OneDriveOAuthReceiver.class);
        when(mockReceiver.start()).thenThrow(new OAuthReceiverException("Exception"));
        final OneDriveOAuthReceiver.OneDriveOAuthReceiverBuilder receiverBuilder = setUpReceiverBuilder(mockReceiver);
        final OneDriveFactoryStateManager.CredentialConfig mockConfig =
                mock(OneDriveFactoryStateManager.CredentialConfig.class);
        when(mockConfig.getClientId()).thenReturn("ClientId");

        try (final MockedStatic<OneDriveOAuthReceiver> receiverMockedStatic = mockStatic(OneDriveOAuthReceiver.class)) {
            receiverMockedStatic.when(() -> OneDriveOAuthReceiver.builder()).thenReturn(receiverBuilder);

            assertThrows(OAuthReceiverException.class, () -> managerUnderTest.authenticate(mockConfig));
        }
    }

    private OneDriveOAuthReceiver.OneDriveOAuthReceiverBuilder setUpReceiverBuilder(
            final OneDriveOAuthReceiver receiver) {
        final OneDriveOAuthReceiver.OneDriveOAuthReceiverBuilder mockBuilder =
                mock(OneDriveOAuthReceiver.OneDriveOAuthReceiverBuilder.class);
        when(mockBuilder.scopes(anyList())).thenReturn(mockBuilder);
        when(mockBuilder.callbackPath(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.port(anyInt())).thenReturn(mockBuilder);
        when(mockBuilder.clientId(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(receiver);
        return mockBuilder;
    }

    private OneDriveConnectionBuilder setUpOneDriveConnectionBuilderMock() {
        final OneDriveConnectionBuilder builder = mock(OneDriveConnectionBuilder.class);
        when(builder.httpClient(any(OkHttpClient.class))).thenReturn(builder);
        when(builder.clientId(anyString())).thenReturn(builder);
        when(builder.clientSecret(anyString())).thenReturn(builder);
        when(builder.redirectUrl(anyString())).thenReturn(builder);
        final OneDriveConnection mockConnection = mock(OneDriveConnection.class);
        lenient().when(builder.build(anyString())).thenReturn(mockConnection);
        lenient().when(builder.build(any(AuthInfo.class))).thenReturn(mockConnection);

        return builder;
    }
}
