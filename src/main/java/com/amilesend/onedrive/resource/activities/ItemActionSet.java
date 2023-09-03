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
package com.amilesend.onedrive.resource.activities;

import com.amilesend.onedrive.resource.activities.action.CommentAction;
import com.amilesend.onedrive.resource.activities.action.DeleteAction;
import com.amilesend.onedrive.resource.activities.action.MentionAction;
import com.amilesend.onedrive.resource.activities.action.MoveAction;
import com.amilesend.onedrive.resource.activities.action.RenameAction;
import com.amilesend.onedrive.resource.activities.action.ShareAction;
import com.amilesend.onedrive.resource.activities.action.VersionAction;
import lombok.Data;

import java.util.Objects;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;

/**
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/itemactionset">
 * API Documentation.</a>
 */
@Data
public class ItemActionSet {
    private CommentAction comment;
    /** CreateAction is either an empty object or null. */
    private Object create;
    private DeleteAction delete;
    /** EditAction is either an empty object or null. */
    private Object edit;
    private MentionAction mention;
    private MoveAction move;
    private RenameAction rename;
    /** RestoreAction is either an empty object or null. */
    private Object restore;
    private ShareAction share;
    private VersionAction version;

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
