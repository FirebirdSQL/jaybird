package org.firebirdsql.encodings;

public abstract class Encoding_OneByte implements Encoding{
        private static char[] byteToChar;
        private static boolean[] validByte;
        private static byte[] charToByte;
        private static String encoding;
        private static boolean isAscii;

        protected static void Initialize(String pEncoding){
            encoding = pEncoding;
            byteToChar = new char[256];
            validByte = new boolean[256];
            charToByte = new byte[256*256];
            byte[] val = new byte[1];
            char[] charArray = null;
            for (int i=0; i< 256; i++){
                val[0] = (byte) i;
                try {
                    charArray = new String(val, 0,1, encoding).toCharArray();
                    byteToChar[i] = charArray[0];
                    charToByte[byteToChar[i]] = (byte) i;
                }
                catch (java.io.UnsupportedEncodingException uee){
                    uee.printStackTrace();
                }
            }
        }

        byte[] bufferB = new byte[128];
        char[] bufferC = new char[128];
        
        // encode
        public byte[] encodeToCharset(String str){
            if (bufferB.length < str.length()) 
                bufferB = new byte[str.length()];
            int length = encodeToCharset(str.toCharArray(), 0, str.length(), bufferB);
            byte[] result = new byte[length];
            System.arraycopy(bufferB, 0, result, 0, length);
            return result;
        }

        public int encodeToCharset(char[] in, int off, int len, byte[] out){
            for (int i = off; i< off+len; i++)
               out[i] = charToByte[in[i]];
            return len;
        }

        // decode from charset
        public String decodeFromCharset(byte[] in){
            if (bufferC.length < in.length)
                bufferC = new char[in.length];                
            int length = decodeFromCharset(in, 0, in.length, bufferC);
            return new String(bufferC, 0, length);
        }
        
        public int decodeFromCharset(byte[] in, int off, int len, char[] out){
            for (int i = off; i< off+len; i++)
               out[i] = byteToChar[(int) in[i] & 0xFF];
            return len;
        }
    }
