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

import com.amilesend.client.connection.Connection;
import com.amilesend.client.connection.ConnectionException;
import com.amilesend.client.connection.RequestException;
import com.amilesend.client.connection.ResponseException;
import com.amilesend.client.connection.file.TransferFileWriter;
import com.amilesend.client.connection.file.TransferProgressCallback;
import com.amilesend.client.parse.parser.GsonParser;
import com.amilesend.onedrive.connection.auth.OneDriveAuthManager;
import com.amilesend.onedrive.parse.GsonFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

import static com.google.common.net.HttpHeaders.CONTENT_ENCODING;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;

/**
 * Wraps a {@link OkHttpClient} that manages authentication refresh and parsing responses to corresponding POJO types.
 * Construct a new instance via the {@link OneDriveConnectionBuilder} that simplifies configuration of the
 * {@link OneDriveAuthManager} and automatically sets a pre-configured {@link Gson} instance for proper request/response
 * serialization.
 *
 * @see OneDriveConnectionBuilder
 * @see OneDriveAuthManager
 */
@SuperBuilder
@Slf4j
public class OneDriveConnection extends Connection<GsonFactory> {
    /**
     * Creates a new {@link Request.Builder} with pre-configured headers for a request that contains both a
     * JSON-formatted request and response body.
     *
     * @return the request builder
     */
    public Request.Builder newWithBodyRequestBuilder() {
        return newRequestBuilder().addHeader(CONTENT_TYPE,  JSON_CONTENT_TYPE);
    }

    /**
     * Executes the given {@link Request} for a remote asynchronous operation and returns the monitoring URL.
     *
     * @param request the request
     * @return the monitoring URL to track the remote asynchronous operation
     * @throws ConnectionException if an error occurred during the transaction
     */
    public String executeRemoteAsync(@NonNull final Request request) throws ConnectionException {
        try {
            try (final Response response = getHttpClient().newCall(request).execute()) {
                validateResponseCode(response);
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
        final OneDriveConnection connectionRef = this;
        final CompletableFuture<T> future = new CompletableFuture<>();
        getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull final Call call, @NonNull final IOException ex) {
                future.completeExceptionally(ex);
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {
                try {
                    validateResponseCode(response);
                    final InputStream responseBodyInputStream =
                            StringUtils.equalsIgnoreCase("gzip", response.header(CONTENT_ENCODING))
                                    ? new GZIPInputStream(response.body().byteStream())
                                    : response.body().byteStream();
                    future.complete(parser.parse(getGsonFactory().getInstance(connectionRef), responseBodyInputStream));
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
     * @throws ConnectionException if an error occurred while downloading the content for the request
     */
    public long download(
            @NonNull final Request request,
            @NonNull final Path folderPath,
            final String name,
            final long sizeBytes,
            @NonNull final TransferProgressCallback callback) throws ConnectionException {
        Validate.notBlank(name, "name must not be blank");

        final Path downloadPath;
        try {
            downloadPath = checkFolderAndGetDestinationPath(folderPath, name);
        } catch (final Exception ex) {
            callback.onFailure(ex);
            throw new RequestException("Unable to determine download path:" + ex.getMessage(), ex);
        }

        try (final Response response = getHttpClient().newCall(request).execute()) {
                return processDownloadResponse(response, downloadPath, sizeBytes, callback);
        } catch (final ConnectionException ex) {
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

        getHttpClient().newCall(request).enqueue(new Callback() {
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
                } catch (final ConnectionException ex) {
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
        validateResponseCode(response);

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
}
