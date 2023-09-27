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
package com.amilesend.onedrive.resource.item;

import com.amilesend.onedrive.resource.identity.IdentitySet;
import com.amilesend.onedrive.resource.item.type.ItemReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines common attributes for drive items.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/baseitem"> API Documentation</a>.
 */
@Data
@SuperBuilder
public class BaseItem {
    /** The unique identifier for the item. */
    private final String id;
    /** The source identity that created the item. */
    private final IdentitySet createdBy;
    /** Describes when the item was created. */
    private final String createdDateTime;
    /** Associated ETag for the item. */
    private final String eTag;
    /** The identity that last modified the item. */
    private final IdentitySet lastModifiedBy;
    /** Describes when the item was last modified. */
    private final String lastModifiedDateTime;
    /** URL for the resource shown in a browser. */
    private final String webUrl;

    /** Description of the item. */
    private String description;
    /** The name of the item. */
    private String name;
    /** Describes the parent information. */
    private ItemReference parentReference;

    /**
     * Utility method for child method operations that determines if a given {@link DriveItemPage} contains a
     * next page.
     *
     * @param page the page
     * @return {@code true} if the page has a next page; else, {@code false}
     */
    protected static boolean hasNextPage(final DriveItemPage page) {
        return page != null && StringUtils.isNotBlank(page.getNextLink());
    }
}
