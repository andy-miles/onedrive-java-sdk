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
package com.amilesend.onedrive.resource.activities.action;

import com.amilesend.onedrive.resource.identity.IdentitySet;
import lombok.Data;

import java.util.List;

/**
 * Provides comment activity information for an item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/commentaction">
 * API Documentation.</a>
 * @see com.amilesend.onedrive.resource.activities.ItemActivity
 */
@Data
public class CommentAction {
    /** Indicates if the activity was a reply to an existing thread. */
    private boolean isReply;
    /** The user who started the thread. */
    private IdentitySet parentAuthor;
    /** The list of users who participated in the thread. */
    private List<IdentitySet> participants;
}
