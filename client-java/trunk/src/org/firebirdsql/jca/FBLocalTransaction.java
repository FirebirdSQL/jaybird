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

package org.firebirdsql.jca;


// imports --------------------------------------

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.LocalTransaction;

import javax.resource.ResourceException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.firebirdsql.jdbc.FBConnection;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */

 public class FBLocalTransaction implements LocalTransaction, javax.resource.cci.LocalTransaction {

    private static final Logger log = LoggerFactory.getLogger(FBLocalTransaction.class,false);

     private FBManagedConnection mc;

     private Xid xid = null;

     //used to determine if local transaction events notify ConnectionEventListeners
     //see jca spec section 6.8.  Basically not null means this is cci LocalTransaction,
     //null means spi.LocalTransaction.
     private FBConnection c = null;

     //should be package!!! perhaps reorganize and eliminate jdbc!!!
     public FBLocalTransaction(FBManagedConnection mc, FBConnection c) {
         this.mc = mc;
         this.c = c;
     }



    /**
     Begin a local transaction
     Throws:
         ResourceException - generic exception if operation fails
         LocalTransactionException - error condition related to local transaction management
         ResourceAdapterInternalException - error condition internal to resource adapter
         EISSystemException - EIS instance specific error condition
    **/
     public void begin() throws ResourceException {
         if (xid != null) {
             throw new ResourceException("local transaction active: can't begin another");
         }
         xid = new FBLocalXid();
         try {
             mc.start(xid, XAResource.TMNOFLAGS);  //FBManagedConnection is its own XAResource
         }
         catch (XAException e) {
            if (log != null) log.warn("couldn't start local transaction: " , e);
            throw new ResourceException("couldn't start local transaction: " + e);
         }
         if (c != null) {
             mc.notify(ConnectionEvent.LOCAL_TRANSACTION_STARTED, c, null);
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
         if (xid == null) {
             throw new ResourceException("no local transaction active: can't commit");
         }
         try {
             mc.end(xid, XAResource.TMNOFLAGS);  //FBManagedConnection is its own XAResource
             mc.commit(xid, true);
         }
         catch (XAException e) {
             throw new ResourceException("couldn't commit local transaction: " + e);
         }
         finally {
             xid = null;
         }
         if (c != null) {
             mc.notify(ConnectionEvent.LOCAL_TRANSACTION_COMMITTED, c, null);
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

    public void rollback() throws ResourceException {
         if (xid == null) {
             throw new ResourceException("no local transaction active: can't rollback");
         }
         try {
             mc.end(xid, XAResource.TMNOFLAGS);  //??? on flags --FBManagedConnection is its own XAResource
             mc.rollback(xid);
         }
         catch (XAException e) {
             throw new ResourceException("couldn't commit local transaction: " + e);
         }
         finally {
             xid = null;
         }
         if (c != null) {
             mc.notify(ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK, c, null);
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
