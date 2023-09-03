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
package com.amilesend.onedrive.resource;

import com.amilesend.onedrive.resource.item.DriveItem;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * Wraps a {@link CompletableFuture} specific to a {@link DriveItem} type for async upload operations.
 */
@RequiredArgsConstructor
public class DriveFileUploadExecution {
    private final CompletableFuture<DriveItem> delegate;

    /**
     * Blocks and retrieves the {@link DriveFile} for the asynchronous operation.
     *
     * @return the drive file
     * @throws Throwable if an error occurred during the asynchronous operation
     */
    public DriveFile get() throws Throwable {
        return new DriveFile(delegate.get());
    }
}
