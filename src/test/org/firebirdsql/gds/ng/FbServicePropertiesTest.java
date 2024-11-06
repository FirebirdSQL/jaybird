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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.JaybirdSystemProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.common.SystemPropertyHelper.withTemporarySystemProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FbServiceProperties}.
 */
class FbServicePropertiesTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "*", "11" })
    void enableProtocolDefaultDerivedFromSystemProperty(String defaultValue) {
        try (var ignored = withTemporarySystemProperty(JaybirdSystemProperties.DEFAULT_ENABLE_PROTOCOL, defaultValue)) {
            assertEquals(defaultValue, new FbServiceProperties().getEnableProtocol(),
                    "Unexpected enableProtocol value");
        }
    }

}