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

import javax.resource.spi.LocalTransaction;

import javax.resource.ResourceException;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */

 public class FBLocalTransaction implements LocalTransaction {
     


    /**
     Begin a local transaction
     Throws:
         ResourceException - generic exception if operation fails
         LocalTransactionException - error condition related to local transaction management
         ResourceAdapterInternalException - error condition internal to resource adapter
         EISSystemException - EIS instance specific error condition
    **/
     public void begin() throws ResourceException {
         throw new ResourceException("Not yet implemented");
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
         throw new ResourceException("Not yet implemented");
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
         throw new ResourceException("Not yet implemented");
     }
     
 }
