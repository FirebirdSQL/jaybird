/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.util.ByteArrayHelper;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

/**
 * The class {@code FBLocalTransaction} represent a local, not distributed, transaction. A flag is used to
 * distinguish the current functionality. This class works by delegating the operations to the internal implementation
 * of the XAResource functionality in FBManagedConnection.
 *
 * @author David Jencks
 */
public final class FBLocalTransaction {

    private final FBManagedConnection mc;
    private Xid xid = null;

    FBLocalTransaction(FBManagedConnection mc) {
        this.mc = requireNonNull(mc, "mc");
    }

    /**
     * Check if managed connection is currently participating in transaction.
     *
     * @return {@code true} if managed connection is participating in transaction.
     * @throws SQLException
     *         if operation cannot be completed.
     */
    public boolean inTransaction() throws SQLException {
        return mc.getGDSHelper().inTransaction();
    }

    /**
     * Begin a local transaction.
     *
     * @throws SQLException
     *         generic exception if operation fails
     */
    public void begin() throws SQLException {
        // throw exception only if xid is known to the managed connection
        if (xid != null && mc.isXidActive(xid)) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_localTransactionActive);
        }

        xid = new FBLocalXid();

        try {
            mc.internalStart(xid, XAResource.TMNOFLAGS);
        } catch (XAException ex) {
            xid = null;
            if (ex.getCause() instanceof SQLException sqle) {
                throw sqle;
            }
            // TODO More specific exception, Jaybird error code (or is this flow unlikely to hit?)
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    public void begin(String sql) throws SQLException {
        if (inTransaction()) {
            throw FbExceptionBuilder.toNonTransientException(JaybirdErrorCodes.jb_setTransactionNotAllowedActiveTx);
        } else if (xid != null && mc.isXidActive(xid)) {
            throw FbExceptionBuilder.toException(JaybirdErrorCodes.jb_localTransactionActive);
        }

        xid = new FBLocalXid();

        try {
            mc.internalStart(xid, sql);
        } catch (XAException ex) {
            xid = null;
            if (ex.getCause() instanceof SQLException sqle) {
                throw sqle;
            }
            // TODO More specific exception, Jaybird error code (or is this flow unlikely to hit?)
            throw new SQLException(ex.getMessage(), ex);
        }
    }

    /**
     * Commit a local transaction.
     *
     * @throws SQLException
     *         generic exception if operation fails
     */
    public void commit() throws SQLException {
        // if there is no xid assigned, but we are still here, that means that automatic commit was called in managed
        // scenario when managed connection was enlisted in global transaction
        if (xid == null) return;

        try (LockCloseable ignored = mc.withLock()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS);
                mc.internalCommit(xid, true);
            } catch (XAException ex) {
                if (ex.getCause() instanceof SQLException sqle) {
                    throw sqle;
                }
                // TODO More specific exception, Jaybird error code (or is this flow unlikely to hit?)
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                xid = null;
            }
        }
    }

    /**
     * Rollback a local transaction.
     *
     * @throws SQLException
     *         generic exception if operation fails
     */
    public void rollback() throws SQLException {
        // if there is no xid assigned, but we are still here, that means that automatic commit was called in managed
        // scenario when managed connection was enlisted in global transaction
        if (xid == null) return;

        try (LockCloseable ignored = mc.withLock()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS); // ??? on flags
                // --FBManagedConnection is its own XAResource
                mc.internalRollback(xid);
            } catch (XAException ex) {
                if (ex.getCause() instanceof SQLException sqle) {
                    throw sqle;
                }
                // TODO More specific exception, Jaybird error code (or is this flow unlikely to hit?)
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                xid = null;
            }
        }
    }

    // This is an intentionally non-implemented xid, so if prepare is called with it, it won't work.
    // Only object identity works for equals!
    private static final class FBLocalXid implements Xid {

        private static final int FORMAT_ID = 0x0102;// ????????????

        private final String strValue;

        private FBLocalXid() {
            strValue = "Xid[" + hashCode() + "]";
        }

        /**
         * Return the global transaction id of this transaction.
         */
        public byte[] getGlobalTransactionId() {
            return ByteArrayHelper.emptyByteArray();
        }

        /**
         * Return the branch qualifier of this transaction.
         */
        public byte[] getBranchQualifier() {
            return ByteArrayHelper.emptyByteArray();
        }

        /**
         * Return the format identifier of this transaction.
         * <p>
         * The format identifier augments the global id and specifies how the
         * global id and branch qualifier should be interpreted.
         * </p>
         */
        public int getFormatId() {
            return FORMAT_ID;
        }

        public String toString() {
            return strValue;
        }
    }
}
