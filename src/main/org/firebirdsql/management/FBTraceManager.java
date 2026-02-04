// SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
// SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

/**
 * Implements the Trace/Audit API available new in Firebird 2.5
 *
 * @author Thomas Steinmaurer
 */
@NullMarked
public class FBTraceManager extends FBServiceManager implements TraceManager {

    private final Map<String, Integer> traceSessions = Collections.synchronizedMap(new HashMap<>());

    private class TraceTask implements Runnable {

        private final ServiceRequestBuffer srb;
        private final FbService service;

        public TraceTask(FbService service, ServiceRequestBuffer srb) {
            this.service = requireNonNull(service, "service");
            this.srb = requireNonNull(srb, "srb");
        }

        public void run() {
            try {
                try (FbService service = this.service) {
                    executeServicesOperation(service, srb);
                }
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    /**
     * Create a new instance of {@code FBTraceManager} based on the default GDSType.
     */
    public FBTraceManager() {
    }

    /**
     * Create a new instance of {@code FBTraceManager} based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    @SuppressWarnings("unused")
    public FBTraceManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of {@code FBTraceManager} based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    @SuppressWarnings("unused")
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
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action) {
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
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action, int traceSessionId) {
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
     */
    private ServiceRequestBuffer getTraceSPB(FbService service, int action, String traceSessionName,
            String configuration) {
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
     */
    public void startTraceSession(@Nullable String traceSessionName, String configuration) throws SQLException {
        if (isNullOrEmpty(configuration)) {
            throw new SQLException("No configuration provided");
        }
        traceSessionName = requireNonNullElse(traceSessionName, "");

        synchronized (this) {
            OutputStream currentLogger = getLogger();
            if (currentLogger instanceof TraceStream traceStream) {
                currentLogger = traceStream.unwrap();
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
    public @Nullable Integer getSessionId(String sessionName) {
        return traceSessions.get(sessionName);
    }

    private class TraceStream extends FilterOutputStream {
        private static final String START_TEXT = "Trace session ID ";

        private final String sessionName;
        private volatile boolean lookForSessionId = true;

        public TraceStream(@Nullable OutputStream out, String sessionName) {
            super(out);
            this.sessionName = sessionName;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (lookForSessionId) {
                findSessionId(b, off, len);
                lookForSessionId = false;
            }

            super.write(b, off, len);
        }

        /**
         * Tries to find the session ID if the sessionName is not empty.
         *
         * @param b
         *         byte array
         * @param off
         *         offset
         * @param len
         *         length
         */
        private void findSessionId(byte[] b, int off, int len) {
            if (sessionName.isEmpty()) return;
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
