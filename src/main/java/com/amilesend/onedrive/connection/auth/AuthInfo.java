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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;


/**
 * Represents the authentication information for OAuth (OBO) access to a user's MS OneDrive account.
 * This is returned in response when making calls to acquire the initial token or when refreshing the token.
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

    @Builder.Default
    private final String tokenType = "bearer";
    @Builder.Default
    private final List<String> scopes = Collections.emptyList();
    private final long expiresIn;
    private final long extExpiresIn;
    @NonNull
    private final String accessToken;
    @NonNull
    private final String refreshToken;

    /**
     * Deserializes the given {@code authInfoJson} string and {@link Gson} to a new {@code AuthInfo} object.
     *
     * @param gson used to deserialize the JSON formatted string to a {@code AuthInfo} object
     * @param authInfoJson the JSON-formatted auth info
     * @return the new {@code AuthInfo} object
     * @throws JsonSyntaxException if there is an error while deserializing the JSON string
     */
    public static AuthInfo fromJson(final Gson gson, final String authInfoJson) {
        final AuthInfoInternal internalAuthInfo = gson.fromJson(authInfoJson, AuthInfoInternal.class);
        return AuthInfo.builder()
                .tokenType(internalAuthInfo.getTokenType())
                .scopes(fromScope(internalAuthInfo.getScope()))
                .expiresIn(internalAuthInfo.getExtExpiresIn())
                .extExpiresIn(internalAuthInfo.getExtExpiresIn())
                .accessToken(internalAuthInfo.getAccessToken())
                .refreshToken(internalAuthInfo.getRefreshToken())
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
     * Serializes this {@code AuthInfo} to a JSON formatted string with the given {@link Gson}.
     *
     * @param gson used to serialize this object to a JSON formatted string
     * @return the JSON formatted {@code AuthInfo}
     */
    public String toJson(final Gson gson) {
        final AuthInfoInternal internalAuthInfo = AuthInfoInternal.builder()
                .tokenType(tokenType)
                .scope(toScope(scopes))
                .expiresIn(expiresIn)
                .extExpiresIn(extExpiresIn)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return gson.toJson(internalAuthInfo);
    }

    private static String toScope(final List<String> scopes) {
        if (CollectionUtils.isEmpty(scopes)) {
            return StringUtils.EMPTY;
        }

        final StringJoiner sj = new StringJoiner(URL_ENCODED_SPACE);
        scopes.forEach(scope -> sj.add(scope.trim()));
        return sj.toString();
    }

    private static List<String> fromScope(final String scope) {
        if (StringUtils.isBlank(scope)) {
            return Collections.emptyList();
        }

        return Arrays.stream(scope.split(" "))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Internal representation that is actually serialized and deserialized to/from JSON. This is to serialize
     * the list of scopes to a space-delimited string used with API resource types that require it.
     */
    @Builder
    @Getter
    @Setter
    private static class AuthInfoInternal {
        private String tokenType;
        private String scope;
        private long expiresIn;
        private long extExpiresIn;
        private String accessToken;
        private String refreshToken;
    }
}
