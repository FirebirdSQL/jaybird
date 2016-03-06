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
package org.firebirdsql.encodings;

import org.firebirdsql.gds.ISCConstants;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

/**
 * Tests for {@link ConnectionEncodingFactory}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestConnectionEncodingFactory {

    private final EncodingFactory standardFactory = EncodingFactory.getRootEncodingFactory();
    // We use US_ASCII as it is unlikely to be a real default on a system, so we know it was correctly applied
    private final EncodingDefinition defaultEncodingDefinition =
            standardFactory.getEncodingDefinitionByCharset(StandardCharsets.US_ASCII);
    private ConnectionEncodingFactory factory;

    @Before
    public void setUp() {
        factory = new ConnectionEncodingFactory(standardFactory, defaultEncodingDefinition);
    }

    @Test
    public void defaultEncodingMustMatchEncodingDefinition() {
        assertSame(defaultEncodingDefinition, factory.getDefaultEncodingDefinition());
        assertSame(defaultEncodingDefinition.getEncoding(), factory.getDefaultEncoding());
    }

    @Test
    public void noneEncodingDefinitionUsesCharsetOfDefaultEncoding() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertFalse(noneEncodingDefinition.isInformationOnly());
        assertEquals(defaultEncodingDefinition.getEncoding().getCharsetName(),
                noneEncodingDefinition.getEncoding().getCharsetName());
    }

    @Test
    public void octetsEncodingDefinitionUsesCharsetOfDefaultEncoding() {
        EncodingDefinition octetsEncoding =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertFalse(octetsEncoding.isInformationOnly());
        assertEquals(defaultEncodingDefinition.getEncoding().getCharsetName(),
                octetsEncoding.getEncoding().getCharsetName());
    }

    @Test
    public void getDefaultEncodingDefinitionForDynamicCharacterSetId() {
        EncodingDefinition defaultEncodingDefinition = factory.getDefaultEncodingDefinition();

        assertSame(defaultEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_dynamic));
        assertSame(defaultEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_dynamic));
    }

    @Test
    public void getNoneEncodingDefinitionByCharacterSetId() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertSame(noneEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_NONE));
        assertSame(noneEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_NONE));
    }

    @Test
    public void getOctetsEncodingDefinitionByCharacterSetId() {
        EncodingDefinition octetsEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertSame(octetsEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_BINARY));
        assertSame(octetsEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_BINARY));
    }

    @Test
    public void getNoneEncodingByFirebirdName() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertSame(noneEncodingDefinition.getEncoding(),
                factory.getEncodingForFirebirdName(EncodingFactory.ENCODING_NAME_NONE));
    }

    @Test
    public void getOctetsEncodingByFirebirdName() {
        EncodingDefinition octetsEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertSame(octetsEncodingDefinition.getEncoding(),
                factory.getEncodingForFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS));
    }
}
