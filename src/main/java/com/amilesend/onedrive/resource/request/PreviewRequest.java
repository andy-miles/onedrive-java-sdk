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
package com.amilesend.onedrive.resource.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a request to preview an item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/api/driveitem_preview">
 * API Documentation</a>.
 */
@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class PreviewRequest {
    /** The optional preview app to use. Valid values include:
     * <ul>
     *     <li>{@code null} - Default and one will be chosen automatically</li>
     *     <li>{@literal onedrive}</li>
     *     <li>{@literal office}</li>
     * </ul>
     */
    private String viewer;
    /** If {@code true}, the embedded view will omit controls. */
    private boolean chromeless;
    /** If {@code true}, the file can be edited from the UI. */
    private boolean allowEdit;
    /** Optional page number of the document formatted as a string. */
    private String page;
    /** Optional zoom level to start at. */
    private double zoom;
}
