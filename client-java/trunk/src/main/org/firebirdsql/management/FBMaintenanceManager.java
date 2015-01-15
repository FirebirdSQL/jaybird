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
 * <p>
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
 * </p>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public class FBMaintenanceManager extends FBServiceManager implements MaintenanceManager {

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * the default GDSType.
     */
    public FBMaintenanceManager() {
        super();
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     * 
     * @param gdsType
     *            type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBMaintenanceManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *            The GDS implementation type to use
     */
    public FBMaintenanceManager(GDSType gdsType) {
        super(gdsType);
    }

    public void setDatabaseAccessMode(int mode) throws SQLException {
        if (mode != ACCESS_MODE_READ_WRITE && mode != ACCESS_MODE_READ_ONLY) {
            throw new IllegalArgumentException("mode must be one of "
                    + "ACCESS_MODE_READ_WRITE or ACCESS_MODE_READ_ONLY");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_access_mode, (byte) mode);
        executeServicesOperation(srb);
    }

    public void setDatabaseDialect(int dialect) throws SQLException {
        if (dialect != 1 && dialect != 3) {
            throw new IllegalArgumentException("dialect must be either 1 or 3");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_set_sql_dialect, dialect);
        executeServicesOperation(srb);
    }

    public void setDefaultCacheBuffer(int pageCount) throws SQLException {
        // TODO: Set to 0 valid as well?
        if (pageCount < 1) {
            throw new IllegalArgumentException("page count must be positive");
        }
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_page_buffers, pageCount);
        executeServicesOperation(srb);
    }

    public void setForcedWrites(boolean forced) throws SQLException {
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_write_mode, (byte) (forced ? ISCConstants.isc_spb_prp_wm_sync
                : ISCConstants.isc_spb_prp_wm_async));
        executeServicesOperation(srb);
    }

    public void setPageFill(int pageFill) throws SQLException {
        if (pageFill != PAGE_FILL_FULL && pageFill != PAGE_FILL_RESERVE) {
            throw new IllegalArgumentException("Page fill must be either PAGE_FILL_FULL or PAGE_FILL_RESERVE");
        }
        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_reserve_space, (byte) pageFill);

        executeServicesOperation(srb);
    }

    // ----------- Database Shutdown -------------------

    public void shutdownDatabase(int shutdownMode, int timeout) throws SQLException {
        if (shutdownMode != SHUTDOWN_ATTACH
                && shutdownMode != SHUTDOWN_TRANSACTIONAL
                && shutdownMode != SHUTDOWN_FORCE) {
            throw new IllegalArgumentException("Shutdown mode must be "
                    + "one of: SHUTDOWN_ATTACH, SHUTDOWN_TRANSACTIONAL, SHUTDOWN_FORCE");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be >= 0");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(shutdownMode, timeout);
        executeServicesOperation(srb);
    }

    public void shutdownDatabase(byte operationMode, int shutdownModeEx, int timeout) throws SQLException {
        if (operationMode != OPERATION_MODE_MULTI 
                && operationMode != OPERATION_MODE_SINGLE
                && operationMode != OPERATION_MODE_FULL_SHUTDOWN) {
            throw new IllegalArgumentException("Operation mode must be one of: OPERATION_MODE_MULTI, "
                    + "OPERATION_MODE_SINGLE, OPERATION_MODE_FULL_SHUTDOWN");
        }
        if (shutdownModeEx != SHUTDOWNEX_FORCE 
                && shutdownModeEx != SHUTDOWNEX_ATTACHMENTS
                && shutdownModeEx != SHUTDOWNEX_TRANSACTIONS) {
            throw new IllegalArgumentException("Extended shutdown mode must be "
                    + "one of: SHUTDOWNEX_FORCE, SHUTDOWNEX_ATTACHMENTS, SHUTDOWNEX_TRANSACTIONS");
        }
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be >= 0");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_shutdown_mode, operationMode);
        srb.addArgument(shutdownModeEx, timeout);
        executeServicesOperation(srb);
    }

    public void bringDatabaseOnline() throws SQLException {
        executePropertiesOperation(ISCConstants.isc_spb_prp_db_online);
    }

    public void bringDatabaseOnline(byte operationMode) throws SQLException {
        if (operationMode != OPERATION_MODE_NORMAL 
                && operationMode != OPERATION_MODE_MULTI
                && operationMode != OPERATION_MODE_SINGLE) {
            throw new IllegalArgumentException("Operation mode must be "
                    + "one of: OPERATION_MODE_NORMAL, OPERATION_MODE_MULTI, OPERATION_MODE_SINGLE");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_online_mode, operationMode);
        executeServicesOperation(srb);
    }

    // -------------- Database Repair ----------------------

    public void markCorruptRecords() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_mend_db);
    }

    public void validateDatabase() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_validate_db);
    }

    public void validateDatabase(int options) throws SQLException {

        if (options < 0 
                || options != 0 && options != VALIDATE_IGNORE_CHECKSUM
                && (options & ~VALIDATE_IGNORE_CHECKSUM) != VALIDATE_READ_ONLY
                && (options & ~VALIDATE_IGNORE_CHECKSUM) != VALIDATE_FULL
                && (options | (VALIDATE_READ_ONLY | VALIDATE_IGNORE_CHECKSUM)) != options
                && (options | (VALIDATE_FULL | VALIDATE_IGNORE_CHECKSUM)) != options) {
            throw new IllegalArgumentException("options must be either 0, "
                    + "VALIDATE_READ_ONLY, or VALIDATE_FULL, optionally combined with VALIDATE_IGNORE_CHECKSUM");
        }

        ServiceRequestBuffer srb = createRepairSRB(options | ISCConstants.isc_spb_rpr_validate_db);
        executeServicesOperation(srb);
    }

    // ----------- Sweeping -------------------------

    public void setSweepThreshold(int transactions) throws SQLException {
        if (transactions < 0) {
            throw new IllegalArgumentException("transactions must be >= 0");
        }

        ServiceRequestBuffer srb = createDefaultPropertiesSRB();
        srb.addArgument(ISCConstants.isc_spb_prp_sweep_interval, transactions);
        executeServicesOperation(srb);
    }

    public void sweepDatabase() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_sweep_db);
    }

    // ----------- Shadow Files ------------------------------------

    public void activateShadowFile() throws SQLException {
        executePropertiesOperation(ISCConstants.isc_spb_prp_activate);
    }

    public void killUnavailableShadows() throws SQLException {
        executeRepairOperation(ISCConstants.isc_spb_rpr_kill_shadows);
    }

    // ----------- Transaction Management ----------------------------

    @Deprecated
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
                    result.add(trId);
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
            trans[idx++] = trId;
        }
        return trans;
    }

    public void commitTransaction(int transactionId) throws SQLException {
        ServiceRequestBuffer srb = createDefaultRepairSRB();
        srb.addArgument(ISCConstants.isc_spb_rpr_commit_trans, transactionId);
        executeServicesOperation(srb);
    }

    public void rollbackTransaction(int transactionId) throws SQLException {
        ServiceRequestBuffer srb = createDefaultRepairSRB();
        srb.addArgument(ISCConstants.isc_spb_rpr_rollback_trans, transactionId);
        executeServicesOperation(srb);
    }

    // ----------- Private implementation methods --------------------

    /**
     * Execute a isc_spb_rpr_* (repair) services operation.
     *
     * @param operation
     *            The identifier for the operation to be executed
     * @throws SQLException
     *             if a database access error occurs
     */
    private void executeRepairOperation(int operation) throws SQLException {
        ServiceRequestBuffer srb = createRepairSRB(operation);
        executeServicesOperation(srb);
    }

    /**
     * Execute a isc_spb_prp_* (properties) services operation.
     *
     * @param operation
     *            The identifier for the operation to be executed
     * @throws SQLException
     *             if a database access error occurs
     */
    private void executePropertiesOperation(int operation) throws SQLException {
        ServiceRequestBuffer srb = createPropertiesSRB(operation);
        executeServicesOperation(srb);
    }

    /**
     * Get a mostly empty properties-operation buffer that can be filled in as 
     * needed. The buffer created by this method cannot have the options 
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultPropertiesSRB() {
        return createPropertiesSRB(0);
    }

    /**
     * Get a mostly empty repair-operation buffer that can be filled in as 
     * needed. The buffer created by this method cannot have the options 
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultRepairSRB() {
        return createRepairSRB(0);
    }

    /**
     * Get a mostly-empty properties-operation request buffer that can be 
     * filled as needed.
     *
     * @param options
     *            The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createPropertiesSRB(int options) {
        return createRequestBuffer(ISCConstants.isc_action_svc_properties, options);
    }

    /**
     * Get a mostly-empty repair-operation request buffer that can be
     * filled as needed.
     *
     * @param options
     *            The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createRepairSRB(int options) {
        return createRequestBuffer(ISCConstants.isc_action_svc_repair, options);
    }
}
