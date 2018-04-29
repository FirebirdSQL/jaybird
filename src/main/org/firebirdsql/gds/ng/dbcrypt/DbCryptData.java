/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
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
