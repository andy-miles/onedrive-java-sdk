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
package com.amilesend.onedrive.connection.file;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class LogProgressCallbackTest {
    private static MockedStatic<LoggerFactory> FACTORY_MOCKED_STATIC = mockStatic(LoggerFactory.class);
    private static LoggingEventBuilder MOCK_LOGGING_EVENT_BUILDER = mock(LoggingEventBuilder.class);
    private static Logger MOCK_LOGGER = mock(Logger.class);
    private LogProgressCallback callbackUnderTest;

    @BeforeAll
    public static void init() {
        FACTORY_MOCKED_STATIC.when(() -> LoggerFactory.getLogger(any(Class.class)))
                .thenReturn(MOCK_LOGGER);
        lenient().when(MOCK_LOGGER.atLevel(any(Level.class))).thenReturn(MOCK_LOGGING_EVENT_BUILDER);
    }

    @AfterAll
    public static void cleanUp() {
        FACTORY_MOCKED_STATIC.close();
    }

    @BeforeEach
    public void setUp() {
        callbackUnderTest = newLogProgressCallback();
    }

    ////////////////
    // onUpdate
    ////////////////

    @Test
    public void onUpdate_withTimeAdvancementAndPercentageChange_shouldLogProgress() {
        callbackUnderTest.setLastUpdateTimestamp(Instant.now().minus(10L, ChronoUnit.SECONDS));
        callbackUnderTest.setLastUpdateProgressValue(0L);

        callbackUnderTest.onUpdate(50L, 100L);

        verify(MOCK_LOGGING_EVENT_BUILDER).log(
                eq("{} Status: {}% ({} of {} bytes)"),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq(50),
                eq(50L),
                eq(100L));
    }

    @Test
    public void onUpdate_withNoProgress_shouldNotLog() {
        callbackUnderTest.setLastUpdateTimestamp(Instant.now().minus(5L, ChronoUnit.SECONDS));
        callbackUnderTest.setLastUpdateProgressValue(50L);

        callbackUnderTest.onUpdate(50L, 100L);

        verifyNoMoreInteractions(MOCK_LOGGING_EVENT_BUILDER);
    }

    @Test
    public void onUpdate_withDurationLessThanUpdateInterval_shouldNotLog() {
        callbackUnderTest.setLastUpdateTimestamp(Instant.now());

        callbackUnderTest.onUpdate(50L, 100L);

        verifyNoMoreInteractions(MOCK_LOGGING_EVENT_BUILDER);
    }

    ////////////////
    // onFailure
    ////////////////

    @Test
    public void onFailure_shouldLogFailure() {
        final Throwable cause = new IllegalStateException("Exception");

        callbackUnderTest.onFailure(cause);

        verify(MOCK_LOGGER).error(
                eq("An error occurred during {}: {}"),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq("Exception"),
                eq(cause));
    }

    ////////////////
    // onComplete
    ////////////////

    @Test
    public void onComplete_shouldLogFailure() {
        final long bytesTransferred = 1000L;

        callbackUnderTest.onComplete(bytesTransferred);

        verify(MOCK_LOGGING_EVENT_BUILDER).log(
                eq("{} complete with {} bytes transferred"),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq(bytesTransferred));
    }

    private static LogProgressCallback newLogProgressCallback() {
        return LogProgressCallback.builder()
                .updateFrequency(Duration.ofSeconds(1L))
                .build();
    }
}
