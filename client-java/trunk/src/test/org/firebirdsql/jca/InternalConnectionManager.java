package org.firebirdsql.jca;

import java.io.PrintWriter;
import java.io.Serializable;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;


/**
 * 
 */
public class InternalConnectionManager implements ConnectionManager,
        ConnectionEventListener, Serializable {

    /**
     * 
     */
    public InternalConnectionManager() {
        super();
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionManager#allocateConnection(javax.resource.spi.ManagedConnectionFactory, javax.resource.spi.ConnectionRequestInfo)
     */
    public Object allocateConnection(ManagedConnectionFactory mcf,
            ConnectionRequestInfo cxRequestInfo) throws ResourceException {

        FBManagedConnection mc = (FBManagedConnection)((FBManagedConnectionFactory)mcf).createManagedConnection(null, cxRequestInfo);
        mc.setManagedEnvironment(true);
        mc.addConnectionEventListener(this);
        return mc.getConnection(null, cxRequestInfo);
    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#connectionClosed(javax.resource.spi.ConnectionEvent)
     */
    public void connectionClosed(ConnectionEvent event) {
        PrintWriter externalLog = ((FBManagedConnection)event.getSource()).getLogWriter();
        try {
            ((FBManagedConnection)event.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
        }

    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionStarted(javax.resource.spi.ConnectionEvent)
     */
    public void localTransactionStarted(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionCommitted(javax.resource.spi.ConnectionEvent)
     */
    public void localTransactionCommitted(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#localTransactionRolledback(javax.resource.spi.ConnectionEvent)
     */
    public void localTransactionRolledback(ConnectionEvent event) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see javax.resource.spi.ConnectionEventListener#connectionErrorOccurred(javax.resource.spi.ConnectionEvent)
     */
    public void connectionErrorOccurred(ConnectionEvent event) {
        PrintWriter externalLog = ((FBManagedConnection)event.getSource()).getLogWriter();
        try {
            ((FBManagedConnection)event.getSource()).destroy();
        }
        catch (ResourceException e) {
            if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
        }
    }

}
