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
