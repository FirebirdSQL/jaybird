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
import java.io.PrintWriter;

import javax.resource.ResourceException;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;



/**


ConnectionManager interface provides a hook for the resource adapter to pass a connection request to
the application server.

An application server provides implementation of the ConnectionManager interface. This
implementation is not specific to any particular type of the resource adapter or connection factory
interface.

The ConnectionManager implementation delegates to the application server to enable latter to
provide quality of services (QoS) - security, connection pool management, transaction management
and error logging/tracing.

An application server implements these services in a generic manner, independent of any resource
adapter and EIS specific mechanisms. The connector architecture does not specify how an application
server implements these services; the implementation is specific to an application server.

After an application server hooks-in its services, the connection request gets delegated to a
ManagedConnectionFactory instance either for the creation of a new physical connection or for the
matching of an already existing physical connection.

An implementation class for ConnectionManager interface is required to implement the
java.io.Serializable interface.

In the non-managed application scenario, the ConnectionManager implementation class can be
provided either by a resource adapter (as a default ConnectionManager implementation) or by
application developers. In both cases, QOS can be provided as components by third party vendors.

**/


public class FBStandAloneConnectionManager implements ConnectionManager , ConnectionEventListener {

     //package constructor
     FBStandAloneConnectionManager() {
     }

     //javax.resource.spi.ConnectionManager implementation

/**
     The method allocateConnection gets called by the resource adapter's connection factory
     instance. This lets connection factory instance (provided by the resource adapter) pass a
     connection request to the ConnectionManager instance.

     The connectionRequestInfo parameter represents information specific to the resource adapter
     for handling of the connection request.

     Parameters:
         ManagedConnectionFactory - used by application server to delegate connection
         matching/creation
         ConnectionRequestInfo - connection request Information
     Returns:
         connection handle with an EIS specific connection interface.
     Throws:
         ResourceException - Generic exception
         ApplicationServerInternalException - Application server specific exception
         SecurityException - Security related error
         ResourceAllocationException - Failed to allocate system resources for connection
         request
         ResourceAdapterInternalException - Resource adapter related error condition

**/
    public java.lang.Object allocateConnection(ManagedConnectionFactory mcf,
       ConnectionRequestInfo cxRequestInfo)
       throws ResourceException {

       ManagedConnection mc = ((FBManagedConnectionFactory)mcf).createManagedConnection(null, cxRequestInfo);
       mc.addConnectionEventListener(this);
       return mc.getConnection(null, cxRequestInfo);
    }


    //javax.resource.spi.ConnectionEventListener implementation

    public void connectionClosed(ConnectionEvent ce) {
        PrintWriter log = ((FBManagedConnection)ce.getSource()).getLogWriter();
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            log.println("Exception closing unmanaged connection: " + e);
        }

    }

    public void connectionErrorOccurred(ConnectionEvent ce) {
        PrintWriter log = ((FBManagedConnection)ce.getSource()).getLogWriter();
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            log.println("Exception closing unmanaged connection: " + e);
        }
    }

    //We are only supposed to be notified of local transactions that a Connection started.
    //Not much we can do with this info...
    public void localTransactionStarted(ConnectionEvent event) {}

    public void localTransactionCommitted(ConnectionEvent event) {}

    public void localTransactionRolledback(ConnectionEvent event) {}
}
