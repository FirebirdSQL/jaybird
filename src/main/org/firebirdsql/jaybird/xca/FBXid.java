/*
 * Firebird Open Source JDBC Driver
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
package org.firebirdsql.jaybird.xca;

import javax.transaction.xa.Xid;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The class {@code FBXid} has methods for serializing xids for
 * firebird use, and reading them back into instances of itself.  It is
 * a key component in adapting xa semantics and recovery to firebird
 * native operations and data format.
 *
 * @author David Jencks
 */
final class FBXid implements Xid {

    // Constant from alice.h
    private static final int TDR_VERSION = 1;
    // constants for xid encoding as used by Jaybird
    private static final int TDR_XID_FORMAT_ID = 5;
    private static final int TDR_XID_GLOBAL_ID = 6;
    private static final int TDR_XID_BRANCH_ID = 4;

    private final int formatId;

    /**
     * Global transaction id of this instance.
     */
    private final byte[] globalId;

    /**
     * Branch qualifier of this instance.
     * This identifies the branch of a transaction.
     */
    private final byte[] branchId;

    private final long firebirdTransactionId;

    /**
     * Return a string that describes any Xid instance.
     */
    static String toString(Xid id) {
        if (id == null)
            return "[NULL Xid]";

        String s = id.getClass().getName();
        s = s.substring(s.lastIndexOf('.') + 1);
        s = s + " [FormatId=" + id.getFormatId() +
                ", GlobalId=" + new String(id.getGlobalTransactionId()).trim() +
                ", BranchQual=" + new String(id.getBranchQualifier()).trim() + "]";

        return s;
    }

    /**
     * Create a xid with the specified values.
     *
     * @param firebirdTransactionId
     *         Firebird transaction id
     * @param formatId
     *         format id
     * @param globalId
     *         global transaction id
     * @param branchId
     *         branch qualifier
     */
    FBXid(long firebirdTransactionId, int formatId, byte[] globalId, byte[] branchId) {
        this.firebirdTransactionId = firebirdTransactionId;
        this.formatId = formatId;
        this.globalId = globalId;
        this.branchId = branchId;
    }

    /**
     * Create a new instance copying an existing one.
     *
     * @param xid
     *         source xid
     */
    FBXid(Xid xid) {
        this(0L, xid.getFormatId(), xid.getGlobalTransactionId(), xid.getBranchQualifier());
    }

    /**
     * Creates a new xid instance from the byte representation supplied. This is called by recover to reconstruct a xid
     * from the toBytes() representation.
     *
     * @param rawIn
     *         Xid serialized in format of {@link #toBytes()}
     * @param firebirdTransactionId
     *         The Firebird transactionId of the recovered Xid.
     * @throws FBIncorrectXidException
     *         if an unexpected value is read from the input stream
     */
    FBXid(byte[] rawIn, long firebirdTransactionId) throws FBIncorrectXidException {
        this.firebirdTransactionId = firebirdTransactionId;
        ByteBuffer buffer = ByteBuffer.wrap(rawIn);
        try {
            if (buffer.get() != TDR_VERSION) {
                throw new FBIncorrectXidException("Wrong TDR_VERSION for xid");
            }
            if (buffer.get() != TDR_XID_FORMAT_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_FORMAT_ID for xid");
            }
            formatId = buffer.getInt();
            if (buffer.get() != TDR_XID_GLOBAL_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_GLOBAL_ID for xid");
            }
            globalId = readBuffer(buffer);
            if (buffer.get() != TDR_XID_BRANCH_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_BRANCH_ID for xid");
            }
            branchId = readBuffer(buffer);
        } catch (BufferUnderflowException e) {
            throw new FBIncorrectXidException("Unexpected format or incomplete serialized xid", e);
        }
    }

    /**
     * @return the global transaction id of this transaction.
     */
    public byte[] getGlobalTransactionId() {
        return globalId.clone();
    }

    /**
     * @return the branch qualifier of this transaction.
     */
    public byte[] getBranchQualifier() {
        return branchId.clone();
    }

    /**
     * The format identifier augments the global id and specifies
     * how the global id and branch qualifier should be interpreted.
     *
     * @return the format identifier of this transaction.
     */
    public int getFormatId() {
        return formatId;
    }

    /**
     * @return Firebird transaction ID or 0 if no transaction id is available.
     */
    public long getFirebirdTransactionId() {
        return firebirdTransactionId;
    }

    /**
     * Compare for equality.
     * <p>
     * Instances are considered equal if they are both instances of FBXid, and if they have the same global
     * transaction id and transaction branch qualifier.
     * </p>
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Xid)) return false;

        Xid other = (Xid) obj;

        boolean result = formatId == other.getFormatId();
        byte[] otherGlobalID = other.getGlobalTransactionId();
        byte[] otherBranchID = other.getBranchQualifier();
        result &= Arrays.equals(globalId, otherGlobalID);
        result &= Arrays.equals(branchId, otherBranchID);

        return result;
    }

    public int hashCode() {
        int hashCode = 1109;
        hashCode = 43 * hashCode + formatId;
        hashCode = 43 * hashCode + Arrays.hashCode(globalId);
        hashCode = 43 * hashCode + Arrays.hashCode(branchId);
        return hashCode;
    }

    public String toString() {
        return toString(this);
    }

    /**
     * @return Serialized byte representation of this Xid
     */
    byte[] toBytes() {
        byte[] b = new byte[1 + 1 + 4 + 1 + 4 + globalId.length + 1 + 4 + branchId.length];
        ByteBuffer.wrap(b)
                .put((byte) TDR_VERSION)
                .put((byte) TDR_XID_FORMAT_ID)
                .putInt(formatId)
                .put((byte) TDR_XID_GLOBAL_ID)
                .putInt(globalId.length)
                .put(globalId)
                .put((byte) TDR_XID_BRANCH_ID)
                .putInt(branchId.length)
                .put(branchId);
        return b;
    }

    private byte[] readBuffer(ByteBuffer buffer) {
        byte[] content = new byte[buffer.getInt()];
        buffer.get(content);
        return content;
    }
}
