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
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.FORM_DATA;

/**
 * Manager that is responsible for obtaining and refreshing tokens to interact with a
 * OneDrive account.  Note: This does not manage the initial stages of the OAUTH request
 * flow and instead relies on a provided auth code or a pre-existing refresh token.
 * <p>Example initializing with an auth code:</p>
 * <pre>
 * AuthManager authManager = AuthManager.builderWithAuthCode()
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
 * AuthManager.builderWithAuthInfo()
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
public class AuthManager {
    private static final String AUTH_TOKEN_URL =
            "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    private static final String FORM_DATA_CONTENT_TYPE = FORM_DATA.toString();

    /** The client identifier. */
    private final String clientId;
    /** The client secret. */
    private final String clientSecret;
    /** The redirect URL. */
    private final String redirectUrl;
    /** The underlying HTTP client. */
    private final OkHttpClient httpClient;
    /** The GSON instance used for JSON serialization. */
    private final Gson gson;
    /** The base URL used to authenticate and refresh authorization tokens. */
    private final String baseTokenUrl;
    /** The current authentication informaiton. */
    @Setter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private volatile AuthInfo authInfo;

    /** Used to initialize and manage authentication for a given auth code. */
    @Builder(builderClassName = "BuilderWithAuthCode",
             buildMethodName = "buildWithAuthCode",
             builderMethodName = "builderWithAuthCode")
    private AuthManager(@NonNull final OkHttpClient httpClient,
                        @NonNull final GsonFactory gsonFactory,
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
        this.gson = gsonFactory.newInstanceForAuthManager();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authInfo = redeemToken(authCode);
    }

    /** Used to manage authentication for an existing AuthInfo that contains a refresh token. */
    @Builder(builderClassName = "BuilderWithAuthInfo",
             builderMethodName = "builderWithAuthInfo",
             buildMethodName = "buildWithAuthInfo")
    private AuthManager(@NonNull final OkHttpClient httpClient,
                        @NonNull final GsonFactory gsonFactory,
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
        this.gson = gsonFactory.newInstanceForAuthManager();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authInfo = authInfo;
        refreshToken();
    }

    /**
     * Determines if the current authentication information is up-to-date.
     *
     * @return {@code true} if authenticated; else, {@code false}
     */
    public boolean isAuthenticated() {
        return authInfo != null;
    }

    /**
     * Determines if the current authentication information is expired.
     *
     * @return {@code true} if expired; else, {@code false}
     */
    public boolean isExpired() {
        if (!isAuthenticated()) {
            throw new AuthManagerException("Not authenticated");
        }

        return System.currentTimeMillis() >= authInfo.getExpiresIn();
    }

    /**
     * Checks to see if the current authentication info is expired and refreshes the tokens
     * accordingly.
     */
    public void refreshIfExpired() {
        if (isExpired()) {
            refreshToken();
        }
    }

    /**
     * Helper method to refresh authentication if expired and return the full token used in request headers.
     *
     * @return the full auth token
     */
    public String refreshIfExpiredAndFetchFullToken() {
        refreshIfExpired();
        return getAuthInfo().getFullToken();
    }

    /**
     * Retrieves the current authentication info.
     *
     * @return the authentication info
     * @see AuthInfo
     */
    public AuthInfo getAuthInfo() {
        refreshIfExpired();
        return authInfo;
    }

    private AuthInfo redeemToken(final String authCode) {
        return fetchAuthInfo(new Request.Builder()
                .url(baseTokenUrl)
                .header(CONTENT_TYPE, FORM_DATA.toString())
                .post(new FormBody.Builder()
                        .add("client_id", clientId)
                        .add("client_secret", clientSecret)
                        .add("redirect_uri", redirectUrl)
                        .add("code", authCode)
                        .add("grant_type", "authorization_code")
                        .build())
                .build());
    }

    private AuthInfo refreshToken() {
       return fetchAuthInfo(new Request.Builder()
                .url(baseTokenUrl)
                .header(CONTENT_TYPE, FORM_DATA_CONTENT_TYPE)
                .post(new FormBody.Builder()
                        .add("client_id", clientId)
                        .add("client_secret", clientSecret)
                        .add("redirect_uri", redirectUrl)
                        .add("refresh_token", authInfo.getRefreshToken())
                        .add("grant_type", "refresh_token")
                        .build())
                .build());
    }

    private AuthInfo fetchAuthInfo(final Request request) {
        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AuthManagerException("Unsuccessful refresh token request: " + response);
                }

                final String json = response.body().string();
                authInfo = AuthInfo.fromJson(gson, json);
                return authInfo;
            }
        } catch (final AuthManagerException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AuthManagerException("Error refreshing token: " + ex.getMessage(), ex);
        }
    }
}
