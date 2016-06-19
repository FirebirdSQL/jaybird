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
package org.firebirdsql.jca;

import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.SQLStateConstants;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.LocalTransactionException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.sql.SQLException;

/**
 * The class {@code FBLocalTransaction} implements LocalTransaction both
 * in the cci and spi meanings. A flag is used to distinguish the current
 * functionality. This class works by delegating the operations to the internal
 * implementations of the XAResource functionality in FBManagedConnection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBLocalTransaction implements FirebirdLocalTransaction,
        javax.resource.cci.LocalTransaction {

    protected final FBManagedConnection mc;

    protected Xid xid = null;

    // used to determine if local transaction events notify
    // ConnectionEventListeners see jca spec section 6.8. Basically
    // not null means this is cci LocalTransaction, null means
    // spi.LocalTransaction.
    protected final ConnectionEvent beginEvent;

    protected final ConnectionEvent commitEvent;

    protected final ConnectionEvent rollbackEvent;

    // should be package!!! perhaps reorganize and eliminate jdbc!!!
    public FBLocalTransaction(FBManagedConnection mc, FBConnection c) {
        this.mc = mc;
        if (c == null) {
            beginEvent = null;
            commitEvent = null;
            rollbackEvent = null;
        } else {
            beginEvent = new ConnectionEvent(mc,
                    ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
            beginEvent.setConnectionHandle(c);

            commitEvent = new ConnectionEvent(mc,
                    ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
            commitEvent.setConnectionHandle(c);

            rollbackEvent = new ConnectionEvent(mc,
                    ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
            rollbackEvent.setConnectionHandle(c);
        }
    }

    /**
     * Get the associated Xid.
     *
     * @return instance of {@link Xid} representing a transaction ID that is managed by this local transaction.
     */
    public Xid getXid() {
        return xid;
    }

    /**
     * Check whether a started transaction is associated with the current
     * database connection.
     */
    public boolean inTransaction() throws ResourceException {
        try {
            return mc.getGDSHelper().inTransaction();
        } catch (SQLException ex) {
            throw new FBResourceException(ex);
        }
    }

    /**
     * Begin a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void begin() throws ResourceException {
        internalBegin();
    }

    /**
     * Perform the internal operations to begin a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void internalBegin() throws ResourceException {
        if (xid != null) {

            // throw exception only if xid is known to the managed connection
            if (mc.isXidActive(xid))
                throw new FBResourceTransactionException(
                        "Local transaction active: can't begin another",
                        SQLStateConstants.SQL_STATE_TRANSACTION_ACTIVE);
        }

        xid = new FBLocalXid();

        try {
            mc.internalStart(xid, XAResource.TMNOFLAGS);
        } catch (XAException | SQLException ex) {
            xid = null;
            throw new FBResourceException(ex);
        }

        if (beginEvent != null)
            mc.notify(FBManagedConnection.localTransactionStartedNotifier, beginEvent);
    }

    /**
     * Commit a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void commit() throws ResourceException {
        internalCommit();
    }

    /**
     * Perform the internal processing to commit a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void internalCommit() throws ResourceException {

        // if there is no xid assigned, but we are still here,
        // that means that automatic commit was called in managed
        // scenario when managed connection was enlisted in global
        // transaction
        if (xid == null) return;

        synchronized (mc.getSynchronizationObject()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS);
                mc.internalCommit(xid, true);
            } catch (XAException ex) {
                throw new FBResourceTransactionException(ex.getMessage(), ex);
            } catch (SQLException ex) {
                throw new FBResourceException(ex);
            } finally {
                xid = null;
            }
            if (commitEvent != null) {
                mc.notify(FBManagedConnection.localTransactionCommittedNotifier, commitEvent);
            }
        }
    }

    /**
     * Rollback a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void rollback() throws ResourceException {
        internalRollback();
    }

    /**
     * Perform the internal processing to rollback a local transaction.
     *
     * @throws ResourceException
     *         generic exception if operation fails
     * @throws LocalTransactionException
     *         error condition related to local transaction management
     * @throws ResourceAdapterInternalException
     *         error condition internal to resource adapter
     * @throws EISSystemException
     *         EIS instance specific error condition
     */
    public void internalRollback() throws ResourceException {

        // if there is no xid assigned, but we are still here,
        // that means that automatic commit was called in managed
        // scenario when managed connection was enlisted in global
        // transaction
        if (xid == null) return;

        synchronized (mc.getSynchronizationObject()) {
            try {
                mc.internalEnd(xid, XAResource.TMSUCCESS); // ??? on flags
                                                           // --FBManagedConnection is its own XAResource
                mc.internalRollback(xid);
            } catch (XAException | SQLException ex) {
                throw new FBResourceTransactionException(ex.getMessage(), ex);
            } finally {
                xid = null;
            }
            if (rollbackEvent != null) {
                mc.notify(FBManagedConnection.localTransactionRolledbackNotifier, rollbackEvent);
            }
        }
    }

    // This is an intentionally non-implemented xid, so if prepare is called
    // with it, it won't work.
    // Only object identity works for equals!
    public static class FBLocalXid implements Xid {

        private static final int formatId = 0x0102;// ????????????

        private String strValue;

        public FBLocalXid() {
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
