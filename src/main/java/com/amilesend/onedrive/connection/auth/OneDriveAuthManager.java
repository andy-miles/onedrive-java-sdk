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

import com.amilesend.client.connection.auth.AuthManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.amilesend.client.connection.Connection.Headers.AUTHORIZATION;

/**
 *  The interface that defines the manager that is responsible for obtaining and refreshing tokens
 *  to interact with a OneDrive account. Note: This does not manage the initial stages of the OAUTH request
 *  flow and instead relies on a provided auth code or a pre-existing refresh token.
 */
public interface OneDriveAuthManager extends AuthManager<OneDriveAuthInfo> {
    String AUTH_TOKEN_URL = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
    String CLIENT_ID_BODY_PARAM = "client_id";
    String CLIENT_SECRET_BODY_PARAM = "client_secret";
    String REDIRECT_URI_BODY_PARAM = "redirect_uri";
    String AUTH_CODE_BODY_ARAM = "code";
    String REFRESH_TOKEN_BODY_PARAM = "refresh_token";
    String GRANT_TYPE_BODY_PARAM = "grant_type";
    String AUTH_CODE_GRANT_TYPE_BODY_PARAM_VALUE = "authorization_code";
    String REFRESH_TOKEN_GRANT_TYPE_BODY_PARAM_VALUE = REFRESH_TOKEN_BODY_PARAM;

    @Override
    default Request.Builder addAuthentication(final Request.Builder requestBuilder) {
        return requestBuilder.addHeader(AUTHORIZATION, refreshIfExpiredAndFetchFullToken());
    }

    /**
     * Determines if the current authentication information is expired.
     *
     * @return {@code true} if expired; else, {@code false}
     */
    boolean isExpired();

    /**
     * Checks to see if the current authentication info is expired and refreshes the tokens
     * accordingly.
     */
    default void refreshIfExpired() {
        if (isExpired()) {
            refreshToken();
        }
    }

    /**
     * Helper method to refresh authentication if expired and return the full token used in request headers.
     *
     * @return the full auth token
     */
    default String refreshIfExpiredAndFetchFullToken() {
        refreshIfExpired();
        return getAuthInfo().getFullToken();
    }

    /** Retrieves the associated endpoint to use for OneDrive operations. */
    String getAuthenticatedEndpoint();

    /**
     * Issues a request to redeem the given {@code authCode} in order to retrieve access and refresh tokens as a
     * {@link OneDriveAuthInfo}.
     *
     * @param authCode the authorization code
     * @return the authorization information
     * @see OneDriveAuthInfo
     */
    OneDriveAuthInfo redeemToken(String authCode);

    /**
     * Issues a request to refresh the auth tokens and returns the refreshed tokens as a {@link OneDriveAuthInfo}.
     *
     * @return the authorization information
     * @see OneDriveAuthInfo
     */
    OneDriveAuthInfo refreshToken();

    /**
     * Helper method to dispatch the request to redeem or refresh authorization tokens.
     *
     * @param httpClient the http client
     * @param request the request
     * @return the authorization information
     */
    static OneDriveAuthInfo fetchAuthInfo(final OkHttpClient httpClient, final Request request) {
        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new AuthManagerException("Unsuccessful refresh token request: " + response);
                }

                final String json = response.body().string();
                return OneDriveAuthInfo.fromJson(json);
            }
        } catch (final AuthManagerException ex) {
            throw ex;
        } catch (final Exception ex) {
            throw new AuthManagerException("Error refreshing token: " + ex.getMessage(), ex);
        }
    }
}
