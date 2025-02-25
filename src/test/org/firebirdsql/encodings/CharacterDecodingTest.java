// SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.encodings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterDecodingTest {

    @Test
    void testOriginal() {
        IEncodingFactory factory = EncodingFactory.getPlatformDefault();
        Encoding encoding = factory.getEncodingForFirebirdName("ISO8859_1");
        String testStr = encoding.decodeFromCharset(new byte[] { 0x61, 0x62, 0x63 });
        assertEquals("\u0061\u0062\u0063", testStr, "Strings should be equal");
    }

}
