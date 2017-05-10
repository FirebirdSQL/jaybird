/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Implements the Trace/Audit API available new in Firebird 2.5
 *
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public class FBTraceManager extends FBServiceManager implements TraceManager {

    private Map<String, Integer> traceSessions = Collections.synchronizedMap(new HashMap<String, Integer>());

    private class TraceTask implements Runnable {

        private final ServiceRequestBuffer srb;
        private final FbService service;

        public TraceTask(FbService service, ServiceRequestBuffer srb) {
            this.srb = srb;
            this.service = service;
        }

        public void run() {
            try {
                System.out.println("Start trace");
                try (FbService service = this.service) {
                    executeServicesOperation(service, srb);
                }
                System.out.println("Trace stopped");
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Create a new instance of <code>FBTraceManager</code> based on the default GDSType.
     */
    public FBTraceManager() {
    }

    /**
     * Create a new instance of <code>FBTraceManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBTraceManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBTraceManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBTraceManager(GDSType gdsType) {
        super(gdsType);
    }

    /**
     * Creates and returns the "trace" service request buffer for the Service Manager.
     *
     * @param service Service handle
     * @param action
     *         The isc_action_svc_trace_* action to be used
     * @return the "trace" service request buffer for the Service Manager.
     * @throws SQLException
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action) throws SQLException {
        ServiceRequestBuffer traceSPB = service.createServiceRequestBuffer();
        traceSPB.addArgument(action);
        return traceSPB;
    }

    /**
     * Creates and returns the "trace" service request buffer for the Service
     * Manager.
     *
     * @param service Service handle
     * @param action
     *         The isc_action_svc_trace_* action to be used
     * @param traceSessionId
     *         The trace session ID
     * @return the "trace" service request buffer for the Service Manager.
     * @throws SQLException
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action, int traceSessionId) throws SQLException {
        ServiceRequestBuffer traceSPB = getTraceSPB(service, action);
        traceSPB.addArgument(isc_spb_trc_id, traceSessionId);
        return traceSPB;
    }

    /**
     * Creates and returns the "trace" service request buffer for the Service Manager.
     *
     * @param service Service handle
     * @param action
     *         The isc_action_svc_trace_* action to be used
     * @param traceSessionName
     *         The trace session name
     * @param configuration
     *         The trace configuration. For an example, look into fbtrace.conf in the root directory of your Firebird
     *         installation
     * @return the "trace" service request buffer for the Service Manager.
     * @throws SQLException
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action, String traceSessionName,
            String configuration) throws SQLException {
        ServiceRequestBuffer traceSPB = getTraceSPB(service, action);
        traceSPB.addArgument(isc_spb_trc_name, traceSessionName);
        traceSPB.addArgument(isc_spb_trc_cfg, configuration);
        return traceSPB;
    }

    /**
     * Starts a trace session with an optional trace session name and configuration
     *
     * @param traceSessionName
     *         The trace session name (optional)
     * @param configuration
     *         The trace configuration. For an example, look into fbtrace.conf in the root directory of your
     *         Firebird installation
     * @throws SQLException
     */
    public void startTraceSession(String traceSessionName, String configuration) throws SQLException {
        if (configuration == null || configuration.equals("")) {
            throw new SQLException("No configuration provided");
        }
        if (traceSessionName == null) {
            traceSessionName = "";
        }

        synchronized (this) {
            OutputStream currentLogger = getLogger();
            if (currentLogger instanceof TraceStream) {
                currentLogger = ((TraceStream) currentLogger).unwrap();
            }
            setLogger(new TraceStream(currentLogger, traceSessionName));

            FbService service = attachServiceManager();
            ServiceRequestBuffer traceSPB = getTraceSPB(service, isc_action_svc_trace_start,
                    traceSessionName, configuration);

            Thread t = new Thread(new TraceTask(service, traceSPB));
            t.start();
        }
    }

    /**
     * Stops a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     * @throws SQLException
     */
    public void stopTraceSession(int traceSessionId) throws SQLException {
        try (FbService service = attachServiceManager()) {
            service.startServiceAction(getTraceSPB(service, isc_action_svc_trace_stop, traceSessionId));
            queueService(service);
        }  catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    /**
     * Suspends a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     * @throws SQLException
     */
    public void suspendTraceSession(int traceSessionId) throws SQLException {
        try (FbService service = attachServiceManager()) {
            service.startServiceAction(getTraceSPB(service, isc_action_svc_trace_suspend, traceSessionId));
            queueService(service);
        }  catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    /**
     * Resumes a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     * @throws SQLException
     */
    public void resumeTraceSession(int traceSessionId) throws SQLException {
        try (FbService service = attachServiceManager()) {
            service.startServiceAction(getTraceSPB(service, isc_action_svc_trace_resume, traceSessionId));
            queueService(service);
        }  catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    /**
     * List all currently registered trace sessions
     *
     * @throws SQLException
     */
    public void listTraceSessions() throws SQLException {
        try (FbService service = attachServiceManager()) {
            service.startServiceAction(getTraceSPB(service, isc_action_svc_trace_list));
            queueService(service);
        }  catch (IOException ioe) {
            throw new SQLException(ioe);
        }
    }

    /**
     * Gets the sessionId for the given name.
     * <p>
     * Returns null if the sessionName does not exist or hasn't been initialized yet.
     * </p>
     * <p>
     * If multiple sessions are started with the same name, the last one is returned.
     * </p>
     *
     * @param sessionName
     *         Name of the session
     * @return Id of the session or null otherwise
     */
    public Integer getSessionId(String sessionName) {
        return traceSessions.get(sessionName);
    }

    @Override
    @Deprecated
    public String loadConfigurationFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)), Charset.defaultCharset());
    }

    private class TraceStream extends FilterOutputStream {
        private static final String START_TEXT = "Trace session ID ";

        private final String sessionName;
        private volatile boolean lookForSessionId = true;

        public TraceStream(OutputStream out, String sessionName) {
            super(out);
            this.sessionName = sessionName;
        }

        public void write(byte b[], int off, int len) throws IOException {
            if (lookForSessionId) {
                findSessionId(b, off, len);
                lookForSessionId = false;
            }

            super.write(b, off, len);
        }

        /**
         * Tries to find the session ID
         *
         * @param b Byte array
         * @param off Offset
         * @param len Length
         */
        private void findSessionId(byte[] b, int off, int len) {
            String sessionStart = new String(b, off, len);
            int traceStartIdx = sessionStart.indexOf(START_TEXT);
            int sessionIdStart = -1;
            int sessionIdEnd = -1;
            if (traceStartIdx >= 0) {
                sessionIdStart = traceStartIdx + START_TEXT.length();
                if (sessionIdStart < sessionStart.length()) {
                    sessionIdEnd = sessionStart.indexOf(' ', sessionIdStart);
                }
            }
            if (sessionIdStart >= 0 && sessionIdEnd > sessionIdStart && sessionIdEnd < sessionStart.length()) {
                try {
                    int sessionId = Integer.parseInt(sessionStart.substring(sessionIdStart, sessionIdEnd));
                    traceSessions.put(sessionName, sessionId);
                } catch (NumberFormatException ex) {
                    // ignore
                }
            }
        }

        public OutputStream unwrap() {
            return out;
        }
    }

}
