package com.amilesend.onedrive.connection;

import lombok.Getter;

import javax.annotation.Nullable;

/** Defines the exception thrown from {@link OneDriveConnection} when a request is throttled. */
public class ThrottledException extends RequestException {
    /**
     * The time in seconds that the request can be retried.
     * Note: This can be null and is optionally provided by the response. */
    @Getter
    @Nullable
    private final Long retryAfterSeconds;

    /**
     * Creates a new {@code ThrottledException}.
     *
     * @param msg the exception message
     * @param retryAfterSeconds the amount of time in seconds that the request can be retried
     */
    public ThrottledException(final String msg, final Long retryAfterSeconds) {
        super(msg);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
