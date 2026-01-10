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
package com.amilesend.onedrive.resource.site.type;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

import static com.amilesend.onedrive.resource.ResourceHelper.objectDefinedEquals;

/**
 * Describes a column definition.
 * <p>
 * <a href="https://learn.microsoft.com/en-us/onedrive/developer/rest-api/resources/columndefinition">
 * API Documentation</a>.
 * @see com.amilesend.onedrive.resource.site.Site
 */
@Builder
@Getter
@ToString
public class ColumnDefinition {
    /** The name of the associated group. */
    private final String columnGroup;
    /** The description of the column. */
    private final String description;
    /** The user-facing name for the column. */
    private final String displayName;
    /** When {@code true}, no two list items may have the save value for this column. */
    private final boolean enforceUniqueValues;
    /** Indicates if the column is displayed in the UI. */
    private final boolean hidden;
    /** The column identifier. */
    private final String id;
    /** Indicates if the column values can be used for sorting and searching. */
    private final boolean indexed;
    /** The API-facing name of the columns. */
    private final String name;
    /** Indicates if the column values are immutable. */
    private final boolean readOnly;
    /** Indicates if teh column value is required. */
    private final boolean required;
    /** When non-null, indicates that the column stores boolean values. */
    private final Object booleanColumn;
    /** When non-null, indicates that the column is calculated based on other columns. */
    private final CalculatedColumn calculated;
    /** When non-null, indicates that the column stores data from a list of choices. */
    private final ChoiceColumn choice;
    /** When non-null, indicates that the column stores currency values. */
    private final CurrencyColumn currency;
    /** When non-null, indicates that the column stores timestamps. */
    private final DateTimeColumn dateTime;
    /** The default value for the column. */
    private final DefaultColumnValue defaultValue;
    /** The column stores a date that is looked up from another source in the site. */
    private final LookupColumn lookup;
    /** The column stores numbers. */
    private final NumberColumn number;
    /** The column stores a person or group. */
    private final PersonOrGroupColumn personOrGroup;
    /** The column stores text. */
    private final TextColumn text;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final ColumnDefinition that = (ColumnDefinition) obj;
        return isEnforceUniqueValues() == that.isEnforceUniqueValues()
                && isHidden() == that.isHidden()
                && isIndexed() == that.isIndexed()
                && isReadOnly() == that.isReadOnly()
                && isRequired() == that.isRequired()
                && Objects.equals(getColumnGroup(), that.getColumnGroup())
                && Objects.equals(getDescription(), that.getDescription())
                && Objects.equals(getDisplayName(), that.getDisplayName())
                && Objects.equals(getId(), that.getId())
                && Objects.equals(getName(), that.getName())
                && objectDefinedEquals(getBooleanColumn(), that.getBooleanColumn())
                && Objects.equals(getCalculated(), that.getCalculated())
                && Objects.equals(getChoice(), that.getChoice())
                && Objects.equals(getCurrency(), that.getCurrency())
                && Objects.equals(getDateTime(), that.getDateTime())
                && Objects.equals(getDefaultValue(), that.getDefaultValue())
                && Objects.equals(getLookup(), that.getLookup())
                && Objects.equals(getNumber(), that.getNumber())
                && Objects.equals(getPersonOrGroup(), that.getPersonOrGroup())
                && Objects.equals(getText(), that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getColumnGroup(),
                getDescription(),
                getDisplayName(),
                isEnforceUniqueValues(),
                isHidden(),
                getId(),
                isIndexed(),
                getName(),
                isReadOnly(),
                isRequired(),
                Objects.nonNull(getBooleanColumn()),
                getCalculated(),
                getChoice(),
                getCurrency(),
                getDateTime(),
                getDefaultValue(),
                getLookup(),
                getNumber(),
                getPersonOrGroup(),
                getText());
    }
}
