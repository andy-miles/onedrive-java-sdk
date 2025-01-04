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

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.FORM_DATA;

/**
 * Manager that is responsible for obtaining and refreshing tokens to interact with a personal
 * OneDrive account.  Note: This does not manage the initial stages of the OAUTH request
 * flow and instead relies on a provided auth code or a pre-existing refresh token.
 * <p>Example initializing with an auth code:</p>
 * <pre>
 * PersonalAccountAuthManager.builderWithAuthCode()
 *         .httpClient(client) // the OKHttpClient instance
 *         .gsonFactory(gsonFactory) // preconfigured Gson instances
 *         .clientId(clientId) // the client ID of your application
 *         .clientSecret(clientSecret) // the client secret of your application
 *         .redirectUrl(redirectUrl) // the redirect URL for OAUTH flow
 *         .authCode(authCode) // The obtained auth code from initial OAUTH handshake
 *         .buildWithAuthCode();
 * </pre>
 * <p>Example initializing with an AuthInfo (pre-existing refresh token):</p>
 * <pre>
 * PersonalAccountAuthManager.builderWithAuthInfo()
 *         .httpClient(client)
 *         .gsonFactory(gsonFactory)
 *         .clientId(clientId)
 *         .clientSecret(clientSecret)
 *         .redirectUrl(redirectUrl)
 *         .authInfo(authInfo) // Instead of an auth code, an AuthInfo object is used to obtain the refresh token
 *         .buildWithAuthInfo();
 * </pre>
 *
 * @see AuthInfo
 */
public class PersonalAccountAuthManager implements AuthManager {
    private static final String PERSONAL_ENDPOINT_URL = "https://graph.microsoft.com/v1.0/me";

    private final Object lock = new Object();

    /** The client identifier. */
    @Getter(AccessLevel.PROTECTED)
    protected final String clientId;
    /** The client secret. */
    @Getter(AccessLevel.PROTECTED)
    protected final String clientSecret;
    /** The redirect URL. */
    @Getter(AccessLevel.PROTECTED)
    protected final String redirectUrl;
    /** The underlying HTTP client. */
    @Getter(AccessLevel.PROTECTED)
    protected final OkHttpClient httpClient;
    /** The GSON instance used for JSON serialization. */
    @Getter(AccessLevel.PROTECTED)
    protected final String baseTokenUrl;
    /** The current authentication information. */
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    protected volatile AuthInfo authInfo;

    /** Used to initialize and manage authentication for a given auth code. */
    @Builder(builderClassName = "BuilderWithAuthCode",
             buildMethodName = "buildWithAuthCode",
             builderMethodName = "builderWithAuthCode")
    protected PersonalAccountAuthManager(
            @NonNull final OkHttpClient httpClient,
            final String baseTokenUrl,
            final String clientId,
            final String clientSecret,
            final String redirectUrl,
            final String authCode) {
        Validate.notBlank(authCode, "authCode must not be blank");
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");

        this.baseTokenUrl = StringUtils.isNotBlank(baseTokenUrl) ? baseTokenUrl : AUTH_TOKEN_URL;
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authInfo = redeemToken(authCode);
    }

    /** Used to manage authentication for an existing AuthInfo that contains a refresh token. */
    @Builder(builderClassName = "BuilderWithAuthInfo",
             builderMethodName = "builderWithAuthInfo",
             buildMethodName = "buildWithAuthInfo")
    private PersonalAccountAuthManager(
            @NonNull final OkHttpClient httpClient,
            final String baseTokenUrl,
            final String clientId,
            final String clientSecret,
            final String redirectUrl,
            @NonNull final AuthInfo authInfo) {
        Validate.notBlank(clientId, "clientId must not be blank");
        Validate.notBlank(clientSecret, "clientSecret must not be blank");
        Validate.notBlank(redirectUrl, "redirectUrl must not be blank");

        this.baseTokenUrl = StringUtils.isNotBlank(baseTokenUrl) ? baseTokenUrl : AUTH_TOKEN_URL;
        this.httpClient = httpClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authInfo = authInfo;
        refreshToken();
    }

    @Override
    public boolean isAuthenticated() {
        return authInfo != null;
    }

    @Override
    public boolean isExpired() {
        if (!isAuthenticated()) {
            throw new AuthManagerException("Not authenticated");
        }

        return System.currentTimeMillis() >= authInfo.getExpiresIn();
    }

    @Override
    public AuthInfo getAuthInfo() {
        refreshIfExpired();
        return authInfo;
    }

    @Override
    public String getAuthenticatedEndpoint() {
        return PERSONAL_ENDPOINT_URL;
    }

    @Override
    public AuthInfo redeemToken(final String authCode) {
        Validate.notBlank(authCode, "authCode must not be blank");
        synchronized (lock) {
            authInfo = AuthManager.fetchAuthInfo(httpClient, new Request.Builder()
                    .url(baseTokenUrl)
                    .header(CONTENT_TYPE, FORM_DATA.toString())
                    .post(new FormBody.Builder()
                            .add(CLIENT_ID_BODY_PARAM, clientId)
                            .add(CLIENT_SECRET_BODY_PARAM, clientSecret)
                            .add(REDIRECT_URI_BODY_PARAM, redirectUrl)
                            .add(AUTH_CODE_BODY_ARAM, authCode)
                            .add(GRANT_TYPE_BODY_PARAM, AUTH_CODE_GRANT_TYPE_BODY_PARAM_VALUE)
                            .build())
                    .build());
            return authInfo;
        }
    }

    @Override
    public AuthInfo refreshToken() {
        synchronized (lock) {
            authInfo = AuthManager.fetchAuthInfo(httpClient, new Request.Builder()
                    .url(baseTokenUrl)
                    .header(CONTENT_TYPE, FORM_DATA_CONTENT_TYPE)
                    .post(new FormBody.Builder()
                            .add(CLIENT_ID_BODY_PARAM, clientId)
                            .add(CLIENT_SECRET_BODY_PARAM, clientSecret)
                            .add(REDIRECT_URI_BODY_PARAM, redirectUrl)
                            .add(REFRESH_TOKEN_BODY_PARAM, authInfo.getRefreshToken())
                            .add(GRANT_TYPE_BODY_PARAM, REFRESH_TOKEN_GRANT_TYPE_BODY_PARAM_VALUE)
                            .build())
                    .build());
            return authInfo;
        }
    }
}
