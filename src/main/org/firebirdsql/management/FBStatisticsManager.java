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
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.firebirdsql.gds.VaxEncoding.iscVaxLong;

/**
 * The <code>FBStatisticsManager</code> class is responsible for replicating the functionality of
 * the <code>gstat</code> command-line tool.
 * <p>
 * This functionality includes:
 * <ul>
 * <li>Retrieving data table statistics</li>
 * <li>Retrieving the database header page</li>
 * <li>Retrieving index statistics</li>
 * <li>Retrieving database logging information</li>
 * <li>Retrieving statistics for the data dictionary</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 */
public class FBStatisticsManager extends FBServiceManager implements StatisticsManager {

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
     * @param gdsType
     *         type must be PURE_JAVA, EMBEDDED, or NATIVE
     */
    public FBStatisticsManager(String gdsType) {
        super(gdsType);
    }

    /**
     * Create a new instance of <code>FBMaintenanceManager</code> based on
     * a given GDSType.
     *
     * @param gdsType
     *         The GDS implementation type to use
     */
    public FBStatisticsManager(GDSType gdsType) {
        super(gdsType);
    }

    public void getHeaderPage() throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createStatsSRB(service, isc_spb_sts_hdr_pages);
            executeServicesOperation(service, srb);
        }
    }

    public void getDatabaseStatistics() throws SQLException {
        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createDefaultStatsSRB(service);
            executeServicesOperation(service, srb);
        }
    }

    public void getDatabaseStatistics(int options) throws SQLException {
        if (options != 0 && (options | possibleStatistics) != possibleStatistics) {
            throw new IllegalArgumentException("options must be 0 or a "
                    + "combination of DATA_TABLE_STATISTICS, "
                    + "SYSTEM_TABLE_STATISTICS, INDEX_STATISTICS, or 0");
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createStatsSRB(service, options);
            executeServicesOperation(service, srb);
        }
    }

    public void getTableStatistics(String[] tableNames) throws SQLException {
        // create space-separated list of tables
        StringBuilder commandLine = new StringBuilder();
        for (int i = 0; i < tableNames.length; i++) {
            commandLine.append(tableNames[i]);
            if (i < tableNames.length - 1)
                commandLine.append(' ');
        }

        try (FbService service = attachServiceManager()) {
            ServiceRequestBuffer srb = createStatsSRB(service, isc_spb_sts_table);
            srb.addArgument(isc_spb_command_line, commandLine.toString());
            executeServicesOperation(service, srb);
        }
    }

    // We actually need 56 bytes: 5 info items * (1 byte type + 2 byte length + (max) 8 byte value) + 1 byte end
    private static final int DB_TRANSACTION_INFO_BUFFER_LENGTH = 100;

    @Override
    public DatabaseTransactionInfo getDatabaseTransactionInfo() throws SQLException {
        try (FbDatabase database = attachDatabase()) {
            return getDatabaseTransactionInfo(database);
        }
    }

    /**
     * Get transaction information for an existing database connection.
     *
     * @param connection
     *         Database connection; must unwrap to {@link FirebirdConnection}.
     * @return Database transaction information
     * @throws SQLException
     *         If {@code connection} does not unwrap to {@link FirebirdConnection}, or for failures to
     *         retrieve information
     */
    public static DatabaseTransactionInfo getDatabaseTransactionInfo(Connection connection) throws SQLException {
        FirebirdConnection firebirdConnection = connection.unwrap(FirebirdConnection.class);
        return getDatabaseTransactionInfo(firebirdConnection.getFbDatabase());
    }

    private static DatabaseTransactionInfo getDatabaseTransactionInfo(
            FbDatabase database) throws SQLException {
        final byte[] infoItems = DatabaseTransactionInfoProcessor.getInfoItems(database.getServerVersion());

        return database.getDatabaseInfo(infoItems, DB_TRANSACTION_INFO_BUFFER_LENGTH,
                new DatabaseTransactionInfoProcessor());
    }

    /**
     * Get a mostly empty buffer that can be filled in as needed.
     * The buffer created by this method cannot have the options bitmask
     * set on it.
     *
     * @param service
     *         Service handle
     */
    private ServiceRequestBuffer createDefaultStatsSRB(FbService service) {
        return createStatsSRB(service, 0);
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
    private ServiceRequestBuffer createStatsSRB(FbService service, int options) {
        return createRequestBuffer(service, isc_action_svc_db_stats, options);
    }

    private static final class DatabaseTransactionInfoProcessor implements InfoProcessor<DatabaseTransactionInfo> {

        private static final Logger log = LoggerFactory.getLogger(DatabaseTransactionInfoProcessor.class);

        @Override
        public DatabaseTransactionInfo process(byte[] info) throws SQLException {
            if (info.length == 0) {
                throw new SQLException("Response buffer for service information request is empty");
            }
            DatabaseTransactionInfo databaseTransactionInfo = new DatabaseTransactionInfo();
            int idx = 0;
            while (info[idx] != isc_info_end) {
                final byte infoItem = info[idx];
                idx++;
                if (infoItem == isc_info_truncated) {
                    log.warn("Transaction information response was truncated at index " + idx + ". Info block size: " +
                            info.length + ". This could indicate a bug in the implementation.");
                    break;
                }

                int valueLength = iscVaxInteger2(info, idx);
                idx += 2;
                long dataItem = iscVaxLong(info, idx, valueLength);
                idx += valueLength;

                switch (infoItem) {
                case isc_info_oldest_transaction:
                    databaseTransactionInfo.setOldestTransaction(dataItem);
                    break;

                case isc_info_oldest_active:
                    databaseTransactionInfo.setOldestActiveTransaction(dataItem);
                    break;

                case isc_info_oldest_snapshot:
                    databaseTransactionInfo.setOldestSnapshotTransaction(dataItem);
                    break;

                case isc_info_next_transaction:
                    databaseTransactionInfo.setNextTransaction(dataItem);
                    break;

                case isc_info_active_tran_count:
                    databaseTransactionInfo.setActiveTransactionCount(dataItem);
                    break;
                    
                default:
                    log.warn("Unknown or unexpected info item: " + infoItem);
                    break;
                }
            }
            return databaseTransactionInfo;
        }

        /**
         * The information items supported by this processor for the provided server version.
         *
         * @param serverVersion Server version
         * @return Array with information items
         */
        public static byte[] getInfoItems(GDSServerVersion serverVersion) {
            if (serverVersion.isEqualOrAbove(2, 0)) {
                return new byte[] { isc_info_oldest_transaction, isc_info_oldest_active, isc_info_oldest_snapshot,
                        isc_info_next_transaction, isc_info_active_tran_count };
            } else {
                /*
                Firebird 1.5 and earlier could only count using isc_info_active_transactions (which returns an entry
                for each active transaction). We are not doing that (Firebird 1.5 isn't supported anymore), but we
                do the minimum necessary to not break on Firebird 1.5, which means we sacrifice transaction count.
                */
                return new byte[] { isc_info_oldest_transaction, isc_info_oldest_active, isc_info_oldest_snapshot,
                        isc_info_next_transaction };
            }
        }
    }
}
