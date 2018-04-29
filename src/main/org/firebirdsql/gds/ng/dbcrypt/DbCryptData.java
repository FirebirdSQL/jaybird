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
package org.firebirdsql.gds.ng.dbcrypt;

/**
 * Data of a database encryption key callback (or reply).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public final class DbCryptData {

    public static final DbCryptData EMPTY_DATA = new DbCryptData(null, 0);

    private final byte[] pluginData;
    private final int replySize;

    /**
     * Initializes {@code DbCryptData} instance.
     *
     * @param pluginData
     *         Data for/from plugin (can be {@code null})
     * @param replySize
     *         Expected reply size (normally use {@code 0} for a reply)
     * @throws IllegalArgumentException when plugin data exceeds maximum length of 32767 bytes
     */
    public DbCryptData(byte[] pluginData, int replySize) {
        if (pluginData != null && pluginData.length > Short.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Length of plugin data exceeds maximum length of 32767 bytes, was: " + pluginData.length);
        }
        this.pluginData = pluginData;
        this.replySize = replySize;
    }

    /**
     * @return Plugin data (can be {@code null})
     */
    public byte[] getPluginData() {
        return pluginData;
    }

    /**
     * Returns the expected reply size.
     * <p>
     * For a protocol version 13 callback, the value will be {@code Integer.MIN_VALUE} as the protocol does not include
     * this information.
     * </p>
     * <p>
     * In the case of a callback, this value is as received from Firebird. Judging by the code in Firebird for protocol
     * v14 and higher, this value may be negative, and should then be considered equivalent to {@code 1}.
     * </p>
     * <p>
     * Plugins can use the value as a hint to the expected size of their reply. However smaller (or larger) replies will
     * work.
     * </p>
     *
     * @return Expected reply size
     */
    public int getReplySize() {
        return replySize;
    }

    /**
     * Creates a reply with plugin data and expected reply size of {@code 0}.
     *
     * @param pluginData
     *         Plugin response data
     * @return Crypt data
     */
    public static DbCryptData createReply(byte[] pluginData) {
        if (pluginData == null) {
            return EMPTY_DATA;
        }
        return new DbCryptData(pluginData, 0);
    }

}
