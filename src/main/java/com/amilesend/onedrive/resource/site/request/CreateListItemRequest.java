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
package com.amilesend.onedrive.resource.site.request;

import com.amilesend.onedrive.parse.strategy.GsonExclude;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;

import java.util.Map;

@Builder
@Data
public class CreateListItemRequest {
    /** The map of fields, with the key being the column name and the Object being its value. */
    @NonNull
    private final Map<String, Object> fields;
    @GsonExclude
    @NonNull
    private final Gson gson;

    /**
     * Returns the JSON representation of this request.
     *
     * @return the JSON string
     */
    public String toJson() {
        return gson.toJson(this);
    }

    /** Validates this request. */
    public void validate() {
        Validate.notEmpty(getFields(), "fields must not be empty");
    }
}
