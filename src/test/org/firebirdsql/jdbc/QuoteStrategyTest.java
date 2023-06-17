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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.gds.ISCConstants.SQL_DIALECT_V6;
import static org.firebirdsql.gds.ISCConstants.SQL_DIALECT_V6_TRANSITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuoteStrategyTest {

    @ParameterizedTest
    @ValueSource(ints = { 0 /* "auto-select" */, SQL_DIALECT_V6_TRANSITION, SQL_DIALECT_V6, 4 /* does not exist */ } )
    void testForDialectWithDialectOtherThan1ReturnsDIALECT_3(int dialect) {
        assertEquals(QuoteStrategy.DIALECT_3, QuoteStrategy.forDialect(dialect));
    }

    @Test
    void testForDialectWithDialect1ReturnsDIALECT_1() {
        assertEquals(QuoteStrategy.DIALECT_1, QuoteStrategy.forDialect(ISCConstants.SQL_DIALECT_V5));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            STRATEGY,  INPUT,       EXPECTED_OUTPUT
            DIALECT_1, simpleName,  simpleName
            DIALECT_1, with"quoted, with"quoted
            DIALECT_1, with'quoted, with'quoted
            NO_QUOTES, simpleName,  simpleName
            NO_QUOTES, with"quoted, with"quoted
            NO_QUOTES, with'quoted, with'quoted
            DIALECT_3, simpleName,  "simpleName"
            DIALECT_3, with"quoted, "with""quoted"
            DIALECT_3, with'quoted, "with'quoted"
            QUOTES,    simpleName,  "simpleName"
            QUOTES,    with"quoted, "with""quoted"
            QUOTES,    with'quoted, "with'quoted"
            """)
    void test_appendQuoted_and_quoteObjectName(QuoteStrategy quoteStrategy, String input, String expectedOutput) {
        var sb = new StringBuilder();
        StringBuilder result = quoteStrategy.appendQuoted(input, sb);

        assertSame(sb, result, "Unexpected StringBuilder returned");
        assertEquals(expectedOutput, sb.toString(), "appendQuoted");
        assertEquals(expectedOutput, quoteStrategy.quoteObjectName(input), "quoteObjectName");
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, quoteCharacter = '`', textBlock = """
            STRATEGY,  INPUT,       EXPECTED_OUTPUT
            DIALECT_1, simpleName,  "simpleName"
            DIALECT_1, with"quoted, "with""quoted"
            DIALECT_1, with'quoted, "with'quoted"
            NO_QUOTES, simpleName,  "simpleName"
            NO_QUOTES, with"quoted, "with""quoted"
            NO_QUOTES, with'quoted, "with'quoted"
            DIALECT_3, simpleName,  'simpleName'
            DIALECT_3, with"quoted, 'with"quoted'
            DIALECT_3, with'quoted, 'with''quoted'
            QUOTES,    simpleName,  'simpleName'
            QUOTES,    with"quoted, 'with"quoted'
            QUOTES,    with'quoted, 'with''quoted'
            """)
    void test_appendLiteral_and_quoteLiteral(QuoteStrategy quoteStrategy, String input, String expectedOutput) {
        var sb = new StringBuilder();
        StringBuilder result = quoteStrategy.appendLiteral(input, sb);

        assertSame(sb, result, "Unexpected StringBuilder returned");
        assertEquals(expectedOutput, sb.toString(), "appendLiteral");
        assertEquals(expectedOutput, quoteStrategy.quoteLiteral(input), "quoteLiteral");
    }

}