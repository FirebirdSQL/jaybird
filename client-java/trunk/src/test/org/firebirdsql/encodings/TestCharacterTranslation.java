package org.firebirdsql.encodings;

import junit.framework.TestCase;


/**
 * 
 */
public class TestCharacterTranslation extends TestCase {

    private byte[] testBytes = new byte[] {
            0x61, 0x62, 0x63
    };
    
    private char[] originalChars = new char[] {
            '\u0061', '\u0062', '\u0063' 
    };
    
    private char[] translatedChars = new char[] {
            '\u0062', '\u0061', '\u0063'
    };
    
    public void testOriginal() {
        Encoding encoding = EncodingFactory.getEncoding("ISO8859_1");
        
        String testStr = encoding.decodeFromCharset(testBytes);
        String checkStr = new String(originalChars);
        
        assertTrue("Strings should be equal", testStr.equals(checkStr));
    }
    
    public void testTranslation() throws Exception {
        Encoding encoding = EncodingFactory.getEncoding("ISO8859_1",
            "org.firebirdsql.encodings.testTranslation");
        
        String testStr = encoding.decodeFromCharset(testBytes);
        String checkStr = new String(translatedChars);
        
        assertTrue("Strings should be equal", testStr.equals(checkStr));
    }
}
