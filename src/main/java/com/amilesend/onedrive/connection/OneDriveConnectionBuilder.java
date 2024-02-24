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
package com.amilesend.onedrive.connection;

import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.connection.auth.AuthManager;
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
    private OkHttpClient httpClient = new OkHttpClientBuilder().build();
    private AuthManager authManager;
    private final GsonFactory gsonFactory;

    /**
     * Creates a new {@code OneDriveConnectionBuilder} instance.
     *
     * @return the builder
     */
    public static OneDriveConnectionBuilder newInstance() {
        return new OneDriveConnectionBuilder(GsonFactory.getInstance());
    }

    @VisibleForTesting
    OneDriveConnectionBuilder(@NonNull final GsonFactory gsonFactory) {
        this.gsonFactory = gsonFactory;
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
     * The {@link AuthManager} used to manage authentication and access tokens.
     *
     * @param authManager the auth manager
     * @return this builder
     */
    public OneDriveConnectionBuilder authManager(final AuthManager authManager) {
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
        final AuthManager authManager = getAuthManagerOrDefault(httpClient, authCode);
        return new OneDriveConnection(httpClient, authManager, gsonFactory, authManager.getAuthenticatedEndpoint());
    }

    /**
     * Builds a new {@link OneDriveConnection} instance with the given {@code authInfo}.
     *
     * @param authInfo the authorization information that contains the refresh token
     * @return the connection
     */
    public OneDriveConnection build(final AuthInfo authInfo) {
        final AuthManager authManager = getAuthManagerOrDefault(httpClient, authInfo);
        return new OneDriveConnection(httpClient, authManager, gsonFactory, authManager.getAuthenticatedEndpoint());
    }

    private AuthManager getAuthManagerOrDefault(final OkHttpClient httpClient, final String authCode) {
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

    private AuthManager getAuthManagerOrDefault(final OkHttpClient httpClient, final AuthInfo authInfo) {
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
