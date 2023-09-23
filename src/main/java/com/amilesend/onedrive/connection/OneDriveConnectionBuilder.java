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
package com.amilesend.onedrive.connection;

import com.amilesend.onedrive.connection.auth.AuthInfo;
import com.amilesend.onedrive.connection.auth.AuthManager;
import com.amilesend.onedrive.connection.http.OkHttpClientBuilder;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.common.annotations.VisibleForTesting;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/** Builder to configure and return a new {@link OneDriveConnection} instance. */
public class OneDriveConnectionBuilder {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private OkHttpClient httpClient;
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
     * Builds a new {@link OneDriveConnection} instance with the given {@code authCode}.
     *
     * @param authCode the authorization code obtained from the OAuth handshake.
     * @return the connection
     */
    public OneDriveConnection build(final String authCode) {
        validate();
        final OkHttpClient client = getHttpClientOrDefault();
        final AuthManager authManager =AuthManager.builderWithAuthCode()
                .httpClient(client)
                .gson(gsonFactory.getInstanceForAuthManager())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUrl(redirectUrl)
                .authCode(authCode)
                .buildWithAuthCode();
        final String useDefaultBaseUrl = StringUtils.EMPTY;
        return new OneDriveConnection(client, authManager, gsonFactory, useDefaultBaseUrl);
    }

    /**
     * Builds a new {@link OneDriveConnection} instance with the given {@code authInfo}.
     *
     * @param authInfo the authorization information that contains the refresh token
     * @return the connection
     */
    public OneDriveConnection build(final AuthInfo authInfo) {
        validate();
        final OkHttpClient client = getHttpClientOrDefault();
        final AuthManager authManager = AuthManager.builderWithAuthInfo()
                .httpClient(client)
                .gson(gsonFactory.getInstanceForAuthManager())
                .clientId(clientId)
                .clientSecret(clientSecret)
                .redirectUrl(redirectUrl)
                .authInfo(authInfo)
                .buildWithAuthInfo();
        final String useDefaultBaseUrl = StringUtils.EMPTY;
        return new OneDriveConnection(client, authManager, gsonFactory, useDefaultBaseUrl);
    }

    private OkHttpClient getHttpClientOrDefault() {
        return httpClient != null ? httpClient : new OkHttpClientBuilder().build();
    }

    private void validate() {
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");
    }
}
