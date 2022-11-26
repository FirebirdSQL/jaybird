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

import org.junit.Test;

import static org.junit.Assert.*;

public class BooleanLiteralTokenTest {

    @Test
    public void trueToken_chars() {
        BooleanLiteralToken token = BooleanLiteralToken.trueToken(0, "true", 0, 4);

        assertTrue("isTrue", token.isTrue());
        assertFalse("isFalse", token.isFalse());
        assertFalse("isUnknown", token.isUnknown());
    }

    @Test
    public void trueToken() {
        BooleanLiteralToken token = BooleanLiteralToken.trueToken(0, "true");

        assertTrue("isTrue", token.isTrue());
        assertFalse("isFalse", token.isFalse());
        assertFalse("isUnknown", token.isUnknown());
    }

    @Test
    public void falseToken_charArray() {
        BooleanLiteralToken token = BooleanLiteralToken.falseToken(0, "false", 0, 5);

        assertFalse("isTrue", token.isTrue());
        assertTrue("isFalse", token.isFalse());
        assertFalse("isUnknown", token.isUnknown());
    }

    @Test
    public void falseToken() {
        BooleanLiteralToken token = BooleanLiteralToken.falseToken(0, "false");

        assertFalse("isTrue", token.isTrue());
        assertTrue("isFalse", token.isFalse());
        assertFalse("isUnknown", token.isUnknown());
    }

    @Test
    public void unknownToken_charArray() {
        BooleanLiteralToken token = BooleanLiteralToken.unknownToken(0, "unknown", 0, 7);

        assertFalse("isTrue", token.isTrue());
        assertFalse("isFalse", token.isFalse());
        assertTrue("isUnknown", token.isUnknown());
    }

    @Test
    public void unknownToken() {
        BooleanLiteralToken token = BooleanLiteralToken.unknownToken(0, "unknown");

        assertFalse("isTrue", token.isTrue());
        assertFalse("isFalse", token.isFalse());
        assertTrue("isUnknown", token.isUnknown());
    }

}