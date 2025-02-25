// SPDX-FileCopyrightText: Copyright 2017-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.fields;

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DatatypeCoder;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.EncodingSpecificDatatypeCoder;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mark Rotteveel
 */
class FieldDescriptorTest {

    private static final int CHARSET_ID_UTF8 = 4;
    private static final int CHARSET_ID_WIN1252 = 53;
    private static final IEncodingFactory encodingFactory = EncodingFactory.createInstance(StandardCharsets.UTF_8);
    private static final DefaultDatatypeCoder defaultDatatypeCoder = new DefaultDatatypeCoder(encodingFactory);

    @Test
    void shouldUseDefaultDatatypeCoder_nonStringType() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_LONG, 0, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
        assertEquals(-1, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseDefaultDatatypeCoder_stringType_defaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, CHARSET_ID_UTF8, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
        assertEquals(2, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseDefaultDatatypeCoder_stringType_dynamicCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, ISCConstants.CS_dynamic, 0);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
        assertEquals(2, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseEncodingSpecificDatatypeCoder_stringType_notDefaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_TEXT, CHARSET_ID_WIN1252, 0);
        EncodingDefinition win1252EncodingDefinition =
                encodingFactory.getEncodingDefinitionByCharacterSetId(CHARSET_ID_WIN1252);

        DatatypeCoder datatypeCoder = descriptor.getDatatypeCoder();
        assertThat(datatypeCoder, instanceOf(EncodingSpecificDatatypeCoder.class));
        assertEquals(win1252EncodingDefinition, datatypeCoder.getEncodingDefinition());
        assertEquals(8, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseDefaultDatatypeCoder_blobTextType_defaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, CHARSET_ID_UTF8);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
        assertEquals(-1, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseDefaultDatatypeCoder_blobTextType_dynamicCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, ISCConstants.CS_dynamic);

        assertSame(defaultDatatypeCoder, descriptor.getDatatypeCoder());
        assertEquals(-1, descriptor.getCharacterLength());
    }

    @Test
    void shouldUseEncodingSpecificDatatypeCoder_blobTextType_notDefaultCharset() {
        FieldDescriptor descriptor = createFieldDescriptor(ISCConstants.SQL_BLOB, 1, CHARSET_ID_WIN1252);
        EncodingDefinition win1252EncodingDefinition =
                encodingFactory.getEncodingDefinitionByCharacterSetId(CHARSET_ID_WIN1252);

        DatatypeCoder datatypeCoder = descriptor.getDatatypeCoder();
        assertThat(datatypeCoder, instanceOf(EncodingSpecificDatatypeCoder.class));
        assertEquals(win1252EncodingDefinition, datatypeCoder.getEncodingDefinition());
        assertEquals(-1, descriptor.getCharacterLength());
    }

    private FieldDescriptor createFieldDescriptor(int type, int subType, int scale) {
        return new FieldDescriptor(1, defaultDatatypeCoder, type, subType, scale, 8, "x", "t", "x", "t", "");
    }
}