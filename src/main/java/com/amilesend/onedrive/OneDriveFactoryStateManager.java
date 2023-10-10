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
import com.amilesend.onedrive.connection.auth.oauth.OAuthReceiverException;
import com.amilesend.onedrive.connection.auth.oauth.OneDriveOAuthReceiver;
import com.amilesend.onedrive.connection.auth.store.AuthInfoStore;
import com.amilesend.onedrive.connection.auth.store.AuthInfoStoreException;
import com.amilesend.onedrive.connection.auth.store.SingleUserFileBasedAuthInfoStore;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.amilesend.onedrive.connection.auth.oauth.OAuthReceiver.browse;

/**
 * A factory that vends authenticated {@link OneDrive} instances for a single authenticated user. It automatically
 * instantiates a new OAuth flow for first-time user authorization grants and leverages persisted refresh tokens to
 * vend subsequent instances.
 * <p>
 * See {@link CredentialConfig} on how you can configure your own application client credentials.
 * <p>
 * This factory maintains the OneDrive instance for repeated access. While the underlying connection automatically
 * refreshes the tokens during the object runtime lifecycle, it is recommended to obtain this instance each time to
 * automatically persist the state to disk so that future consuming application instances do not have to initiate a
 * new OAuth flow due to stale refresh tokens. Token state can be manually persisted via {@link #saveState()}}, but
 * can be more easily done with levering try-with-resources:
 * <pre>
 * try (OneDriveFactoryStateManager manager= OneDriveFactoryStateManager.builder()
 *                 .stateFile(Paths.get("./OneDriveState.json"))
 *                 .build()) {
 *     DriveFolder root = manager.getInstance().getUserDrive().getRootFolder();
 *
 *     // Perform operations on the root folder's contents
 * }
 * </pre>
 * <p>
 * While this factory is customizable through its builder, the defaults are intended to simplify configuration for a
 * majority of use-cases (e.g., for a desktop application). The only required attribute is defining the path to the
 * authentication info state file. If your use-case requires configuring the underlying {@code OkHttpClient} instance
 * (e.g., configuring your own SSL cert verification, proxy, and/or connection timeouts), you can configure the client
 * with the provided {@link OkHttpClientBuilder}, or alternatively with OkHttp's builder: {@link OkHttpClient.Builder}.
 */
@Slf4j
public class OneDriveFactoryStateManager<T extends OneDrive> implements AutoCloseable {
    private static final int DEFAULT_RECEIVER_PORT = 8890;
    private static final String DEFAULT_CALLBACK_PATH = "/Callback";
    private static final String DEFAULT_REDIRECT_URL = "http://localhost:8890" + DEFAULT_CALLBACK_PATH;
    private static final List<String> DEFAULT_SCOPES = List.of("Files.ReadWrite.All", "offline_access", "User.Read");
    private static final String DEFAULT_USER_AUTH_KEY = "DefaultUser";

    /** The store used to persist user auth token state. */
    private final AuthInfoStore authInfoStore;
    /** The JSON serializer configured for persisting auth state. */
    // Optional
    private final Gson stateGson;
    // Optional
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private CredentialConfig credentialConfig;
    /** The http client. */
    // Optional for custom configuration (e.g., SSL, proxy, etc.).
    private OkHttpClient httpClient;
    /** The port for the OAUTH redirect receiver to listen on. */
    private int receiverPort;
    /** The list of scopes (permissions) for accessing the Graph API. */
    private List<String> scopes;
    /** The redirect URL for the OAUTH redirect receiver. */
    private String redirectUrl;
    /** The callback path for the OAUTH redirect receiver. */
    private String callbackPath;
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private T onedrive;
    private Class<T> onedriveType;

    /**
     * Builds a new {@code OneDriveFactoryStateManager}.
     *
     * @param onedriveType the class type of the OneDrive instance that is to be created
     * @param httpClient the http client
     * @param receiverPort the port for the OAUTH redirect receiver to listen on
     * @param redirectUrl the redirect URL for the OAUTH redirect receiver
     * @param callbackPath the callback path for the OAUTH redirect receiver
     * @param scopes the list of scopes (permissions) for accessing the Graph API
     * @param stateGson the JSON serializer configured for persisting auth state
     * @param credentialConfig the application client credential configuration
     * @param stateFile the optional persisted auth state
     * @param authInfoStore the store used to persist and retrieve the auth state
     */
    @Builder
    private OneDriveFactoryStateManager(final Class<T> onedriveType,
                                        final OkHttpClient httpClient,
                                        final Integer receiverPort,
                                        final String redirectUrl,
                                        final String callbackPath,
                                        final List<String> scopes,
                                        final Gson stateGson,
                                        final CredentialConfig credentialConfig,
                                        final Path stateFile,
                                        final AuthInfoStore authInfoStore) {
        this.onedriveType = onedriveType == null ? (Class<T>) OneDrive.class : onedriveType;
        this.httpClient = httpClient == null ? new OkHttpClientBuilder().build() : httpClient;
        this.stateGson = stateGson == null ? GsonFactory.getInstance().getInstanceForStateManager() : stateGson;
        this.redirectUrl = StringUtils.isBlank(redirectUrl) ? DEFAULT_REDIRECT_URL : redirectUrl;
        this.callbackPath = callbackPath == null ? DEFAULT_CALLBACK_PATH : callbackPath;
        this.scopes = scopes == null ? DEFAULT_SCOPES : scopes;
        this.credentialConfig = credentialConfig;
        this.receiverPort = receiverPort == null ? DEFAULT_RECEIVER_PORT : receiverPort.intValue();
        Validate.isTrue(stateFile != null || authInfoStore != null,
                "Either stateFile or authInfoStore must be defined");
        this.authInfoStore = Optional.ofNullable(authInfoStore)
                .orElseGet(() -> new SingleUserFileBasedAuthInfoStore(stateFile));
    }

    @Override
    public void close() throws Exception {
        saveState();
    }

    /**
     * Obtains an authenticated {@link OneDrive} instance.
     *
     * @return the authenticated OneDrive instance
     * @throws OneDriveException if unable to authenticate while creating a new OneDrive instance
     */
    public T getInstance() throws OneDriveException {
        try {
            final T oneDrive = fetchOneDrive();
            log.info("OneDrive logged in user: {}", oneDrive.getUserDisplayName());
            return oneDrive;
        } catch (final OAuthReceiverException ex) {
            throw new OneDriveException("Error while obtaining OneDrive instance", ex);
        }
    }

    /**
     * Persists the authentication state.
     *
     * @throws OneDriveException if unable to save the authentication information
     */
    public void saveState() throws OneDriveException {
        if (onedrive == null) {
            return;
        }

        try {
            authInfoStore.store(DEFAULT_USER_AUTH_KEY, onedrive.getAuthInfo());
        } catch (final AuthInfoStoreException ex) {
            throw new OneDriveException("Unable to save state: " + ex.getMessage(), ex);
        }
    }

    @VisibleForTesting
    T fetchOneDrive() throws OAuthReceiverException, OneDriveException {
        if (onedrive != null) {
            return onedrive;
        }

        try {
            final CredentialConfig config = loadCredentialConfig();
            final Optional<AuthInfo> authInfoOpt = loadState();
            final OneDriveConnectionBuilder connectionBuilder = OneDriveConnectionBuilder.newInstance()
                    .httpClient(httpClient)
                    .clientId(config.getClientId())
                    .clientSecret(config.getClientSecret())
                    .redirectUrl(redirectUrl);
            OneDriveConnection connection;
            // If persisted state exists, use it to leverage the refresh token; otherwise, obtain the auth code
            if (authInfoOpt.isPresent()) {
                log.debug("Creating OneDriveConnection from persisted state");
                connection = connectionBuilder.build(authInfoOpt.get());
            } else {
                log.debug("No state found. Authenticating application for user");
                connection = connectionBuilder.build(authenticate(config));
            }

            onedrive = onedriveType.getDeclaredConstructor(OneDriveConnection.class).newInstance(connection);
            saveState();
            return onedrive;
        } catch (final IOException | ReflectiveOperationException ex) {
            throw new OneDriveException(
                    "An error occurred while fetching credential or auth state: " + ex.getMessage(), ex);
        }
    }

    @VisibleForTesting
    Optional<AuthInfo> loadState() throws OneDriveException {
        try {
            return Optional.ofNullable(authInfoStore.retrieve(DEFAULT_USER_AUTH_KEY));
        } catch (final AuthInfoStoreException ex) {
            throw new OneDriveException("Unable to load state: " + ex.getMessage(), ex);
        }
    }

    @VisibleForTesting
    CredentialConfig loadCredentialConfig() throws IOException {
        return credentialConfig == null
                ? CredentialConfig.loadDefaultCredentialConfigResource(stateGson)
                : credentialConfig;
    }

    @VisibleForTesting
    String authenticate(final CredentialConfig config) throws OAuthReceiverException {
        try (final OneDriveOAuthReceiver receiver = OneDriveOAuthReceiver.builder()
                .clientId(config.getClientId())
                .port(receiverPort)
                .callbackPath(callbackPath)
                .scopes(scopes)
                .build()
                .start()) {
            browse(receiver.getAuthCodeUri());
            // Obtain the authorization code in order to exchange it for an access token
            final String authCode = receiver.waitForCode();
            log.debug("AuthCode: {}", authCode);
            return authCode;
        }
    }

    /**
     * Defines the consuming application's client credentials.
     * <p>
     * The {@code clientId} and {@code clientSecret} are obtained from the Azure application registration console.
     * See the
     * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/getting-started/app-registration">
     * API documentation</a> for more information.
     * <p>
     * Once your client identifier and secret are obtain, you may bundle your credentials as a JSON formatted text file
     * within your JAR so that it's accessible via a resource. By default, you may save your credentials as
     * {@code ms-onedrive-credentials.json} bundled as a JAR resource.  Example format:
     * <pre>
     * {
     *   "clientId" : "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
     *   "clientSecret" : "xxxxxxxxxxxxxxxxxxxxxxxxxxx"
     * }
     * </pre>
     */
    @Data
    public static class CredentialConfig {
        private static final String DEFAULT_RESOURCE_CONFIG_PATH = "/ms-onedrive-credentials.json";

        private String clientId;
        private String clientSecret;

        public static CredentialConfig loadDefaultCredentialConfigResource(final Gson gson) throws IOException {
            try (final InputStreamReader isr = new InputStreamReader(
                    CredentialConfig.class.getResourceAsStream(DEFAULT_RESOURCE_CONFIG_PATH))) {
                return gson.fromJson(isr, CredentialConfig.class);
            }
        }
    }
}
