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
package com.amilesend.onedrive.resource.site;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;

/**
 * Defines a collection of sites. Note: When defined in a {@link Site}, indicates that the site is the root for the
 * site collection.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/sitecollection"> API Documentation.</a>
 */
@Builder
@Getter
@ToString
public class SiteCollection {
    /** The location code (e.g., "JPN"). */
    private final String dataLocationCode;
    /** The hostname for the sharepoint site. */
    private final String hostname;
    /** If defined, indicates that the collection is the top-most collection among other collections. */
    private final Object root;

    /**
     * Indicates if this site collection is the root collection.
     *
     * @return {@code true} if this is the root collection; else, {@code false}
     */
    public boolean isRoot() {
        return getRoot() != null;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final SiteCollection that = (SiteCollection) obj;
        return Objects.equals(getDataLocationCode(), that.getDataLocationCode())
                && Objects.equals(getHostname(), that.getHostname())
                && objectDefinedEquals(getRoot(), that.getRoot());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDataLocationCode(), getHostname(), Objects.nonNull(getRoot()));
    }
}
