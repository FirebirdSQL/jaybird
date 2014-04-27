/*
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

package org.firebirdsql.management;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Implements the Trace/Audit API availabe new in Firebird 2.5. 
 * This functionality includes:
 * <ul>
 *      <li>Starting a new trace session
 *      <li>Stopping an existing trace session
 *      <li>Suspending an existing trace session
 *      <li>Resuming a suspended trace session
 *      <li>Retrieving a list of trace sessions
 * </ul>
 *
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public interface TraceManager extends ServiceManager {

    /**
     * Starts a trace session with an optioanl trace session name and configuration
     *
     * @param traceSessionName The trace session name (optional)
     * @param configuration The trace configuration. For an example, look into fbtrace.conf in the root directory of your Firebird installation
	 * @throws SQLException
     */
    void startTraceSession(String traceSessionName, String configuration) throws SQLException;

    /**
     * Stops a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
    void stopTraceSession(int traceSessionId) throws SQLException;
    
    /**
     * Suspends a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
    void suspendTraceSession(int traceSessionId) throws SQLException;
    
    /**
     * Resumes a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
    void resumeTraceSession(int traceSessionId) throws SQLException;
    
    /**
     * List all currently registered trace sessions
     * 
	 * @throws SQLException
     */
    void listTraceSessions() throws SQLException;
    
	/**
	 * Loads a configuration from the specified fileName
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    String loadConfigurationFromFile(String fileName) throws FileNotFoundException, IOException;
    
    /**
     * Gets the sessionId for the given name.
     * <p>
     * Returns null if the sessionName does not exist or hasn't been initialized yet.
     * </p>
     * <p>
     * If multiple sessions are started with the same name, the last one is returned.
     * </p>
     * 
     * @param sessionName Name of the session
     * @return Id of the session or null otherwise
     */
    Integer getSessionId(String sessionName);
    
}
