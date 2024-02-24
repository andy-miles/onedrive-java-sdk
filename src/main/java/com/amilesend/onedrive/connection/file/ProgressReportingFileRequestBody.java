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
package com.amilesend.onedrive.connection.file;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

import java.io.File;
import java.io.IOException;

/**
 * A customized implementation of {@link RequestBody} that invokes a {@link TransferProgressCallback} to notify
 * of transfer updates for file uploads.
 *
 * @see TransferProgressCallback
 */
@Builder
public class ProgressReportingFileRequestBody extends RequestBody {
    private static final int TRANSFER_CHUNK_SIZE = 1024;

    /** The source files to read and upload. */
    @NonNull
    private final File file;
    /** The mime type of the file. */
    @Builder.Default
    private final String contentType = com.google.common.net.MediaType.OCTET_STREAM.toString();
    /**
     * The {@link TransferProgressCallback}.
     * Default is a {@link com.amilesend.onedrive.connection.file.LogProgressCallback}.
     */
    @Builder.Default
    private final TransferProgressCallback callback = LogProgressCallback.builder()
            .transferType(LogProgressCallback.TransferType.UPLOAD)
            .build();

    /**
     * Gets the content type formatted as a {@link MediaType}.
     *
     * @return the content type
     */
    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    /**
     * Gets the content type formatted as a string.
     *
     * @return the content type
     */
    public String getContentTypeStringValue() {
        return contentType;
    }

    /**
     * Gets the size of the configured file (in bytes).
     *
     * @return the size in bytes
     */
    public long contentLength() {
        return file.length();
    }

    /**
     * Writes the configured {@link #file} to the given {@code outputBufferedSink}. This is typically a wrapped output
     * stream for the request body.
     *
     * @param outputBufferedSink the sink to stream the file contents to
     * @throws IOException if an error occurred while reading the configured file
     */
    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    @Override
    public void writeTo(@NonNull BufferedSink outputBufferedSink) throws IOException {
        final long totalBytes = contentLength();
        try {
            try (final BufferedSource source = Okio.buffer(Okio.source(file))) {
                long processedBytes = 0L;
                long readBytes;

                while ((readBytes = source.read(outputBufferedSink.getBuffer(), TRANSFER_CHUNK_SIZE)) != -1) {
                    processedBytes += readBytes;
                    callback.onUpdate(processedBytes, totalBytes);
                }
                outputBufferedSink.flush();
                callback.onComplete(processedBytes);
            }
        } catch (final IOException ex) {
            callback.onFailure(ex);
            throw ex;
        }
    }
}
