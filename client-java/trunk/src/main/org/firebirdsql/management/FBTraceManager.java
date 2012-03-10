/*
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import java.sql.SQLException;
import java.io.*;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Implements the Trace/Audit API available new in Firebird 2.5
 * 
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public class FBTraceManager extends FBServiceManager implements TraceManager {
	
    private class TraceTask implements Runnable {
    	
    	private ServiceRequestBuffer srb;
    	
    	public TraceTask(ServiceRequestBuffer srb) {
    		this.srb = srb;
    	}
    	
    	public void run() {
    		try {
    			System.out.println("Start trace");
    			executeServicesOperation(srb);
    			System.out.println("Trace stopped");
    		} catch (FBSQLException e) {
    			
    		}
    	}
    }
	
	/**
     * Create a new instance of <code>FBTraceManager</code> based on
     * the default GDSType.
     */
    public FBTraceManager() {
    }

    /**
     * Create a new instance of <code>FBTraceManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBTraceManager(String gdsType) {
    	super(gdsType);
    }

    /**
     * Create a new instance of <code>FBTraceManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBTraceManager(GDSType gdsType) {
    	super(gdsType);
    }
    
    /**
     * Creates and returns the "trace" service request buffer for the Service
     * Manager.
     * 
     * @param action The isc_action_svc_trace_* action to be used
     * @return the "trace" service request buffer for the Service Manager.
     * @throws SQLException
     */
	private ServiceRequestBuffer getTraceSPB(int action) throws SQLException {
    	ServiceRequestBuffer traceSPB = getGds().createServiceRequestBuffer(action);
        return traceSPB;
    }

	/**
     * Creates and returns the "trace" service request buffer for the Service
     * Manager.
     * 
	 * @param action The isc_action_svc_trace_* action to be used
	 * @param traceSessionId The trace session ID
	 * @return the "trace" service request buffer for the Service Manager.
	 * @throws SQLException
	 */
	private ServiceRequestBuffer getTraceSPB(int action, int traceSessionId) throws SQLException {
		ServiceRequestBuffer traceSPB = getTraceSPB(action);
	    traceSPB.addArgument(ISCConstants.isc_spb_trc_id, traceSessionId);
	    return traceSPB;
	}

	/**
     * Creates and returns the "trace" service request buffer for the Service
     * Manager.
     * 
	 * @param action The isc_action_svc_trace_* action to be used
	 * @param traceSessionName The trace session name
	 * @param configuration The trace configuration. For an example, look into fbtrace.conf in the root directory of your Firebird installation
	 * @return the "trace" service request buffer for the Service Manager.
	 * @throws SQLException
	 */
	private ServiceRequestBuffer getTraceSPB(int action, String traceSessionName, String configuration) throws SQLException {
		ServiceRequestBuffer traceSPB = getTraceSPB(action);
	    traceSPB.addArgument(ISCConstants.isc_spb_trc_name, traceSessionName);
	    traceSPB.addArgument(ISCConstants.isc_spb_trc_cfg, configuration);
	    return traceSPB;
	}


    /**
     * Starts a trace session with an optioanl trace session name and configuration
     *
     * @param traceSessionName The trace session name (optional)
     * @param configuration The trace configuration. For an example, look into fbtrace.conf in the root directory of your Firebird installation
	 * @throws SQLException
     */
	public void startTraceSession(String traceSessionName, String configuration) throws SQLException {
    	
    	if ((configuration == null) || (configuration.equals(""))) {
    		throw new FBSQLException("No configuration provided");
    	}
    	if (traceSessionName == null) {
    		traceSessionName = "";
    	}
    	
    	Thread t = new Thread(new TraceTask(getTraceSPB(ISCConstants.isc_action_svc_trace_start, traceSessionName, configuration)));
    	t.start();
    }

    /**
     * Stops a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
	public void stopTraceSession(int traceSessionId) throws SQLException {
    	executeServicesOperation(getTraceSPB(ISCConstants.isc_action_svc_trace_stop, traceSessionId));
    }
    
    /**
     * Suspends a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
    public void suspendTraceSession(int traceSessionId) throws SQLException {
    	executeServicesOperation(getTraceSPB(ISCConstants.isc_action_svc_trace_suspend, traceSessionId));
    }
    
    /**
     * Resumes a trace session with the given trace session ID
     *
     * @param traceSessionId The trace session ID
	 * @throws SQLException
     */
    public void resumeTraceSession(int traceSessionId) throws SQLException {
    	executeServicesOperation(getTraceSPB(ISCConstants.isc_action_svc_trace_resume, traceSessionId));
    }
    
    
    /**
     * List all currently registered trace sessions
     * 
	 * @throws SQLException
     */
	public void listTraceSessions() throws SQLException {
		executeServicesOperation(getTraceSPB(ISCConstants.isc_action_svc_trace_list));
	}
	
	/**
	 * Loads a configuration from the specified fileName
	 *
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public String loadConfigurationFromFile(String fileName) throws FileNotFoundException, IOException {
		
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		StringBuffer sb = new StringBuffer(256);
		String s;
		try {
			while ((s = br.readLine()) != null) {
				sb.append(s);
			}
		} finally {
			br.close();
			fr.close();
		}
		
		return sb.toString();
	}
	
}
