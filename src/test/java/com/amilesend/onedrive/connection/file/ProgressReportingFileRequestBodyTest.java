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

import com.google.common.net.MediaType;
import lombok.SneakyThrows;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProgressReportingFileRequestBodyTest {
    private static final String ZIP_CONTENT_TYPE = MediaType.ZIP.toString();
    private static final String OCTET_STREAM_TYPE = MediaType.OCTET_STREAM.toString();

    @Mock
    private File mockFile;
    @Mock
    private TransferProgressCallback mockCallback;
    private ProgressReportingFileRequestBody bodyUnderTest;

    @BeforeEach
    public void setUp() {
        bodyUnderTest = ProgressReportingFileRequestBody.builder()
                .file(mockFile)
                .callback(mockCallback)
                .contentType(ZIP_CONTENT_TYPE)
                .build();
    }

    @Test
    public void builder_withNullFile_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> ProgressReportingFileRequestBody.builder().build());

    }

    @Test
    public void contentType_withValidType_shouldReturnMediaType() {
        assertAll(
                () -> assertEquals(ZIP_CONTENT_TYPE, bodyUnderTest.contentType().toString()),
                () -> assertEquals(ZIP_CONTENT_TYPE, bodyUnderTest.getContentTypeStringValue()));
    }

    @Test
    public void contentType_withNoneDefined_shouldReturnOctetStream() {
        bodyUnderTest = ProgressReportingFileRequestBody.builder()
                .file(mockFile)
                .build();

        assertAll(
                () -> assertEquals(OCTET_STREAM_TYPE, bodyUnderTest.contentType().toString()),
                () -> assertEquals(OCTET_STREAM_TYPE, bodyUnderTest.getContentTypeStringValue()));
    }

    @Test
    public void contentType_withUnknownType_shouldReturnNull() {
        bodyUnderTest = ProgressReportingFileRequestBody.builder()
                .file(mockFile)
                .callback(mockCallback)
                .contentType("whoknows")
                .build();

        assertNull(bodyUnderTest.contentType());
    }

    @Test
    public void contentLength_withFile_shouldReturnFileLength() {
        when(mockFile.length()).thenReturn(1234L);
        assertEquals(1234L, bodyUnderTest.contentLength());
    }

    @SneakyThrows
    @Test
    public void writeTo_withValidSink_shouldWriteToSink() {
        when(mockFile.length()).thenReturn(1024L);

        final BufferedSink mockOutputSink = mock(BufferedSink.class);
        when(mockOutputSink.getBuffer()).thenReturn(mock(Buffer.class));

        final Source mockSource = mock(Source.class);
        final BufferedSource mockBufferedSource = mock(BufferedSource.class);
        when(mockBufferedSource.read(any(Buffer.class), anyLong()))
                .thenReturn(1024L)
                .thenReturn(-1L);

        try(final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class)) {
            okioMockedStatic.when(() -> Okio.source(any(File.class))).thenReturn(mockSource);
            okioMockedStatic.when(() -> Okio.buffer(any(Source.class))).thenReturn(mockBufferedSource);

            bodyUnderTest.writeTo(mockOutputSink);

            assertAll(
                    () -> verify(mockCallback).onUpdate(eq(1024L), eq(1024L)),
                    () -> verify(mockCallback).onComplete(eq(1024L)));
        }
    }

    @SneakyThrows
    @Test
    public void writeTo_withIOException_shouldNotifyCallbackAndThrowException() {
        when(mockFile.length()).thenReturn(1024L);

        final BufferedSink mockOutputSink = mock(BufferedSink.class);
        when(mockOutputSink.getBuffer()).thenReturn(mock(Buffer.class));

        final Source mockSource = mock(Source.class);
        final BufferedSource mockBufferedSource = mock(BufferedSource.class);
        when(mockBufferedSource.read(any(Buffer.class), anyLong()))
                .thenThrow(new IOException("Exception"));

        try(final MockedStatic<Okio> okioMockedStatic = mockStatic(Okio.class)) {
            okioMockedStatic.when(() -> Okio.source(any(File.class))).thenReturn(mockSource);
            okioMockedStatic.when(() -> Okio.buffer(any(Source.class))).thenReturn(mockBufferedSource);

            final Throwable thrown = assertThrows(IOException.class, () -> bodyUnderTest.writeTo(mockOutputSink));

            verify(mockCallback).onFailure(eq(thrown));
        }
    }

    @SneakyThrows
    @Test
    public void write_to_withNullSource_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> bodyUnderTest.writeTo(null));
    }
}
