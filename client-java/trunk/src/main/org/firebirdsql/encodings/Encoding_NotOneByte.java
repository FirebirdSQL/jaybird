package org.firebirdsql.encodings;

public class Encoding_NotOneByte implements Encoding{

    String encoding = null;

    public Encoding_NotOneByte(String encoding){
        this.encoding = encoding;
    }
    // encode
    public byte[] encodeToCharset(String in){
        byte[] result = null;
        try {
            result = in.getBytes(encoding);
        }
        catch (java.io.UnsupportedEncodingException uee){
        }            
        return result;
    }
    public int encodeToCharset(char[] in, int off, int len, byte[] out){
        byte[] by = null;
        try {
            by = new String(in).getBytes(encoding);
				System.arraycopy(by, 0, out, 0, by.length);
        }
        catch (java.io.UnsupportedEncodingException uee){
        }
		  return by.length;
    }
    // decode
    public String decodeFromCharset(byte[] in){
        String result = null;
        try {
            result = new String(in,encoding);
        }
        catch (java.io.UnsupportedEncodingException uee){
        }
        return result;
    }
    public int decodeFromCharset(byte[] in, int off, int len, char[] out){
        String str = null; 
        try {
            str = new String(in, encoding);
				str.getChars(0, str.length(), out, 0);
        }
        catch (java.io.UnsupportedEncodingException uee){
        }
		  return str.length();
    }
}
