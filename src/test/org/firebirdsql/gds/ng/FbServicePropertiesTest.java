// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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