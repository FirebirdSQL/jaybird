/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.InfoTruncatedException;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.util.Volatile;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLNonTransientException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Provides access to the table statistics of a {@link java.sql.Connection}.
 * <p>
 * The table statistics are execution statistics of the specific connection, and are not global statistics.
 * </p>
 * <p>
 * This manager retains a reference to underlying {@link org.firebirdsql.gds.ng.FbDatabase} instance and holds a cache
 * of table names. Closing this manager will remove the reference the database handle (but not close it!) and releases
 * the cache.
 * </p>
 * <p>
 * This class is not thread-safe (though its use of the underlying connection is thread-safe).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@Volatile(reason = "Experimental")
public final class FBTableStatisticsManager implements AutoCloseable {

    private static final int MAX_RETRIES = 3;

    private Map<Integer, String> tableMapping = new HashMap<>();
    private FirebirdConnection connection;
    /**
     * Table slack is a number which is used to pad the table count used for calculating the buffer size in an attempt
     * to prevent or fix truncation of the info request. It is incremented when a truncation is handled.
     */
    private int tableSlack;

    private FBTableStatisticsManager(FirebirdConnection connection) throws SQLException {
        if (connection.isClosed()) {
            throw new SQLNonTransientConnectionException("This connection is closed and cannot be used now.",
                    SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
        }
        this.connection = connection;
    }

    /**
     * Creates a table statistics manager for the current connection.
     *
     * @param connection
     *         Connection to gather statistics on.
     * @return a table statistics manager
     * @throws SQLException
     *         if {@code connection} is closed
     */
    public static FBTableStatisticsManager of(Connection connection) throws SQLException {
        return new FBTableStatisticsManager(
                connection.unwrap(FirebirdConnection.class));
    }

    /**
     * Obtains a snapshot of the table statistics of this connection.
     * <p>
     * A table is only present in the map if this connection touched it in a way which generated a statistic.
     * </p>
     *
     * @return map from table name to table statistics
     * @throws InfoTruncatedException
     *         if a truncated response is received, after retrying 3 times (total: 4 attempts) while increasing
     *         the buffer size; it is possible that subsequent calls to this method may recover (as that will increase
     *         the buffer size even more)
     * @throws SQLException
     *         if the connection is closed, or if obtaining the statistics failed due to a database access error
     */
    public Map<String, TableStatistics> getTableStatistics() throws SQLException {
        checkClosed();
        FbDatabase db = connection.getFbDatabase();
        InfoTruncatedException lastTruncation;
        TableStatisticsProcessor tableStatisticsProcessor = new TableStatisticsProcessor();
        int attempt = 0;
        do {
            try {
                return db.getDatabaseInfo(getInfoItems(), bufferSize(getTableCount()), tableStatisticsProcessor);
            } catch (InfoTruncatedException e) {
                /* Occurrence of truncation should be rare. It could occur if all tables have all statistics items, and
                   new tables are added after the last updateMapping() call or statistics were previously requested by
                   a different instance and this instance was created after tables have been dropped.
                   Here, tableSlack is incremented to account for tables removed, while updateTableMapping() is called
                   to account for new tables. */
                tableSlack++;
                updateTableMapping();
                lastTruncation = e;
            }
        } while (attempt++ < MAX_RETRIES);
        throw lastTruncation;
    }

    /**
     * @return the actual table count (so excluding {@link #tableSlack}).
     */
    private int getTableCount() throws SQLException {
        int size = tableMapping.size();
        if (size != 0) return size;
        updateTableMapping();
        return tableMapping.size();
    }

    /**
     * Clears the reference to the connection and clears the cache.
     * <p>
     * This method does not close the connection wrapped by this manager.
     * </p>
     * <p>
     * Closing a table statistics manager is not required. It can be used to clean up early, or to prevent a user of
     * this class from gathering further statistics.
     * </p>
     */
    @Override
    public void close() {
        connection = null;
        tableMapping.clear();
        tableMapping = null;
    }

    private void checkClosed() throws SQLException {
        if (connection != null && !connection.isClosed()) return;
        if (connection != null) {
            // release reference and clear cache
            close();
        }
        throw new SQLNonTransientException("This statistics manager is closed and cannot be used now.");
    }

    private void updateTableMapping() throws SQLException {
        DatabaseMetaData md = connection.getMetaData();
        try (ResultSet rs = md.getTables(
                null, null, "%", new String[] { "SYSTEM TABLE", "TABLE", "GLOBAL TEMPORARY" })) {
            while (rs.next()) {
                tableMapping.put(rs.getInt("JB_RELATION_ID"), rs.getString("TABLE_NAME"));
            }
        }
    }

    private int bufferSize(int maxTables) {
        // NOTE: In the current implementation in Firebird, the limit is actually 1 + 8 * (3 + 65535)
        long size = 1 + 8 * (3 + 6 * (long) (maxTables + tableSlack));
        if (size <= 0) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(size, Integer.MAX_VALUE);
    }

    private static byte[] getInfoItems() {
        return new byte[] {
                isc_info_read_seq_count,
                isc_info_read_idx_count,
                isc_info_insert_count,
                isc_info_update_count,
                isc_info_delete_count,
                isc_info_backout_count,
                isc_info_purge_count,
                isc_info_expunge_count
        };
    }

    /**
     * Info processor to retrieve table statistics.
     * <p>
     * This is a stateful object and not thread-safe. It should not be shared, and not be reused or only reused in
     * a small scope. For example, in the current implementation, it is shared for the retry attempts within
     * {@link #getTableStatistics()}, and this is OK because doing so prevents unnecessary calls to
     * {@link #updateTableMapping()} from this processor.
     * </p>
     */
    private final class TableStatisticsProcessor implements InfoProcessor<Map<String, TableStatistics>> {

        private final Map<String, TableStatistics.TableStatisticsBuilder> statisticsBuilders = new HashMap<>();
        private boolean allowTableMappingUpdate = true;

        @Override
        public Map<String, TableStatistics> process(byte[] infoResponse) throws SQLException {
            try {
                int idx = 0;
                decodeLoop:
                while (idx < infoResponse.length) {
                    int infoItem = infoResponse[idx++];
                    switch (infoItem) {
                    case isc_info_end:
                        break decodeLoop;
                    case isc_info_truncated:
                        throw new InfoTruncatedException("Received isc_info_truncated, and this processor cannot recover automatically", infoResponse.length);
                    case isc_info_read_seq_count:
                    case isc_info_read_idx_count:
                    case isc_info_insert_count:
                    case isc_info_update_count:
                    case isc_info_delete_count:
                    case isc_info_backout_count:
                    case isc_info_purge_count:
                    case isc_info_expunge_count:
                        int length = VaxEncoding.iscVaxInteger2(infoResponse, idx);
                        idx += 2;
                        processStatistics(infoItem, infoResponse, idx, idx += length);
                        break;
                    default:
                        System.getLogger(TableStatisticsProcessor.class.getName()).log(System.Logger.Level.DEBUG,
                                "Received unexpected info item {0}, this is likely an implementation bug", infoItem);
                        break decodeLoop;
                    }
                }

                return statisticsBuilders.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toTableStatistics()));
            } finally {
                statisticsBuilders.clear();
            }
        }

        void processStatistics(int statistic, byte[] buffer, int start, int end) throws SQLException {
            int idx = start;
            while (idx <= end - 6) {
                int tableId = VaxEncoding.iscVaxInteger2(buffer, idx);
                idx += 2;
                /*
                 NOTE: Due to a bug in Firebird, it is possible that an 8 byte value is sent, but there is no way to
                 discern this from the case where a 4 byte value is sent. Unfortunately, this means that once an
                 8 byte value has been sent, we'll decode the wrong values
                 See https://github.com/FirebirdSQL/firebird/issues/7414
                */
                long value = VaxEncoding.iscVaxInteger(buffer, idx, 4);
                idx += 4;
                getBuilder(tableId).addStatistic(statistic, value);
            }
        }

        private String getTableName(Integer tableId) throws SQLException {
            String tableName = tableMapping.get(tableId);
            if (tableName == null) {
                // mapping empty or out of date (e.g. new table created since the last update)
                if (allowTableMappingUpdate) {
                    updateTableMapping();
                    // Ensure that if we have multiple tables missing, we don't repeatedly update the table mapping, as
                    // that wouldn't result in new information.
                    allowTableMappingUpdate = false;
                    tableName = tableMapping.get(tableId);
                }
                if (tableName == null) {
                    // fallback
                    tableName = "UNKNOWN_TABLE_ID_" + tableId;
                }
            }
            return tableName;
        }

        private TableStatistics.TableStatisticsBuilder getBuilder(int tableId) throws SQLException {
            String tableName = getTableName(tableId);
            return statisticsBuilders.computeIfAbsent(tableName, TableStatistics::builder);
        }
    }
}
