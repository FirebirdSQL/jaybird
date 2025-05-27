// SPDX-FileCopyrightText: Copyright 2009 Thomas Steinmaurer
// SPDX-FileCopyrightText: Copyright 2011-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import java.sql.SQLException;

/**
 * Implements the Trace/Audit API available new in Firebird 2.5.
 * <p>
 * This functionality includes:
 * <ul>
 * <li>Starting a new trace session</li>
 * <li>Stopping an existing trace session</li>
 * <li>Suspending an existing trace session</li>
 * <li>Resuming a suspended trace session</li>
 * <li>Retrieving a list of trace sessions</li>
 * </ul>
 * </p>
 *
 * @author Thomas Steinmaurer
 */
public interface TraceManager extends ServiceManager {

    /**
     * Starts a trace session with an optional trace session name and configuration
     *
     * @param traceSessionName
     *         The trace session name (optional)
     * @param configuration
     *         The trace configuration. For an example, look into fbtrace.conf in the root directory of your Firebird
     *         installation
     */
    void startTraceSession(String traceSessionName, String configuration) throws SQLException;

    /**
     * Stops a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     */
    void stopTraceSession(int traceSessionId) throws SQLException;

    /**
     * Suspends a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     */
    void suspendTraceSession(int traceSessionId) throws SQLException;

    /**
     * Resumes a trace session with the given trace session ID
     *
     * @param traceSessionId
     *         The trace session ID
     */
    void resumeTraceSession(int traceSessionId) throws SQLException;

    /**
     * List all currently registered trace sessions
     */
    void listTraceSessions() throws SQLException;

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
     * @return ID of the session or null otherwise
     */
    Integer getSessionId(String sessionName);

}
