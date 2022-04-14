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
package org.firebirdsql.jdbc.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanLiteralTokenTest {

    @Test
    void trueToken_chars() {
        BooleanLiteralToken token = BooleanLiteralToken.trueToken(0, "true".toCharArray(), 0, 4);

        assertTrue(token.isTrue(), "isTrue");
        assertFalse(token.isFalse(), "isFalse");
        assertFalse(token.isUnknown(), "isUnknown");
    }

    @Test
    void trueToken() {
        BooleanLiteralToken token = BooleanLiteralToken.trueToken(0, "true");

        assertTrue(token.isTrue(), "isTrue");
        assertFalse(token.isFalse(), "isFalse");
        assertFalse(token.isUnknown(), "isUnknown");
    }

    @Test
    void falseToken_charArray() {
        BooleanLiteralToken token = BooleanLiteralToken.falseToken(0, "false".toCharArray(), 0, 5);

        assertFalse(token.isTrue(), "isTrue");
        assertTrue(token.isFalse(), "isFalse");
        assertFalse(token.isUnknown(), "isUnknown");
    }

    @Test
    void falseToken() {
        BooleanLiteralToken token = BooleanLiteralToken.falseToken(0, "false");

        assertFalse(token.isTrue(), "isTrue");
        assertTrue(token.isFalse(), "isFalse");
        assertFalse(token.isUnknown(), "isUnknown");
    }

    @Test
    void unknownToken_charArray() {
        BooleanLiteralToken token = BooleanLiteralToken.unknownToken(0, "unknown".toCharArray(), 0, 7);

        assertFalse(token.isTrue(), "isTrue");
        assertFalse(token.isFalse(), "isFalse");
        assertTrue(token.isUnknown(), "isUnknown");
    }

    @Test
    void unknownToken() {
        BooleanLiteralToken token = BooleanLiteralToken.unknownToken(0, "unknown");

        assertFalse(token.isTrue(), "isTrue");
        assertFalse(token.isFalse(), "isFalse");
        assertTrue(token.isUnknown(), "isUnknown");
    }

}