/*
 * Firebird Open Source JDBC Driver
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for {@link DefaultEncodingSet}. Note that most of this class is tested through
 * {@link EncodingFactoryTest}. This is only to test additional behaviour.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
