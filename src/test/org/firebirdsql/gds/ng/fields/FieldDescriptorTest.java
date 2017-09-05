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
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.EncodingSpecificDatatypeCoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FieldDescriptorTest {

    private static final int CHARSET_ID_UTF8 = 4;
    private static final int CHARSET_ID_WIN1252 = 53;
    private static final IEncodingFactory encodingFactory = EncodingFactory.createInstance(StandardCharsets.UTF_8);
    private static final DefaultDatatypeCoder defaultDatatypeCoder = new DefaultDatatypeCoder(encodingFactory);

    @Test
    public void shouldUseDefaultDatatypeCoder_nonStringType() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_LONG, 0, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
    }

    @Test
    public void shouldUseDefaultDatatypeCoder_stringType_defaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, CHARSET_ID_UTF8, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
    }

    @Test
    public void shouldUseDefaultDatatypeCoder_stringType_dynamicCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, ISCConstants.CS_dynamic, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
    }

    @Test
    public void shouldUseEncodingSpecificDatatypeCoder_stringType_notDefaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, CHARSET_ID_WIN1252, 0);
        EncodingDefinition win1252EncodingDefinition =
                encodingFactory.getEncodingDefinitionByCharacterSetId(CHARSET_ID_WIN1252);

        DatatypeCoder datatypeCoder = descriptor.getDatatypeCoder();
        assertThat(datatypeCoder, instanceOf(EncodingSpecificDatatypeCoder.class));
        assertEquals(win1252EncodingDefinition, datatypeCoder.getEncodingDefinition());
    }

    @Test
    public void shouldUseDefaultDatatypeCoder_blobTextType_defaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, CHARSET_ID_UTF8);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
    }

    @Test
    public void shouldUseDefaultDatatypeCoder_blobTextType_dynamicCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, ISCConstants.CS_dynamic);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
    }

    @Test
    public void shouldUseEncodingSpecificDatatypeCoder_blobTextType_notDefaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, CHARSET_ID_WIN1252);
        EncodingDefinition win1252EncodingDefinition =
                encodingFactory.getEncodingDefinitionByCharacterSetId(CHARSET_ID_WIN1252);

        DatatypeCoder datatypeCoder = descriptor.getDatatypeCoder();
        assertThat(datatypeCoder, instanceOf(EncodingSpecificDatatypeCoder.class));
        assertEquals(win1252EncodingDefinition, datatypeCoder.getEncodingDefinition());
    }

    private FieldDescriptor createFieldDescriptor(int type, int subType, int scale) {
        return new FieldDescriptor(1, defaultDatatypeCoder, type, subType, scale, 4, "x", "t", "x", "t", "");
    }
}