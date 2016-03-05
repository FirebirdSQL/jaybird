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

import org.firebirdsql.gds.ISCConstants;

import java.sql.SQLException;
import java.util.List;

/**
 * A <code>MaintenanceManager</code> is responsible for replicating the
 * functionality provided by the <code>gfix</code> command-line tool.
 * <p>
 * Among the responsibilities of this class are:
 * <ul>
 * <li>Database shutdown
 * <li>Extended database shutdown/online modes
 * <li>Changing database mode to read-only or read-write
 * <li>Enabling or disabling forced writes in the database
 * <li>Changing the dialect of the database
 * <li>Setting the cache size at database-level
 * <li>Mending databases and making minor repairs
 * <li>Sweeping databases
 * <li>Displaying, committing, or recovering limbo transactions
 * <li>Activating and killing shadow files
 * <li>Configuring page fill
 * </ul>
 * </p>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:tsteinmaurer@users.sourceforge.net">Thomas Steinmaurer</a>
 */
public interface MaintenanceManager extends ServiceManager {

    /**
     * Database read-write mode
     */
    int ACCESS_MODE_READ_WRITE = ISCConstants.isc_spb_prp_am_readwrite;

    /**
     * Database read-only mode
     */
    int ACCESS_MODE_READ_ONLY = ISCConstants.isc_spb_prp_am_readonly;

    /**
     * Don't allow new connections while waiting to shut down.
     */
    int SHUTDOWN_ATTACH = ISCConstants.isc_spb_prp_deny_new_attachments;

    /**
     * Don't allow new transactions while waiting to shut down.
     */
    int SHUTDOWN_TRANSACTIONAL = ISCConstants.isc_spb_prp_deny_new_transactions;

    /**
     * Forced database shutdown.
     */
    int SHUTDOWN_FORCE = ISCConstants.isc_spb_prp_shutdown_db;

    /**
     * Only report corrupt or misallocated structures, don't fix.
     */
    int VALIDATE_READ_ONLY = ISCConstants.isc_spb_rpr_check_db;

    /**
     * Ignore checksums during repair operations.
     */
    int VALIDATE_IGNORE_CHECKSUM = ISCConstants.isc_spb_rpr_ignore_checksum;

    /**
     * Do a full check on record and pages structures, releasing unassigned
     * record fragments.
     */
    int VALIDATE_FULL = ISCConstants.isc_spb_rpr_full;

    /**
     * Fully fill pages when inserting records.
     */
    int PAGE_FILL_FULL = ISCConstants.isc_spb_prp_res_use_full;

    /**
     * While inserting records, reserve 20% of each page for later record deltas
     */
    int PAGE_FILL_RESERVE = ISCConstants.isc_spb_prp_res;

    /**
     * Operation mode normal online.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    byte OPERATION_MODE_NORMAL = ISCConstants.isc_spb_prp_sm_normal;

    /**
     * Operation mode multi shutdown/online.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    byte OPERATION_MODE_MULTI = ISCConstants.isc_spb_prp_sm_multi;

    /**
     * Operation mode single shutdown/online.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    byte OPERATION_MODE_SINGLE = ISCConstants.isc_spb_prp_sm_single;

    /**
     * Operation mode full shutdown.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    byte OPERATION_MODE_FULL_SHUTDOWN = ISCConstants.isc_spb_prp_sm_full;

    /**
     * Force shutdown.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    int SHUTDOWNEX_FORCE = ISCConstants.isc_spb_prp_force_shutdown;

    /**
     * Shutdown attachments.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    int SHUTDOWNEX_ATTACHMENTS = ISCConstants.isc_spb_prp_attachments_shutdown;

    /**
     * Shutdown transactions.
     * <p>
     * To be used with the {@link #shutdownDatabase(byte, int, int)} method.
     * </p>
     *
     * @see #shutdownDatabase(byte, int, int)
     * @since Firebird 2.5
     */
    int SHUTDOWNEX_TRANSACTIONS = ISCConstants.isc_spb_prp_transactions_shutdown;

    /**
     * Set the database to have read-write or read-only access.
     *
     * @param mode
     *         Must be either {@code ACCESS_MODE_READ_WRITE} or {@code ACCESS_MODE_READ_ONLY}
     * @throws SQLException
     *         if a database access error occurs
     */
    void setDatabaseAccessMode(int mode) throws SQLException;

    /**
     * Set the database's dialect.
     *
     * @param dialect
     *         The database dialect, must be either 1 or 3
     * @throws SQLException
     *         if a database access error occurs
     */
    void setDatabaseDialect(int dialect) throws SQLException;

    /**
     * Set the default page-buffer count to be cached in the database.
     *
     * @param pageCount
     *         The number of pages to be cached, must be a positive
     * @throws SQLException
     *         If the given page count cannot be set, or a database access error occurs
     */
    void setDefaultCacheBuffer(int pageCount) throws SQLException;

    /**
     * Enable or disable forced (synchronous) writes in the database.
     * <p>
     * Note, it is considered to be a <b>very</b> bad idea to disable forced
     * writes on Windows platforms.
     * </p>
     *
     * @param forced
     *         If {@code true}, forced writes will be used in the database, otherwise buffered writes will be used.
     * @throws SQLException
     *         if a database access error occurs
     */
    void setForcedWrites(boolean forced) throws SQLException;

    /**
     * Set the page fill strategy for when inserting records.
     * <p>
     * {@code pageFill} can be one of:
     * <ul>
     * <li>{@code PAGE_FILL_FULL} Fully fill database pages
     * <li>{@code PAGE_FILL_RESERVE} Reserve 20% of page space for later
     * record deltas
     * </ul>
     * </p>
     *
     * @param pageFill
     *         The page-filling strategy, either {@code PAGE_FILL_FULL} or {@code PAGE_FILL_RESERVE}
     * @throws SQLException
     *         if a database access error occurs
     */
    void setPageFill(int pageFill) throws SQLException;

    // ----------- Database Shutdown -------------------

    /**
     * Shutdown the current database.
     * <p>
     * Shutdown can be done in three modes:
     * <ul>
     * <li>{@code SHUTDOWN_ATTACH} - No new non-owner connections will be allowed to the database during the shutdown,
     * and shutdown is cancelled if there are still processes connected at the end of the timeout.</li>
     * <li>{@code SHUTDOWN_TRANSACTIONAL} - No new transactions can be started during the timeout period, and shutdown
     * is cancelled if there are still active transactions at the end of the timeout.</li>
     * <li>{@code SHUTDOWN_FORCE} - Forcefully shuts down the database at the end of the timeout.</li>
     * </ul>
     * </p>
     *
     * @param shutdownMode
     *         One of {@code SHUTDOWN_ATTACH}, {@code SHUTDOWN_TRANSACTIONAL}, or {@code SHUTDOWN_FORCE}.
     * @param timeout
     *         The maximum amount of time allocated for the operation, in seconds
     * @throws SQLException
     *         if the requested operation cannot be completed within the given timeout, or a database access error
     *         occurs
     */
    void shutdownDatabase(int shutdownMode, int timeout) throws SQLException;

    /**
     * Shutdown the current database with enhanced modes (FB 2.5 or higher).
     * <p>
     * There are three operation modes for shutdown available:
     * <ul>
     * <li>{@code OPERATION_MODE_MULTI} - Multi-user maintenance. Unlimited SYSDBA/database owner connections are
     * allowed.</li>
     * <li>{@code OPERATION_MODE_SINGLE} - Single-user maintenance. Only one SYSDBA/database owner connection is
     * allowed.</li>
     * <li>{@code OPERATION_MODE_FULL_SHUTDOWN} - Full shutdown. Full exclusive shutdown. No connections are allowed.</li>
     * </ul>
     * </p>
     * <p>
     * There are three extended shutdown modes for shutdown available:
     * <ul>
     * <li>{@code SHUTDOWNEX_FORCE} - Force shutdown.</li>
     * <li>{@code SHUTDOWNEX_ATTACHMENTS} - Shutdown attachments.</li>
     * <li>{@code SHUTDOWNEX_TRANSACTIONS} - Shutdown transactions.</li>
     * </ul>
     * </p>
     *
     * @param operationMode
     *         one of {@code OPERATION_MODE_*} operation modes listed above
     * @param shutdownModeEx
     *         one of {@code SHUTDOWNEX_*} extended shutdown modes listed above
     * @param timeout
     *         The maximum amount of time allocated for the operation, in seconds. 0 = immediately.
     * @throws SQLException
     *         if the requested operation cannot be completed within the given timeout, or a database access error
     *         occurs
     * @since Firebird 2.5
     */
    void shutdownDatabase(byte operationMode, int shutdownModeEx, int timeout) throws SQLException;

    /**
     * Bring a shutdown database online.
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void bringDatabaseOnline() throws SQLException;

    /**
     * Bring a shutdown database online with enhanced operation modes (FB 2.5 or
     * higher).
     * <p>
     * There are three operation modes for bringing a database online available:
     * <ul>
     * <li>{@code OPERATION_MODE_NORMAL} - Normal operation modes.</li>
     * <li>{@code OPERATION_MODE_MULTI} - Multi-user maintenance. Unlimited SYSDBA/database owner connections are
     * allowed.</li>
     * <li>{@code OPERATION_MODE_SINGLE} - Single-user maintenance. Only one SYSDBA/database owner connection is
     * allowed.</li>
     * </ul>
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     * @since Firebird 2.5
     */
    void bringDatabaseOnline(byte operationMode) throws SQLException;

    // -------------- Database Repair ----------------------

    /**
     * Mark corrupt records in the database as unavailable.
     * <p>
     * This operation ensures that the corrupt records are skipped (for example,
     * during a subsequent backup).
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void markCorruptRecords() throws SQLException;

    /**
     * Locate and release database pages that are allocated but unassigned to
     * any data structures. This method also reports corrupt structures.
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void validateDatabase() throws SQLException;

    /**
     * Locate and release database pages that are allocated but unassigned to
     * any data structures. This method also reports corrupt structures.
     * <p>
     * The value supplied for {@code options} must be one of the following:
     * <ul>
     * <li>0 - Simple validation</li>
     * <li>{@code VALIDATE_READ_ONLY} - read-only validation, no repair</li>
     * <li>{@code VALIDATE_FULL} - full validation and repair</li>
     * </ul>
     * </p>
     * <p>
     * The value for {@code options} can additionally be combined in a bitmask with {@code VALIDATE_IGNORE_CHECKSUM} to
     * ignore checksums while performing validation.
     * </p>
     *
     * @param options
     *         Either 0, {@code VALIDATE_READ_ONLY}, or {@code VALIDATE_FULL}
     * @throws SQLException
     *         if a database access error occurs
     */
    void validateDatabase(int options) throws SQLException;

    // ----------- Sweeping -------------------------

    /**
     * Set the database automatic sweep interval to a given number of
     * transactions.
     * <p>
     * The Firebird default value is 20,000. If {@code transactions} is 0, automatic sweeping is disabled.
     * </p>
     *
     * @param transactions
     *         The interval of transactions between automatic sweeps of the database. Can be set to 0, which disables
     *         automatic sweeping of the database.
     * @throws SQLException
     *         if a database access error occurs
     */
    void setSweepThreshold(int transactions) throws SQLException;

    /**
     * Perform an immediate sweep of the database.
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void sweepDatabase() throws SQLException;

    // ----------- Shadow Files ------------------------------------

    /**
     * Activate a database shadow file to be used as the actual database.
     * <p>
     * This method is the equivalent of <b>{@code gfix -activate}</b>.
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void activateShadowFile() throws SQLException;

    /**
     * Remove references to unavailable shadow files.
     * <p>
     * This method is the equivalent of <b>{@code gfix -kill}</b>.
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void killUnavailableShadows() throws SQLException;

    // ----------- Transaction Management ----------------------------

    /**
     * Retrieve the ID of each limbo transaction. The output of this method is
     * written to the logger.
     *
     * @throws SQLException
     *         if a database access error occurs
     * @deprecated Use {@link #limboTransactionsAsList()} or
     * {@link #getLimboTransactions()} instead
     */
    @Deprecated
    void listLimboTransactions() throws SQLException;

    /**
     * Retrieve the ID of each limbo transaction as a List of Integer objects.
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    List<Integer> limboTransactionsAsList() throws SQLException;

    /**
     * Retrieve the ID of each limbo transaction as an array of ints.
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    int[] getLimboTransactions() throws SQLException;

    /**
     * Commit a limbo transaction based on its ID.
     *
     * @param transactionId
     *         The ID of the limbo transaction to be committed
     * @throws SQLException
     *         if a database access error occurs or the given transaction ID is not valid
     */
    void commitTransaction(int transactionId) throws SQLException;

    /**
     * Rollback a limbo transaction based on its ID.
     *
     * @param transactionId
     *         The ID of the limbo transaction to be rolled back
     * @throws SQLException
     *         if a database access error occurs or the given transaction ID is not valid
     */
    void rollbackTransaction(int transactionId) throws SQLException;
}
