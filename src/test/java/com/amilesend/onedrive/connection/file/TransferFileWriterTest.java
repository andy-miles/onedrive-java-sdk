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
package com.amilesend.onedrive.connection.file;

import lombok.SneakyThrows;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransferFileWriterTest {
    @Mock
    private Path mockOutput;
    @Mock
    private TransferProgressCallback mockCallback;
    private TransferFileWriter writerUnderTest;

    @BeforeEach
    public void setUp() {
        writerUnderTest = TransferFileWriter.builder()
                .output(mockOutput)
                .callback(mockCallback)
                .build();
    }

    @Test
    public void builder_withInvalidParameters_shouldThrowException() {
        assertAll(
                () -> assertThrows(NullPointerException.class,
                        () -> TransferFileWriter.builder()
                                .callback(mockCallback)
                                .build()),
                () -> assertThrows(NullPointerException.class,
                        () -> TransferFileWriter.builder()
                                .output(mockOutput)
                                .build()));
    }

    @SneakyThrows
    @Test
    public void write_withValidSource_shouldNotifyCallbackAndReturnTransferredBytes() {
        final BufferedSource mockSource = mock(BufferedSource.class);
        when(mockSource.read(any(Buffer.class), anyLong()))
                .thenReturn(1024L)
                .thenReturn(-1L);

        final Sink mockSink = mock(Sink.class);
        final BufferedSink mockBufferedSink = mock(BufferedSink.class);
        when(mockBufferedSink.getBuffer()).thenReturn(mock(Buffer.class));

        try (final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class)) {
            okioMockedStatic.when(() -> Okio.sink(any(Path.class))).thenReturn(mockSink);
            okioMockedStatic.when(() -> Okio.buffer(any(Sink.class))).thenReturn(mockBufferedSink);

            final long actual = writerUnderTest.write(mockSource, 1024L);

            assertAll(
                    () -> verify(mockCallback).onUpdate(eq(1024L), eq(1024L)),
                    () -> verify(mockCallback).onComplete(eq(1024L)),
                    () -> assertEquals(1024L, actual));

        }
    }

    @SneakyThrows
    @Test
    public void write_withIOException_shouldNotifyCallbackAndThrowException() {
        final BufferedSource mockSource = mock(BufferedSource.class);
        when(mockSource.read(any(Buffer.class), anyLong()))
                .thenThrow(new IOException("Exception"));

        final Sink mockSink = mock(Sink.class);
        final BufferedSink mockBufferedSink = mock(BufferedSink.class);
        when(mockBufferedSink.getBuffer()).thenReturn(mock(Buffer.class));

        try (final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class)) {
            okioMockedStatic.when(() -> Okio.sink(any(Path.class))).thenReturn(mockSink);
            okioMockedStatic.when(() -> Okio.buffer(any(Sink.class))).thenReturn(mockBufferedSink);

            final Throwable thrown = assertThrows(IOException.class,
                    () -> writerUnderTest.write(mockSource, 1024L));

            verify(mockCallback).onFailure(eq(thrown));
        }
    }

    @SneakyThrows
    @Test
    public void write_withNullSource_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> writerUnderTest.write(null, 1024L));
    }

    @SneakyThrows
    @Test
    public void write_withInvalidSize_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> writerUnderTest.write(mock(BufferedSource.class), -1L));
    }
}
