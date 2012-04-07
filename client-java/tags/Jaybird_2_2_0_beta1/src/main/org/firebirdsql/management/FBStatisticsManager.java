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

import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;

/**
 * The <code>FBStatisticsManager</code> class is responsible for replicating 
 * the functionality of the <code>gstat</code> command-line tool.
 * This functionality includes:
 * <ul>
 *      <li>Retrieving data table statistics
 *      <li>Retrieving the database header page
 *      <li>Retrieving index statistics
 *      <li>Retrieving database logging information
 *      <li>Retrieving statistics for the data dictionary
 * </ul>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 */
public class FBStatisticsManager extends FBServiceManager 
                                implements StatisticsManager {

    private static final int possibleStatistics = 
        DATA_TABLE_STATISTICS | SYSTEM_TABLE_STATISTICS | INDEX_STATISTICS |
        RECORD_VERSION_STATISTICS;

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * the default GDSType.
     */
    public FBStatisticsManager() {
    	super();
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBStatisticsManager(String gdsType) {
    	super(gdsType);
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType The GDS implementation type to use
     */
    public FBStatisticsManager(GDSType gdsType) {
        super(gdsType);
    }

    public void getHeaderPage() throws SQLException {
        ServiceRequestBuffer srb = createStatsSRB(
                ISCConstants.isc_spb_sts_hdr_pages);
        executeServicesOperation(srb);
    }

    public void getDatabaseStatistics() throws SQLException {
        ServiceRequestBuffer srb = createDefaultStatsSRB();
        executeServicesOperation(srb);
    }

    public void getDatabaseStatistics(int options) throws SQLException {
        if (options != 0 && (options | possibleStatistics) != possibleStatistics){
            throw new IllegalArgumentException("options must be 0 or a " 
                    + "combination of DATA_TABLE_STATISTICS, "
                    + "SYSTEM_TABLE_STATISTICS, INDEX_STATISTICS, or 0");
        }
        
        ServiceRequestBuffer srb = createStatsSRB(options);
        executeServicesOperation(srb);
    }

    public void getTableStatistics(String[] tableNames) throws SQLException {
        ServiceRequestBuffer srb = createStatsSRB(ISCConstants.isc_spb_sts_table);

        // create space-separated list of tables
        StringBuffer commandLine = new StringBuffer();
        for (int i = 0; i < tableNames.length; i++) {
            commandLine.append(tableNames[i]);
            if (i < tableNames.length - 1)
                commandLine.append(' ');
        }
        
        //FIXME should be isc_spb_command_line, but FB 2.0 does not like it
        srb.addArgument(ISCConstants.isc_spb_command_line, commandLine.toString());
        
        executeServicesOperation(srb);
    }


    //---------- Private implementation methods -----------------
    

    /**
     * Get a mostly empty buffer that can be filled in as needed. 
     * The buffer created by this method cannot have the options bitmask
     * set on it.
     */
    private ServiceRequestBuffer createDefaultStatsSRB(){
        return createStatsSRB(0);
    }

    /**
     * Get a mostly-empty repair-operation request buffer that can be
     * filled as needed.
     *
     * @param options The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createStatsSRB(int options){
        return createRequestBuffer(
                ISCConstants.isc_action_svc_db_stats, 
                options);
    }

}
