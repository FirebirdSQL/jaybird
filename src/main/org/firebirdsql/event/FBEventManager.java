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
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An {@link org.firebirdsql.event.EventManager} implementation to listen for database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBEventManager implements EventManager {

    private static final Logger log = LoggerFactory.getLogger(FBEventManager.class);

    private final GDSType gdsType;
    private FbDatabase fbDatabase;
    private final IConnectionProperties connectionProperties = new FbConnectionProperties();
    private boolean connected = false;
    private final Map<String, Set<EventListener>> listenerMap = Collections.synchronizedMap(new HashMap<String, Set<EventListener>>());
    private final Map<String, GdsEventHandler> handlerMap = Collections.synchronizedMap(new HashMap<String, GdsEventHandler>());
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
    }

    @Override
    public void connect() throws SQLException {
        if (connected) {
            throw new IllegalStateException("Connect called while already connected");
        }
        final FbDatabaseFactory databaseFactory = GDSFactory.getDatabaseFactoryForType(gdsType);

        fbDatabase = databaseFactory.connect(connectionProperties);
        fbDatabase.attach();
        connected = true;

        eventDispatcher = new EventDispatcher();
        dispatchThread = new Thread(eventDispatcher);
        dispatchThread.setDaemon(true);
        dispatchThread.start();
    }

    @Override
    public void disconnect() throws SQLException {
        if (!connected) {
            throw new IllegalStateException("Disconnect called while not connected");
        }
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
                try {
                    fbDatabase.close();
                } catch (SQLException e) {
                    chain.append(e);
                }
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

    public boolean isConnected() {
        return connected;
    }

    @Override
    public void setUser(String user) {
        connectionProperties.setUser(user);
    }

    @Override
    public String getUser() {
        return connectionProperties.getUser();
    }

    @Override
    public void setPassword(String password) {
        connectionProperties.setPassword(password);
    }

    @Override
    public String getPassword() {
        return connectionProperties.getPassword();
    }

    @Override
    public void setDatabase(String database) {
        connectionProperties.setDatabaseName(database);
    }

    @Override
    public String getDatabase() {
        return connectionProperties.getDatabaseName();
    }

    @Override
    public String getHost() {
        return connectionProperties.getServerName();
    }

    @Override
    public void setHost(String host) {
        connectionProperties.setServerName(host);
    }

    @Override
    public int getPort() {
        return connectionProperties.getPortNumber();
    }

    @Override
    public void setPort(int port) {
        connectionProperties.setPortNumber(port);
    }

    @Override
    public WireCrypt getWireCrypt() {
        return connectionProperties.getWireCrypt();
    }

    @Override
    public void setWireCrypt(WireCrypt wireCrypt) {
        connectionProperties.setWireCrypt(wireCrypt);
    }

    @Override
    public String getDbCryptConfig() {
        return connectionProperties.getDbCryptConfig();
    }

    @Override
    public void setDbCryptConfig(String dbCryptConfig) {
        connectionProperties.setDbCryptConfig(dbCryptConfig);
    }

    @Override
    public String getAuthPlugins() {
        return connectionProperties.getAuthPlugins();
    }

    @Override
    public void setAuthPlugins(String authPlugins) {
        connectionProperties.setAuthPlugins(authPlugins);
    }

    /**
     * Get the time in milliseconds, after which the async thread will exit from the {@link Object#wait(long)} method
     * and check whether it was stopped or not.
     * <p>
     * Default value is 1000 (1 second).
     * </p>
     *
     * @return wait timeout in milliseconds
     */
    @SuppressWarnings("UnusedDeclaration")
    public long getWaitTimeout() {
        return waitTimeout;
    }

    /**
     * Set the time in milliseconds, after which the async thread will exit from the {@link Object#wait(long)} method
     * and check whether it was stopped or not.
     * <p>
     * Default value is 1000 (1 second).
     * </p>
     *
     * @param waitTimeout
     *         wait timeout in milliseconds
     */
    @SuppressWarnings("UnusedDeclaration")
    public synchronized void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public void addEventListener(String eventName, EventListener listener) throws SQLException {
        if (!connected) {
            throw new IllegalStateException("Can't add event listeners to disconnected EventManager");
        }
        if (listener == null || eventName == null) {
            throw new NullPointerException();
        }
        synchronized (listenerMap) {
            if (!listenerMap.containsKey(eventName)) {
                registerListener(eventName);
                listenerMap.put(eventName, new HashSet<EventListener>());
            }
            Set<EventListener> listenerSet = listenerMap.get(eventName);
            listenerSet.add(listener);
        }
    }

    public void removeEventListener(String eventName, EventListener listener) throws SQLException {
        if (eventName == null || listener == null) {
            throw new NullPointerException();
        }
        Set<EventListener> listenerSet = listenerMap.get(eventName);
        if (listenerSet != null) {
            listenerSet.remove(listener);
            if (listenerSet.isEmpty()) {
                listenerMap.remove(eventName);
                unregisterListener(eventName);
            }
        }
    }

    public int waitForEvent(String eventName) throws InterruptedException, SQLException {
        return waitForEvent(eventName, 0);
    }

    public int waitForEvent(String eventName, final int timeout) throws InterruptedException, SQLException {
        if (!connected) {
            throw new IllegalStateException("Can't wait for events with disconnected EventManager");
        }
        if (eventName == null) {
            throw new NullPointerException();
        }
        final Object lock = new Object();
        OneTimeEventListener listener = new OneTimeEventListener(lock);
        try {
            synchronized (lock) {
                addEventListener(eventName, listener);
                lock.wait(timeout);
            }
        } finally {
            removeEventListener(eventName, listener);
        }
        return listener.getEventCount();
    }

    private void registerListener(String eventName) throws SQLException {
        GdsEventHandler handler = new GdsEventHandler(eventName);
        handlerMap.put(eventName, handler);
        handler.register();
    }

    private void unregisterListener(String eventName) throws SQLException {
        GdsEventHandler handler = handlerMap.get(eventName);
        try {
            if (handler != null) handler.unregister();
        } finally {
            handlerMap.remove(eventName);
        }
    }

    class GdsEventHandler implements org.firebirdsql.gds.EventHandler {

        private final EventHandle eventHandle;
        private boolean initialized = false;
        private volatile boolean cancelled = false;

        public GdsEventHandler(String eventName) throws SQLException {
            eventHandle = fbDatabase.createEventHandle(eventName, this);
        }

        public synchronized void register() throws SQLException {
            if (cancelled) {
                throw new IllegalStateException("Trying to register a cancelled event handler");
            }
            fbDatabase.queueEvent(eventHandle);
        }

        public synchronized void unregister() throws SQLException {
            if (cancelled) return;
            fbDatabase.cancelEvent(eventHandle);
            cancelled = true;
        }

        @Override
        public synchronized void eventOccurred(EventHandle eventHandle) {
            if (!cancelled) {
                try {
                    fbDatabase.countEvents(eventHandle);
                } catch (SQLException e) {
                    String message = "Exception processing event counts";
                    log.warn(message + ": " + e + "; see debug level for stacktrace");
                    log.debug(message, e);
                }

                if (initialized && !cancelled) {
                    eventQueue.add(new DatabaseEventImpl(eventHandle.getEventName(), eventHandle.getEventCount()));
                } else {
                    initialized = true;
                }

                try {
                    register();
                } catch (SQLException e) {
                    String message = "Exception registering for event";
                    log.warn(message + ": " + e + "; see debug level for stacktrace");
                    log.debug(message, e);
                }
            }
        }
    }

    class EventDispatcher implements Runnable {

        private volatile boolean running = false;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            running = true;
            DatabaseEvent event;
            while (running) {
                try {
                    event = eventQueue.poll(waitTimeout, TimeUnit.MILLISECONDS);
                    if (event == null) continue;

                    synchronized (listenerMap) {
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

    private final Object lock;

    public OneTimeEventListener(Object lock) {
        this.lock = lock;
    }

    @Override
    public void eventOccurred(DatabaseEvent event) {
        if (eventCount == -1) {
            eventCount = event.getEventCount();
        }
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public int getEventCount() {
        return eventCount;
    }
}

class DatabaseEventImpl implements DatabaseEvent {

    private int eventCount;

    private String eventName;

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
