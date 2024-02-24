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
package com.amilesend.onedrive.connection.auth;

import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.EXPIRES_TIME_MILLIS;
import static com.amilesend.onedrive.connection.auth.PersonalAccountAuthManagerFunctionalTest.TOKEN_JSON_RESPONSE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthInfoTest {
    private static final String TOKEN_NO_SCOPE_JSON_RESPONSE =
            "{" +
            "\"access_token\": \"AccessToken\"," +
            "\"expires_in\": " + EXPIRES_TIME_MILLIS + "," +
            "\"ext_expires_in\": " + EXPIRES_TIME_MILLIS + "," +
            "\"refresh_token\": \"RefreshToken\"," +
            "\"token_type\": \"Bearer\"" +
            "}";

    final Gson gson = GsonFactory.getInstance().getInstanceForAuthManager();

    @Test
    public void fromJson_withValidJson_shouldReturnAuthInfo() {
        final AuthInfo actual = AuthInfo.fromJson(TOKEN_JSON_RESPONSE);

        assertAll(
                () -> assertEquals("AccessToken", actual.getAccessToken()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExpiresIn()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExtExpiresIn()),
                () -> assertEquals("Bearer AccessToken", actual.getFullToken()),
                () -> assertEquals("RefreshToken", actual.getRefreshToken()),
                () -> assertEquals(List.of("Scope1", "Scope2", "Scope3"), actual.getScopes()),
                () -> assertEquals("Bearer", actual.getTokenType()));
    }

    @Test
    public void fromJson_withNoScopes_shouldReturnAuthInfo() {
        final AuthInfo actual = AuthInfo.fromJson(TOKEN_NO_SCOPE_JSON_RESPONSE);

        assertAll(
                () -> assertEquals("AccessToken", actual.getAccessToken()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExpiresIn()),
                () -> assertEquals(EXPIRES_TIME_MILLIS, actual.getExtExpiresIn()),
                () -> assertEquals("Bearer AccessToken", actual.getFullToken()),
                () -> assertEquals("RefreshToken", actual.getRefreshToken()),
                () -> assertTrue(CollectionUtils.isEmpty(actual.getScopes())),
                () -> assertEquals("Bearer", actual.getTokenType()));
    }

    @Test
    public void toJson_withValidAuthInfo_shouldReturnJson() {
        final String actual = AuthInfo.builder()
                .accessToken("AccessToken")
                .expiresIn(EXPIRES_TIME_MILLIS)
                .extExpiresIn(EXPIRES_TIME_MILLIS)
                .refreshToken("RefreshToken")
                .scopes(List.of("Scope1", "Scope2", "Scope3"))
                .build()
                .toJson();

        assertAll(
                () -> assertTrue(actual.startsWith("{")),
                () -> assertTrue(StringUtils.contains(actual, "\"token_type\":\"Bearer\"")),
                () -> assertTrue(StringUtils.contains(actual, "\"scope\":\"Scope1%20Scope2%20Scope3\"")),
                () -> assertTrue(StringUtils.contains(actual, "\"expires_in\":" + EXPIRES_TIME_MILLIS)),
                () -> assertTrue(StringUtils.contains(actual, "\"ext_expires_in\":"+ EXPIRES_TIME_MILLIS)),
                () -> assertTrue(StringUtils.contains(actual, "\"access_token\":\"AccessToken\"")),
                () -> assertTrue(StringUtils.contains(actual, "\"refresh_token\":\"RefreshToken\"")),
                () -> assertTrue(actual.endsWith("}")));
    }

    @Test
    public void toJson_withNoScopes_shouldReturnJson() {
        final String actual = AuthInfo.builder()
                .accessToken("AccessToken")
                .expiresIn(EXPIRES_TIME_MILLIS)
                .extExpiresIn(EXPIRES_TIME_MILLIS)
                .refreshToken("RefreshToken")
                .build()
                .toJson();

        assertAll(
                () -> assertTrue(actual.startsWith("{")),
                () -> assertTrue(StringUtils.contains(actual, "\"token_type\":\"Bearer\"")),
                () -> assertTrue(StringUtils.contains(actual, "\"expires_in\":" + EXPIRES_TIME_MILLIS)),
                () -> assertTrue(StringUtils.contains(actual, "\"ext_expires_in\":"+ EXPIRES_TIME_MILLIS)),
                () -> assertTrue(StringUtils.contains(actual, "\"access_token\":\"AccessToken\"")),
                () -> assertTrue(StringUtils.contains(actual, "\"refresh_token\":\"RefreshToken\"")),
                () -> assertTrue(actual.endsWith("}")));
    }
}
