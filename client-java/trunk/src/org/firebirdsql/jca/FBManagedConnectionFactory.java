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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import java.util.Set;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *ManagedConnectionFactory instance is a factory of both ManagedConnection and EIS-specific
connection factory instances. This interface supports connection pooling by providing methods for
matching and creation of ManagedConnection instance. 
 
 */

public class FBManagedConnectionFactory implements  ManagedConnectionFactory {


/**
     Creates a Connection Factory instance. The Connection Factory instance gets initialized with
     the passed ConnectionManager. In the managed scenario, ConnectionManager is provided by the
     application server.
     Parameters:
         cxManager - ConnectionManager to be associated with created EIS connection factory
         instance
     Returns:
         EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory
         instance
     Throws:
         ResourceException - Generic exception
         ResourceAdapterInternalException - Resource adapter related error condition
**/
    public java.lang.Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        throw new ResourceException("not yet implemented");
    }




/**
     Creates a Connection Factory instance. The Connection Factory instance gets initialized with a
     default ConnectionManager provided by the resource adapter.
     Returns:
         EIS-specific Connection Factory instance or javax.resource.cci.ConnectionFactory
         instance
     Throws:
         ResourceException - Generic exception
         ResourceAdapterInternalException - Resource adapter related error condition

**/
    public java.lang.Object createConnectionFactory() throws ResourceException {
        throw new ResourceException("not yet implemented");
    }



/**
     Creates a new physical connection to the underlying EIS resource manager, 

     ManagedConnectionFactory uses the security information (passed as Subject) and additional
     ConnectionRequestInfo (which is specific to ResourceAdapter and opaque to application server)
     to create this new connection.
     Parameters:
         Subject - Caller's security information
         cxRequestInfo - Additional resource adapter specific connection request information
     Returns:
         ManagedConnection instance
     Throws:
         ResourceException - generic exception
         SecurityException - security related error
         ResourceAllocationException - failed to allocate system resources for connection
         request
         ResourceAdapterInternalException - resource adapter related error condition
         EISSystemException - internal error condition in EIS instance

**/
    public ManagedConnection createManagedConnection(Subject subject,
                                                 ConnectionRequestInfo cxRequestInfo)
                                          throws ResourceException {
        throw new ResourceException("not yet implemented");
    }




/**
     Returns a matched connection from the candidate set of connections. 

     ManagedConnectionFactory uses the security info (as in Subject) and information provided
     through ConnectionRequestInfo and additional Resource Adapter specific criteria to do
     matching. Note that criteria used for matching is specific to a resource adapter and is not
     prescribed by the Connector specification.

     This method returns a ManagedConnection instance that is the best match for handling the
     connection allocation request.

     Parameters:
         connectionSet - candidate connection set
         Subject - caller's security information
         cxRequestInfo - additional resource adapter specific connection request information
     Returns:
         ManagedConnection if resource adapter finds an acceptable match otherwise null
     Throws:
         ResourceException - generic exception
         SecurityException - security related error
         ResourceAdapterInternalException - resource adapter related error condition
         NotSupportedException - if operation is not supported

**/

    public ManagedConnection matchManagedConnections(java.util.Set connectionSet,
                                                 javax.security.auth.Subject subject,
                                                 ConnectionRequestInfo cxRequestInfo)
                                          throws ResourceException {
        throw new ResourceException("not yet implemented");
    }




/**
     Set the log writer for this ManagedConnectionFactory instance.

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnectionfactory instance will be printed.

     ApplicationServer manages the association of output stream with the
     ManagedConnectionFactory. When a ManagedConnectionFactory object is created the log
     writer is initially null, in other words, logging is disabled. Once a log writer is associated with a
     ManagedConnectionFactory, logging and tracing for ManagedConnectionFactory instance is
     enabled. 

     The ManagedConnection instances created by ManagedConnectionFactory "inherits" the log
     writer, which can be overridden by ApplicationServer using ManagedConnection.setLogWriter
     to set ManagedConnection specific logging and tracing.
     Parameters:
         out - PrintWriter - an out stream for error logging and tracing
     Throws:
         ResourceException - generic exception
         ResourceAdapterInternalException - resource adapter related error condition

**/

    public void setLogWriter(java.io.PrintWriter out) throws ResourceException {
        throw new ResourceException("not yet implemented");
    }



/**
     Get the log writer for this ManagedConnectionFactory instance. 

     The log writer is a character output stream to which all logging and tracing messages for this
     ManagedConnectionFactory instance will be printed 

     ApplicationServer manages the association of output stream with the
     ManagedConnectionFactory. When a ManagedConnectionFactory object is created the log
     writer is initially null, in other words, logging is disabled.
     Returns:
         PrintWriter
     Throws:
         ResourceException - generic exception

**/
    public java.io.PrintWriter getLogWriter() throws ResourceException {
        throw new ResourceException("not yet implemented");
    }




/**
     Returns the hash code for the ManagedConnectionFactory
     Overrides:
         hashCode in class java.lang.Object
     Returns:
         hash code for the ManagedConnectionFactory

**/
    public int hashCode() {
        return 0;
    }



/**
     Check if this ManagedConnectionFactory is equal to another ManagedConnectionFactory.
     Overrides:
         equals in class java.lang.Object
     Returns:
         true if two instances are equal

**/

    public boolean equals(java.lang.Object other) {
        return false;
    }

   
 } 





