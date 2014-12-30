/*
 * $Id$
 * 
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

import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.*;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;



/**
 * The class <code>FBStandAloneConnectionManager</code> provides the 
 * default implementation of ConnectionManager for standalone use.
 * There is no pooling or other features..
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class FBStandAloneConnectionManager 
    implements ConnectionManager, ConnectionEventListener, Serializable {

    private static final long serialVersionUID = -4933951275930670896L;
    
    private transient final static Logger log = 
        LoggerFactory.getLogger(FBStandAloneConnectionManager.class,false);
        
     //package constructor
     FBStandAloneConnectionManager() {
     }

     //javax.resource.spi.ConnectionManager implementation

    /**
     * Allocate a new <code>ManagedConnection</code>.
     *
     * @param mcf The <code>ManagedConnectionFactory</code> used to create 
     *        the new connection.
     * @param cxRequestInfo The parameters to be used in creating the 
     *        new connection
     * @throws ResourceException If the connection cannot be allocated
     */
    public Object allocateConnection(ManagedConnectionFactory mcf,
       ConnectionRequestInfo cxRequestInfo)
       throws ResourceException {

       FBManagedConnection mc = (FBManagedConnection)((FBManagedConnectionFactory)mcf).createManagedConnection(null, cxRequestInfo);
       mc.setManagedEnvironment(false);
       mc.setConnectionSharing(false);
       mc.addConnectionEventListener(this);
       return mc.getConnection(null, cxRequestInfo);
    }


    //javax.resource.spi.ConnectionEventListener implementation

    /**
     * <code>javax.resource.spi.ConnectionEventListener</code> callback for 
     * when a <code>ManagedConnection</code> is closed.
     *
     * @param ce contains information about the connection that has be closed
     */
    public void connectionClosed(ConnectionEvent ce) {
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (log!=null) log.debug("Exception closing unmanaged connection: ", e);
        }
    }

    /**
     * <code>javax.resource.spi.ConnectionEventListener</code> callback for 
     * when a Local Transaction was rolled back within the context of a
     * <code>ManagedConnection</code>.
     *
     * @param ce contains information about the connection 
     */
    public void connectionErrorOccurred(ConnectionEvent ce) {
        if (log!=null) log.debug("ConnectionErrorOccurred, ", ce.getException());
        try {
            ((FBManagedConnection)ce.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (log!=null) log.debug("further problems destroying connection: ", e);
        }
    }

    //We are only supposed to be notified of local transactions that a Connection started.
    //Not much we can do with this info...
    
    /**
     * Ignored event callback
     */
    public void localTransactionStarted(ConnectionEvent event) {}

    /**
     * Ignored event callback
     */
    public void localTransactionCommitted(ConnectionEvent event) {}

    /**
     * Ignored event callback
     */
    public void localTransactionRolledback(ConnectionEvent event) {}
}
