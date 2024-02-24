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
package com.amilesend.onedrive.connection.auth.oauth;


import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class OneDriveOAuthReceiverTest {
    private static final List<String> scopes = List.of("Scope1", "Scope2", "Scope3");

    private OneDriveOAuthReceiver receiverUnderTest = spy(OneDriveOAuthReceiver.builder()
            .port(9000)
            .callbackPath("/Callback")
            .clientId("ClientId")
            .scopes(scopes)
            .build());

    @SneakyThrows
    @Test
    public void getAuthCodeUri_shouldReturnUri() {
        doReturn("RedirectUri").when(receiverUnderTest).getRedirectUri();

        assertEquals(
                "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
                        + "?client_id=ClientId"
                        + "&scope=Scope1%20Scope2%20Scope3"
                        + "&response_type=code"
                        + "&redirect_uri=RedirectUri",
                receiverUnderTest.getAuthCodeUri());
    }
}
