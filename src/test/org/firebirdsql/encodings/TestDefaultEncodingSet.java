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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Additional tests for {@link DefaultEncodingSet}. Note that most of this class is tested through
 * {@link TestEncodingFactory}. This is only to test additional behaviour.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestDefaultEncodingSet {

    /**
     * Test if an encoding is loaded as an informationOnly {@link EncodingDefinition} if
     * the charset is unsupported (ie does not exist).
     */
    @Test
    public void testDefinitionWithUnsupportedCharset() {
        final EncodingDefinition encodingDefinition = DefaultEncodingSet
                .createEncodingDefinition("INVALID", "INVALID", 132, 1, false);
        assertNotNull("Expected a non-null EncodingDefinition", encodingDefinition);
        assertEquals("Unexpected firebirdEncodingName", "INVALID", encodingDefinition.getFirebirdEncodingName());
        assertNull("Expected javaCharset to be null", encodingDefinition.getJavaCharset());
        assertTrue("Expected informationOnly EncodingDefinition", encodingDefinition.isInformationOnly());
    }
}
