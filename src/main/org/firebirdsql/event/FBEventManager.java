/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.event;

import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.gds.ng.listeners.DatabaseListener;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An {@link org.firebirdsql.event.EventManager} implementation to listen for database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public class FBEventManager implements EventManager {

    private static final Logger log = LoggerFactory.getLogger(FBEventManager.class);

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
        // NOTE In this implementation, we don't take into account pooled connections that might be closed while
        //  the FbDatabase instance remains in use. This means that at the moment, it is possible that the event manager
        //  can remain in use for longer than the Connection.
        fbDatabase.addDatabaseListener(new DatabaseListener() {
            @Override
            public void detaching(FbDatabase database) {
                try {
                    if (!isConnected()) return;
                    try {
                        disconnect();
                    } catch (SQLException e) {
                        log.error("Exception on disconnect of event manager on connection detaching.", e);
                    }
                } finally {
                    database.removeDatabaseListener(this);
                    fbDatabase = null;
                }
            }
        });
        eventManagerBehaviour = new ManagedEventManagerBehaviour();
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
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        try (LockCloseable ignored = withLock()) {
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
                try {
                    eventManagerBehaviour.disconnectDatabase();
                } catch (SQLException e) {
                    chain.append(e);
                }
                connected = false;
            }
        } finally {
            EventDispatcher eventDispatcher = this.eventDispatcher;
            if (eventDispatcher != null) {
                eventDispatcher.stop();
                dispatchThread.interrupt();
                // join the thread and wait until it dies
                try {
                    dispatchThread.join();
                } catch (InterruptedException ex) {
                    chain.append(new FBSQLException(ex));
                } finally {
                    this.eventDispatcher = null;
                    dispatchThread = null;
                }
            }
        }
        if (chain.hasException()) throw chain.getException();
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
    @Deprecated
    public void setDatabase(String database) {
        setDatabaseName(database);
    }

    @Override
    @Deprecated
    public String getDatabase() {
        return getDatabaseName();
    }

    @Override
    @Deprecated
    public String getHost() {
        return getServerName();
    }

    @Override
    @Deprecated
    public void setHost(String host) {
        setServerName(host);
    }

    @Override
    @Deprecated
    public int getPort() {
        return getPortNumber();
    }

    @Override
    @Deprecated
    public void setPort(int port) {
        setPortNumber(port);
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
        OneTimeEventListener listener = new OneTimeEventListener();
        try {
            addEventListener(eventName, listener);
            listener.await(timeout, TimeUnit.MILLISECONDS);
        } finally {
            removeEventListener(eventName, listener);
        }
        return listener.getEventCount();
    }

    private void registerListener(String eventName) throws SQLException {
        GdsEventHandler handler = new GdsEventHandler(eventName);
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

    private interface EventManagerBehaviour {
        void connectDatabase() throws SQLException;
        void disconnectDatabase() throws SQLException;
    }

    /**
     * Default behaviour where the event manager owns the connection.
     */
    private class DefaultEventManagerBehaviour implements EventManagerBehaviour {

        @Override
        public void connectDatabase() throws SQLException {
            FbDatabaseFactory databaseFactory = GDSFactory.getDatabaseFactoryForType(gdsType);
            fbDatabase = databaseFactory.connect(connectionProperties);
            fbDatabase.attach();
        }

        @Override
        public void disconnectDatabase() throws SQLException {
            fbDatabase.close();
        }
    }

    /**
     * Behaviour where the lifetime of the connection used by the event manager is managed elsewhere.
     */
    private class ManagedEventManagerBehaviour implements EventManagerBehaviour {

        @Override
        public void connectDatabase() throws SQLException {
            // using existing connection
            if (fbDatabase == null) {
                // fbDatabase has already detached
                throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_notConnectedToServer).toSQLException();
            }
        }

        @Override
        public void disconnectDatabase() {
            // fbDatabase will be closed where it was opened
        }
    }

    class GdsEventHandler implements org.firebirdsql.gds.EventHandler {

        private final EventHandle eventHandle;
        private volatile boolean initialized = false;
        private volatile boolean cancelled = false;

        public GdsEventHandler(String eventName) throws SQLException {
            eventHandle = fbDatabase.createEventHandle(eventName, this);
        }

        public void register() throws SQLException {
            if (cancelled) {
                throw new IllegalStateException("Trying to register a cancelled event handler");
            }
            fbDatabase.queueEvent(eventHandle);
        }

        public void unregister() throws SQLException {
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
                log.warnDebug("Exception processing event counts", e);
            }

            if (initialized && !cancelled) {
                eventQueue.add(new DatabaseEventImpl(eventHandle.getEventName(), eventHandle.getEventCount()));
            } else {
                initialized = true;
            }

            try {
                register();
            } catch (SQLException e) {
                log.warnDebug("Exception registering for event", e);
            }
        }
    }

    class EventDispatcher implements Runnable {

        private volatile boolean running = true;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            DatabaseEvent event;
            while (running) {
                try {
                    event = eventQueue.poll(waitTimeout, TimeUnit.MILLISECONDS);
                    if (event == null) continue;

                    try (LockCloseable ignored = withLock()) {
                        Set<EventListener> listenerSet = listenerMap.get(event.getEventName());
                        if (listenerSet != null) {
                            for (EventListener listener : listenerSet) {
                                listener.eventOccurred(event);
                            }
                        }
                    }
                } catch (InterruptedException ie) {
                    // Ignore interruption; continue if not explicitly stopped
                }
            }
        }
    }
}

class OneTimeEventListener implements EventListener {

    private int eventCount = -1;

    private final Lock lock = new ReentrantLock();
    private final Condition receivedEvent = lock.newCondition();

    @Override
    public void eventOccurred(DatabaseEvent event) {
        lock.lock();
        try {
            if (eventCount == -1) {
                eventCount = event.getEventCount();
            }
            receivedEvent.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void await(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            // Event already received, no need to wait
            if (eventCount != -1) return;
            if (time == 0) {
                receivedEvent.await();
            } else {
                receivedEvent.await(time, unit);
            }
        } finally {
            lock.unlock();
        }
    }

    public int getEventCount() {
        return eventCount;
    }
}

class DatabaseEventImpl implements DatabaseEvent {

    private final int eventCount;

    private final String eventName;

    public DatabaseEventImpl(String eventName, int eventCount) {
        this.eventName = eventName;
        this.eventCount = eventCount;
    }

    @Override
    public int getEventCount() {
        return this.eventCount;
    }

    @Override
    public String getEventName() {
        return this.eventName;
    }

    @Override
    public String toString() {
        return "DatabaseEvent['" + eventName + " * " + eventCount + "]";
    }
}
