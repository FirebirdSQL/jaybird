// SPDX-FileCopyrightText: Copyright 2016-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link ConnectionEncodingFactory}.
 *
 * @author Mark Rotteveel
 */
class ConnectionEncodingFactoryTest {

    private final EncodingFactory standardFactory = EncodingFactory.getRootEncodingFactory();
    // We use US_ASCII as it is unlikely to be a real default on a system, so we know it was correctly applied
    private final EncodingDefinition defaultEncodingDefinition =
            standardFactory.getEncodingDefinitionByCharset(StandardCharsets.US_ASCII);
    private final ConnectionEncodingFactory factory =
            new ConnectionEncodingFactory(standardFactory, defaultEncodingDefinition);

    @Test
    void defaultEncodingMustMatchEncodingDefinition() {
        assertSame(defaultEncodingDefinition, factory.getDefaultEncodingDefinition());
        assertSame(defaultEncodingDefinition.getEncoding(), factory.getDefaultEncoding());
    }

    @Test
    void noneEncodingDefinitionUsesCharsetOfDefaultEncoding() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertFalse(noneEncodingDefinition.isInformationOnly());
        assertEquals(defaultEncodingDefinition.getEncoding().getCharsetName(),
                noneEncodingDefinition.getEncoding().getCharsetName());
    }

    @Test
    void octetsEncodingDefinitionUsesCharsetOfDefaultEncoding() {
        EncodingDefinition octetsEncoding =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertFalse(octetsEncoding.isInformationOnly());
        assertEquals(defaultEncodingDefinition.getEncoding().getCharsetName(),
                octetsEncoding.getEncoding().getCharsetName());
    }

    @Test
    void getDefaultEncodingDefinitionForDynamicCharacterSetId() {
        EncodingDefinition defaultEncodingDefinition = factory.getDefaultEncodingDefinition();

        assertSame(defaultEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_dynamic));
        assertSame(defaultEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_dynamic));
    }

    @Test
    void getNoneEncodingDefinitionByCharacterSetId() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertSame(noneEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_NONE));
        assertSame(noneEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_NONE));
    }

    @Test
    void getOctetsEncodingDefinitionByCharacterSetId() {
        EncodingDefinition octetsEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertSame(octetsEncodingDefinition, factory.getEncodingDefinitionByCharacterSetId(ISCConstants.CS_BINARY));
        assertSame(octetsEncodingDefinition.getEncoding(),
                factory.getEncodingForCharacterSetId(ISCConstants.CS_BINARY));
    }

    @Test
    void getNoneEncodingByFirebirdName() {
        EncodingDefinition noneEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_NONE);

        assertSame(noneEncodingDefinition.getEncoding(),
                factory.getEncodingForFirebirdName(EncodingFactory.ENCODING_NAME_NONE));
    }

    @Test
    void getOctetsEncodingByFirebirdName() {
        EncodingDefinition octetsEncodingDefinition =
                factory.getEncodingDefinitionByFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS);

        assertSame(octetsEncodingDefinition.getEncoding(),
                factory.getEncodingForFirebirdName(EncodingFactory.ENCODING_NAME_OCTETS));
    }
}
