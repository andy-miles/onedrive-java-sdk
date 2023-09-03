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

import com.amilesend.onedrive.connection.auth.AuthManager;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.gson.Gson;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public abstract class OneDriveConnectionTestBase {
    protected static final String REQUEST_URL = "http://localhost";
    protected static final int SUCCESS_RESPONSE_CODE = 200;
    protected static final int REMOTE_SUCCESS_RESPONSE_CODE = 202;
    protected static final int REQUEST_ERROR_CODE = 403;
    protected static final int SERVER_ERROR_RESPONSE_CODE = 503;
    protected static final long BYTES_TRANSFERRED = 1024L;

    @Mock
    protected OkHttpClient mockHttpClient;
    @Mock
    protected AuthManager mockAuthManager;
    @Mock
    protected Gson mockGson;
    @Mock
    protected GsonFactory mockGsonFactory;
    protected OneDriveConnection connectionUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockAuthManager.refreshIfExpiredAndFetchFullToken()).thenReturn("FullAuthToken");
        when(mockGsonFactory.newInstanceForConnection(any(OneDriveConnection.class))).thenReturn(mockGson);
        connectionUnderTest =
                spy(new OneDriveConnection(mockHttpClient, mockAuthManager, mockGsonFactory, StringUtils.EMPTY));

    }
    protected Callback getCallbackFromCallMock(final Call mockCall) {
        final ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        verify(mockCall).enqueue(callbackCaptor.capture());
        return callbackCaptor.getValue();
    }

    protected Response newMockedResponse(final int code) {
        final ResponseBody mockBody = mock(ResponseBody.class);
        lenient().when(mockBody.byteStream()).thenReturn(mock(InputStream.class));
        lenient().when(mockBody.source()).thenReturn(mock(BufferedSource.class));

        final Response mockResponse = mock(Response.class);
        lenient().when(mockResponse.code()).thenReturn(code);
        lenient().when(mockResponse.isSuccessful()).thenReturn(code == SUCCESS_RESPONSE_CODE);
        lenient().when(mockResponse.body()).thenReturn(mockBody);

        return mockResponse;
    }

    protected Response newMockedResponse(final int code, final String locationUrl) {
        final Response mockResponse = mock(Response.class);
        when(mockResponse.code()).thenReturn(code);
        lenient().when(mockResponse.isSuccessful()).thenReturn(String.valueOf(code).startsWith("2"));
        lenient().when(mockResponse.header(eq("Location"))).thenReturn(locationUrl);

        return mockResponse;
    }

    @SneakyThrows
    protected Response setUpHttpClientMock(final Response mockResponse) {
        final Call mockCall = mock(Call.class);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);

        return mockResponse;
    }

    @SneakyThrows
    protected Call setUpHttpClientMockAsync() {
        final Call mockCall = mock(Call.class);
        doNothing().when(mockCall).enqueue(any(Callback.class));
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        return mockCall;
    }
}
