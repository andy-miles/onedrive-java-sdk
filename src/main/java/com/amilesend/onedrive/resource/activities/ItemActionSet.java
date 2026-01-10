/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2026 Andy Miles (andy.miles@amilesend.com)
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
package com.amilesend.onedrive.resource.activities;

import com.amilesend.onedrive.resource.activities.action.CommentAction;
import com.amilesend.onedrive.resource.activities.action.DeleteAction;
import com.amilesend.onedrive.resource.activities.action.MentionAction;
import com.amilesend.onedrive.resource.activities.action.MoveAction;
import com.amilesend.onedrive.resource.activities.action.RenameAction;
import com.amilesend.onedrive.resource.activities.action.ShareAction;
import com.amilesend.onedrive.resource.activities.action.VersionAction;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;

/**
 * Describes actions that compose an activity on an item.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/itemactionset">
 * API Documentation.</a>
 * @see com.amilesend.onedrive.resource.activities.ItemActivity
 */
@Builder
@Getter
@ToString
public class ItemActionSet {
    /** The comment that was added to the item. */
    private final CommentAction comment;
    /** An item was created. {@code create} is either an empty object or {@code null}. */
    private final Object create;
    /** An item was deleted. */
    private final DeleteAction delete;
    /** An item was edited.  {@code edit} is either an empty object or {@code null}. */
    private final Object edit;
    /** A user was mentioned in the item. */
    private final MentionAction mention;
    /** The item was moved. */
    private final MoveAction move;
    /** The item was renamed. */
    private final RenameAction rename;
    /** The item was restored. {@code restore} is either an empty object or {@code null}. */
    private final Object restore;
    /** The item was shared. */
    private final ShareAction share;
    /** The item was versioned. */
    private final VersionAction version;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ItemActionSet that = (ItemActionSet) obj;
        return Objects.equals(getComment(), that.getComment())
                && objectDefinedEquals(getCreate(), that.getCreate())
                && Objects.equals(getDelete(), that.getDelete())
                && objectDefinedEquals(getEdit(), that.getEdit())
                && Objects.equals(getMention(), that.getMention())
                && Objects.equals(getMove(), that.getMove())
                && Objects.equals(getRename(), that.getRename())
                && objectDefinedEquals(getRestore(), that.getRestore())
                && Objects.equals(getShare(), that.getShare())
                && Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getComment(),
                Objects.nonNull(getCreate()),
                getDelete(),
                Objects.nonNull(getEdit()),
                getMention(),
                getMove(),
                getRename(),
                Objects.nonNull(getRestore()),
                getShare(),
                getVersion());
    }
}
