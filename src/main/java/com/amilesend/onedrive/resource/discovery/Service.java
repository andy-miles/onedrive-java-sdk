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

/**
 * Describes a single service as part of a discovery query response (business accounts only).
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/getting-started/aad-oauth">
 * Auth API Documentation.</a>
 * <p>
 * <a href="https://learn.microsoft.com/en-us/previous-versions/office/office-365-api/api/discovery-service-rest-operations">
 * Discovery Service REST API Reference</a>
 */
@Builder
@Data
public class Service {
    /** The associated service information. */
    @SerializedName("@odata.type")
    private final String type;
    /** The service capability. */
    private final String capability;
    /** The name of the service. */
    private final String serviceName;
    /** The API version for the service. */
    private final String serviceApiVersion;
    /** The service endpoint (required for configuring the OneDriveConnection instance). */
    private final String serviceEndpointUri;
    /** The service resourceId (required for requesting access tokens). */
    private final String serviceResourceId;
}
