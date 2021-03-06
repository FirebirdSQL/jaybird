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

import org.firebirdsql.jdbc.SQLStateConstants;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;

/**
 * The class {@code FBLocalTransaction} represent a local, not distributed, transaction. A flag is used to
 * distinguish the current functionality. This class works by delegating the operations to the internal implementation
 * of the XAResource functionality in FBManagedConnection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public final class FBLocalTransaction {

    private final FBManagedConnection mc;
    private Xid xid = null;

    FBLocalTransaction(FBManagedConnection mc) {
        this.mc = mc;
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
            // TODO More specific exception, Jaybird error code
            throw new SQLException("Local transaction active: can't begin another",
                    SQLStateConstants.SQL_STATE_TRANSACTION_ACTIVE);
        }

        xid = new FBLocalXid();

        try {
            mc.internalStart(xid, XAResource.TMNOFLAGS);
        } catch (XAException ex) {
            xid = null;
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
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

        synchronized (mc.getSynchronizationObject()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS);
                mc.internalCommit(xid, true);
            } catch (XAException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
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

        synchronized (mc.getSynchronizationObject()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS); // ??? on flags
                // --FBManagedConnection is its own XAResource
                mc.internalRollback(xid);
            } catch (XAException ex) {
                if (ex.getCause() instanceof SQLException) {
                    throw (SQLException) ex.getCause();
                }
                // TODO More specific exception, Jaybird error code (or is this flow unlikely to hit?)
                throw new SQLException(ex.getMessage(), ex);
            } finally {
                xid = null;
            }
        }
    }

    // This is an intentionally non-implemented xid, so if prepare is called
    // with it, it won't work.
    // Only object identity works for equals!
    private static final class FBLocalXid implements Xid {

        private static final int formatId = 0x0102;// ????????????

        private final String strValue;

        private FBLocalXid() {
            strValue = "Xid[" + hashCode() + "]";
        }

        /**
         * Return the global transaction id of this transaction.
         */
        public byte[] getGlobalTransactionId() {
            return null;
        }

        /**
         * Return the branch qualifier of this transaction.
         */
        public byte[] getBranchQualifier() {
            return null;
        }

        /**
         * Return the format identifier of this transaction.
         * <p>
         * The format identifier augments the global id and specifies how the
         * global id and branch qualifier should be interpreted.
         * </p>
         */
        public int getFormatId() {
            return formatId;
        }

        public String toString() {
            return strValue;
        }
    }
}
