/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 * 
 * Copyright (C) All Rights Reserved.
 * 
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *   - Redistributions of source code must retain the above copyright 
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above 
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   - Neither the name of the firebird development team nor the names
 *     of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written 
 *     permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
 * SUCH DAMAGE.
 */
package org.firebirdsql.event;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.EventHandler;
import org.firebirdsql.gds.EventHandle;
import org.firebirdsql.gds.IscDbHandle;

import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.jdbc.FBSQLException;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Collections;

import java.sql.SQLException;

/**
 * An <code>EventListener</code> implementation to listen for database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 */
public class FBEventManager implements EventManager {

    private GDS gds;
    private IscDbHandle dbHandle;
    private boolean connected = false;
    private String user = "";
    private String password = "";
    private String database = "";
    private int port = 3050;
    private String host = "localhost";
    private Map listenerMap = Collections.synchronizedMap(new HashMap());
    private Map handlerMap = Collections.synchronizedMap(new HashMap());
    private List eventQueue = new ArrayList();
    private EventDispatcher eventDispatcher;
    private Thread dispatchThread;
    private long waitTimeout = 1000;

    public FBEventManager() {
    	this(GDSFactory.getDefaultGDSType());
    }

    public FBEventManager(GDSType gdsType){
        gds = GDSFactory.getGDSForType(gdsType);
        dbHandle = gds.createIscDbHandle();
    }

    /**
     * Make a connection with a database to listen for events.
     *
     * @throws SQLException If a database communication error occurs
     */
    public void connect() throws SQLException {
        if (connected){
            throw new IllegalStateException(
                    "Connect called while already connected");        
        }
        DatabaseParameterBuffer dpb = gds.createDatabaseParameterBuffer();
        dpb.addArgument(DatabaseParameterBuffer.USER, user);
        dpb.addArgument(DatabaseParameterBuffer.PASSWORD, password);
        String connString = host + "/" + port + ":" + database;
        try {
            gds.iscAttachDatabase(connString, dbHandle, dpb);
        } catch (GDSException e){
            throw new FBSQLException(e);
        }
        connected = true;
        eventDispatcher = new EventDispatcher();
        dispatchThread = new Thread(eventDispatcher);
        dispatchThread.setDaemon(true);
        dispatchThread.start();
    }

    /**
     * Close the connection to the database.
     *
     * @throws SQLException If a database communication error occurs
     */
    public void disconnect() throws SQLException {
        if (!connected){
            throw new IllegalStateException(
                    "Disconnect called while not connected");
        }
        for (Iterator eventItr = new HashSet(handlerMap.keySet()).iterator(); 
                eventItr.hasNext();){
            String eventName = (String)eventItr.next();
            try {
                unregisterListener(eventName);
            } catch (GDSException e1){
                throw new FBSQLException(e1);
            }
        }

        handlerMap.clear();
        listenerMap.clear();

        try {
            gds.iscDetachDatabase(dbHandle);
        } catch (GDSException e2){
            throw new FBSQLException(e2);
        }
        connected = false;

        eventDispatcher.stop();
        
        // join the thread and wait until it dies
        try {
            dispatchThread.join();
        } catch(InterruptedException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Check whether this object is connected to the database.
     * @return <code>true</code> if object is connected to the 
     * database, otherwise <code>false</code>.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Sets the username for the connection to the database .
     * @param username for the connection to the database.
     */
    public void setUser(String user){
        this.user = user;
    }

    /**
     * Returns the username for the connection to the databaes.
     * @return the username for the connection to the database.
     */
    public String getUser(){
        return this.user;
    }

    /**
     * Sets the password for the connection to the database.
     * @param password for the connection to the database.
     */
    public void setPassword(String password){
        this.password = password;
    }

    /**
     * Returns the password for the connection to the database.
     * @return the password for the connection to the database.
     */
    public String getPassword(){
        return this.password;
    }

    /**
     * Sets the database path for the connection to the database.
     * @param database path for the connection to the database.
     */
    public void setDatabase(String database){
        this.database = database;
    }

    /**
     * Returns the database path for the connection to the database.
     * @return the database path for the connection to the database.
     */
    public String getDatabase(){
        return this.database;
    }

    /**
     * Returns the host for the connection to the database.
     * @return the host for the connection to the database.
     */
    public String getHost(){
        return this.host;
    }

    /**
     * Sets the host for the connection to the database.
     * @param host for the connection to the database.
     */
    public void setHost(String host){
        this.host = host;
    }
       
    /**
     * Returns the port for the connection to the database.
     * @return the port for the connection to the database.
     */
    public int getPort(){
        return this.port;
    }

    /**
     * Sets the port for the connection to the database.
     * @param port for the connection to the database.
     */
    public void setPort(int port){
        this.port = port;
    }
    
    /**
     * Get the time in milliseconds, after which the async threa will exit from
     * the {@link Object#wait(long)} method and check whether it was stopped or 
     * not. 
     * <p>
     * Default value is 1000 (1 second);
     * 
     * @return wait timeout in milliseconds
     */
    public long getWaitTimeout() {
        return waitTimeout;
    }

    
    /**
     * Set the time in milliseconds, after which the async threa will exit from
     * the {@link Object#wait(long)} method and check whether it was stopped or 
     * not. 
     * <p>
     * Default value is 1000 (1 second);
     * 
     * @param waitTimeout wait timeout in milliseconds
     */
    public void setWaitTimeout(long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    /**
     * Register an EventListener that will be called when an event occurs.
     *
     * @param eventName The name of the event for which the listener will
     *                  be notified
     * @param listener The EventListener that will be called when the given
     *                 event occurs
     * @throws SQLException If a database access error occurs
     */
    public void addEventListener(
            String eventName, EventListener listener) throws SQLException {
        if (!connected){
            throw new IllegalStateException(
                    "Can't add event listeners to disconnected EventManager");
        }
        if (listener == null || eventName == null){
            throw new NullPointerException();
        }
        synchronized (listenerMap){
            if (!listenerMap.containsKey(eventName)){
                try {
                    registerListener(eventName);
                } catch (GDSException e){
                    throw new FBSQLException(e);
                }
                listenerMap.put(eventName, new HashSet());
            }
            Set listenerSet = (Set)listenerMap.get(eventName);
            listenerSet.add(listener);
        }
    }

    /**
     * Remove an EventListener for a given event.
     *
     * @param eventName The name of the event for which the listener 
     *                  will be unregistered.
     * @param listener The EventListener that is to be unregistered
     * @throws SQLException If a database access error occurs
     */
    public void removeEventListener(
            String eventName, EventListener listener) throws SQLException {
        if (eventName == null || listener == null){
            throw new NullPointerException();
        }
        Set listenerSet = (Set)listenerMap.get(eventName);
        if (listenerSet != null){
            listenerSet.remove(listener);
            if (listenerSet.isEmpty()){
                listenerMap.remove(eventName);
                try {
                    unregisterListener(eventName);
                } catch (GDSException e){
                    throw new FBSQLException(e);
                }
            }
        }
    }

    /**
     * Wait for the one-time occurence of an event.
     *
     * This method blocks indefinitely until the event identified by the
     * value of <code>eventName</code> occurs. The return value is the
     * number of occurrences of the requested event.
     *
     * @param eventName The name of the event to wait for
     * @return The number of occurences of the requested event
     * @throws InterruptedException If interrupted while waiting
     * @throws SQLException If a database access error occurs
     */
    public int waitForEvent(String eventName) 
            throws InterruptedException, SQLException {
        return waitForEvent(eventName, 0);
    }

    /**
     * Wait for the one-time occurence of an event.
     *
     * This method blocks for a maximum of <code>timeout</code> milliseconds,
     * waiting for the event identified by <code>eventName</code> to occur.
     * A timeout value of <code>0</code> means wait indefinitely.
     *
     * The return value is the number of occurences of the event in question,
     * or <code>-1</code> if the call timed out.
     *
     * @param exenvtName The name of the event to wait for
     * @param timeout The maximum number of milliseconds to wait
     * @return The number of occurrences of the requested event, or 
     *         <code>-1</code> if the call timed out
     * @throws InterruptedException If interrupted while waiting
     * @throws SQLException If a database access error occurs
     */
    public int waitForEvent(String eventName, final int timeout) 
            throws InterruptedException, SQLException {
        if (!connected){
            throw new IllegalStateException(
                    "Can't wait for events with disconnected EventManager");
        }
        if (eventName == null){
            throw new NullPointerException();
        }
        final Object lock = new Object();
        OneTimeEventListener listener = new OneTimeEventListener(lock);
        synchronized (lock){
            addEventListener(eventName, listener);
            lock.wait(timeout);
        }
              
        removeEventListener(eventName, listener);
        return listener.getEventCount();
    }

    private void registerListener(String eventName) throws GDSException {
        GdsEventHandler handler = new GdsEventHandler(eventName);
        handlerMap.put(eventName, handler);
        handler.register();
    }

    private void unregisterListener(String eventName) throws GDSException {
        GdsEventHandler handler = (GdsEventHandler)handlerMap.get(eventName);
        handler.unregister();
        handlerMap.remove(eventName);
    }

    class GdsEventHandler implements org.firebirdsql.gds.EventHandler {

        private EventHandle eventHandle;
        private boolean initialized = false;
        private boolean cancelled = false;
        private Object registerLock = new Object();

        public GdsEventHandler(String eventName) throws GDSException {
            eventHandle = gds.createEventHandle(eventName);
            gds.iscEventBlock(eventHandle);
        }

        public synchronized void register() throws GDSException {
            if (cancelled){
                throw new IllegalStateException(
                        "Trying to register a cancelled event handler");
            }
            gds.iscQueueEvents(dbHandle, eventHandle, this);
        }

        public synchronized void unregister() throws GDSException {
            if (cancelled){
                throw new IllegalStateException(
                        "Trying to cancel a cancelled event handler");
            }
            gds.iscCancelEvents(dbHandle, eventHandle);
            cancelled = true;
        }

        public synchronized void eventOccurred() {
            if (!cancelled){
                try {
                    gds.iscEventCounts(eventHandle);
                } catch (GDSException e1){
                    e1.printStackTrace();
                }

                if (initialized && !cancelled){

                    DatabaseEvent event = new DatabaseEventImpl(
                        eventHandle.getEventName(),
                        eventHandle.getEventCount());

                    synchronized (eventQueue){
                        eventQueue.add(event);
                        eventQueue.notify();
                    }
                } else {
                    initialized = true;
                }
                 
                try {
                    register();
                } catch (GDSException e2){
                    e2.printStackTrace();
                }
            }         
        }

    }


    class EventDispatcher implements Runnable {
        
        private volatile boolean running = false;

        public void stop(){
            running = false;
        }

        public void run(){
            running = true;
            List events = new ArrayList(); 
            while (running){
                synchronized (eventQueue){
                    while (eventQueue.isEmpty() && running){
                        try {
                            eventQueue.wait(waitTimeout);
                        } catch (InterruptedException ie){ }
                    }
                    events.addAll(eventQueue);
                    eventQueue.clear();
                }
                
                for (Iterator eItr = events.iterator(); eItr.hasNext();){
                    DatabaseEvent event = (DatabaseEvent)eItr.next();
                    Set listenerSet = null;
                    synchronized (listenerMap){
                        listenerSet = 
                            (Set)listenerMap.get(event.getEventName());
                        if (listenerSet != null){
                            Iterator listenerItr = listenerSet.iterator();
                            while (listenerItr.hasNext()){
                                EventListener listener = 
                                    (EventListener)listenerItr.next();
                                listener.eventOccurred(event);
                            }
                        }
                    }
                }
                events.clear();
            }
        }
    }

}


class OneTimeEventListener implements EventListener {

    private int eventCount = -1;

    private Object lock; 

    public OneTimeEventListener(Object lock){
        this.lock = lock;
    }

    public  void eventOccurred(DatabaseEvent event){
        if (eventCount == -1){
            eventCount = event.getEventCount();
        }
        synchronized (lock){
            lock.notify();
        }
    }

    public int getEventCount(){
        return eventCount;
    }
}




class DatabaseEventImpl implements DatabaseEvent {
    
    private int eventCount;

    private String eventName;

    public DatabaseEventImpl(String eventName, int eventCount){
        this.eventName = eventName;
        this.eventCount = eventCount;
    }

    public int getEventCount(){
        return this.eventCount;
    }

    public String getEventName(){
        return this.eventName;
    }

    public String toString(){
        return "DatabaseEvent['" + eventName + " * " + eventCount + "]";
    }
}
