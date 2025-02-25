// SPDX-FileCopyrightText: Copyright 2017-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDatatypeCoderMockTest {

    @Mock
    private IEncodingFactory encodingFactory;
    @Mock private Encoding encoding;
    private DefaultDatatypeCoder defaultDatatypeCoder;

    @BeforeEach
    void setup() {
        lenient().when(encodingFactory.getDefaultEncoding()).thenReturn(encoding);
        defaultDatatypeCoder = new DefaultDatatypeCoder(encodingFactory);
    }

    @Test
    void encodeStringDelegatesToEncoding() {
        final String inputValue = "result value";
        final byte[] resultValue = { 1, 2, 3, 4};
        when(encoding.encodeToCharset(inputValue)).thenReturn(resultValue);

        byte[] result = defaultDatatypeCoder.encodeString(inputValue);

        assertArrayEquals(resultValue, result);
    }

    @Test
    void createWriterDelegatesToEncoding(@Mock OutputStream outputStream) {
        final Writer writer = new StringWriter();
        when(encoding.createWriter(outputStream)).thenReturn(writer);

        Writer result = defaultDatatypeCoder.createWriter(outputStream);

        assertSame(writer, result);
    }

    @Test
    void decodeStringDelegatesToEncoding() {
        final byte[] inputValue = { 1, 2, 3, 4};
        final String resultValue = "result value";
        when(encoding.decodeFromCharset(inputValue)).thenReturn(resultValue);

        String result = defaultDatatypeCoder.decodeString(inputValue);

        assertEquals(resultValue, result);
    }

    @Test
    void createReaderDelegatesToEncoding(@Mock InputStream inputStream) {
        final Reader reader = new StringReader("test");
        when(encoding.createReader(inputStream)).thenReturn(reader);

        Reader result = defaultDatatypeCoder.createReader(inputStream);

        assertSame(reader, result);
    }
    
}