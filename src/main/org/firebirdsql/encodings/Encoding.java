/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.encodings;

/**
 * Encoding translates between a Java string and a byte array for a specific (Firebird) encoding.
 * <p>
 * Encoding implementations need to be thread-safe.
 * </p>
 */
public interface Encoding {

    /**
     * Encodes the supplied String to bytes in this encoding.
     *
     * @param in
     *         String to encode
     * @return Byte array with encoded string
     */
    byte[] encodeToCharset(String in);

    /**
     * Decodes the supplied byte array to a String.
     *
     * @param in
     *         byte array to decode
     * @return String after decoding the byte array
     */
    String decodeFromCharset(byte[] in);

    /**
     * Decodes a part of the supplied byte array to a String.
     *
     * @param in
     *         byte array to decode
     * @param offset
     *         Offset into the byte array
     * @param length
     *         Length in bytes to decode
     * @return String after decoding the byte array
     */
    String decodeFromCharset(byte[] in, int offset, int length);

    /**
     * Derives an {@link Encoding} that applies the specified character translation.
     *
     * @param translator
     *         The translation to apply
     * @return The derived Encoding, or this encoding if {@code translator} is {@code null}
     */
    Encoding withTranslation(CharacterTranslator translator);

    /**
     * @return The name of the Java character set.
     */
    String getCharsetName();
}
