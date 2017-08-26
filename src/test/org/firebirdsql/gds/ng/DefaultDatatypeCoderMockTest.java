/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DefaultDatatypeCoderMockTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Mock
    private IEncodingFactory encodingFactory;
    @Mock private Encoding encoding;
    private DefaultDatatypeCoder defaultDatatypeCoder;

    @Before
    public void setup() {
        context.checking(new Expectations() {{
            allowing(encodingFactory).getDefaultEncoding(); will(returnValue(encoding));
        }});
        defaultDatatypeCoder = new DefaultDatatypeCoder(encodingFactory);
    }

    @Test
    public void encodeStringDelegatesToEncoding() {
        final String inputValue = "result value";
        final byte[] resultValue = { 1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(encoding).encodeToCharset(inputValue); will(returnValue(resultValue));
        }});

        byte[] result = defaultDatatypeCoder.encodeString(inputValue);

        assertArrayEquals(resultValue, result);
    }

    @Test
    public void createWriterDelegatesToEncoding() {
        final OutputStream outputStream = context.mock(OutputStream.class);
        final Writer writer = context.mock(Writer.class);

        context.checking(new Expectations() {{
            oneOf(encoding).createWriter(outputStream); will(returnValue(writer));
        }});

        Writer result = defaultDatatypeCoder.createWriter(outputStream);

        assertSame(writer, result);
    }

    @Test
    public void decodeStringDelegatesToEncoding() {
        final byte[] inputValue = { 1, 2, 3, 4};
        final String resultValue = "result value";

        context.checking(new Expectations() {{
            oneOf(encoding).decodeFromCharset(inputValue); will(returnValue(resultValue));
        }});

        String result = defaultDatatypeCoder.decodeString(inputValue);

        assertEquals(resultValue, result);
    }

    @Test
    public void createReaderDelegatesToEncoding() {
        final InputStream inputStream = context.mock(InputStream.class);
        final Reader reader = context.mock(Reader.class);

        context.checking(new Expectations() {{
            oneOf(encoding).createReader(inputStream); will(returnValue(reader));
        }});

        Reader result = defaultDatatypeCoder.createReader(inputStream);

        assertSame(reader, result);
    }
    
}