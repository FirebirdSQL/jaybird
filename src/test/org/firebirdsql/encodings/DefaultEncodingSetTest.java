// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for {@link DefaultEncodingSet}. Note that most of this class is tested through
 * {@link EncodingFactoryTest}. This is only to test additional behaviour.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class DefaultEncodingSetTest {

    /**
     * Test if an encoding is loaded as an informationOnly {@link EncodingDefinition} if
     * the charset is unsupported (ie does not exist).
     */
    @Test
    void testDefinitionWithUnsupportedCharset() {
        final EncodingDefinition encodingDefinition = new DefaultEncodingDefinition("INVALID", "INVALID", 1, 132, false);
        assertNotNull(encodingDefinition, "Expected a non-null EncodingDefinition");
        assertEquals("INVALID", encodingDefinition.getFirebirdEncodingName(), "Unexpected firebirdEncodingName");
        assertNull(encodingDefinition.getJavaCharset(), "Expected javaCharset to be null");
        assertTrue(encodingDefinition.isInformationOnly(), "Expected informationOnly EncodingDefinition");
    }
}
