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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * Represents the authentication information for OAuth (OBO) access to a user's MS OneDrive account.
 * This is returned in response when making calls to acquire the initial token or when refreshing the token.
 * <p>
 * Note: For business accounts, the auth tokens are only valid with a single resource. If
 * the user is to access a different resource (different service/site), then new access tokens are required.
 * See {@link BusinessAccountAuthManager} for more information.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/graph/auth-v2-user?tabs=http">Official documentation</a>
 */
@Slf4j
@Builder
@Getter
@EqualsAndHashCode
public class AuthInfo {
    private static final String STANDARD_SPACE = " ";
    private static final String URL_ENCODED_SPACE = "%20";

    /** The auth token type. Default is {@code Bearer}. */
    @Builder.Default
    private final String tokenType = "Bearer";
    /** The list of scopes, or permissions to access the Graph API. */
    @Builder.Default
    private final List<String> scopes = Collections.emptyList();
    /** Time in milliseconds when the auth token expires. */
    private final long expiresIn;
    /** Time in milliseconds when the auth token expires. */
    private final long extExpiresIn;
    /** The current authorization token used to make API requests. */
    @NonNull
    private final String accessToken;
    /** The current refresh token used to refresh access tokens. */
    @NonNull
    private final String refreshToken;
    /** The associated resource identifier associated with the access tokens (business accounts only). */
    @Builder.Default
    private final String resourceId = StringUtils.EMPTY;

    /**
     * Deserializes the given {@code authInfoJson} string to a new {@code AuthInfo} object.
     *
     * @param authInfoJson the JSON-formatted auth info
     * @return the new {@code AuthInfo} object
     * @throws JsonSyntaxException if there is an error while deserializing the JSON string
     */
    public static AuthInfo fromJson(final String authInfoJson) {
        final Gson gson = GsonFactory.getInstance().getInstanceForAuthManager();
        final AuthInfoInternal internalAuthInfo = gson.fromJson(authInfoJson, AuthInfoInternal.class);
        return AuthInfo.builder()
                .accessToken(internalAuthInfo.getAccessToken())
                .expiresIn(internalAuthInfo.getExpiresIn())
                .extExpiresIn(internalAuthInfo.getExtExpiresIn())
                .refreshToken(internalAuthInfo.getRefreshToken())
                .resourceId(internalAuthInfo.getResourceId())
                .scopes(fromScope(internalAuthInfo.getScope()))
                .tokenType(internalAuthInfo.getTokenType())
                .build();
    }

    /**
     * Gets the full token that is used in request headers to sign API requests.
     * Full token strings are formatted as {@code "[Token Type] [Access token]"}
     *
     * @return the full token
     */
    public String getFullToken() {
        return new StringJoiner(STANDARD_SPACE)
                .add(tokenType)
                .add(accessToken)
                .toString();
    }

    /**
     * Serializes this {@code AuthInfo} to a JSON formatted string.
     *
     * @return the JSON formatted {@code AuthInfo}
     */
    public String toJson() {
        final Gson gson = GsonFactory.getInstance().getInstanceForAuthManager();
        final AuthInfoInternal internalAuthInfo = AuthInfoInternal.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .extExpiresIn(extExpiresIn)
                .refreshToken(refreshToken)
                .resourceId(resourceId)
                .scope(toScope(scopes))
                .tokenType(tokenType)
                .build();
        return gson.toJson(internalAuthInfo);
    }

    /**
     * Creates a copy of this object while injecting the given {@code resourceId}.
     *
     * @param resourceId the resource identifier associated with the auth tokens
     * @return the copy
     */
    public AuthInfo copyWithResourceId(final String resourceId) {
        return AuthInfo.builder()
                .accessToken(getAccessToken())
                .expiresIn(getExpiresIn())
                .extExpiresIn(getExtExpiresIn())
                .refreshToken(getRefreshToken())
                .resourceId(resourceId)
                .scopes(getScopes())
                .tokenType(getTokenType())
                .build();
    }

    private static String toScope(final List<String> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return StringUtils.EMPTY;
        }

        return String.join(URL_ENCODED_SPACE, scopes);
    }

    private static List<String> fromScope(final String scope) {
        if (StringUtils.isBlank(scope)) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(
                Arrays.stream(scope.split(" "))
                        .map(String::trim)
                        .collect(Collectors.toList()));
    }

    /**
     * Internal representation that is actually serialized and deserialized to/from JSON. This is to serialize
     * the list of scopes to a space-delimited string used with API resource types that require it.
     */
    @Builder
    @Getter
    private static class AuthInfoInternal {
        /** The type of token. */
        private final String tokenType;
        /** The scopes associated with the authentication. */
        private final String scope;
        /** The expiration time in milliseconds. */
        private final long expiresIn;
        /** The expiration time in milliseconds. */
        private final long extExpiresIn;
        /** The current access token. */
        private final String accessToken;
        /** the current refresh token. */
        private final String refreshToken;
        /** The associated resource identifier associated with the access tokens (business accounts only). */
        private final String resourceId;
    }
}
