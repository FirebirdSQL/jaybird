/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jdbc;


// imports --------------------------------------
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

//import javax.resource.ResourceException;
//import javax.resource.cci.Record;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



public class FBXAResource implements XAResource {
//    private FBManagedConnection fbmc;
//    private Xid current;
    private boolean active = false;
    private int timeout_ignored = 0;
    
//    private Object kb = null;//knowledgebase

//    public FBXAResource(FBManagedConnection fbmc) {
//        this.fbmc = fbmc;
//    }


    /**
     * Gets whether there is outstanding work on behalf of a Transaction.  If
     * there is not, then a connection that is closed will cause the
     * XAConnection to be closed or returned to a pool.  If there is, then the
     * XAConnection must be kept open until commit or rollback is called.
     */
    public boolean isTransaction() {
        return false;//fbmc.getCurrentXid() != null;
    }

    /**
     * Closes this instance permanently.
     */
    public void close() {
//        fbmc.setCurrentXid(null);
//        fbmc = null;
    }

    /**
     * Commits a transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the commit on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void commit(Xid id, boolean twoPhase) throws XAException {
/*        if(active && !twoPhase) {// End was not called!
            try {
                fbmc.getLogWriter().println("WARNING: Connection not closed before transaction commit.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
            }
            catch (ResourceException re) {
                throw new XAException("couldn't log message: error : " + re.getMessage());
            }
        }
//        if(current == null || !id.equals(current)) // wrong Xid
//            throw new XAException(XAException.XAER_NOTA);

        //we'd propagate commit to other containers here if we had more than one container.
        fbmc.setTransactionOver(id);
        if(active) {
            active = false; // No longer associated with the original transaction
        }
//        else
    //not sure what to do        fbmc.??;  // No longer in use at all*/
    }

    /**
     * Dissociates a resource from a global transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end called twice), or the
     *     transaction ID is wrong.
     */
    public void end(Xid id, int flags) throws javax.transaction.xa.XAException {
/*        if(!active) // End was called twice!
            throw new XAException(XAException.XAER_PROTO);
        if(rmc.getCurrentXid() == null || !id.equals(fbmc.getCurrentXid()))
            throw new XAException(XAException.XAER_NOTA);
        active = false;
Log.getLog().debug("FBXAResource ended: " + this + ", Xid: " + id);
        fbmc.setCurrentXid(null);*/
    }

    /**
     * Indicates that no further action will be taken on behalf of this
     * transaction (after a heuristic failure).  It is assumed this will be
     * called after a failed commit or rollback.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), or the
     *     transaction ID is wrong.
     */
    public void forget(Xid id) throws javax.transaction.xa.XAException {
    }

    /**
     * Gets the transaction timeout.
     */
    public int getTransactionTimeout() throws javax.transaction.xa.XAException {
        return timeout_ignored;
    }

    public boolean isSameRM(XAResource res) throws javax.transaction.xa.XAException {
        return res instanceof FBXAResource; //Optimistic, but maybe it'll work out.
    }

    /**
     * Prepares a transaction to commit.  Since JDBC 1.0 does not support
     * 2-phase commits, this claims the commit is OK (so long as some work was
     * done on behalf of the specified transaction).
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, or the connection was set to Auto-Commit.
     */
    public int prepare(Xid id) throws javax.transaction.xa.XAException {
/*        if(active) {// End was not called!
            try {
                fbmc.getLogWriter().println("WARNING: Connection not closed before transaction commit.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
            }
            catch (ResourceException re) {
                throw new XAException("couldn't log message: error : " + re.getMessage());
            }
        }*/
//        if(current == null || !id.equals(current)) // wrong Xid
//            throw new XAException(XAException.XAER_NOTA);
//propagate prepare to other containers.
        return XA_OK;
    }

    public Xid[] recover(int flag) throws javax.transaction.xa.XAException {
/*        if(fbmc.getCurrentXid() == null)
            return new Xid[0];
        else
            return new Xid[]{fbmc.getCurrentXid()};*/
         return null;
    }

    /**
     * Rolls back the work, assuming it was done on behalf of the specified
     * transaction.
     * @throws XAException
     *     Occurs when the state was not correct (end never called), the
     *     transaction ID is wrong, the connection was set to Auto-Commit,
     *     or the rollback on the underlying connection fails.  The error code
     *     differs depending on the exact situation.
     */
    public void rollback(Xid id) throws javax.transaction.xa.XAException {
/*        if(active) {// End was not called!
            try {
                fbmc.getLogWriter().println("WARNING: Connection not closed before transaction rollback.\nConnection will not participate in any future transactions.\nAre you sure you want to be doing this?");
            }
            catch (ResourceException re) {
                throw new XAException("couldn't log message: error : " + re.getMessage());
            }
        }
//        if(current == null || !id.equals(current)) // wrong Xid
//            throw new XAException(XAException.XAER_NOTA);
//autocommit not supported        if(rmc.getAutoCommit())
//            throw new XAException(XAException.XA_HEURCOM);

//propagate to other containers
        rmc.setTransactionOver(id);
        if(active) {
            active = false; // No longer associated with the original transaction
        }
//        else
//not sure what to do here            xaCon.transactionFinished(); // No longer in use at all
*/
    }

    /**
     * Sets the transaction timeout.  This is saved, but the value is not used
     * by the current implementation.
     */
    public boolean setTransactionTimeout(int timeout) throws javax.transaction.xa.XAException {
        timeout_ignored = timeout;
        return true;
    }

    /**
     * Associates a JDBC connection with a global transaction.  We assume that
     * end will be called followed by prepare, commit, or rollback.
     * If start is called after end but before commit or rollback, there is no
     * way to distinguish work done by different transactions on the same
     * connection).  If start is called more than once before
     * end, either it's a duplicate transaction ID or illegal transaction ID
     * (since you can't have two transactions associated with one DB
     * connection).
     * @throws XAException
     *     Occurs when the state was not correct (start called twice), the
     *     transaction ID is wrong, or the instance has already been closed.
     */
    public void start(Xid id, int flags) throws javax.transaction.xa.XAException {
/*        if(active) {// Start was called twice!
            if(fbmc.getCurrentXid() != null && id.equals(fbmc.getCurrentXid()))
                throw new XAException(XAException.XAER_DUPID);
            else
                throw new XAException(XAException.XAER_PROTO);
        }
//        if(current != null && !id.equals(current))
//            throw new XAException(XAException.XAER_NOTA);
//autocommit not supported        if (fbmc.getAutoCommit()) {
//            throw new XAException(XAException.XA_RBOTHER);
//        }

Log.getLog().debug("RuleXAResource started: " + this + ", Xid: " + id);
        fbmc.setCurrentXid(id);
        active = true;*/
    }
    
    
    
}
