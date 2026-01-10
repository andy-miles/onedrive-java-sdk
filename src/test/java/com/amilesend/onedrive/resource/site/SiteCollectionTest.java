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
package com.amilesend.onedrive.resource.site;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SiteCollectionTest {
    @Test
    public void isRoot_withRootObjectDefined_shouldReturnTrue(){
        final SiteCollection siteCollectionUnderTest = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Hostname")
                .root(new Object())
                .build();
        assertTrue(siteCollectionUnderTest.isRoot());
    }

    @Test
    public void isRoot_withNoRootObjectDefined_shouldReturnFalse() {
        final SiteCollection siteCollectionUnderTest = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Hostname")
                .build();
        assertFalse(siteCollectionUnderTest.isRoot());
    }

    @Test
    public void equals_withNonEqualMembers_shouldReturnFalse() {
        final SiteCollection thisSite = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Hostname")
                .root(new Object())
                .build();
        final SiteCollection duplicateSite = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Hostname")
                .root(new Object())
                .build();
        final SiteCollection differentDataLocationCode = SiteCollection.builder()
                .dataLocationCode("Different")
                .hostname("Hostname")
                .root(new Object())
                .build();
        final SiteCollection differentHostname = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Different")
                .root(new Object())
                .build();
        final SiteCollection differentRoot = SiteCollection.builder()
                .dataLocationCode("USA")
                .hostname("Hostname")
                .build();

        assertAll(
                () -> assertTrue(thisSite.equals(thisSite)),
                () -> assertTrue(thisSite.equals(duplicateSite)),
                () -> assertFalse(thisSite.equals(null)),
                () -> assertFalse(thisSite.equals(Site.builder().build())),
                () -> assertFalse(thisSite.equals(differentDataLocationCode)),
                () -> assertFalse(thisSite.equals(differentHostname)),
                () -> assertFalse(thisSite.equals(differentRoot)));
    }
}
