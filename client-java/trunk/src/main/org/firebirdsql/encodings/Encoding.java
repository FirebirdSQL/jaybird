package org.firebirdsql.encodings;

public interface Encoding{

    // encode
    public abstract byte[] encodeToCharset(String in);
    public abstract int encodeToCharset(char[] in, int off, int len, byte[] out);

    // decode
    public abstract String decodeFromCharset(byte[] in);
    public abstract int decodeFromCharset(byte[] in, int off, int len, char[] out);
}
