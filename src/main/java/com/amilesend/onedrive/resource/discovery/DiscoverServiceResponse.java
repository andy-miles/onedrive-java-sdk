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
package com.amilesend.onedrive.resource.discovery;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * The service discovery response (business accounts only).
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/getting-started/aad-oauth">
 * API Documentation.</a>
 */
@Builder
@Data
public class DiscoverServiceResponse {
    /** The response context. */
    @SerializedName("@odata.context")
    private final String context;
    /** The list of available services. */
    @SerializedName("value")
    private List<Service> services;
}
