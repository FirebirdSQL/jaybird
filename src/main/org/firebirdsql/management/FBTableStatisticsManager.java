// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.InfoProcessor;
import org.firebirdsql.gds.ng.InfoTruncatedException;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.firebirdsql.util.Volatile;
import org.jspecify.annotations.Nullable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jaybird.util.StringUtils.isNullOrEmpty;

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

    private Map<Integer, ObjectReference> tableMapping = new HashMap<>();
    private @Nullable FirebirdConnection connection;
    /**
     * Table slack is a number which is used to pad the table count used for calculating the buffer size in an attempt
     * to prevent or fix truncation of the info request. It is incremented when a truncation is handled.
     */
    private int tableSlack;

    private FBTableStatisticsManager(FirebirdConnection connection) throws SQLException {
        if (connection.isClosed()) {
            throw FbExceptionBuilder.connectionClosed();
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
     * <p>
     * The method {@link #toKey(String, String)} can be used to produce a key as used for entries in the map.
     * </p>
     *
     * @return map from table reference ({@code <table-name>} for schemaless, or
     * {@code <quoted-schema>.<quoted-table-name>} for schema-bound) to table statistics
     * @throws InfoTruncatedException
     *         if a truncated response is received, after retrying 3 times (total: 4 attempts) while increasing
     *         the buffer size; it is possible that subsequent calls to this method may recover (as that will increase
     *         the buffer size even more)
     * @throws SQLException
     *         if the connection is closed, or if obtaining the statistics failed due to a database access error
     * @see #toKey(String, String)
     */
    public Map<String, TableStatistics> getTableStatistics() throws SQLException {
        checkClosed();
        @SuppressWarnings("DataFlowIssue") FbDatabase db = connection.getFbDatabase();
        InfoTruncatedException lastTruncation;
        var tableStatisticsProcessor = new TableStatisticsProcessor();
        int attempt = 0;
        do {
            try {
                return db.getDatabaseInfo(getInfoItems(), bufferSize(getTableCount()), tableStatisticsProcessor)
                        .values().stream()
                        .collect(toMap(FBTableStatisticsManager::toKey, Function.identity()));
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
     * Produces a key to the map returned by {@link #getTableStatistics()}.
     *
     * @param schema
     *         schema, or {@code null} or empty string for schemaless
     * @param tableName
     *         table name
     * @return key: {@code <table-name>} for schemaless, or {@code <quoted-schema>.<quoted-table-name>} for schema-bound
     * @since 7
     */
    public static String toKey(@Nullable String schema, String tableName) {
        return isNullOrEmpty(schema) ? tableName : toKey(ObjectReference.of(schema, tableName));
    }

    /**
     * Produces a key to the map returned by {@link #getTableStatistics()}.
     *
     * @param tableStatistics
     *         table statistics object
     * @return key
     * @see #toKey(String, String)
     * @since 7
     */
    public static String toKey(TableStatistics tableStatistics) {
        return toKey(tableStatistics.table());
    }

    /**
     * Produces a key to the map returned by {@link #getTableStatistics()}.
     * <p>
     * The behaviour is undefined when called with an {@link ObjectReference} of more than two identifiers.
     * </p>
     *
     * @param objectReference
     *         table object reference
     * @return key
     * @see #toKey(String, String)
     * @since 7
     */
    static String toKey(ObjectReference objectReference) {
        if (objectReference.size() == 1) {
            return objectReference.first().name();
        }
        // We assume object reference is size 2, but this will 'work' even if that assumption is wrong
        return objectReference.toString();
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
        tableMapping = Map.of();
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
        @SuppressWarnings("DataFlowIssue") DatabaseMetaData md = connection.getMetaData();
        try (ResultSet rs = md.getTables(
                null, null, "%", new String[] { "SYSTEM TABLE", "TABLE", "GLOBAL TEMPORARY" })) {
            while (rs.next()) {
                tableMapping.put(rs.getInt("JB_RELATION_ID"),
                        ObjectReference.of(rs.getString("TABLE_SCHEM"), rs.getString("TABLE_NAME")));
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
    private final class TableStatisticsProcessor implements InfoProcessor<Map<ObjectReference, TableStatistics>> {

        private final Map<ObjectReference, TableStatistics.TableStatisticsBuilder> statisticsBuilders = new HashMap<>();
        private boolean allowTableMappingUpdate = true;

        @Override
        public Map<ObjectReference, TableStatistics> process(byte[] infoResponse) throws SQLException {
            try {
                decodeResponse(infoResponse);
                return statisticsBuilders.entrySet().stream()
                        .collect(toMap(Map.Entry::getKey, e -> e.getValue().toTableStatistics()));
            } finally {
                statisticsBuilders.clear();
            }
        }

        private void decodeResponse(byte[] infoResponse) throws SQLException {
            int idx = 0;
            while (idx < infoResponse.length) {
                int infoItem = infoResponse[idx++];
                switch (infoItem) {
                case isc_info_end -> {
                    return;
                }
                case isc_info_truncated -> throw new InfoTruncatedException(
                        "Received isc_info_truncated, and this processor cannot recover automatically",
                        infoResponse.length);
                case isc_info_read_seq_count, isc_info_read_idx_count, isc_info_insert_count, isc_info_update_count,
                        isc_info_delete_count, isc_info_backout_count, isc_info_purge_count, isc_info_expunge_count -> {
                    int length = VaxEncoding.iscVaxInteger2(infoResponse, idx);
                    idx += 2;
                    processStatistics(infoItem, infoResponse, idx, idx += length);
                }
                default -> {
                    System.getLogger(TableStatisticsProcessor.class.getName()).log(System.Logger.Level.DEBUG,
                            "Received unexpected info item {0}, this is likely an implementation bug", infoItem);
                    return;
                }
                }
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

        private ObjectReference getTable(Integer tableId) throws SQLException {
            ObjectReference table = tableMapping.get(tableId);
            if (table == null) {
                // mapping empty or out of date (e.g. new table created since the last update)
                if (allowTableMappingUpdate) {
                    updateTableMapping();
                    // Ensure that if we have multiple tables missing, we don't repeatedly update the table mapping, as
                    // that wouldn't result in new information.
                    allowTableMappingUpdate = false;
                    table = tableMapping.get(tableId);
                }
                if (table == null) {
                    // fallback
                    table = ObjectReference.of("UNKNOWN_TABLE_ID_" + tableId);
                }
            }
            return table;
        }

        private TableStatistics.TableStatisticsBuilder getBuilder(int tableId) throws SQLException {
            return statisticsBuilders.computeIfAbsent(getTable(tableId), TableStatistics::builder);
        }
    }
}
