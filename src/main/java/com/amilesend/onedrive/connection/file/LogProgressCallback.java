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

import com.google.common.annotations.VisibleForTesting;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    /** Prefix to include in every logging statement. */
    private final String prefix;
    /** Used to chain multiple callbacks. */
    private final Optional<TransferProgressCallback> chainedCallback;
    // Used to limit unnecessarily entries to the log.
    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final AtomicInteger lastUpdateProgressValue = new AtomicInteger();
    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final AtomicReference<Instant> lastUpdateTimestamp = new AtomicReference<>(Instant.now());

    @Builder
    private LogProgressCallback(
            final TransferType transferType,
            final Level loggingLevel,
            final Duration updateFrequency,
            final String prefix,
            final TransferProgressCallback chainedCallback) {
        log = LoggerFactory.getLogger(LogProgressCallback.class);
        this.transferType = Optional.ofNullable(transferType).orElse(TransferType.UNDEFINED);
        this.loggingLevel = Optional.ofNullable(loggingLevel).orElse(Level.INFO);
        this.updateFrequency = Optional.ofNullable(updateFrequency).orElse(DEFAULT_UPDATE_FREQUENCY);
        this.prefix = Optional.ofNullable(prefix).orElse(StringUtils.EMPTY);
        this.chainedCallback = Optional.ofNullable(chainedCallback);
    }

    /**
     * Helper method to format the logging prefix to use.
     *
     * @param source the source
     * @param destination the destination
     * @return the logging prefix
     */
    public static String formatPrefix(final String source, final String destination) {
        Validate.notBlank(source, "source must not be blank");
        Validate.notBlank(destination, "destination must not be blank");

        return new StringBuilder("[")
                .append(source)
                .append(" -> ")
                .append(destination)
                .append("] ")
                .toString();
    }

    @Override
    public void onUpdate(final long currentBytes, final long totalBytes) {
        try {
            if (Duration.between(lastUpdateTimestamp.get(), Instant.now()).compareTo(updateFrequency) < 0) {
                return;
            }

            final int currentProgressPercent = (int) Math.floor(((double) currentBytes / (double) totalBytes) * 100D);
            if (currentProgressPercent == lastUpdateProgressValue.get()) {
                return;
            }

            log.atLevel(loggingLevel)
                    .log("{}{} Status: {}% ({} of {} bytes)",
                            prefix,
                            transferType.getLogPrefix(),
                            currentProgressPercent,
                            currentBytes,
                            totalBytes);
            lastUpdateTimestamp.set(Instant.now());
            lastUpdateProgressValue.set(currentProgressPercent);
        } finally {
            chainedCallback.ifPresent(c -> c.onUpdate(currentBytes, totalBytes));
        }
    }

    @Override
    public void onFailure(final Throwable cause) {
        try {
            log.error("{}An error occurred during {}: {}", prefix, transferType.getLogPrefix(), cause.getMessage(), cause);
        } finally {
            chainedCallback.ifPresent(c -> c.onFailure(cause));
        }
    }

    @Override
    public void onComplete(final long bytesTransferred) {
        try {
            log.atLevel(loggingLevel).log(
                    "{}{} complete with {} bytes transferred",
                    prefix,
                    transferType.getLogPrefix(),
                    bytesTransferred);
        } finally {
            chainedCallback.ifPresent(c -> c.onComplete(bytesTransferred));
        }
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
