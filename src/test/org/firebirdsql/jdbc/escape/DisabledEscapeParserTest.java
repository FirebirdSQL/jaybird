// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link DisabledEscapeParser}.
 */
class DisabledEscapeParserTest {

    private final JdbcEscapeParser escapeParser = DisabledEscapeParser.getInstance();

    @Test
    void toNative_returnsStringAsIs() throws Exception {
        final String testString = "SELECT * FROM some_table WHERE {fn abs(x)} = ?";

        assertEquals(testString, escapeParser.toNative(testString));
    }

    @ParameterizedTest
    @EnumSource(CallEscapeHandling.class)
    void withCallEscapeHandling_returnsSameInstance(CallEscapeHandling callEscapeHandling) {
        assertSame(escapeParser, escapeParser.with(callEscapeHandling));
    }

}