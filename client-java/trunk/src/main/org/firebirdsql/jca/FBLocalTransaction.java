/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jca;


import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;

import javax.resource.ResourceException;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.firebirdsql.gds.GDSException;

import org.firebirdsql.jdbc.AbstractConnection;

/**
 * The class <code>FBLocalTransaction</code> implements
 * LocalTransaction both in the cci and spi meanings.  A flag is used
 * to distinguish the current functionality. This class works by
 * delegating the operations to the internal implementations of the
 * XAResource functionality in FBManagedConnection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBLocalTransaction implements LocalTransaction, javax.resource.cci.LocalTransaction {

     private final FBManagedConnection mc;

     private Xid xid = null;

     //used to determine if local transaction events notify
     //ConnectionEventListeners see jca spec section 6.8.  Basically
     //not null means this is cci LocalTransaction, null means
     //spi.LocalTransaction.
     private final ConnectionEvent beginEvent;
     private final ConnectionEvent commitEvent;
     private final ConnectionEvent rollbackEvent;

     //should be package!!! perhaps reorganize and eliminate jdbc!!!
     public FBLocalTransaction(FBManagedConnection mc, AbstractConnection c) {
         this.mc = mc;
         if (c == null) 
         {
             beginEvent = null;
             commitEvent = null;
             rollbackEvent = null;
         } // end of if ()
         else
         {
             beginEvent = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_STARTED, null);
             beginEvent.setConnectionHandle(c);
             commitEvent = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, null);
             commitEvent.setConnectionHandle(c);

             rollbackEvent = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, null);
             rollbackEvent.setConnectionHandle(c);
         } // end of else
         
     }



    /**
     * Begin a local transaction
     *  Throws:
     *    ResourceException - generic exception if operation fails
     *    LocalTransactionException - error condition related to local transaction management
     *    ResourceAdapterInternalException - error condition internal to resource adapter
     *    EISSystemException - EIS instance specific error condition
     **/
     public void begin() throws ResourceException
    {
        try 
        {
            internalBegin();
        }
        catch (GDSException ge)
        {
            throw new FBResourceException(ge);
        } // end of try-catch
     }

     public void internalBegin() throws GDSException {
         if (xid != null) {
             throw new GDSException("local transaction active: can't begin another");
         }
         xid = new FBLocalXid();
         
         synchronized(mc) {
             mc.internalStart(xid, XAResource.TMNOFLAGS);
             if (beginEvent != null) {
                 mc.notify(FBManagedConnection.localTransactionStartedNotifier, beginEvent);
             }
         }
     }


    /**
     Commit a local transaction
     Throws:
         ResourceException - generic exception if operation fails
         LocalTransactionException - error condition related to local transaction management
         ResourceAdapterInternalException - error condition internal to resource adapter
         EISSystemException - EIS instance specific error condition
    **/
     public void commit() throws ResourceException {
         try 
         {
             internalCommit();             
         }
         catch (GDSException ge)
         {
             throw new FBResourceException("Problem committing local tx: " + ge.getMessage(), ge);
         } // end of try-catch
     }


     public void internalCommit() throws GDSException {
         if (xid == null) {
             throw new GDSException("no local transaction active: can't commit");
         }
         
         synchronized(mc)
         {
             try 
             {
                 mc.internalEnd(xid, XAResource.TMSUCCESS);
                 mc.internalCommit(xid, true);
             }
             finally {
                 xid = null;
             }
             if (commitEvent != null) {
                 mc.notify(FBManagedConnection.localTransactionCommittedNotifier, commitEvent);
             }
         }
     }




     /**
     Rollback a local transaction
     Throws:
         ResourceException - generic exception if operation fails
         LocalTransactionException - error condition related to local transaction management
         ResourceAdapterInternalException - error condition internal to resource adapter
         EISSystemException - EIS instance specific error condition
    **/

    public void rollback() throws ResourceException
    {
        try 
        {
            internalRollback();
        }
        catch (GDSException ge)
        {
            throw new FBResourceException("Problem rolling back local tx: " + ge.getMessage(), ge);
        } // end of try-catch
     }


    public void internalRollback() throws GDSException {
         if (xid == null) {
             throw new GDSException("no local transaction active: can't rollback");
         }
         
         synchronized(mc) {
             try {
                 mc.internalEnd(xid, XAResource.TMSUCCESS);  //??? on flags --FBManagedConnection is its own XAResource
                 mc.internalRollback(xid);
             }
             finally {
                 xid = null;
             }
             if (rollbackEvent != null) {
                 mc.notify(FBManagedConnection.localTransactionRolledbackNotifier, rollbackEvent);
             }
         }
     }


     //This is an intentionally non-implemented xid, so if prepare is called with it, it won't work.
     //Only object identity works for equals!
     static class FBLocalXid implements Xid {

         private static final int formatId = 0x0102;//????????????

         public FBLocalXid() {
         }

        /**
         *  Return the global transaction id of this transaction.
         */
        public byte[] getGlobalTransactionId()
        {
           return null;
        }

        /**
         *  Return the branch qualifier of this transaction.
         */
        public byte[] getBranchQualifier()
        {
            return null;
        }

        /**
         *  Return the format identifier of this transaction.
         *
         *  The format identifier augments the global id and specifies
         *  how the global id and branch qualifier should be interpreted.
         */
        public int getFormatId() {
           return formatId;
        }
    }


 }
