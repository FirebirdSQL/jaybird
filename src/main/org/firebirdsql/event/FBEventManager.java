/*
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2006-2008 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
 SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
*/
package org.firebirdsql.event;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jaybird.xca.FatalErrorHelper;
import org.firebirdsql.jdbc.FirebirdConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * An {@link org.firebirdsql.event.EventManager} implementation to listen for database events.
 *
 * @author Gabriel Reid
 * @author Mark Rotteveel
 * @author Vasiliy Yashkov
 */
public class FBEventManager implements EventManager {

    private static final System.Logger log = System.getLogger(FBEventManager.class.getName());

    private final Lock lock = new ReentrantLock();
    private final LockCloseable unlock = lock::unlock;
    private final GDSType gdsType;
    private FbDatabase fbDatabase;
    private final IConnectionProperties connectionProperties;
    private final EventManagerBehaviour eventManagerBehaviour;
    private volatile boolean connected = false;
    private final Map<String, Set<EventListener>> listenerMap = new HashMap<>();
    private final Map<String, GdsEventHandler> handlerMap = new HashMap<>();
    private final BlockingQueue<DatabaseEvent> eventQueue = new LinkedBlockingQueue<>();
    private EventDispatcher eventDispatcher;
    private Thread dispatchThread;
    private volatile long waitTimeout = 1000;
    private final DbListener dbListener = new DbListener();

    @SuppressWarnings("UnusedDeclaration")
    public FBEventManager() {
        this(GDSFactory.getDefaultGDSType());
    }

    public FBEventManager(GDSType gdsType) {
        this.gdsType = gdsType;
        connectionProperties = new FbConnectionProperties();
        eventManagerBehaviour = new DefaultEventManagerBehaviour();
        connectionProperties.setType(gdsType.toString());
    }

    /**
     * Constructs an event manager using an existing connection.
     *
     * @param connection Connection that unwraps to {@link FirebirdConnection}.
     */
    private FBEventManager(Connection connection) throws SQLException {
        this.fbDatabase = connection.unwrap(FirebirdConnection.class).getFbDatabase();
        gdsType = null;
        connectionProperties = fbDatabase.getConnectionProperties().asImmutable();
        eventManagerBehaviour = new ManagedEventManagerBehaviour();
        // NOTE In this implementation, we don't take into account pooled connections that might be closed while
        //  the FbDatabase instance remains in use. This means that at the moment, it is possible that the event manager
        //  can remain in use for longer than the Connection.
    }

    /**
     * Creates an {@link EventManager} for a connection.
     * <p>
     * The created event manager does not allow setting the properties and will instead
     * throw {@link UnsupportedOperationException} for the setters.
     * </p>
     * <p>
     * The returned instance is not necessarily an implementation of {@link FBEventManager}.
     * </p>
     *
     * @param connection
     *         A connection that unwraps to {@link org.firebirdsql.jdbc.FirebirdConnection}
     * @return An event manager
     * @throws SQLException
     *         When {@code connection} does not unwrap to {@link org.firebirdsql.jdbc.FirebirdConnection}
     * @since 3.0.7
     */
    public static EventManager createFor(Connection connection) throws SQLException {
        return new FBEventManager(connection);
    }

    protected final LockCloseable withLock() {
        lock.lock();
        return unlock;
    }

    @Override
    public final void setType(String type) {
        throw new IllegalStateException("Type must be specified on construction");
    }

    @Override
    public void connect() throws SQLException {
        try (LockCloseable ignored = withLock()) {
            if (connected) {
                throw new IllegalStateException("Connect called while already connected");
            }
            eventManagerBehaviour.connectDatabase();
            connected = true;

            eventDispatcher = new EventDispatcher();
            dispatchThread = new Thread(eventDispatcher);
            dispatchThread.setDaemon(true);
            dispatchThread.start();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connected) {
            disconnect();
        }
    }

    @Override
    public void disconnect() throws SQLException {
        if (!connected) {
            throw new IllegalStateException("Disconnect called while not connected");
        }
        var chain = new SQLExceptionChainBuilder();
        try (LockCloseable ignored = withLock()) {
            try {
                for (String eventName : new HashSet<>(handlerMap.keySet())) {
                    try {
                        unregisterListener(eventName);
                    } catch (Exception e) {
                        chain.append(e instanceof SQLException sqle ? sqle : new SQLException(e));
                        if (FatalErrorHelper.isBrokenConnection(e)) {
                            // It makes no sense to continue
                            break;
                        }
                    }
                }
            } finally {
                handlerMap.clear();
                listenerMap.clear();
                try {
                    eventManagerBehaviour.disconnectDatabase();
                } catch (SQLException e) {
                    chain.append(e);
                }
                connected = false;
            }
        } finally {
            try {
                terminateDispatcher();
            } catch (SQLException e) {
                chain.append(e);
            }
        }
        chain.throwIfPresent();
    }

    private void terminateDispatcher() throws SQLException {
        EventDispatcher eventDispatcher = this.eventDispatcher;
        if (eventDispatcher != null) {
            eventDispatcher.stop();
            dispatchThread.interrupt();
            // join the thread and wait until it dies
            try {
                dispatchThread.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
               throw new SQLException(ex);
            } finally {
                this.eventDispatcher = null;
                dispatchThread = null;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    // Methods from AttachmentProperties which were previously explicitly implemented, are redirecting to the
    // interface default method so they cen be introspected as JavaBean properties. Methods from AttachmentProperties
    // which were not defined in this class in Jaybird 4 or earlier are not redirected

    @Override
    public void setUser(String user) {
        EventManager.super.setUser(user);
    }

    @Override
    public String getUser() {
        return EventManager.super.getUser();
    }

    @Override
    public void setPassword(String password) {
        EventManager.super.setPassword(password);
    }

    @Override
    public String getPassword() {
        return EventManager.super.getPassword();
    }

    @Override
    public String getServerName() {
        return EventManager.super.getServerName();
    }

    @Override
    public void setServerName(String serverName) {
        EventManager.super.setServerName(serverName);
    }

    @Override
    public int getPortNumber() {
        return EventManager.super.getPortNumber();
    }

    @Override
    public void setPortNumber(int portNumber) {
        EventManager.super.setPortNumber(portNumber);
    }

    @Override
    public String getDatabaseName() {
        return connectionProperties.getDatabaseName();
    }

    @Override
    public void setDatabaseName(String databaseName) {
        connectionProperties.setDatabaseName(databaseName);
    }

    @Override
    public WireCrypt getWireCryptAsEnum() {
        return connectionProperties.getWireCryptAsEnum();
    }

    @Override
    public void setWireCryptAsEnum(WireCrypt wireCrypt) {
        connectionProperties.setWireCryptAsEnum(wireCrypt);
    }

    @Override
    public String getWireCrypt() {
        return EventManager.super.getWireCrypt();
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        EventManager.super.setWireCrypt(wireCrypt);
    }

    @Override
    public String getDbCryptConfig() {
        return EventManager.super.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        EventManager.super.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return EventManager.super.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        EventManager.super.setAuthPlugins(authPlugins);
    }

    @Override
    public long getWaitTimeout() {
        return waitTimeout;
    }

    @Override
    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    @Override
    public void addEventListener(String eventName, EventListener listener) throws SQLException {
        if (listener == null || eventName == null) {
            throw new NullPointerException();
        }
        if (!connected) {
            throw new IllegalStateException("Can't add event listeners to disconnected EventManager");
        }
        try (LockCloseable ignored = withLock()) {
            Set<EventListener> listenerSet = listenerMap.get(eventName);
            if (listenerSet == null) {
                registerListener(eventName);
                listenerSet = new HashSet<>();
                listenerMap.put(eventName, listenerSet);
            }
            listenerSet.add(listener);
        }
    }

    @Override
    public void removeEventListener(String eventName, EventListener listener) throws SQLException {
        if (eventName == null || listener == null) {
            throw new NullPointerException();
        }
        try (LockCloseable ignored = withLock()) {
            Set<EventListener> listenerSet = listenerMap.get(eventName);
            if (listenerSet != null) {
                listenerSet.remove(listener);
                if (listenerSet.isEmpty()) {
                    listenerMap.remove(eventName);
                    unregisterListener(eventName);
                }
            }
        }
    }

    @Override
    public int waitForEvent(String eventName) throws InterruptedException, SQLException {
        return waitForEvent(eventName, 0);
    }

    @Override
    public int waitForEvent(String eventName, final int timeout) throws InterruptedException, SQLException {
        if (eventName == null) {
            throw new NullPointerException();
        }
        if (!connected) {
            throw new IllegalStateException("Can't wait for events with disconnected EventManager");
        }
        var listener = new OneTimeEventListener();
        try {
            addEventListener(eventName, listener);
            listener.await(timeout, TimeUnit.MILLISECONDS);
        } finally {
            removeEventListener(eventName, listener);
        }
        return listener.getEventCount();
    }

    private void registerListener(String eventName) throws SQLException {
        var handler = new GdsEventHandler(eventName);
        try (LockCloseable ignored = withLock()) {
            handlerMap.put(eventName, handler);
            handler.register();
        }
    }

    private void unregisterListener(String eventName) throws SQLException {
        try (LockCloseable ignored = withLock()) {
            GdsEventHandler handler = handlerMap.remove(eventName);
            if (handler != null) handler.unregister();
        }
    }

    @Override
    public String getProperty(String name) {
        return connectionProperties.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value) {
        if (PropertyNames.type.equals(name)) {
            // Triggers exception
            setType(value);
        }
        connectionProperties.setProperty(name, value);
    }

    @Override
    public Integer getIntProperty(String name) {
        return connectionProperties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        connectionProperties.setIntProperty(name, value);
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        return connectionProperties.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        connectionProperties.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return connectionProperties.connectionPropertyValues();
    }

    private void addDbListeners(FbDatabase database) {
        if (database == null) return;
        database.addDatabaseListener(dbListener);
        database.addExceptionListener(dbListener);
    }

    private void removeDbListeners(FbDatabase database) {
        if (database == null) return;
        database.removeExceptionListener(dbListener);
        database.removeDatabaseListener(dbListener);
    }

    /**
     * Returns the current {@link FbDatabase} instance.
     * <p>
     * This method is intended for use by tests only.
     * </p>
     *
     * @return underlying {@link FbDatabase}
     */
    FbDatabase getFbDatabase() {
        return fbDatabase;
    }

    private sealed interface EventManagerBehaviour {
        void connectDatabase() throws SQLException;
        void disconnectDatabase() throws SQLException;
        void handleDetaching(FbDatabase database);
        void handleErrorOccurred(Object source, SQLException exception);
    }

    /**
     * Default behaviour where the event manager owns the connection.
     */
    private final class DefaultEventManagerBehaviour implements EventManagerBehaviour {

        @Override
        public void connectDatabase() throws SQLException {
            FbDatabaseFactory databaseFactory = GDSFactory.getDatabaseFactoryForType(gdsType);
            fbDatabase = databaseFactory.connect(connectionProperties);
            addDbListeners(fbDatabase);
            fbDatabase.attach();
        }

        @Override
        public void disconnectDatabase() throws SQLException {
            removeDbListeners(fbDatabase);
            fbDatabase.close();
        }

        @Override
        public void handleDetaching(FbDatabase database) {
            try (LockCloseable ignored = withLock()) {
                listenerMap.clear();
                handlerMap.clear();
                connected = false;
            }
        }

        @Override
        public void handleErrorOccurred(Object source, SQLException exception) {
            FbDatabase fbDatabase = FBEventManager.this.fbDatabase;
            if (fbDatabase == null || !fbDatabase.isAttached()) return;

            if (FatalErrorHelper.isBrokenConnection(exception)) {
                try {
                    fbDatabase.forceClose();
                } catch (SQLException e) {
                    log.log(DEBUG, "Ignored exception force closing exception", e);
                } finally {
                    listenerMap.clear();
                    handlerMap.clear();
                    connected = false;
                    try {
                        terminateDispatcher();
                    } catch (SQLException e) {
                        log.log(DEBUG, "Ignored exception terminating dispatcher", e);
                    }
                }
            } else if (FatalErrorHelper.isFatal(exception)) {
                try {
                    disconnect();
                } catch (SQLException e) {
                    log.log(DEBUG, "Ignored exception during disconnect", e);
                }
            }
        }
    }

    /**
     * Behaviour where the lifetime of the connection used by the event manager is managed elsewhere.
     */
    private final class ManagedEventManagerBehaviour implements EventManagerBehaviour {

        @Override
        public void connectDatabase() throws SQLException {
            // using existing connection
            if (fbDatabase == null || !fbDatabase.isAttached()) {
                fbDatabase = null;
                // fbDatabase has already detached
                throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_notConnectedToServer);
            }
            addDbListeners(fbDatabase);
        }

        @Override
        public void disconnectDatabase() {
            // fbDatabase will be closed where it was opened
        }

        @Override
        public void handleDetaching(FbDatabase database) {
            try {
                if (!isConnected()) return;
                disconnect();
            } catch (SQLException e) {
                log.log(ERROR, "Exception on disconnect of event manager on connection detaching", e);
            }
        }

        @Override
        public void handleErrorOccurred(Object source, SQLException exception) {
            // do nothing, should be handled by the handling in FBManagedConnection
        }
    }

    private final class DbListener implements DatabaseListener, ExceptionListener {

        @Override
        public void detaching(FbDatabase database) {
            removeDbListeners(database);
            try {
                eventManagerBehaviour.handleDetaching(database);
            } finally {
                fbDatabase = null;
                try {
                    terminateDispatcher();
                } catch (SQLException e) {
                    log.log(DEBUG, "Ignored exception terminating dispatcher", e);
                }
            }
        }

        @Override
        public void errorOccurred(Object source, SQLException ex) {
            eventManagerBehaviour.handleErrorOccurred(source, ex);
        }
    }

    final class GdsEventHandler implements org.firebirdsql.gds.EventHandler {

        private final EventHandle eventHandle;
        private volatile boolean initialized = false;
        private volatile boolean cancelled = false;

        GdsEventHandler(String eventName) throws SQLException {
            eventHandle = fbDatabase.createEventHandle(eventName, this);
        }

        void register() throws SQLException {
            if (cancelled) {
                throw new IllegalStateException("Trying to register a cancelled event handler");
            }
            fbDatabase.queueEvent(eventHandle);
        }

        void unregister() throws SQLException {
            if (cancelled) return;
            fbDatabase.cancelEvent(eventHandle);
            cancelled = true;
        }

        @Override
        public void eventOccurred(EventHandle eventHandle) {
            if (cancelled) {
                return;
            }
            try {
                fbDatabase.countEvents(eventHandle);
            } catch (SQLException e) {
                log.log(WARNING, "Exception processing event counts; see debug level for stacktrace");
                log.log(DEBUG, "Exception processing event counts", e);
            }

            if (initialized && !cancelled) {
                eventQueue.add(new DatabaseEventImpl(eventHandle.getEventName(), eventHandle.getEventCount()));
            } else {
                initialized = true;
            }

            try {
                register();
            } catch (SQLException e) {
                log.log(WARNING, "Exception registering for event; see debug level for stacktrace");
                log.log(DEBUG, "Exception registering for event", e);
            }
        }
    }

    final class EventDispatcher implements Runnable {

        private volatile boolean running = true;

        void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    DatabaseEvent event = eventQueue.poll(waitTimeout, TimeUnit.MILLISECONDS);
                    if (event != null) {
                        notify(event);
                    }
                } catch (InterruptedException ie) {
                    // Ignore interruption; continue if not explicitly stopped
                }
            }
        }

        private void notify(DatabaseEvent event) {
            try (LockCloseable ignored = withLock()) {
                Set<EventListener> listenerSet = listenerMap.get(event.getEventName());
                if (listenerSet != null) {
                    for (EventListener listener : listenerSet) {
                        listener.eventOccurred(event);
                    }
                }
            }
        }
    }
}
