// SPDX-FileCopyrightText: Copyright 2004 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2006 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2016-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.management;

import org.firebirdsql.gds.ISCConstants;
import org.jspecify.annotations.NullMarked;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A {@code StatisticsManager} is responsible for replicating the functionality of the {@code gstat} command-line tool.
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
 * @author Gabriel Reid
 */
@NullMarked
public interface StatisticsManager extends ServiceManager {

    /**
     * Request statistics on data tables.
     */
    int DATA_TABLE_STATISTICS = ISCConstants.isc_spb_sts_data_pages;

    /**
     * Request statistics on indexes.
     */
    int INDEX_STATISTICS = ISCConstants.isc_spb_sts_idx_pages;

    /**
     * Request statistics on system tables.
     */
    int SYSTEM_TABLE_STATISTICS = ISCConstants.isc_spb_sts_sys_relations;

    /**
     * Request statistics on record versions.
     */
    int RECORD_VERSION_STATISTICS = ISCConstants.isc_spb_sts_record_versions;

    /**
     * Fetch the database statistics header page.
     * <p>
     * The header information is written to this {@code StatisticsManager}'s logger.
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void getHeaderPage() throws SQLException;

    /**
     * Get the full database statistics information, excluding system table information.
     * <p>
     * The statistics information is written to this {@code StatisticsManager}'s logger.
     * </p>
     * <p>
     * The listed data includes:
     * <ul>
     * <li>statistics header page</li>
     * <li>log statistics</li>
     * <li>index statistics</li>
     * <li>data table statistics</li>
     * </ul>
     * </p>
     * <p>
     * Invoking this method is equivalent to the default behaviour of {@code gstat} on the command-line.
     * </p>
     *
     * @throws SQLException
     *         if a database access error occurs
     */
    void getDatabaseStatistics() throws SQLException;

    /**
     * Get specific database statistics.
     * <p>
     * The statistics information is written to this {@code StatisticsManager}'s logger. All invocations of
     * this method will result in the header page and log data being output.
     * </p>
     * <p>
     * The following options can be supplied as a bitmask:
     * <ul>
     * <li>{@code DATA_TABLE_STATISTICS}</li>
     * <li>{@code SYSTEM_TABLE_STATISTICS}</li>
     * <li>{@code INDEX_STATISTICS}</li>
     * <li>{@code RECORD_VERSION_STATISTICS}</li>
     * </ul>
     * </p>
     * <p>
     * If this method is invoked with {@code 0} as the {@code options} value, only the header and log statistics will
     * be output.
     * </p>
     *
     * @param options
     *         A bitmask combination of
     *         {@code DATA_TABLE_STATISTICS},
     *         {@code SYSTEM_TABLE_STATISTICS},
     *         {@code INDEX_STATISTICS}, or
     *         {@code RECORD_VERSION_STATISTICS}.
     *         Can also be {@code 0}, which is equivalent to calling method {@link #getDatabaseStatistics()}
     */
    void getDatabaseStatistics(int options) throws SQLException;

    /**
     * Get the table statistics.
     * <p>
     * For a more detailed description, see {@link #getTableStatistics(Collection, Collection)}.
     * </p>
     *
     * @param tableNames
     *         table names to analyze
     * @throws SQLException
     *         if something went wrong
     * @see #getTableStatistics(Collection)
     * @see #getTableStatistics(Collection, Collection)
     */
    default void getTableStatistics(String... tableNames) throws SQLException {
        getTableStatistics(Arrays.asList(tableNames));
    }

    /**
     * Get the table statistics.
     * <p>
     * For a more detailed description, see {@link #getTableStatistics(Collection, Collection)}.
     * </p>
     *
     * @param tableNames
     *         table names to analyze
     * @throws SQLException
     *         if something went wrong
     * @see #getTableStatistics(Collection, Collection)
     * @since 7
     */
    default void getTableStatistics(Collection<String> tableNames) throws SQLException {
        getTableStatistics(List.of(), tableNames);
    }

    /**
     * Get the table statistics.
     * <p>
     * The statistics information is written to this {@code StatisticsManager}'s logger.
     * </p>
     * <p>
     * The listed data includes:
     * <ul>
     * <li>the primary pointer and index root page numbers</li>
     * <li>number of data pages and their average fill</li>
     * <li>fill distribution</li>
     * </ul>
     * </p>
     * <p>
     * Invoking this method is equivalent to the behaviour of
     * {@code gstat -a [ -sch <schema> ]... [ -t <table name> ]...} on the commandline. For &mdash; unsupported &mdash;
     * Firebird 2.5 and older, it's equivalent to {@code gstat -t <table name> [ <table name>... ]} or {@code gstat -a}
     * when no tables are specified.
     * </p>
     *
     * @param schemaNames
     *         schemas to analyze; if empty, all schemas are analyzed (ignored on Firebird 5.0 or older)
     * @param tableNames
     *         table names to analyze; if empty, all tables (restricted by {@code schemaNames}) are analyzed
     * @throws SQLException
     *         if something went wrong (in current Firebird versions this includes when any of the tables cannot be
     *         found)
     * @since 7
     */
    void getTableStatistics(Collection<String> schemaNames, Collection<String> tableNames) throws SQLException;

    /**
     * Get transaction information of the database specified in {@code database}.
     *
     * @return Database transaction information
     * @throws SQLException
     *         If {@code database} is not specified, or for failures to connect or retrieve information
     * @since 3
     */
    DatabaseTransactionInfo getDatabaseTransactionInfo() throws SQLException;

    final class DatabaseTransactionInfo {
        private long oldestTransaction;
        private long oldestActiveTransaction;
        private long oldestSnapshotTransaction;
        private long nextTransaction;
        private long activeTransactionCount = -1;

        public long getOldestTransaction() {
            return oldestTransaction;
        }

        void setOldestTransaction(long oldestTransaction) {
            this.oldestTransaction = oldestTransaction;
        }

        public long getOldestActiveTransaction() {
            return oldestActiveTransaction;
        }

        void setOldestActiveTransaction(long oldestActiveTransaction) {
            this.oldestActiveTransaction = oldestActiveTransaction;
        }

        public long getOldestSnapshotTransaction() {
            return oldestSnapshotTransaction;
        }

        void setOldestSnapshotTransaction(long oldestSnapshotTransaction) {
            this.oldestSnapshotTransaction = oldestSnapshotTransaction;
        }

        public long getNextTransaction() {
            return nextTransaction;
        }

        void setNextTransaction(long nextTransaction) {
            this.nextTransaction = nextTransaction;
        }

        /**
         * @return active transaction count; {@code -1} means that this information wasn't retrieved (Firebird 1.5 and
         * lower)
         */
        public long getActiveTransactionCount() {
            return activeTransactionCount;
        }

        void setActiveTransactionCount(long activeTransactionCount) {
            this.activeTransactionCount = activeTransactionCount;
        }
    }
}
