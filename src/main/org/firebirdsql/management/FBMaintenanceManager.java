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
import java.util.LinkedList;
import java.util.List;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBSQLException;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;

/**
 * The <code>FBMaintenanceManager</code> class is responsible for replicating
 * the functionality provided by the <code>gfix</code> command-line tool.
 * Among the responsibilities of this class are:
 * <ul>
 *      <li>Database shutdown
 *      <li>Extended database shutdown/online modes new with Firebird 2.5
 *      <li>Changing database mode to read-only or read-write
 *      <li>Enabling or disabling forced writes in the database
 *      <li>Changing the dialect of the database
 *      <li>Setting the cache size at database-level
 *      <li>Mending databases and making minor repairs
 *      <li>Sweeping databases
 *      <li>Activating and killing shadow files
 *      <li>Displaying, committing, or recovering limbo transactions
 * </ul>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public class FBMaintenanceManager extends FBServiceManager 
                                implements MaintenanceManager {


    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * the default GDSType.
     */
    public FBMaintenanceManager()
    {
    	super();
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBMaintenanceManager(String gdsType)
    {
    	super(gdsType);
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType The GDS implementation type to use
     */
    public FBMaintenanceManager(GDSType gdsType){
        super(gdsType);
    }

    /**
     * Set the database to have read-write or read-only access.
     *
     * @param mode Must be either <code>ACCESS_MODE_READ_WRITE</code> 
     *        or <code>ACCESS_MODE_READ_ONLY</code>
     * @throws SQLException if a database access error occurs
     */
    public void setDatabaseAccessMode(int mode) throws SQLException {

        if (mode != ACCESS_MODE_READ_WRITE && mode != ACCESS_MODE_READ_ONLY){
            throw new IllegalArgumentException("mode must be one of "
                    + "ACCESS_MODE_READ_WRITE or ACCESS_MODE_READ_ONLY");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_access_mode, (byte)mode);
        executeServicesOperation(srb);
    }

    /**
     * Set the database's dialect.
     *
     * @param dialect The database dialect, must be either 1 or 3
     * @throws SQLException if a database access error occurs 
     */
    public void setDatabaseDialect(int dialect) throws SQLException {

        if (dialect != 1 && dialect != 3){
            throw new IllegalArgumentException("dialect must be either "
                    + "1 or 3");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_set_sql_dialect, dialect);
        executeServicesOperation(srb);
    }

    /**
     * Set the default page-buffer count to be cached in the database. The
     * Firebird default is 2048.
     *
     * @param pageCount The number of pages to be cached, must be positive 
     * @throws SQLException If the given page count cannot be set, or a
     *         database access error occurs
     */
    public void setDefaultCacheBuffer(int pageCount) throws SQLException {
        if (pageCount < 1){
            throw new IllegalArgumentException("page count must be positive");
        }
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_page_buffers, pageCount);
        executeServicesOperation(srb);
    }


    /**
     * Enable or disable forced (synchronous) writes in the database.
     * Note, it is considered to be a <b>very</b> bad idea to use buffered 
     * writing on Windows platforms.
     *
     * @param forced If <code>true</code>, forced writes will be used in the
     *        database, otherwise buffered writes will be used.
     * @throws SQLException if a database access error occurs
     */
    public void setForcedWrites(boolean forced) throws SQLException {
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_write_mode,
                (byte) (forced ?  ISCConstants.isc_spb_prp_wm_sync 
                    : ISCConstants.isc_spb_prp_wm_async));
        executeServicesOperation(srb);
    }

 
    /**
     * Set the page fill strategy for when inserting records.
     * <code>pageFill</code> can be one of:
     * <ul>
     *      <li><code>PAGE_FILL_FULL</code> Fully fill database pages
     *      <li><code>PAGE_FILL_RESERVE</code> Reserve 20% of page space for
     *      later record deltas
     * </ul>
     *
     * @param pageFill The page-filling strategy, either 
     *        <code>PAGE_FILL_FULL</code> or <code>PAGE_FILL_RESERVE</code>
     * @throws SQLException if a database access error occurs
     */
    public void setPageFill(int pageFill) throws SQLException {
        if (pageFill != PAGE_FILL_FULL && pageFill != PAGE_FILL_RESERVE){
            throw new IllegalArgumentException( "Page fill must be either "
                    + "PAGE_FILL_FULL or PAGE_FILL_RESERVE");
        }
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_reserve_space, 
                        (byte)pageFill);

        executeServicesOperation(srb);
    }


    //----------- Database Shutdown -------------------

    /**
     * Shutdown the current database.
     * Shutdown can be done in three modes:
     * <ul>
     *      <li><code>SHUTDOWN_ATTACH</code> - No new non-owner connections 
     *      will be allowed to the database during the shutdown, and shutdown
     *      is cancelled if there are still processes connected at the end
     *      of the timeout.
     *
     *      <li><code>SHUTDOWN_TRANSACTIONAL</code> - No new transactions can 
     *      be started during the timeout period, and shutdown is cancelled
     *      if there are still active transactions at the end of the timeout.
     *
     *      <li><code>SHUTDOWN_FORCE</code> - Forcefully shuts down the 
     *      database at the end of the timeout.
     * </ul>
     *
     * @param shutdownMode One of <code>SHUTDOWN_ATTACH</code>, 
     *        <code>SHUTDOWN_TRANSACTIONAL</code>, 
     *        or <code>SHUTDOWN_FORCE</code>.
     * @param timeout The maximum amount of time allocated for the operation,
     *        in seconds
     * @throws SQLException if the requested operation cannot be completed 
     *         within the given timeout, or a database access error occurs
     */
    public void shutdownDatabase(int shutdownMode, int timeout) 
            throws SQLException {
        
        if (shutdownMode != SHUTDOWN_ATTACH
                && shutdownMode != SHUTDOWN_TRANSACTIONAL
                && shutdownMode != SHUTDOWN_FORCE){
            throw new IllegalArgumentException("Shutdown mode must be "
                    + "one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, " 
                    + "SHUTDOWN_FORCE");
        }
        if (timeout < 0){
            throw new IllegalArgumentException(
                    "Timeout must be >= 0");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(shutdownMode, timeout);
        executeServicesOperation(srb);
    }

    
    public void shutdownDatabase(byte operationMode, int shutdownModeEx, int timeout) throws SQLException {

    	if (
    			operationMode != OPERATION_MODE_MULTI 
    			&& operationMode != OPERATION_MODE_SINGLE 
    			&& operationMode != OPERATION_MODE_FULL_SHUTDOWN 
    		) {
    	    throw new IllegalArgumentException("Operation mode must be "
    	            + "one of: OPERATION_MODE_MULTI, " 
    	            + "OPERATION_MODE_SINGLE, OPERATION_MODE_FULL_SHUTDOWN");
    	}
    	if (
    			shutdownModeEx != SHUTDOWNEX_FORCE
    			&& shutdownModeEx != SHUTDOWNEX_ATTACHMENTS 
    			&& shutdownModeEx != SHUTDOWNEX_TRANSACTIONS 
    		) {
    	    throw new IllegalArgumentException("Extended shutdown mode must be "
    	            + "one of: SHUTDOWNEX_FORCE, SHUTDOWNEX_ATTACHMENTS, " 
    	            + "SHUTDOWNEX_TRANSACTIONS");
    	}
    	if (timeout < 0) {
    		throw new IllegalArgumentException("Timeout must be >= 0");
    	}

		ServiceRequestBuffer srb = createDefaultPropertiesSRB();
		srb.addArgument(ISCConstants.isc_spb_prp_shutdown_mode, operationMode);
		srb.addArgument(shutdownModeEx, timeout);
		executeServicesOperation(srb);
    }


    /**
     * Bring a shutdown database online.
     *
     * @throws SQLException if a database access error occurs
     */
    public void bringDatabaseOnline() throws SQLException {
        executePropertiesOperation(ISCConstants.isc_spb_prp_db_online);
    }

    /**
     * Bring a shutdown database online with enhanced operation modes
     * new since Firebird 2.5.
     *
     * @throws SQLException if a database access error occurs
     */
    public void bringDatabaseOnline(byte operationMode) throws SQLException {

    	if (
    			operationMode != OPERATION_MODE_NORMAL
    			&& operationMode != OPERATION_MODE_MULTI 
    			&& operationMode != OPERATION_MODE_SINGLE 
    		) {
    	    throw new IllegalArgumentException("Operation mode must be "
    	            + "one of: OPERATION_MODE_NORMAL, OPERATION_MODE_MULTI, " 
    	            + "OPERATION_MODE_SINGLE");
    	}

		ServiceRequestBuffer srb = createDefaultPropertiesSRB();
		srb.addArgument(ISCConstants.isc_spb_prp_online_mode, operationMode);
		executeServicesOperation(srb);
    }

    //-------------- Database Repair ----------------------

    /**
     * Mark corrupt records in the database as unavailable.
     * This operation ensures that the corrupt records are skipped (for 
     * example, during a subsequent backup). This method is the equivalent
     * of <b><code>gfix -mend</code></b>.
     *
     * @throws SQLException if a database access error occurs
     */
    public void markCorruptRecords() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_mend_db);
    }

    /**
     * Locate and release database pages that are allocated but unassigned
     * to any data structures. This method also reports corrupt structures.
     *
     * @throws SQLException if a database access error occurs
     */
    public void validateDatabase() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_validate_db);
    }

    /**
     * Locate and release database pages that are allocated but unassigned
     * to any data structures. This method also reports corrupt structures.
     * The value supplied for <code>options</code> must be one of the
     * following:
     * <ul>
     *      <li>0 - Simple validation
     *      <li><code>VALIDATE_READ_ONLY</code> - read-only validation, 
     *      no repair
     *      <li><code>VALIDATE_FULL</code> - full validation and repair
     * </ul>
     *
     * The value for <code>options</code> can additionally be combined in
     * a bitmask with <code>VALIDATE_IGNORE_CHECKSUM</code> to ignore
     * checksums while performing validation.
     *
     * @param options Either 0, <code>VALIDATE_READ_ONLY</code>, or
     *        <code>VALIDATE_FULL</code>
     * @throws SQLException if a database access error occurs
     */
    public void validateDatabase(int options) throws SQLException {
        
        if (options < 0 
                || options != 0 && options != VALIDATE_IGNORE_CHECKSUM
                && (options & ~VALIDATE_IGNORE_CHECKSUM) != VALIDATE_READ_ONLY
                && (options & ~VALIDATE_IGNORE_CHECKSUM) != VALIDATE_FULL
                && (options | 
                    (VALIDATE_READ_ONLY | VALIDATE_IGNORE_CHECKSUM)) != options
                && (options |
                    (VALIDATE_FULL | VALIDATE_IGNORE_CHECKSUM)) != options) {
            throw new IllegalArgumentException("options must be either 0, " 
                    + "VALIDATE_READ_ONLY, or VALIDATE_FULL, optionally "
                    + "combined with VALIDATE_IGNORE_CHECKSUM");
        }

        ServiceRequestBuffer srb = createRepairSRB( 
                options | ISCConstants.isc_spb_rpr_validate_db);
        executeServicesOperation(srb);
    }

   

    //----------- Sweeping -------------------------
   
    /**
     * Set the database automatic sweep interval to a given number of 
     * transactions. The Firebird default value is 20,000. If 
     * <code>transactions</code> is 0, automatic sweeping is disabled.
     *
     * @param transactions The interval of transactions between automatic
     *        sweeps of the database. Can be set to 0, which disables
     *        automatic sweeping of the database.
     * @throws SQLException if a database access error occurs
     */
    public void setSweepThreshold(int transactions) throws SQLException {
        if (transactions < 0){
            throw new IllegalArgumentException("transactions must be >= 0");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_sweep_interval, transactions);
        executeServicesOperation(srb);
    }

    /**
     * Perform an immediate sweep of the database.
     *
     * @throws SQLException if a database access error occurs
     */
    public void sweepDatabase() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_sweep_db);
    }


    //----------- Shadow Files ------------------------------------
   
    /**
     * Activate a database shadow file to be used as the actual database.
     * This method is the equivalent of <b><code>gfix -activate</code></b>.
     *
     * @throws SQLException if a database access error occurs
     */
    public void activateShadowFile() throws SQLException {
        executePropertiesOperation(ISCConstants.isc_spb_prp_activate);
    }

    /**
     * Remove references to unavailable shadow files. This method is the
     * equivalent of <b><code>gfix -kill</code></b>.
     *
     * @throws SQLException if a database access error occurs
     */
    public void killUnavailableShadows() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_kill_shadows);
    }

    //----------- Transaction Management ----------------------------
   
    /**
     * Retrieve the ID of each limbo transaction. The output of this  method 
     * is written to the logger.
     *
     * @throws SQLException if a database access error occurs
     */
    public void listLimboTransactions() throws SQLException {
        PrintStream ps = new PrintStream(getLogger());
        for (Integer trId : limboTransactionsAsList()) {
            ps.print(trId + "\n");
        }
    }
    
    public List<Integer> limboTransactionsAsList() throws SQLException {
        // See also fbscvmgr.cpp method printInfo
        OutputStream saveOut = getLogger();
        try {
            List<Integer> result = new LinkedList<Integer>();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            setLogger(out);
            executeRepairOperation(ISCConstants.isc_spb_rpr_list_limbo_trans);
            byte output[] = out.toByteArray();

            int idx = 0;
            while (idx < output.length) {
                switch (output[idx++]) {
                case ISCConstants.isc_spb_single_tra_id:
                case ISCConstants.isc_spb_multi_tra_id:
                    int trId = getGds().iscVaxInteger(output, idx, 4);
                    idx += 4;
                    result.add(Integer.valueOf(trId));
                    break;
                // Information items we will ignore for now
                case ISCConstants.isc_spb_tra_id:
                    idx += 4;
                    break;
                case ISCConstants.isc_spb_tra_state:
                case ISCConstants.isc_spb_tra_advise:
                    idx++;
                    break;
                case ISCConstants.isc_spb_tra_host_site:
                case ISCConstants.isc_spb_tra_remote_site:
                case ISCConstants.isc_spb_tra_db_path:
                    int length = getGds().iscVaxInteger(output, idx, 2);
                    idx += 2;
                    idx += length;
                    break;
                default:
                    GDSException gdsException = new GDSException(ISCConstants.isc_arg_gds,
                            ISCConstants.isc_fbsvcmgr_info_err);
                    gdsException.setNext(new GDSException(ISCConstants.isc_arg_number, output[idx - 1] & 0xFF));
                    throw new FBSQLException(gdsException);
                }
            }
            return result;
        } finally {
            setLogger(saveOut);
        }
    }
    
    public int[] getLimboTransactions() throws SQLException {
        List<Integer> limboTransactions = limboTransactionsAsList();
        int[] trans = new int[limboTransactions.size()];
        int idx = 0;
        for (Integer trId : limboTransactions) {
            trans[idx++] = trId.intValue();
        }
        return trans;
    }

    /**
     * Commit a limbo transaction based on its ID.
     *
     * @param transactionId The ID of the limbo transaction to be committed
     * @throws SQLException if a database access error occurs or the 
     *         given transaction ID is not valid
     */
    public void commitTransaction(int transactionId) throws SQLException {
        ServiceRequestBuffer srb = createDefaultRepairSRB();
        srb.addArgument(ISCConstants.isc_spb_rpr_commit_trans, transactionId);
        executeServicesOperation(srb);
    }

    /**
     * Rollback a limbo transaction based on its ID.
     *
     * @param transactionId The ID of the limbo transaction to be rolled back
     * @throws SQLException if a database access error occurs or the
     *         given transaction ID is not valid
     */
    public void rollbackTransaction(int transactionId) throws SQLException {
        ServiceRequestBuffer srb = createDefaultRepairSRB();
        srb.addArgument(
                ISCConstants.isc_spb_rpr_rollback_trans, 
                transactionId);
        executeServicesOperation(srb);
    }


    //----------- Private imlementation methods --------------------
    
    /**
     * Execute a isc_spb_rpr_* (repair) services operation.
     *
     * @param operation The identifier for the operation to be executed
     * @throws SQLException if a database access error occurs
     */
    private void executeRepairOperation(int operation) throws SQLException {
        ServiceRequestBuffer srb = createRepairSRB(operation);
        executeServicesOperation(srb);
    }

    /**
     * Execute a isc_spb_prp_* (properties) services operation.
     *
     * @param operation The identifier for the operation to be executed
     * @throws SQLException if a database access error occurs
     */
    private void executePropertiesOperation(int operation) 
            throws SQLException {
        ServiceRequestBuffer srb = createPropertiesSRB(operation);
        executeServicesOperation(srb);
    }
                

    /**
     * Get a mostly empty properties-operation buffer that can be filled in as 
     * needed. The buffer created by this method cannot have the options 
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultPropertiesSRB(){
        return createPropertiesSRB(0);
    }

    /**
     * Get a mostly empty repair-operation buffer that can be filled in as 
     * needed. The buffer created by this method cannot have the options 
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultRepairSRB(){
        return createRepairSRB(0);
    }


    /**
     * Get a mostly-empty properties-operation request buffer that can be 
     * filled as needed.
     *
     * @param options The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createPropertiesSRB(int options){
        return createRequestBuffer(
                ISCConstants.isc_action_svc_properties, 
                options);
    }

    /**
     * Get a mostly-empty repair-operation request buffer that can be
     * filled as needed.
     *
     * @param options The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createRepairSRB(int options){
        return createRequestBuffer(ISCConstants.isc_action_svc_repair, options);
    }

}
