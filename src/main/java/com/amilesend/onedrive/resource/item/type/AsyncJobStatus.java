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
package com.amilesend.onedrive.resource.item.type;

import lombok.Builder;
import lombok.Data;

/**
 * Provides information on the status of an asynchronous job.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/asyncjobstatus">
 * API Documentation</a>.
 */
@Builder
@Data
public class AsyncJobStatus {
    /**
     * The type of job being performed. Valid values include:
     * <ul>
     *     <li>{@literal ItemCopy}</li>
     *     <li>{@literal DownloadUrl}</li>
     * </ul>
     */
    private final String operation;
    /** A value between 0.0 and 100.0 that indicates the current progress as a percentage. */
    private final double percentageComplete;
    /** The identifier associated with the job. */
    private final String resourceId;
    /**
     * The current status associated with the job. Valid values include:
     * <ul>
     *     <li>{@literal notStarted}</li>
     *     <li>{@literal inProgress}</li>
     *     <li>{@literal completed}</li>
     *     <li>{@literal failed}</li>
     *     <li>{@literal cancelled}</li>
     *     <li>{@literal waiting}</li>
     *     <li>{@literal cancelPending}</li>
     * </ul>
     */
    private final String status;
    /** A detailed description of the job status. */
    private final String statusDescription;
}
