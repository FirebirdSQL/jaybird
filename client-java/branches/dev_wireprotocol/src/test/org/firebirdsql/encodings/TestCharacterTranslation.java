package org.firebirdsql.encodings;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestCharacterTranslation {

    private byte[] testBytes = new byte[] {
            0x61, 0x62, 0x63
    };

    private char[] originalChars = new char[] {
            '\u0061', '\u0062', '\u0063'
    };

    private char[] translatedChars = new char[] {
            '\u0062', '\u0061', '\u0063'
    };

    @Test
    public void testOriginal() {
        Encoding encoding = EncodingFactory.getEncoding("ISO8859_1");

        String testStr = encoding.decodeFromCharset(testBytes);
        String checkStr = new String(originalChars);

        assertTrue("Strings should be equal", testStr.equals(checkStr));
    }

    @Test
    public void testTranslation() throws Exception {
        Encoding encoding = EncodingFactory.getEncoding("ISO8859_1",
                "org.firebirdsql.encodings.testTranslation");

        String testStr = encoding.decodeFromCharset(testBytes);
        String checkStr = new String(translatedChars);

        assertTrue("Strings should be equal", testStr.equals(checkStr));
    }

    protected static final byte[] TRANSLATION_TEST_BYTES = new byte[] {
            (byte) 0xde, (byte) 0xbd, (byte) 0xd8, (byte) 0xda, (byte) 0xdb, (byte) 0xcc, (byte) 0xce, (byte) 0xcf
    };

    protected static final String TRANSLATION_TEST = "\u00df\u00a7\u00c4\u00d6\u00dc\u00e4\u00f6\u00fc";

    @Test
    public void testHPUXTranslations() throws Exception {
        Encoding encoding = EncodingFactory.getEncoding("Cp1252", "translation.hpux");

        byte[] direct = encoding.encodeToCharset(TRANSLATION_TEST);

        assertArrayEquals("Encoded content should be correct", TRANSLATION_TEST_BYTES, direct);

        String reverse = encoding.decodeFromCharset(TRANSLATION_TEST_BYTES);

        assertEquals("Decoded content shouls be correct", TRANSLATION_TEST, reverse);
    }
}
