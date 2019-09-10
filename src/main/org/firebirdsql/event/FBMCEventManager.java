package org.firebirdsql.event;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jca.FBManagedConnection;

import java.sql.SQLException;

/**
 * A managed connection based {@link EventManager} implementation to listen for database events.
 * FBMCEventManager accepts an existing {@link FBManagedConnection} connection and listens for events from it.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public class FBMCEventManager extends AbstractEventManager {
    private final GDSHelper gdsHelper;

    public FBMCEventManager(FBManagedConnection mc) throws SQLException {
        if (mc == null)
            throw new IllegalArgumentException("Connection is null");
        gdsHelper = mc.getGDSHelper();
        fbDatabase = gdsHelper.getCurrentDatabase();

        connected = true; // already connected

        eventDispatcher = new EventDispatcher();
        dispatchThread = new Thread(eventDispatcher);
        dispatchThread.setDaemon(true);
        dispatchThread.start();
    }

    @Override
    public void connect() throws SQLException {
        // nothing, connection is open elsewhere, just listening
    }

    @Override
    public void disconnect() throws SQLException {
        // nothing, connection will be closed where it was opened
    }

    @Override
    public String getUser() {
        return gdsHelper.getConnectionProperties().getUser();
    }

    @Override
    public void setUser(String user) {
        // nothing
    }

    @Override
    public String getPassword() {
        return gdsHelper.getConnectionProperties().getPassword();
    }

    @Override
    public void setPassword(String password) {
        // nothing
    }

    @Override
    public String getDatabase() {
        return gdsHelper.getConnectionProperties().getDatabaseName();
    }

    @Override
    public void setDatabase(String database) {
        // nothing
    }

    @Override
    public String getHost() {
        return gdsHelper.getConnectionProperties().getServerName();
    }

    @Override
    public void setHost(String host) {
        // nothing
    }

    @Override
    public int getPort() {
        return gdsHelper.getConnectionProperties().getPortNumber();
    }

    @Override
    public void setPort(int port) {
        // nothing
    }

    @Override
    public WireCrypt getWireCrypt() {
        return gdsHelper.getConnectionProperties().getWireCrypt();
    }

    @Override
    public void setWireCrypt(WireCrypt wireCrypt) {
        // nothing;
    }

    @Override
    public String getDbCryptConfig() {
        return gdsHelper.getConnectionProperties().getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        // nothing;
    }
}
