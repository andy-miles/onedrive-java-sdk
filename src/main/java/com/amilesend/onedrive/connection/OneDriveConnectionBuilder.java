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

import com.amilesend.onedrive.connection.auth.OneDriveAuthInfo;
import com.amilesend.onedrive.connection.auth.OneDriveAuthManager;
import com.amilesend.onedrive.connection.auth.PersonalAccountAuthManager;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.Validate;

/** Builder to configure and return a new {@link OneDriveConnection} instance. */
public class OneDriveConnectionBuilder {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String userAgent;
    private OkHttpClient httpClient = new OkHttpClientBuilder().build();
    private OneDriveAuthManager authManager;
    private final GsonFactory gsonFactory;

    /**
     * Creates a new {@code OneDriveConnectionBuilder} instance.
     *
     * @return the builder
     */
    public static OneDriveConnectionBuilder newInstance() {
        return new OneDriveConnectionBuilder(new GsonFactory());
    }

    @VisibleForTesting
    OneDriveConnectionBuilder(@NonNull final GsonFactory gsonFactory) {
        this.gsonFactory = gsonFactory;
    }

    /**
     * Sets the user-agent string that is included in requests.
     *
     * @param userAgent the user agent
     * @return this builder
     */
    public OneDriveConnectionBuilder userAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * The underlying http client. This is optional.
     *
     * @param httpClient the http client
     * @return this builder
     */
    public OneDriveConnectionBuilder httpClient(final OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    /**
     * The application client identifier. This is required.
     *
     * @param clientId the application client identifier
     * @return this builder
     */
    public OneDriveConnectionBuilder clientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * The application client secret. This is required.
     *
     * @param clientSecret the application client secret
     * @return this builder
     */
    public OneDriveConnectionBuilder clientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    /**
     * The OAuth redirect URL. This is required.
     *
     * @param redirectUrl the OAuth redirectUrl
     * @return this builder
     */
    public OneDriveConnectionBuilder redirectUrl(final String redirectUrl) {
        this.redirectUrl = redirectUrl;
        return this;
    }

    /**
     * The {@link OneDriveAuthManager} used to manage authentication and access tokens.
     *
     * @param authManager the auth manager
     * @return this builder
     */
    public OneDriveConnectionBuilder authManager(final OneDriveAuthManager authManager) {
        this.authManager = authManager;
        return this;
    }

    /**
     * Builds a new {@link OneDriveConnection} instance with the given {@code authCode}.
     *
     * @param authCode the authorization code obtained from the OAuth handshake.
     * @return the connection
     */
    public OneDriveConnection build(final String authCode) {
        final OneDriveAuthManager authManager = getAuthManagerOrDefault(httpClient, authCode);
        return OneDriveConnection.builder()
                .httpClient(httpClient)
                .authManager(authManager)
                .gsonFactory(gsonFactory)
                .baseUrl(authManager.getAuthenticatedEndpoint())
                .userAgent(userAgent)
                .isGzipContentEncodingEnabled(true)
                .build();
    }

    /**
     * Builds a new {@link OneDriveConnection} instance with the given {@code authInfo}.
     *
     * @param authInfo the authorization information that contains the refresh token
     * @return the connection
     */
    public OneDriveConnection build(final OneDriveAuthInfo authInfo) {
        final OneDriveAuthManager authManager = getAuthManagerOrDefault(httpClient, authInfo);
        return OneDriveConnection.builder()
                .httpClient(httpClient)
                .authManager(authManager)
                .gsonFactory(gsonFactory)
                .baseUrl(authManager.getAuthenticatedEndpoint())
                .userAgent(userAgent)
                .isGzipContentEncodingEnabled(true)
                .build();
    }

    private OneDriveAuthManager getAuthManagerOrDefault(final OkHttpClient httpClient, final String authCode) {
        if (authManager != null) {
            return authManager;
        }

        validateRequiredParametersForAuth();
        return PersonalAccountAuthManager.builderWithAuthCode()
                .httpClient(httpClient)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUrl(redirectUrl)
                .authCode(authCode)
                .buildWithAuthCode();
    }

    private OneDriveAuthManager getAuthManagerOrDefault(final OkHttpClient httpClient, final OneDriveAuthInfo authInfo) {
        if (authManager != null) {
            return authManager;
        }

        validateRequiredParametersForAuth();
        return PersonalAccountAuthManager.builderWithAuthInfo()
                .httpClient(httpClient)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUrl(redirectUrl)
                .authInfo(authInfo)
                .buildWithAuthInfo();
    }

    private void validateRequiredParametersForAuth() {
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");
    }
}
