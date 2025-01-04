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
package com.amilesend.onedrive.connection.file;

/**
 * The interface that is invoked during file upload/download transfers that enables client consumers to
 * inspect the progress of the transfer.
 */
public interface TransferProgressCallback {

    /**
     * Callback to notify consumers of the current amount of bytes that have transferred.
     *
     * @param currentBytes the current amount of accumulative bytes transferred thus far
     * @param totalBytes the total amount of bytes for the transfer
     */
    void onUpdate(long currentBytes, long totalBytes);

    /**
     * Callback to notify consumers of a failure during a transfer.
     *
     * @param cause the cause of the failure
     */
    void onFailure(Throwable cause);

    /**
     * Callback to notify consumers of a completed transfer.
     * @param bytesTransferred the total bytes transfered
     */
    void onComplete(long bytesTransferred);
}
