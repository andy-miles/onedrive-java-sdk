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
package com.amilesend.onedrive.connection;

import com.amilesend.onedrive.connection.auth.AuthManager;
import com.amilesend.onedrive.connection.file.TransferFileWriter;
import com.amilesend.onedrive.connection.file.TransferProgressCallback;
import com.amilesend.onedrive.parse.GsonFactory;
import com.amilesend.onedrive.parse.resource.parser.GsonParser;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.ACCEPT_ENCODING;
import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.JSON_UTF_8;

/**
 * Wraps a {@link OkHttpClient} that manages authentication refresh and parsing responses to corresponding POJO types.
 * Construct a new instance via the {@link OneDriveConnectionBuilder} that simplifies configuration of the
 * {@link AuthManager} and automatically sets a pre-configured {@link Gson} instance for proper request/response
 * serialization.
 *
 * @see OneDriveConnectionBuilder
 * @see AuthManager
 */
@Slf4j
public class OneDriveConnection {
    public static final String JSON_CONTENT_TYPE = JSON_UTF_8.toString();
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse(JSON_CONTENT_TYPE);
    private static final String GZIP_ENCODING = "gzip";
    private static final String THROTTLED_RETRY_AFTER_HEADER = "Retry-After";
    private static int THROTTLED_RESPONSE_CODE = 429;

    private final OkHttpClient httpClient;
    /** The authorization manager used to manage auth tokens. */
    @Getter
    private final AuthManager authManager;
    /** The configured GSON instance used for marshalling request and responses to/from JSON. */
    @Getter
    private final Gson gson;
    /** The base URL for the Graph API. */
    @Getter
    private final String baseUrl;

    /**
     * Creates a new {@code OneDriveConnection} object. It is recommended to use the {@link OneDriveConnectionBuilder}
     * instead.
     *
     * @param httpClient the configured HTTP client
     * @param authManager the authorization manager used to manage auth and refresh tokens
     * @param gsonFactory the factory used to vend configured GSON instances for request/reply serialization
     * @param baseUrl the base URL to use for Graph API invocations
     * @see OneDriveConnectionBuilder
     */
    public OneDriveConnection(
            @NonNull final OkHttpClient httpClient,
            @NonNull final AuthManager authManager,
            @NonNull final GsonFactory gsonFactory,
            final String baseUrl) {
        this.httpClient = httpClient;
        this.authManager = authManager;
        this.gson = gsonFactory.newInstanceForConnection(this);
        this.baseUrl = StringUtils.isBlank(baseUrl) ? authManager.getAuthenticatedEndpoint() : baseUrl;
    }

    /**
     * Creates a new {@link Request.Builder} with pre-configured headers for requests that expect no responses in
     * the body (e.g., post or upload/download operations).
     *
     * @return the request builder.
     */
    public Request.Builder newSignedForRequestBuilder() {
        return new Request.Builder()
                .addHeader(AUTHORIZATION, authManager.refreshIfExpiredAndFetchFullToken());
    }

    /**
     * Creates a new {@link Request.Builder} with pre-configured headers for request that expect a JSON-formatted
     * response body.
     *
     * @return the request builder
     */
    public Request.Builder newSignedForApiRequestBuilder() {
        return newSignedForRequestBuilder()
                .addHeader(ACCEPT_ENCODING, GZIP_ENCODING)
                .addHeader(ACCEPT, JSON_CONTENT_TYPE);
    }

    /**
     * Creates a new {@link Request.Builder} with pre-configured headers for a request that contains both a
     * JSON-formatted request and response body.
     *
     * @return the request builder
     */
    public Request.Builder newSignedForApiWithBodyRequestBuilder() {
        return newSignedForApiRequestBuilder()
                .addHeader(CONTENT_TYPE,  JSON_CONTENT_TYPE);
    }

    /**
     * Executes the given {@link Request} and parses the JSON-formatted response with given {@link GsonParser}.
     *
     * @param request the request
     * @param parser the parser to decode the response body
     * @return the response as a POJO resource type
     * @param <T> the POJO resource type
     * @throws OneDriveConnectionException if an error occurred during the transaction
     */
    public <T> T execute(@NonNull final Request request, @NonNull final GsonParser<T> parser)
            throws OneDriveConnectionException {
        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (log.isDebugEnabled())
                    log.debug("Received response: {}", response);
                validateResponse(response);

                return parser.parse(getGson(), new GZIPInputStream(response.body().byteStream()));
            }
        } catch (final IOException ex) {
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        } catch (final JsonParseException ex) {
            throw new ResponseParseException("Error parsing response: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes the given {@link Request} and returns the associated HTTP response code. This is used for
     * transactions that do not expect a response in the body.
     *
     * @param request the request
     * @return the HTTP response code
     * @throws OneDriveConnectionException if an error occurred during the transaction
     */
    public int execute(@NonNull final Request request) throws OneDriveConnectionException {
        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (log.isDebugEnabled())
                    log.debug("Received response: {}", response);
                validateResponse(response);
                return response.code();
            }
        } catch (final IOException ex) {
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes the given {@link Request} for a remote asynchronous operation and returns the monitoring URL.
     *
     * @param request the request
     * @return the monitoring URL to track the remote asynchronous operation
     * @throws OneDriveConnectionException if an error occurred during the transaction
     */
    public String executeRemoteAsync(@NonNull final Request request) throws OneDriveConnectionException {
        try {
            try (final Response response = httpClient.newCall(request).execute()) {
                if (log.isDebugEnabled())
                    log.debug("Received response: {}", response);
                validateResponse(response);
                final int code = response.code();
                // Specific to remote async operations
                if (code != 202) {
                    throw new ResponseException("Expected a 202 response code. Got " + code);
                }

                return response.header("Location");
            }
        } catch (final IOException ex) {
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        }
    }

    /**
     * Executes the given {@link Request} and parses the JSON-formatted response with given {@link GsonParser}.
     *
     * @param request the request
     * @param parser the parser to decode the response body
     * @return the CompletableFuture used to fetch the parsed response or failure exception reason
     * @param <T> the POJO resource type
     */
    public <T> CompletableFuture<T> executeAsync(
            @NonNull final Request request,
            @NonNull final GsonParser<T> parser) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException ex) {
                future.completeExceptionally(ex);
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                try {
                    if (log.isDebugEnabled())
                        log.debug("Received response: {}", response);
                    validateResponse(response);
                    future.complete(parser.parse(getGson(), new GZIPInputStream(response.body().byteStream())));
                } catch (final Exception ex) {
                    future.completeExceptionally(ex);
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    /**
     * Downloads the contents for the given {@code request} to the specified {@code folderPath} and {@code name}.
     *
     * @param request the request
     * @param folderPath the path of the folder to download the contents to
     * @param name the name of the file to download the contents to
     * @param sizeBytes the total size of the expected file in bytes
     * @param callback the {@link TransferProgressCallback} call to invoke to report download transfer progress
     * @return the size of the downloaded file in bytes
     * @throws OneDriveConnectionException if an error occurred while downloading the content for the request
     */
    public long download(
            @NonNull final Request request,
            @NonNull final Path folderPath,
            final String name,
            final long sizeBytes,
            @NonNull final TransferProgressCallback callback) throws OneDriveConnectionException {
        Validate.notBlank(name, "name must not be blank");

        final Path downloadPath;
        try {
            downloadPath = checkFolderAndGetDestinationPath(folderPath, name);
        } catch (final Exception ex) {
            callback.onFailure(ex);
            throw new RequestException("Unable to determine download path:" + ex.getMessage(), ex);
        }

        try (final Response response = httpClient.newCall(request).execute()) {
                return processDownloadResponse(response, downloadPath, sizeBytes, callback);
        } catch (final OneDriveConnectionException ex) {
            // Response failed validation, notify the callback
            callback.onFailure(ex);
            throw ex;
        } catch (final Exception ex) {
            // The underlying TransferFileWriter will record an onFailure to the callback.
            throw new RequestException("Unable to execute request: " + ex.getMessage(), ex);
        }
    }

    /**
     * Downloads the contents for the given {@code request} asynchronously to the specified {@code folderPath} and
     * {@code name}.
     *
     * @param request the request
     * @param folderPath the path of the folder to download the contents to
     * @param name the name of the file to download the contents to
     * @param sizeBytes the total size of the expected file in bytes
     * @param callback the {@link TransferProgressCallback} call to invoke to report download transfer progress
     * @return the CompletableFuture used to fetch the number of bytes downloaded
     */
    public CompletableFuture<Long> downloadAsync(
            @NonNull final Request request,
            @NonNull final Path folderPath,
            final String name,
            final long sizeBytes,
            @NonNull final TransferProgressCallback callback) {
        Validate.notBlank(name, "name must not be blank");
        final CompletableFuture<Long> future = new CompletableFuture<>();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException ex) {
                callback.onFailure(ex);
                future.completeExceptionally(ex);
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                final Path downloadPath;
                try {
                    downloadPath = checkFolderAndGetDestinationPath(folderPath, name);
                } catch (final Exception ex) {
                    callback.onFailure(ex);
                    future.completeExceptionally(ex);
                    throw new RequestException("Unable to determine download path:" + ex.getMessage(), ex);
                }

                try {
                    final long totalBytes = processDownloadResponse(response, downloadPath, sizeBytes, callback);
                    future.complete(Long.valueOf(totalBytes));
                } catch (final OneDriveConnectionException ex) {
                    // Response failed validation, notify the callback
                    callback.onFailure(ex);
                    future.completeExceptionally(ex);
                    throw ex;
                } catch (final Exception ex) {
                    // The underlying TransferFileWriter will record an onFailure to the callback.
                    future.completeExceptionally(ex);
                    throw ex;
                } finally {
                    response.close();
                }
            }
        });

        return future;
    }

    @VisibleForTesting
    long processDownloadResponse(
            final Response response,
            final Path downloadPath,
            final long sizeBytes,
            final TransferProgressCallback callback) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Received response: {}", response);
        }
        validateResponse(response);

        final long totalBytes = TransferFileWriter.builder()
                .output(downloadPath)
                .callback(callback)
                .build()
                .write(response.body().source(), sizeBytes);

        if (log.isDebugEnabled()) {
            log.debug("Downloaded [{}] bytes to [{}]", totalBytes, downloadPath);
        }
        return totalBytes;
    }

    @VisibleForTesting
    Path checkFolderAndGetDestinationPath(final Path folderPath, final String name) throws IOException {
        final Path normalizedFolderPath = folderPath.toAbsolutePath().normalize();
        if (Files.exists(normalizedFolderPath) && !Files.isDirectory(normalizedFolderPath)) {
            throw new IllegalArgumentException(normalizedFolderPath + " must not already exist as a file");
        }

        Files.createDirectories(normalizedFolderPath);
        return normalizedFolderPath.resolve(name);
    }

    private static void validateResponse(final Response response) {
        final int code = response.code();
        if (code == THROTTLED_RESPONSE_CODE) {
            final Long retryAfterSeconds = extractRetryAfterHeaderValue(response);
            final String msg = retryAfterSeconds != null
                    ? "Request throttled. Retry after " + retryAfterSeconds + " seconds"
                    : "Request throttled";
            throw new ThrottledException(msg, retryAfterSeconds);
        }

        final boolean isRequestError = String.valueOf(code).startsWith("4");
        if (isRequestError) {
            throw new RequestException(new StringBuilder("Error with request (")
                    .append(code)
                    .append("): ")
                    .append(response)
                    .toString());
        } else if (!response.isSuccessful()) {
            throw new ResponseException(new StringBuilder("Unsuccessful response (")
                    .append(code)
                    .append("): ")
                    .append(response)
                    .toString());
        }
    }

    private static Long extractRetryAfterHeaderValue(final Response response) {
        final String retryAfterHeaderValue = response.header(THROTTLED_RETRY_AFTER_HEADER);
        return StringUtils.isNotBlank(retryAfterHeaderValue) ? Long.valueOf(retryAfterHeaderValue) : null;
    }
}
