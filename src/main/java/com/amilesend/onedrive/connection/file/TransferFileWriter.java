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
package com.amilesend.onedrive.connection.file;

import lombok.Builder;
import lombok.NonNull;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Path;

/**
 * A file writer utility used while downloading files that reports transfer progress to the configured
 * {@link TransferProgressCallback}.
 *
 * @see TransferProgressCallback
 */
@Builder
public class TransferFileWriter {
    private static final int TRANSFER_CHUNK_SIZE = 1024;

    /** The path of the file to download and persist the contents to. */
    @NonNull
    private final Path output;
    /** The {@link TransferProgressCallback}. */
    @NonNull
    private final TransferProgressCallback callback;

    /**
     * Writes the given {@code source} (streamed response body) to the configured {@link #output}.
     *
     * @param source the source to read the contents from
     * @param size the total expected size of the contents in bytes
     * @return the number of total read bytes
     * @throws IOException if unable to write the contents to the configured path
     */
    public long write(@NonNull final BufferedSource source, final long size) throws IOException {
        Validate.isTrue(size >= 0L, "Size should be >= 0");

        try {
            try (final BufferedSink outSink = Okio.buffer(Okio.sink(output))) {
                long processedBytes = 0L;
                long readBytes;

                while ((readBytes = source.read(outSink.getBuffer(), TRANSFER_CHUNK_SIZE)) != -1L) {
                    processedBytes += readBytes;
                    callback.onUpdate(processedBytes, size);
                }
                outSink.flush();
                callback.onComplete(processedBytes);
                return processedBytes;
            }
        } catch (final IOException ex) {
            callback.onFailure(ex);
            throw ex;
        }
    }
}
