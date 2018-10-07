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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class QuoteStrategyTest {

    private static final String SIMPLE_NAME = "simpleName";
    private static final String SIMPLE_NAME_QUOTED = "\"simpleName\"";
    private static final String WITH_QUOTE = "with\"quoted";
    private static final String WITH_QUOTE_QUOTED = "\"with\"\"quoted\"";

    @Test
    public void testForDialectWithDialect0ReturnsQUOTES() {
        assertEquals(QuoteStrategy.QUOTES, QuoteStrategy.forDialect(0));
    }

    @Test
    public void testForDialectWithDialect1ReturnsNO_QUOTES() {
        assertEquals(QuoteStrategy.NO_QUOTES, QuoteStrategy.forDialect(ISCConstants.SQL_DIALECT_V5));
    }

    @Test
    public void testForDialectWithDialect2ReturnsQUOTES() {
        assertEquals(QuoteStrategy.QUOTES, QuoteStrategy.forDialect(ISCConstants.SQL_DIALECT_V6_TRANSITION));
    }

    @Test
    public void testForDialectWithDialect3ReturnsQUOTES() {
        assertEquals(QuoteStrategy.QUOTES, QuoteStrategy.forDialect(ISCConstants.SQL_DIALECT_V6));
    }

    @Test
    public void testNO_QUOTES_appendQuoted_appendsWithoutQuotes() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = QuoteStrategy.NO_QUOTES.appendQuoted(SIMPLE_NAME, sb);

        assertSame("Unexpected StringBuilder returned", sb, result);
        assertEquals(SIMPLE_NAME, sb.toString());
    }

    @Test
    public void testNO_QUOTES_quoteObjectName_returnsWithoutQuotes() {
        assertEquals(SIMPLE_NAME, QuoteStrategy.NO_QUOTES.quoteObjectName(SIMPLE_NAME));
    }

    @Test
    public void testQUOTES_appendQuoted_appendsWithQuotes() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = QuoteStrategy.QUOTES.appendQuoted(SIMPLE_NAME, sb);

        assertSame("Unexpected StringBuilder returned", sb, result);
        assertEquals(SIMPLE_NAME_QUOTED, sb.toString());
    }

    @Test
    public void testQUOTES_quoteObjectName_returnsWithQuotes() {
        assertEquals(SIMPLE_NAME_QUOTED, QuoteStrategy.QUOTES.quoteObjectName(SIMPLE_NAME));
    }

    @Test
    public void testQUOTES_quoteObjectName_escapesQuotes() {
        assertEquals(WITH_QUOTE_QUOTED, QuoteStrategy.QUOTES.quoteObjectName(WITH_QUOTE));
    }

    @Test
    public void testQUOTES_appendQuoted_escapesQuotes() {
        StringBuilder sb = new StringBuilder();
        StringBuilder result = QuoteStrategy.QUOTES.appendQuoted(WITH_QUOTE, sb);

        assertSame("Unexpected StringBuilder returned", sb, result);
        assertEquals(WITH_QUOTE_QUOTED, sb.toString());
    }
}