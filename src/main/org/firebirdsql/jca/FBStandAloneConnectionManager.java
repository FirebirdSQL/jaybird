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



import java.io.PrintWriter;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import java.io.Serializable;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;



/**
 * The class <code>FBStandAloneConnectionManager</code> provides the 
 * default implementation of ConnectionManager for standalone use.
 * There is no pooling or other features..
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBStandAloneConnectionManager 
    implements ConnectionManager, ConnectionEventListener, Serializable
{

    private transient final static Logger log = 
        LoggerFactory.getLogger(FBStandAloneConnectionManager.class,false);
        
     //package constructor
     FBStandAloneConnectionManager() {
     }

     //javax.resource.spi.ConnectionManager implementation

    public Object allocateConnection(ManagedConnectionFactory mcf,
       ConnectionRequestInfo cxRequestInfo)
       throws ResourceException {

       ManagedConnection mc = ((FBManagedConnectionFactory)mcf).createManagedConnection(null, cxRequestInfo);
       mc.addConnectionEventListener(this);
       return mc.getConnection(null, cxRequestInfo);
    }


    //javax.resource.spi.ConnectionEventListener implementation

    public void connectionClosed(ConnectionEvent ce) {
        PrintWriter externalLog = ((FBManagedConnection)ce.getSource()).getLogWriter();
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
        }

    }

    public void connectionErrorOccurred(ConnectionEvent ce) {
        PrintWriter externalLog = ((FBManagedConnection)ce.getSource()).getLogWriter();
        if (log!=null) log.debug("ConnectionErrorOccurred, ", ce.getException());
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (log!=null) log.debug("further problems destroying connection: ", e);
            if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
        }
    }

    //We are only supposed to be notified of local transactions that a Connection started.
    //Not much we can do with this info...
    public void localTransactionStarted(ConnectionEvent event) {}

    public void localTransactionCommitted(ConnectionEvent event) {}

    public void localTransactionRolledback(ConnectionEvent event) {}
}
