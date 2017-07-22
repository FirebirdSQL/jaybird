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
package org.firebirdsql.management;

import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.util.NumericHelper;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * The <code>FBMaintenanceManager</code> class is responsible for replicating the functionality provided by
 * the <code>gfix</code> command-line tool.
 * <p>
 * Among the responsibilities of this class are:
 * <ul>
 * <li>Database shutdown</li>
 * <li>Extended database shutdown/online modes new with Firebird 2.5</li>
 * <li>Changing database mode to read-only or read-write</li>
 * <li>Enabling or disabling forced writes in the database</li>
 * <li>Changing the dialect of the database</li>
 * <li>Setting the cache size at database-level</li>
 * <li>Mending databases and making minor repairs</li>
 * <li>Sweeping databases</li>
 * <li>Activating and killing shadow files</li>
 * <li>Displaying, committing, or recovering limbo transactions</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBMaintenanceManager extends FBServiceManager implements MaintenanceManager {

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on the default GDSType.
     */
    public FBMaintenanceManager() {
        super();
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBMaintenanceManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on a given GDSType.
     *
     * @param gdsType
     *         The GDS implementation type to use
     */
    public FBMaintenanceManager(GDSType gdsType) {
        super(gdsType);
    }

    public void setDatabaseAccessMode(int mode) throws SQLException {
        if (mode != ACCESS_MODE_READ_WRITE && mode != ACCESS_MODE_READ_ONLY) {
            throw new IllegalArgumentException("mode must be one of ACCESS_MODE_READ_WRITE or ACCESS_MODE_READ_ONLY");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_access_mode, (byte) mode);
            executeServicesOperation(service, srb);
        }
    }

    public void setDatabaseDialect(int dialect) throws SQLException {
        if (dialect != 1 && dialect != 3) {
            throw new IllegalArgumentException("dialect must be either 1 or 3");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_set_sql_dialect, dialect);
            executeServicesOperation(service, srb);
        }
    }

    public void setDefaultCacheBuffer(int pageCount) throws SQLException {
        if (pageCount != 0 && pageCount < 50) {
            throw new IllegalArgumentException("page count must be 0 or >= 50, value was: " + pageCount);
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_page_buffers, pageCount);
            executeServicesOperation(service, srb);
        }
    }

    public void setForcedWrites(boolean forced) throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_write_mode, (byte) (forced ? isc_spb_prp_wm_sync : isc_spb_prp_wm_async));
            executeServicesOperation(service, srb);
        }
    }

    public void setPageFill(int pageFill) throws SQLException {
        if (pageFill != PAGE_FILL_FULL && pageFill != PAGE_FILL_RESERVE) {
            throw new IllegalArgumentException("Page fill must be either PAGE_FILL_FULL or PAGE_FILL_RESERVE");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_reserve_space, (byte) pageFill);
            executeServicesOperation(service, srb);
        }
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

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(shutdownMode, timeout);
            executeServicesOperation(service, srb);
        }
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

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_shutdown_mode, operationMode);
            srb.addArgument(shutdownModeEx, timeout);
            executeServicesOperation(service, srb);
        }
    }

    public void bringDatabaseOnline() throws SQLException {
        executePropertiesOperation(isc_spb_prp_db_online);
    }

    public void bringDatabaseOnline(byte operationMode) throws SQLException {
        if (operationMode != OPERATION_MODE_NORMAL
                && operationMode != OPERATION_MODE_MULTI
                && operationMode != OPERATION_MODE_SINGLE) {
            throw new IllegalArgumentException("Operation mode must be "
                    + "one of: OPERATION_MODE_NORMAL, OPERATION_MODE_MULTI, OPERATION_MODE_SINGLE");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_online_mode, operationMode);
            executeServicesOperation(service, srb);
        }
    }

    // -------------- Database Repair ----------------------

    public void markCorruptRecords() throws SQLException {
        executeRepairOperation(isc_spb_rpr_mend_db);
    }

    public void validateDatabase() throws SQLException {
        executeRepairOperation(isc_spb_rpr_validate_db);
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

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createRepairSRB(service, options | isc_spb_rpr_validate_db);
            executeServicesOperation(service, srb);
        }
    }

    // ----------- Sweeping -------------------------

    public void setSweepThreshold(int transactions) throws SQLException {
        if (transactions < 0) {
            throw new IllegalArgumentException("transactions must be >= 0");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultPropertiesSRB(service);
            srb.addArgument(isc_spb_prp_sweep_interval, transactions);
            executeServicesOperation(service, srb);
        }
    }

    public void sweepDatabase() throws SQLException {
        executeRepairOperation(isc_spb_rpr_sweep_db);
    }

    // ----------- Shadow Files ------------------------------------

    public void activateShadowFile() throws SQLException {
        executePropertiesOperation(isc_spb_prp_activate);
    }

    public void killUnavailableShadows() throws SQLException {
        executeRepairOperation(isc_spb_rpr_kill_shadows);
    }

    // ----------- Transaction Management ----------------------------

    @Deprecated
    public void listLimboTransactions() throws SQLException {
        final PrintStream ps = new PrintStream(getLogger());
        for (Long trId : limboTransactionsAsList()) {
            ps.print(trId + "\n");
        }
    }

    public List<Long> limboTransactionsAsList() throws SQLException {
        // See also fbscvmgr.cpp method printInfo
        final OutputStream saveOut = getLogger();
        final List<Long> result = new ArrayList<>();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] output;
        try {
            setLogger(out);
            executeRepairOperation(isc_spb_rpr_list_limbo_trans);
            output = out.toByteArray();
        } finally {
            setLogger(saveOut);
        }

        int idx = 0;
        while (idx < output.length) {
            switch (output[idx++]) {
            case isc_spb_single_tra_id:
            case isc_spb_multi_tra_id: {
                long trId = iscVaxLong(output, idx, 4);
                idx += 4;
                result.add(trId);
                break;
            }
            case isc_spb_single_tra_id_64:
            case isc_spb_multi_tra_id_64: {
                long trId = iscVaxLong(output, idx, 8);
                idx += 8;
                result.add(trId);
                break;
            }
            // Information items we will ignore for now
            case isc_spb_tra_id:
                idx += 4;
                break;
            case isc_spb_tra_id_64:
                idx += 8;
                break;
            case isc_spb_tra_state:
            case isc_spb_tra_advise:
                idx++;
                break;
            case isc_spb_tra_host_site:
            case isc_spb_tra_remote_site:
            case isc_spb_tra_db_path:
                int length = iscVaxInteger2(output, idx);
                idx += 2;
                idx += length;
                break;
            default:
                throw new FbExceptionBuilder()
                        .exception(isc_fbsvcmgr_info_err)
                        .messageParameter(output[idx - 1] & 0xFF)
                        .toSQLException();
            }
        }
        return result;
    }

    @Override
    public long[] getLimboTransactions() throws SQLException {
        final List<Long> limboTransactions = limboTransactionsAsList();
        final long[] trans = new long[limboTransactions.size()];
        int idx = 0;
        for (long trId : limboTransactions) {
            trans[idx++] = trId;
        }
        return trans;
    }

    @Override
    public void commitTransaction(final long transactionId) throws SQLException {
        handleTransaction(transactionId, isc_spb_rpr_commit_trans, isc_spb_rpr_commit_trans_64);
    }

    @Override
    public void rollbackTransaction(final long transactionId) throws SQLException {
        handleTransaction(transactionId, isc_spb_rpr_rollback_trans, isc_spb_rpr_rollback_trans_64);
    }

    private void handleTransaction(final long transactionId, final int action32bit, final int action64bit)
            throws SQLException {
        if (transactionId < 0) {
            throw new SQLException("Only positive transactionIds are supported");
        }
        final boolean is32Bit = NumericHelper.fitsUnsigned32BitInteger(transactionId);
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultRepairSRB(service);
            srb.addArgument(is32Bit ? action32bit : action64bit, transactionId);
            executeServicesOperation(service, srb);
        }
    }

    // ----------- Private implementation methods --------------------

    /**
     * Execute a isc_spb_rpr_* (repair) services operation.
     *
     * @param operation
     *         The identifier for the operation to be executed
     * @throws SQLException
     *         if a database access error occurs
     */
    private void executeRepairOperation(int operation) throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createRepairSRB(service, operation);
            executeServicesOperation(service, srb);
        }
    }

    /**
     * Execute a isc_spb_prp_* (properties) services operation.
     *
     * @param operation
     *         The identifier for the operation to be executed
     * @throws SQLException
     *         if a database access error occurs
     */
    private void executePropertiesOperation(int operation) throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createPropertiesSRB(service, operation);
            executeServicesOperation(service, srb);
        }
    }

    /**
     * Get a mostly empty properties-operation buffer that can be filled in as
     * needed. The buffer created by this method cannot have the options
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultPropertiesSRB(FbService service) {
        return createPropertiesSRB(service, 0);
    }

    /**
     * Get a mostly empty repair-operation buffer that can be filled in as
     * needed. The buffer created by this method cannot have the options
     * bitmask set on it.
     */
    private ServiceRequestBuffer createDefaultRepairSRB(FbService service) {
        return createRepairSRB(service, 0);
    }

    /**
     * Get a mostly-empty properties-operation request buffer that can be
     * filled as needed.
     *
     * @param service
     *         Service handle
     * @param options
     *         The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createPropertiesSRB(FbService service, int options) {
        return createRequestBuffer(service, isc_action_svc_properties, options);
    }

    /**
     * Get a mostly-empty repair-operation request buffer that can be
     * filled as needed.
     *
     * @param service
     *         Service handle
     * @param options
     *         The options bitmask for the request buffer
     */
    private ServiceRequestBuffer createRepairSRB(FbService service, int options) {
        return createRequestBuffer(service, isc_action_svc_repair, options);
    }
}
