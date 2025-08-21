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
package com.amilesend.onedrive.resource.item;

import com.amilesend.client.util.Validate;
import com.amilesend.client.util.VisibleForTesting;
import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import static com.amilesend.onedrive.parse.resource.parser.Parsers.ASYNC_JOB_STATUS_PARSER;

/**
 * Utility that allows consumers to query for remote asynchronous operation status from the OneDrive API.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/concepts/long-running-actions">
 * API Documentation</a>
 *
 * @see AsyncJobStatus
 */
@Builder
@EqualsAndHashCode
public class AsyncJob {
    @Getter(AccessLevel.PACKAGE)
    @VisibleForTesting
    private final String monitorUrl;
    @EqualsAndHashCode.Exclude
    private final OneDriveConnection connection;

    /**
     * Creates a new {@code AsyncJob}.
     *
     * @param monitorUrl the URL used to monitor the asynchronous job status
     * @param connection the API connection used to query the job status
     */
    public AsyncJob(final String monitorUrl, @NonNull OneDriveConnection connection) {
        Validate.notBlank(monitorUrl, "monitorUrl must not be blank");

        this.monitorUrl = monitorUrl;
        this.connection = connection;
    }

    /**
     * Gets the current {@link AsyncJobStatus} for the remote asynchronous operation.
     *
     * @return the job status
     * @see AsyncJobStatus
     */
    public AsyncJobStatus getStatus() {
        return connection.execute(
                connection.newRequestBuilder()
                        .url(monitorUrl)
                        .build(),
                ASYNC_JOB_STATUS_PARSER);
    }
}
