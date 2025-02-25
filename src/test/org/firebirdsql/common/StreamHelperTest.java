// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class StreamHelperTest {

    @Test
    void testReverseClosedRange() {
        int[] result = StreamHelper.reverseClosedRange(1, 5).toArray();

        assertArrayEquals(new int[] { 5, 4, 3, 2, 1 }, result);
    }

}