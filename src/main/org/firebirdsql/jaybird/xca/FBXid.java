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
package org.firebirdsql.jaybird.xca;

import javax.resource.ResourceException;
import javax.transaction.xa.Xid;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The class <code>FBXid</code> has methods for serializing xids for
 * firebird use, and reading them back into instances of itself.  It is
 * a key component in adapting xa semantics and recovery to firebird
 * native operations and data format.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
class FBXid implements Xid {
    //Constants from alice.h
    public static final int TDR_VERSION = 1;
    public static final int TDR_HOST_SITE = 1;
    public static final int TDR_DATABASE_PATH = 2;
    public static final int TDR_TRANSACTION_ID = 3;
    public static final int TDR_REMOTE_SITE = 4;
    //new constants for xid encoding
    public static final int TDR_XID_FORMAT_ID = 5;
    public static final int TDR_XID_GLOBAL_ID = 6;
    public static final int TDR_XID_BRANCH_ID = 4;

    private int formatId;

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
     * Create a new instance copying an existing one.
     */
    public FBXid(Xid xid) {
        formatId = xid.getFormatId();
        globalId = xid.getGlobalTransactionId();
        branchId = xid.getBranchQualifier();
        firebirdTransactionId = 0;
    }

    /**
     * Creates a new <code>FBXid</code> instance from the byte representation
     * supplied. This is called by recover to reconstruct an xid
     * from the toBytes() representation.
     *
     * @param rawIn
     *         Xid serialized in format of {@link #toBytes()} as {@link java.io.InputStream}
     * @param firebirdTransactionId
     *         The Firebird transactionId of the recovered Xid.
     * @throws ResourceException
     *         if an error occurs
     */
    FBXid(InputStream rawIn, long firebirdTransactionId) throws ResourceException {
        this.firebirdTransactionId = firebirdTransactionId;

        try {
            if (read(rawIn) != TDR_VERSION) {
                throw new FBIncorrectXidException("Wrong TDR_VERSION for xid");
            }
            if (read(rawIn) != TDR_XID_FORMAT_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_FORMAT_ID for xid");
            }
            formatId = readInt(rawIn);
            if (read(rawIn) != TDR_XID_GLOBAL_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_GLOBAL_ID for xid");
            }
            globalId = readBuffer(rawIn);
            if (read(rawIn) != TDR_XID_BRANCH_ID) {
                throw new FBIncorrectXidException("Wrong TDR_XID_BRANCH_ID for xid");
            }
            branchId = readBuffer(rawIn);
        } catch (IOException ioe) {
            throw new FBResourceException("IOException: " + ioe, ioe);
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
     * Instances are considered equal if they are both instances of XidImpl,
     * and if they have the same global transaction id and transaction
     * branch qualifier.
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

    //package

    /**
     * @return Length of the serialized byte representation.
     */
    int getLength() {
        return 1 + 1 + 4 + 1 + 4 + globalId.length + 1 + 4 + branchId.length;
    }

    /**
     * @return Serialized byte representation of this Xid
     */
    byte[] toBytes() {
        byte[] b = new byte[getLength()];
        int i = 0;
        b[i++] = (byte) TDR_VERSION;
        b[i++] = (byte) TDR_XID_FORMAT_ID;
        b[i++] = (byte) ((formatId >>> 24) & 0xff);
        b[i++] = (byte) ((formatId >>> 16) & 0xff);
        b[i++] = (byte) ((formatId >>> 8) & 0xff);
        b[i++] = (byte) ((formatId) & 0xff);
        b[i++] = (byte) TDR_XID_GLOBAL_ID;
        b[i++] = (byte) ((globalId.length >>> 24) & 0xff);
        b[i++] = (byte) ((globalId.length >>> 16) & 0xff);
        b[i++] = (byte) ((globalId.length >>> 8) & 0xff);
        b[i++] = (byte) ((globalId.length) & 0xff);
        System.arraycopy(globalId, 0, b, i, globalId.length);
        i += globalId.length;
        b[i++] = (byte) TDR_XID_BRANCH_ID;
        b[i++] = (byte) ((branchId.length >>> 24) & 0xff);
        b[i++] = (byte) ((branchId.length >>> 16) & 0xff);
        b[i++] = (byte) ((branchId.length >>> 8) & 0xff);
        b[i++] = (byte) ((branchId.length) & 0xff);
        System.arraycopy(branchId, 0, b, i, branchId.length);
        return b;
    }

    private int read(InputStream in) throws IOException {
        return in.read();
    }

    private int readInt(InputStream in) throws IOException {
        return (read(in) << 24) | (read(in) << 16) | (read(in) << 8) | (read(in));
    }

    private byte[] readBuffer(InputStream in) throws IOException {
        int len = readInt(in);
        byte[] buffer = new byte[len];
        readFully(in, buffer, 0, len);
        return buffer;
    }

    private void readFully(InputStream in, byte[] buffer, int offset, int length) throws IOException {
        if (length == 0)
            return;

        do {
            int counter = in.read(buffer, offset, length);
            if (counter == -1)
                throw new EOFException();

            offset += counter;
            length -= counter;
        } while (length > 0);
    }
}

