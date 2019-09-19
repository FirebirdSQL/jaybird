package org.firebirdsql.event;

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Implementation of managed event manager extending an {@link FBEventManager} event listener.
 * FBMCEventManager accepts an existing {@link Connection} connection and listens for events from it.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public class FBMCEventManager extends FBEventManager {

    public FBMCEventManager(Connection connection) throws SQLException {
        if (connection == null)
            throw new IllegalArgumentException("Connection is null");
        fbDatabase = connection.unwrap(FBConnection.class).getFbDatabase();
    }

    @Override
    public void connect() throws SQLException {
        // connection is open elsewhere, just listening
        connected = true;

        eventDispatcher = new EventDispatcher();
        dispatchThread = new Thread(eventDispatcher);
        dispatchThread.setDaemon(true);
        dispatchThread.start();
    }

    @Override
    public void disconnect() throws SQLException {
        // fbDatabase will be closed where it was opened
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        try {
            try {
                for (String eventName : new HashSet<>(handlerMap.keySet())) {
                    try {
                        unregisterListener(eventName);
                    } catch (SQLException e) {
                        chain.append(e);
                    } catch (Exception e) {
                        chain.append(new SQLException(e));
                    }
                }
            } finally {
                handlerMap.clear();
                listenerMap.clear();
                connected = false;
            }
        } finally {
            eventDispatcher.stop();

            // join the thread and wait until it dies
            try {
                dispatchThread.join();
            } catch (InterruptedException ex) {
                chain.append(new FBSQLException(ex));
            }
        }
        if (chain.hasException()) throw chain.getException();
    }

    @Override
    public String getUser() {
        return fbDatabase.getConnectionProperties().getUser();
    }

    @Override
    public void setUser(String user) {
        immutable();
    }

    @Override
    public String getPassword() {
        return fbDatabase.getConnectionProperties().getPassword();
    }

    @Override
    public void setPassword(String password) {
        immutable();
    }

    @Override
    public String getDatabase() {
        return fbDatabase.getConnectionProperties().getDatabaseName();
    }

    @Override
    public void setDatabase(String database) {
        immutable();
    }

    @Override
    public String getHost() {
        return fbDatabase.getConnectionProperties().getServerName();
    }

    @Override
    public void setHost(String host) {
        immutable();
    }

    @Override
    public int getPort() {
        return fbDatabase.getConnectionProperties().getPortNumber();
    }

    @Override
    public void setPort(int port) {
        immutable();
    }

    @Override
    public WireCrypt getWireCrypt() {
        return fbDatabase.getConnectionProperties().getWireCrypt();
    }

    @Override
    public void setWireCrypt(WireCrypt wireCrypt) {
        immutable();
    }

    @Override
    public String getDbCryptConfig() {
        return fbDatabase.getConnectionProperties().getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        immutable();
    }

    /**
     * Throws an UnsupportedOperationException
     */
    protected final void immutable() {
        throw new UnsupportedOperationException("this object is immutable");
    }
}
