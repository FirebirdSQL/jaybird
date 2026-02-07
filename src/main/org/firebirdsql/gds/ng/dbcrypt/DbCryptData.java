// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.dbcrypt;

import org.jspecify.annotations.Nullable;

/**
 * Data of a database encryption key callback (or reply).
 *
 * @author Mark Rotteveel
 * @since 3.0.4
 */
public final class DbCryptData {

    public static final DbCryptData EMPTY_DATA = new DbCryptData(null, 0);

    private final byte @Nullable [] pluginData;
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
    public DbCryptData(byte @Nullable [] pluginData, int replySize) {
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
    public byte @Nullable [] getPluginData() {
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
     * Plugins can use the value as a hint to the expected size of their reply. However, smaller (or larger) replies
     * will work.
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
    public static DbCryptData createReply(byte @Nullable [] pluginData) {
        if (pluginData == null) {
            return EMPTY_DATA;
        }
        return new DbCryptData(pluginData, 0);
    }

}
