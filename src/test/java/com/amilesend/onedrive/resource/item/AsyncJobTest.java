/*
 * onedrive-java-sdk - A Java SDK to access OneDrive drives and files.
 * Copyright © 2023-2024 Andy Miles (andy.miles@amilesend.com)
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

import com.amilesend.onedrive.connection.OneDriveConnection;
import com.amilesend.onedrive.parse.GsonParser;
import com.amilesend.onedrive.parse.resource.parser.AsyncJobStatusParser;
import com.amilesend.onedrive.resource.item.type.AsyncJobStatus;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsyncJobTest {
    private static final String MONITOR_URL = "http://localhost/monitor";


    @Mock
    private OneDriveConnection mockConnection;
    private AsyncJob jobUnderTest;

    @BeforeEach
    public void setUp() {
        lenient().when(mockConnection.newSignedForApiRequestBuilder()).thenReturn(new Request.Builder());
        jobUnderTest = new AsyncJob(MONITOR_URL, mockConnection);
    }

    @Test
    public void ctor_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new AsyncJob(null, mockConnection)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new AsyncJob(StringUtils.EMPTY, mockConnection)),
                () -> assertThrows(NullPointerException.class, () -> new AsyncJob(MONITOR_URL, null)));
    }

    @Test
    public void getStatus_shouldReturnStatus() {
        final AsyncJobStatus expected = mock(AsyncJobStatus.class);
        when(mockConnection.execute(any(Request.class), any(GsonParser.class))).thenReturn(expected);

        final AsyncJobStatus actual = jobUnderTest.getStatus();

        final ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        assertAll(
                () -> assertEquals(expected, actual),
                () -> verify(mockConnection).execute(requestCaptor.capture(), isA(AsyncJobStatusParser.class)),
                () -> assertEquals(MONITOR_URL, requestCaptor.getValue().url().toString()),
                () -> assertEquals("GET", requestCaptor.getValue().method()));
    }
}
