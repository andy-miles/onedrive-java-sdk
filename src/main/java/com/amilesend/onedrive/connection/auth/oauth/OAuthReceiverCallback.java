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
package com.amilesend.onedrive.connection.auth.oauth;

import com.amilesend.client.util.Pair;
import com.amilesend.client.util.StringUtils;
import com.amilesend.client.util.VisibleForTesting;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * HttpServer handler that takes the verifier token passed over from the OAuth provider and
 * stashes it where {@link OAuthReceiver#waitForCode} will find it.
 */
@Slf4j
public class OAuthReceiverCallback implements HttpHandler {
    private static final String LANDING_HTML_RESOURCE = "/SuccessLanding.html";
    private static final String LANDING_HTML_FALLBACK = new StringBuilder("<html>")
            .append("<head><title>OAuth 2.0 Authentication Token Received</title></head>")
            .append("<body>")
            .append("Received verification code. You may now close this window.")
            .append("</body>")
            .append("</html>\n")
            .toString();

    private final String callbackPath;
    private final String successLandingPageUrl;
    private final String failureLandingPageUrl;
    /** To block until receiving an authorization response or stop() is called. */
    private final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

    /** The authorization code. */
    @VisibleForTesting // The Setter
    @Setter(value = AccessLevel.PACKAGE)
    @Getter
    private volatile String code;
    /** The error code. */
    @VisibleForTesting // The Setter
    @Setter(value = AccessLevel.PACKAGE)
    @Getter
    private volatile String error;

    /**
     * Builds a new {@code OAuthReceiverCallback}
     *
     * @param callbackPath the path to listen for the redirect
     * @param successLandingPageUrl optional URL for a custom successful landing page
     * @param failureLandingPageUrl optional URL for a custom failure landing page
     */
    @Builder
    public OAuthReceiverCallback(
            @NonNull final String callbackPath,
            final String successLandingPageUrl,
            final String failureLandingPageUrl) {
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    /**
     * Handles the given request and sets the corresponding redirect status.
     *
     * @param httpExchange the exchange containing the request from the client
     * @throws IOException if unable to retrieve request information from the exchange
     */
    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        if (!callbackPath.equals(httpExchange.getRequestURI().getPath())) {
            return;
        }

        try {
            final Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
            error = params.get("error");
            code = params.get("code");

            final Headers respHeaders = httpExchange.getResponseHeaders();
            if (StringUtils.isBlank(error) && StringUtils.isNotBlank(successLandingPageUrl)) {
                respHeaders.add("Location", successLandingPageUrl);
                httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);
            } else if (StringUtils.isNotBlank(error) && StringUtils.isNotBlank(failureLandingPageUrl)) {
                respHeaders.add("Location", failureLandingPageUrl);
                httpExchange.sendResponseHeaders(HTTP_MOVED_TEMP, -1);
            } else {
                writeLandingHtml(httpExchange, respHeaders);
            }
            httpExchange.close();
        } finally {
            releaseLock();
        }
    }

    /**
     * Blocks until the server receives a login result, or the server is stopped by {@link OAuthReceiver#stop()},
     * to return an authorization code.
     *
     * @return authorization code if login succeeds; may return {@code null} if the server is stopped
     *     by {@link OAuthReceiver#stop()}
     * @throws OAuthReceiverException if the server receives an error code (through an HTTP request parameter
     *     {@code error})
     */
    public String waitForCode() throws OAuthReceiverException {
        waitUnlessSignaled.acquireUninterruptibly();
        if (StringUtils.isNotBlank(error)) {
            throw new OAuthReceiverException("User authorization failed (" + error + ")");
        }
        return code;
    }

    /** Releases the lock. */
    public void releaseLock() {
        waitUnlessSignaled.release();
    }

    private static Map<String, String> queryToMap(final String query) {
        if (StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }

        return Arrays.stream(query.split("&"))
                .filter(StringUtils::isNotBlank)
                .map(kv -> {
                    if (!kv.contains("=")){
                        return Pair.of(kv, StringUtils.EMPTY);
                    }

                    final String pair[] = kv.split("=");
                    if (pair.length != 2 && pair.length > 0) {
                        return Pair.of(pair[0], StringUtils.EMPTY);
                    }

                    return Pair.of(pair[0], pair[1]);
                })
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private static void writeLandingHtml(final HttpExchange exchange, final Headers headers) throws IOException {
        try (final OutputStream os = exchange.getResponseBody();
             final OutputStreamWriter doc = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            exchange.sendResponseHeaders(HTTP_OK, 0);
            headers.add("ContentType", "text/html");
            doc.write(getLandingHtml());
            doc.flush();
        }
    }

    private static String getLandingHtml() {
        try {
            final Path resourcePath = Paths.get(OAuthReceiverCallback.class
                    .getClassLoader()
                    .getResource(LANDING_HTML_RESOURCE)
                    .toURI());
            return Files.readString(resourcePath);
        } catch (final IOException | URISyntaxException | NullPointerException ex) {
            log.warn("Error trying to load resource: {}", LANDING_HTML_RESOURCE);
            return LANDING_HTML_FALLBACK;
        }
    }
}
