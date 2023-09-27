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
package com.amilesend.onedrive.connection.http;

import com.amilesend.onedrive.connection.OneDriveConnectionException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;

/**
 * Utility to configure and build a {@link OkHttpClient} with the option of default configuration for this SDK.
 * This supports configuring a client that:
 * <ul>
 *     <li>Customizes the SSL trust manager and hostname verifier</li>
 *     <li>Configures a proxy with username and password</li>
 *     <li>Configures support to follow redirects</li>
 *     <li>Configures connection and read/write timeouts (default is disabled)</li>
 * </ul>
 */
public class OkHttpClientBuilder {
    private X509TrustManager trustManager;
    private HostnameVerifier hostnameVerifier;
    private Proxy proxy;
    private String proxyUsername;
    private String proxyPassword;
    private Authenticator proxyAuthenticator;
    private boolean isRedirectsAllowed = true;
    private boolean isForTest;
    private Duration connectTimeout = Duration.ofMillis(10000L);
    private Duration readTimeout = Duration.ofMillis(10000L);
    private Duration writeTimeout = Duration.ofMillis(10000L);

    /**
     * Sets the SSL/TLS trust manager to use with the HTTP client.
     *
     * @param trustManager the trust manager
     * @return the builder instance
     * @see X509TrustManager
     */
    public OkHttpClientBuilder trustManager(final X509TrustManager trustManager) {
        this.trustManager = trustManager;
        return this;
    }

    /**
     * Sets the hostname verifier to use with the HTTP client.
     *
     * @param hostnameVerifier the trust manager
     * @return the builder instance
     * @see HostnameVerifier
     */
    public OkHttpClientBuilder hostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    /**
     * Sets the proxy and associated username plus password to use with the HTTP client. Note: if {@code username} and
     * {@code password} is undefined, then no credential-based authentication will be configured.
     *
     * @param proxy the proxy settings
     * @param username the username
     * @param password the password
     * @return the builder instance
     */
    public OkHttpClientBuilder proxy(final Proxy proxy, final String username, final String password) {
        this.proxy = proxy;
        this.proxyUsername = username;
        this.proxyPassword = password;
        return this;
    }

    /**
     * Sets the proxy and associated authenticator to use with the HTTP client.
     *
     * @param proxy the proxy settings
     * @param proxyAuthenticator the proxy authenticator
     * @return the builder instance
     */
    public OkHttpClientBuilder proxy(final Proxy proxy, final Authenticator proxyAuthenticator) {
        this.proxy = proxy;
        this.proxyAuthenticator = proxyAuthenticator;
        return this;
    }

    /**
     * Sets the connection timeout for the HTTP client.
     *
     * @param connectTimeout the connection timeout
     * @return the builder instance
     */
    public OkHttpClientBuilder connectTimeout(final Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the read timeout for the HTTP client.
     *
     * @param readTimeout the read timeout
     * @return the builder instance
     */
    public OkHttpClientBuilder readTimeout(final Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the write timeout for the HTTP client.
     *
     * @param writeTimeout the write timeout.
     * @return the builder instance
     */
    public OkHttpClientBuilder writeTimeout(final Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the flag to allow for automatic URL redirects when responses return 300-based HTTP responses.
     *
     * @param isRedirectsAllowed If {@code true}, then the client will automatically invoke the redirected URL;
     *                           else, {@code false}
     * @return the builder instance
     */
    public OkHttpClientBuilder isRedirectsAllowed(final boolean isRedirectsAllowed) {
        this.isRedirectsAllowed = isRedirectsAllowed;
        return this;
    }

    /**
     * Sets the flag to allow for non-SSL/TLS based requests used for testing. Note: Should not be set for normal
     * use with real calls to the Graph API.
     *
     * @param isForTest If {@code true}, then the client will allow HTTP-based invocations; else, {@code false}.
     * @return the builder instance
     */
    public OkHttpClientBuilder isForTest(final boolean isForTest) {
        this.isForTest = isForTest;
        return this;
    }

    /**
     * Builds a new {@code OkHttpClient} instance.
     *
     * @return the configured HTTP client
     */
    public OkHttpClient build() {
        return configureProxy(
                configureSsl(new OkHttpClient.Builder()
                        .followSslRedirects(isRedirectsAllowed)
                        .followRedirects(isRedirectsAllowed)
                        .connectTimeout(connectTimeout)
                        .readTimeout(readTimeout)
                        .writeTimeout(writeTimeout)
                        .connectionSpecs(getConnectionSpecs())))
                .build();
    }

    private List<ConnectionSpec> getConnectionSpecs() {
        return isForTest
                ? List.of(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS)
                : List.of(ConnectionSpec.MODERN_TLS);
    }

    private OkHttpClient.Builder configureSsl(OkHttpClient.Builder builder) {
        if (trustManager == null) {
            return builder;
        }

        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{ trustManager }, new SecureRandom());
            builder = builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
        } catch (final KeyManagementException | NoSuchAlgorithmException ex) {
            throw new OneDriveConnectionException("Unable to create HttpClient: " + ex.getMessage(), ex);
        }

        if (hostnameVerifier == null) {
            return builder;
        }

        return builder.hostnameVerifier(hostnameVerifier);
    }

    private OkHttpClient.Builder configureProxy(OkHttpClient.Builder builder) {
        if (proxy == null) {
            return builder;
        }

        builder = builder.proxy(proxy);

        final Authenticator authenticatorToSet;
        if (proxyAuthenticator != null) {
            authenticatorToSet = proxyAuthenticator;
        } else if (StringUtils.isNotBlank(proxyUsername) || StringUtils.isNotBlank(proxyPassword)) {
            authenticatorToSet = new CredentialAuthenticator(proxyUsername, proxyPassword);
        } else {
            authenticatorToSet = null;
        }

        return authenticatorToSet != null ? builder.proxyAuthenticator(authenticatorToSet) : builder;
    }

    @RequiredArgsConstructor
    private static class CredentialAuthenticator implements Authenticator {
        private final String username;
        private final String password;

        @Nullable
        @Override
        public Request authenticate(final Route route, @NonNull final Response response) {
            final String credential = Credentials.basic(username, password);
            return response.request()
                    .newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
        }
    }
}
