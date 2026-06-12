// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for {@link Primitives}.
 */
@SuppressWarnings("ObviousNullCheck")
class PrimitivesTest {

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void orFalse(boolean value) {
        assertEquals(value, Primitives.orFalse(value));
    }

    @Test
    void orFalse_null() {
        assertFalse(Primitives.orFalse(null));
    }

    @ParameterizedTest
    @ValueSource(bytes = { -1, 0, 1})
    void orZero_Byte(byte value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Byte_null() {
        assertEquals(0, Primitives.orZero((Byte) null));
    }

    @ParameterizedTest
    @ValueSource(shorts = { -1, 0, 1})
    void orZero_Short(short value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Short_null() {
        assertEquals(0, Primitives.orZero((Short) null));
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 1})
    void orZero_Integer(int value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Integer_null() {
        assertEquals(0, Primitives.orZero((Integer) null));
    }

    @ParameterizedTest
    @ValueSource(longs = { -1, 0, 1})
    void orZero_Long(long value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Long_null() {
        assertEquals(0, Primitives.orZero((Long) null));
    }

    @ParameterizedTest
    @ValueSource(floats = { -1f, 0f, 1f})
    void orZero_Float(float value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Float_null() {
        assertEquals(0f, Primitives.orZero((Float) null));
    }

    @ParameterizedTest
    @ValueSource(doubles = { -1d, 0d, 1d})
    void orZero_Double(double value) {
        assertEquals(value, Primitives.orZero(value));
    }

    @Test
    void orZero_Double_null() {
        assertEquals(0d, Primitives.orZero((Double) null));
    }

}