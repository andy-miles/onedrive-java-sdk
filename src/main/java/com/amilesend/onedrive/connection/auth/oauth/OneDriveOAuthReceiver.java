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
package com.amilesend.onedrive.connection.auth.oauth;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.StringJoiner;

/**
 * Extends {@link OAuthReceiver} that accounts for MS Graph scopes and client ID in order to provide the URI to query
 * for the auth code during the OAUTH exchange.
 *
 * @see #getAuthCodeUri()
 * @see OAuthReceiver
 */
public class OneDriveOAuthReceiver extends OAuthReceiver {
    /** The registered application client identifier. */
    @Getter
    private final String clientId;
    /** The list of scopes (permissions) for accessing the Graph API. */
    @Getter
    private final List<String> scopes;

    /**
     * Builes a new {@code OneDriveOAuthReceiver}.
     *
     * @param host host of the receiver (Default: {@code localhost})
     * @param port optional port of the receiver to listen on
     * @param callbackPath the path to listen for the redirect (Default: {@code /Callback})
     * @param clientId the registered application client identifier
     * @param scopes the list of scopes (permissions) for accessing the Graph API
     * @param successLandingPageUrl optional URL for a custom successful landing page
     * @param failureLandingPageUrl optional URL for a custom failure landing page
     */
    @Builder
    private OneDriveOAuthReceiver(
            final String host,
            final int port,
            final String callbackPath,
            final String clientId,
            final @NonNull List<String> scopes,
            final String successLandingPageUrl,
            final String failureLandingPageUrl) {
        super(host, port, callbackPath, successLandingPageUrl, failureLandingPageUrl);
        this.clientId = clientId;
        this.scopes = scopes;
    }

    /**
     * Gets the AuthCode URI that is used when initiating an OAuth handshake.
     *
     * @return the AuthCode URI
     * @throws OAuthReceiverException if an error occurred while fetching the redirect URI attribute
     */
    public String getAuthCodeUri() throws OAuthReceiverException {
        return new StringBuilder("https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=")
                .append(getClientId())
                .append("&scope=")
                .append(getFormattedScopes())
                .append("&response_type=code&redirect_uri=")
                .append(getRedirectUri())
                .toString();
    }

    private String getFormattedScopes() {
        final StringJoiner sj = new StringJoiner("%20");
        for (String s : getScopes()) sj.add(s);
        return sj.toString();
    }
}
