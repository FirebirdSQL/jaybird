// SPDX-FileCopyrightText: Copyright 2021-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanLiteralTokenTest {

    @Test
    void trueToken_chars() {
        BooleanLiteralToken token = BooleanLiteralToken.trueToken(0, "true", 0, 4);

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
        BooleanLiteralToken token = BooleanLiteralToken.falseToken(0, "false", 0, 5);

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
        BooleanLiteralToken token = BooleanLiteralToken.unknownToken(0, "unknown", 0, 7);

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