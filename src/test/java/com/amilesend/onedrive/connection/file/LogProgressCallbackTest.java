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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class LogProgressCallbackTest {
    private static final long DURATION_INTERVAL_SECONDS = 10L;
    private static MockedStatic<LoggerFactory> FACTORY_MOCKED_STATIC = mockStatic(LoggerFactory.class);
    private static LoggingEventBuilder MOCK_LOGGING_EVENT_BUILDER = mock(LoggingEventBuilder.class);
    private static Logger MOCK_LOGGER = mock(Logger.class);
    private LogProgressCallback callbackUnderTest;
    @Mock
    private TransferProgressCallback mockChainedCallback;

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
        callbackUnderTest = LogProgressCallback.builder()
                .chainedCallback(mockChainedCallback)
                .updateFrequency(Duration.ofSeconds(DURATION_INTERVAL_SECONDS))
                .build();
    }

    @AfterEach
    public void resetMocks() {
        reset(MOCK_LOGGING_EVENT_BUILDER);
    }

    ////////////////
    // onUpdate
    ////////////////

    @Test
    public void onUpdate_withTimeAdvancementAndPercentageChange_shouldLogProgress() {
        final Instant lastUpdated = callbackUnderTest.getLastUpdateTimestamp();
        final long lapsedDurationIntervalSeconds = DURATION_INTERVAL_SECONDS + 1L;
        callbackUnderTest.setLastUpdateTimestamp(lastUpdated.minus(lapsedDurationIntervalSeconds, ChronoUnit.SECONDS));
        callbackUnderTest.setLastUpdateProgressValue(0L);

        callbackUnderTest.onUpdate(50L, 100L);

        verify(MOCK_LOGGING_EVENT_BUILDER).log(
                eq("{}{} Status: {}% ({} of {} bytes)"),
                eq(StringUtils.EMPTY),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq(50),
                eq(50L),
                eq(100L));
    }

    @Test
    public void onUpdate_withNoProgress_shouldNotLog() {
        final Instant lastUpdated = callbackUnderTest.getLastUpdateTimestamp();
        final long lapsedDurationIntervalSeconds = DURATION_INTERVAL_SECONDS + 1L;
        callbackUnderTest.setLastUpdateTimestamp(lastUpdated.minus(lapsedDurationIntervalSeconds, ChronoUnit.SECONDS));
        callbackUnderTest.setLastUpdateProgressValue(50L);

        callbackUnderTest.onUpdate(50L, 100L);

        verifyNoMoreInteractions(MOCK_LOGGING_EVENT_BUILDER);
    }

    @Test
    public void onUpdate_withDurationLessThanUpdateInterval_shouldNotLog() {
        callbackUnderTest.setLastUpdateTimestamp(Instant.now());
        callbackUnderTest.setLastUpdateProgressValue(0L);

        callbackUnderTest.onUpdate(50L, 100L);

        verifyNoMoreInteractions(MOCK_LOGGING_EVENT_BUILDER);
    }

    @Test
    public void onUpdate_withChainedCallback_shouldInvokeCallback() {
        callbackUnderTest.onUpdate(50L, 100L);

        verify(mockChainedCallback).onUpdate(eq(50L), eq(100L));
    }

    ////////////////
    // onFailure
    ////////////////

    @Test
    public void onFailure_shouldLogFailure() {
        final Throwable cause = new IllegalStateException("Exception");

        callbackUnderTest.onFailure(cause);

        verify(MOCK_LOGGER).error(
                eq("{}An error occurred during {}: {}"),
                eq(StringUtils.EMPTY),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq("Exception"),
                eq(cause));
    }

    @Test
    public void onFailure_withChainedCallback_shouldInvokeCallback() {
        final Throwable cause = new IllegalStateException("Exception");

        callbackUnderTest.onFailure(cause);

        verify(mockChainedCallback).onFailure(eq(cause));
    }

    ////////////////
    // onComplete
    ////////////////

    @Test
    public void onComplete_shouldLogCompletion() {
        final long bytesTransferred = 1000L;

        callbackUnderTest.onComplete(bytesTransferred);

        verify(MOCK_LOGGING_EVENT_BUILDER).log(
                eq("{}{} complete with {} bytes transferred"),
                eq(StringUtils.EMPTY),
                eq(LogProgressCallback.TransferType.UNDEFINED.getLogPrefix()),
                eq(bytesTransferred));
    }

    @Test
    public void onComplete_withChainedCallback_shouldInvokeCallback() {
        final long bytesTransferred = 1000L;

        callbackUnderTest.onComplete(bytesTransferred);

        verify(mockChainedCallback).onComplete(eq(bytesTransferred));
    }
}
