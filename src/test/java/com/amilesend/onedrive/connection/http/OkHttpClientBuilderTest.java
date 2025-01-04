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
package com.amilesend.onedrive.connection.http;

import com.amilesend.onedrive.connection.OneDriveConnectionException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.tls.OkHostnameVerifier;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class OkHttpClientBuilderTest {

    ////////////////////
    // SSL
    ////////////////////

    @Test
    public void builder_withSSLConfigured_shouldReturnClient() {
        final X509TrustManager trustManager = new NoOpTrustManager();
        final HostnameVerifier hostnameVerifier = new DummyHostnameVerifier(true);

        final OkHttpClient actual = new OkHttpClientBuilder()
                .trustManager(trustManager)
                .hostnameVerifier(hostnameVerifier)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(trustManager, actual.x509TrustManager()),
                () -> assertEquals(hostnameVerifier, actual.hostnameVerifier()));
    }

    @Test
    public void builder_withSSLAndNoHostnameVerifierConfigured_shouldReturnClient() {
        final X509TrustManager trustManager = new NoOpTrustManager();

        final OkHttpClient actual = new OkHttpClientBuilder()
                .trustManager(trustManager)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(trustManager, actual.x509TrustManager()),
                () -> assertInstanceOf(OkHostnameVerifier.class, actual.hostnameVerifier()));
    }

    @Test
    public void builder_withSSLAndNoSuchAlgorithmException_shouldThrowException() {
        try (final MockedStatic<SSLContext> sslContextMockedStatic = mockStatic(SSLContext.class)) {
            sslContextMockedStatic.when(() -> SSLContext.getInstance(anyString()))
                    .thenThrow(new NoSuchAlgorithmException("Exception"));

            final Throwable thrown = assertThrows(OneDriveConnectionException.class,
                    () -> new OkHttpClientBuilder()
                            .trustManager(new NoOpTrustManager())
                            .build());

            assertInstanceOf(NoSuchAlgorithmException.class, thrown.getCause());
        }
    }

    @SneakyThrows
    @Test
    public void builder_withSSLAndKeyManagementException_shouldThrowException() {
        final SSLContext mockContext = mock(SSLContext.class);
        doThrow(new KeyManagementException("Exception"))
                .when(mockContext)
                .init(any(), any(TrustManager[].class), any(SecureRandom.class));

        try (final MockedStatic<SSLContext> sslContextMockedStatic = mockStatic(SSLContext.class)) {
            sslContextMockedStatic.when(() -> SSLContext.getInstance(anyString()))
                    .thenReturn(mockContext);

            final Throwable thrown = assertThrows(OneDriveConnectionException.class,
                    () -> new OkHttpClientBuilder()
                            .trustManager(new NoOpTrustManager())
                            .build());

            assertInstanceOf(KeyManagementException.class, thrown.getCause());
        }
    }

    @Test
    public void builder_withNoCustomSSLConfigured_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder().build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertInstanceOf(X509TrustManager.class, actual.x509TrustManager()),
                () -> assertInstanceOf(OkHostnameVerifier.class, actual.hostnameVerifier()));
    }

    ////////////////////
    // Proxy
    ////////////////////

    @Test
    public void builder_withProxyAndAuthenticator_shouldReturnClient() {
        final Proxy proxy = newProxy();
        final Authenticator authenticator = Authenticator.NONE;

        final OkHttpClient actual = new OkHttpClientBuilder()
                .proxy(proxy, authenticator)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(proxy, actual.proxy()),
                () -> assertEquals(authenticator, actual.authenticator()));
    }

    @Test
    public void builder_withProxyAndCredentials_shouldReturnClient() {
        final Proxy proxy = newProxy();

        final OkHttpClient actual = new OkHttpClientBuilder()
                .proxy(proxy, "username", "password")
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(proxy, actual.proxy()),
                () -> assertInstanceOf(Authenticator.class, actual.proxyAuthenticator()));


    }

    @SneakyThrows
    @Test
    public void builder_WithProxyAndCredentials_shouldAuthenticateWithBasicCredentials() {
        final OkHttpClient client = new OkHttpClientBuilder()
                .proxy(newProxy(), "username", "password")
                .build();

        // Test the Authenticator lambda
        final Request mockNewRequest = mock(Request.class);
        final Request.Builder mockRequestBuilder = mock(Request.Builder.class);
        when(mockRequestBuilder.header(anyString(), anyString())).thenReturn(mockRequestBuilder);
        when(mockRequestBuilder.build()).thenReturn(mockNewRequest);
        final Request mockOriginalRequest = mock(Request.class);
        when(mockOriginalRequest.newBuilder()).thenReturn(mockRequestBuilder);
        final Response mockResponse = mock(Response.class);
        when(mockResponse.request()).thenReturn(mockOriginalRequest);

        final Authenticator authenticator = client.proxyAuthenticator();
        final Request actual = authenticator.authenticate(mock(Route.class), mockResponse);

        assertEquals(mockNewRequest, actual);
    }

    @Test
    public void builder_withProxyAndNoAuthConfigured_shouldReturnClient() {
        final Proxy proxy = newProxy();

        final OkHttpClient actual = new OkHttpClientBuilder()
                .proxy(proxy, null, null)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(proxy, actual.proxy()),
                () -> assertInstanceOf(Authenticator.class, actual.proxyAuthenticator()));
    }

    private static class NoOpTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            // Nothing
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType)
                throws CertificateException {
            //Nothing
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    ////////////////////
    // Other attributes
    ////////////////////

    @Test
    public void builder_withIsForTest_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder()
                .isForTest(true)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertTrue(actual.connectionSpecs().contains(ConnectionSpec.CLEARTEXT)),
                () -> assertTrue(actual.connectionSpecs().contains(ConnectionSpec.MODERN_TLS)));
    }

    @Test
    public void builder_withIsForProduction_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder().build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertFalse(actual.connectionSpecs().contains(ConnectionSpec.CLEARTEXT)),
                () -> assertTrue(actual.connectionSpecs().contains(ConnectionSpec.MODERN_TLS)));
    }

    @Test
    public void builder_withRedirectsDisabled_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder()
                .isRedirectsAllowed(false)
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertFalse(actual.followRedirects()),
                () -> assertFalse(actual.followSslRedirects()));
    }

    @Test
    public void builder_withRedirectsEnabled_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder().build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertTrue(actual.followRedirects()),
                () -> assertTrue(actual.followSslRedirects()));
    }

    @Test
    public void builder_withTimeoutsConfigured_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .readTimeout(Duration.ofSeconds(2))
                .writeTimeout(Duration.ofSeconds(3))
                .build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(1000, actual.connectTimeoutMillis()),
                () -> assertEquals(2000, actual.readTimeoutMillis()),
                () -> assertEquals(3000, actual.writeTimeoutMillis()));
    }

    @Test
    public void builder_withNoTimeoutsConfigured_shouldReturnClient() {
        final OkHttpClient actual = new OkHttpClientBuilder().build();

        assertAll(
                () -> assertNotNull(actual),
                () -> assertEquals(10000, actual.connectTimeoutMillis()),
                () -> assertEquals(10000, actual.readTimeoutMillis()),
                () -> assertEquals(10000, actual.writeTimeoutMillis()));
    }

    @RequiredArgsConstructor
    public static class DummyHostnameVerifier implements HostnameVerifier {
        private final boolean isVerified;


        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return isVerified;
        }
    }

    private Proxy newProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 3128));
    }
}
