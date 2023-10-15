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
package com.amilesend.onedrive.data;

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.resource.item.BaseItem;
import com.amilesend.onedrive.resource.site.List;
import com.amilesend.onedrive.resource.site.ListItem;
import com.amilesend.onedrive.resource.site.ListItemVersion;
import com.amilesend.onedrive.resource.site.Site;
import com.amilesend.onedrive.resource.site.SiteCollection;
import com.amilesend.onedrive.resource.site.response.GetColumnValuesResponse;
import com.amilesend.onedrive.resource.site.type.CalculatedColumn;
import com.amilesend.onedrive.resource.site.type.ChoiceColumn;
import com.amilesend.onedrive.resource.site.type.ColumnDefinition;
import com.amilesend.onedrive.resource.site.type.ColumnLink;
import com.amilesend.onedrive.resource.site.type.ContentType;
import com.amilesend.onedrive.resource.site.type.ContentTypeOrder;
import com.amilesend.onedrive.resource.site.type.CurrencyColumn;
import com.amilesend.onedrive.resource.site.type.DateTimeColumn;
import com.amilesend.onedrive.resource.site.type.DefaultColumnValue;
import com.amilesend.onedrive.resource.site.type.ListInfo;
import com.amilesend.onedrive.resource.site.type.LookupColumn;
import com.amilesend.onedrive.resource.site.type.NumberColumn;
import com.amilesend.onedrive.resource.site.type.PersonOrGroupColumn;
import com.amilesend.onedrive.resource.site.type.TextColumn;
import lombok.experimental.UtilityClass;

import java.util.Map;

import static com.amilesend.onedrive.data.DriveTestDataHelper.newDrive;
import static com.amilesend.onedrive.data.DriveTestDataHelper.newDriveItem;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newIdentitySet;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newParentReference;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newPublicationFacet;
import static com.amilesend.onedrive.data.TypeTestDataHelper.newSharePointIds;

@UtilityClass
public class SiteTestDataHelper {

    public static Map<String, Object> newFieldValueSet() {
        return Map.of(
                "column1", "SomeString",
                "column2", false,
                "column3", 1000D,
                "column4", 100.5D);
    }

    public static List newList(final OneDriveConnection connection, final String siteId) {
        return newList(connection, siteId, null);
    }

    public static List newList(
            final OneDriveConnection connection,
            final String siteId,
            final Object system) {
        return List.builder()
                .columns(java.util.List.of(newColumnDefinition()))
                .connection(connection)
                .contentTypes(java.util.List.of(newContentType(), newContentType()))
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedDateTimestampValue")
                .description("ListDescriptionValue")
                .displayName("ListDisplayNameValue")
                .drive(newDrive(connection))
                .eTag("eTagValue")
                .id("ListIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                .list(newListInfo())
                .name("ListNameValue")
                .parentReference(newParentReference())
                .siteId(siteId)
                .system(system)
                .webUrl("http://someurl")
                .build();
    }

    public static ListItem newListItem(
            final OneDriveConnection connection,
            final String siteId,
            final String listId) {
        return ListItem.builder()
                .connection(connection)
                .contentType(newContentType())
                .deleted(false)
                .driveItem(newDriveItem(connection, 1))
                .fields(newFieldValueSet())
                .listId(listId)
                .sharepointIds(newSharePointIds())
                .siteId(siteId)
                // BaseItem
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedDateTimestampValue")
                .description("ListItemDescriptionValue")
                .eTag("eTagValue")
                .id("ListItemIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedDateTimestampValue")
                .name("ListItemNameValue")
                .parentReference(newParentReference())
                .webUrl("http://someurl")
                .build();
    }

    public static ListItemVersion newListItemVersion(
            final OneDriveConnection connection,
            final String siteId,
            final String listId,
            final String listItemId) {
        return ListItemVersion.builder()
                .connection(connection)
                .fields(newFieldValueSet())
                .id("ListItemVersionIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedDateTimestampValue")
                .listId(listId)
                .listItemId(listItemId)
                .published(newPublicationFacet("ListItemVersionIdValue"))
                .siteId(siteId)
                .build();
    }

    public static GetColumnValuesResponse newGetColumnValuesResponse() {
        return GetColumnValuesResponse.builder()
                .fields(newFieldValueSet())
                .id("ListItemIdValue")
                .build();
    }

    public static Site newSite(final OneDriveConnection connection) {
        return newSite(connection, new Object());
    }

    public static Site newSite(final OneDriveConnection connection, final Object root) {
        final String siteId = "SiteIdValue";
        return Site.builder()
                .columns(java.util.List.of(newColumnDefinition()))
                .connection(connection)
                .contentTypes(java.util.List.of(newContentType()))
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedDateTimeValue")
                .description("SiteDescriptionValue")
                .displayName("SiteDisplayNameValue")
                .eTag("eTagValue")
                .id(siteId)
                .items(java.util.List.of(newBaseItem()))
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedTimestampValue")
                .name("SiteNameValue")
                .parentReference(newParentReference())
                .root(root)
                .sharepointIds(newSharePointIds())
                .siteCollection(newSiteCollection())
                .webUrl("http://someurl")
                .build();
    }

    private static BaseItem newBaseItem() {
        return BaseItem.builder()
                .createdBy(newIdentitySet())
                .createdDateTime("CreatedDateTimeValue")
                .description("BaseItemDescription")
                .eTag("eTagValue")
                .id("BaseItemIdValue")
                .lastModifiedBy(newIdentitySet())
                .lastModifiedDateTime("LastModifiedDateTimestampValue")
                .name("BaseItemName")
                .parentReference(newParentReference())
                .webUrl("http://someurl")
                .build();
    }

    private static CalculatedColumn newCalculatedColumn() {
        return CalculatedColumn.builder()
                .format("dateTime")
                .formula("=A1")
                .outputType("text")
                .build();
    }

    private static ChoiceColumn newChoiceColumn() {
        return ChoiceColumn.builder()
                .allowTextEntry(true)
                .choices(java.util.List.of("Choice1", "Choice2", "Choice3"))
                .displayAs("dropDownMenu")
                .build();
    }

    private static ColumnDefinition newColumnDefinition() {
        return ColumnDefinition.builder()
                .booleanColumn(new Object())
                .calculated(newCalculatedColumn())
                .choice(newChoiceColumn())
                .columnGroup("ColumnGroupName")
                .currency(CurrencyColumn.builder().locale("en-US").build())
                .dateTime(newDateTimeColumn())
                .defaultValue(newDefaultColumnValue())
                .description("DescriptionValue")
                .displayName("DisplayNameValue")
                .enforceUniqueValues(false)
                .hidden(false)
                .id("ColumnDefinitionId")
                .indexed(true)
                .lookup(newLookupColumn())
                .name("ColumnDefinitionName")
                .number(newNumberColumn())
                .personOrGroup(newPersonOrGroupColumn())
                .readOnly(false)
                .required(false)
                .text(newTextColumn())
                .build();
    }

    private static ContentType newContentType() {
        return ContentType.builder()
                .columnLinks(java.util.List.of(newColumnLink(1), newColumnLink(2)))
                .description("ContentTypeDescriptionValue")
                .group("GroupValue")
                .hidden(false)
                .id("ContentTypeIdValue")
                .inheritedFrom(newParentReference())
                .name("ContentTypeName")
                .order(newContentTypeOrder())
                .parentId("ParentIdValue")
                .readOnly(false)
                .sealed(false)
                .build();
    }

    private static ContentTypeOrder newContentTypeOrder() {
        return ContentTypeOrder.builder()
                ._default(true)
                .position(1)
                .build();
    }

    private static ColumnLink newColumnLink(final int suffix) {
        return ColumnLink.builder()
                .id("ColumnLinkId" + suffix)
                .name("ColumnLinkName" + suffix)
                .build();
    }

    private static DateTimeColumn newDateTimeColumn() {
        return DateTimeColumn.builder()
                .displayAs("default")
                .format("dateTime")
                .build();
    }

    private static DefaultColumnValue newDefaultColumnValue() {
        return DefaultColumnValue.builder()
                .formula("FormulaValue")
                .value("ValueContent")
                .build();
    }

    private static LookupColumn newLookupColumn() {
        return LookupColumn.builder()
                .allowMultipleValues(true)
                .allowUnlimitedLength(false)
                .columnName("ColumnNameValue")
                .listId("ListIdValue")
                .primaryLookupColumnId("ColumnIdValue")
                .build();
    }

    private static NumberColumn newNumberColumn() {
        return NumberColumn.builder()
                .decimalPlaces("two")
                .displayAs("number")
                .maximum(100D)
                .minimum(0D)
                .build();
    }

    private static PersonOrGroupColumn newPersonOrGroupColumn() {
        return PersonOrGroupColumn.builder()
                .allowMultipleSelection(true)
                .chooseFromType("peopleOnly")
                .displayAs("account")
                .build();
    }

    private static SiteCollection newSiteCollection() {
        return SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("hostnamevalue")
                .root(new Object())
                .build();
    }

    private static TextColumn newTextColumn() {
        return TextColumn.builder()
                .allowMultipleLines(true)
                .appendChangesToExistingText(true)
                .linesForEditing(5)
                .maxLength(1000)
                .textType("plain")
                .build();
    }

    private static ListInfo newListInfo() {
        return ListInfo.builder()
                .contentTypesEnabled(true)
                .hidden(false)
                .template("documentLibrary")
                .build();
    }
}
