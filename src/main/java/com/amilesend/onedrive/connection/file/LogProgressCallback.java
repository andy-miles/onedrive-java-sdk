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

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.Duration;
import java.time.Instant;

/**
 * A log-based implementation of {@link TransferProgressCallback} that logs transfer progress.
 * @see TransferProgressCallback
 */
public class LogProgressCallback implements TransferProgressCallback {
    private static final Duration DEFAULT_UPDATE_FREQUENCY = Duration.ofMillis(100L);

    /**
     * The Transfer type. Default is {@link TransferType#UNDEFINED}.
     * @see TransferType
     */
    private final TransferType transferType;
    /** The logging level to record progress updates. Default is {@link Level#INFO} */
    private final Level loggingLevel;
    /** Duration between logging updates. */
    private final Duration updateFrequency;
    /** The logger instance. */
    private final Logger log;
    // Used to limit unnecessarily entries to the log.
    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    volatile long lastUpdateProgressValue = 0L;
    @VisibleForTesting
    @Setter(AccessLevel.PACKAGE)
    private volatile Instant lastUpdateTimestamp = Instant.now();

    @Builder
    private LogProgressCallback(
            final TransferType transferType,
            final Level loggingLevel,
            final Duration updateFrequency) {
        log = LoggerFactory.getLogger(LogProgressCallback.class);
        this.transferType = transferType == null ? TransferType.UNDEFINED : transferType;
        this.loggingLevel = loggingLevel == null ? Level.INFO : loggingLevel;
        this.updateFrequency = updateFrequency == null
                ? DEFAULT_UPDATE_FREQUENCY
                : updateFrequency;
    }

    @Override
    public void onUpdate(final long currentBytes, final long totalBytes) {
        if (Duration.between(lastUpdateTimestamp, Instant.now()).compareTo(updateFrequency) < 0) {
            return;
        }

        final int currentProgressPercent = (int) Math.floor(((double) currentBytes / (double) totalBytes) * 100D);
        if (currentProgressPercent == lastUpdateProgressValue) {
            return;
        }

        log.atLevel(loggingLevel)
                .log("{} Status: {}% ({} of {} bytes)",
                        transferType.getLogPrefix(),
                        currentProgressPercent,
                        currentBytes,
                        totalBytes);
        lastUpdateTimestamp = Instant.now();
        lastUpdateProgressValue = currentProgressPercent;
    }

    @Override
    public void onFailure(final Throwable cause) {
        log.error("An error occurred during {}: {}", transferType.getLogPrefix(), cause.getMessage(), cause);
    }

    @Override
    public void onComplete(final long bytesTransferred) {
        log.atLevel(loggingLevel)
                .log("{} complete with {} bytes transferred", transferType.getLogPrefix(), bytesTransferred);
    }

    /** Describes that transfer type used for logging progress. */
    @RequiredArgsConstructor
    public enum TransferType {
        /** Indicates that the transfer is for a file upload. */
        UPLOAD("Upload"),
        /** Indicates that the transfer is for a file download.  */
        DOWNLOAD("Download"),
        /** Used when the type is not defined and defaults to a generic "Transfer".*/
        UNDEFINED("Transfer");

        /** The type formatted for log records. */
        @Getter
        private final String logPrefix;
    }
}
