// SPDX-FileCopyrightText: Copyright 2022-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.management;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.firebirdsql.util.Volatile;

import static org.firebirdsql.gds.ISCConstants.isc_info_backout_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_delete_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_expunge_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_insert_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_purge_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_read_idx_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_read_seq_count;
import static org.firebirdsql.gds.ISCConstants.isc_info_update_count;

/**
 * Snapshot of the table statistics of a connection (of the table reported by {@code tableName}).
 *
 * @author Mark Rotteveel
 * @see FBTableStatisticsManager
 * @since 5
 */
@SuppressWarnings("unused")
@Volatile(reason = "Experimental")
public final class TableStatistics {

    private final ObjectReference table;
    private final long readSeqCount;
    private final long readIdxCount;
    private final long insertCount;
    private final long updateCount;
    private final long deleteCount;
    private final long backoutCount;
    private final long purgeCount;
    private final long expungeCount;

    private TableStatistics(ObjectReference table, long readSeqCount, long readIdxCount, long insertCount,
            long updateCount, long deleteCount, long backoutCount, long purgeCount, long expungeCount) {
        this.table = table;
        this.readSeqCount = readSeqCount;
        this.readIdxCount = readIdxCount;
        this.insertCount = insertCount;
        this.updateCount = updateCount;
        this.deleteCount = deleteCount;
        this.backoutCount = backoutCount;
        this.purgeCount = purgeCount;
        this.expungeCount = expungeCount;
    }

    /**
     * @return table name (or {@code UNKNOWN_TABLE_ID_<table id>} if the table was not found)
     * @see #tableReference()
     */
    public String tableName() {
        return table.last().name();
    }

    /**
     * @return schema of table (or empty string if schemaless or the table was not found)
     * @see #tableReference()
     * @since 7
     */
    public String schema() {
        return table.size() == 2 ? table.first().name() : "";
    }

    /**
     * The table reference.
     * <p>
     * Contrary to the key used for {@link FBTableStatisticsManager#getTableStatistics()}, the table name is always
     * quoted, even for schemaless tables. If you want to derive a key for the table from an instance of this class, use
     * {@link FBTableStatisticsManager#toKey(TableStatistics)}.
     * </p>
     *
     * @return fully qualified and quoted table reference ({@code [<quoted-schema>.]<quoted-table-name>})
     * @see #tableName()
     * @see #schema()
     * @see FBTableStatisticsManager#toKey(TableStatistics)
     * @since 7
     */
    public String tableReference() {
        return table.toString();
    }

    /**
     * @return object reference of the table
     * @since 7
     */
    ObjectReference table() {
        return table;
    }

    /**
     * @return count of sequential reads
     */
    public long readSeqCount() {
        return readSeqCount;
    }

    /**
     * @return count of indexed reads
     */
    public long readIdxCount() {
        return readIdxCount;
    }

    /**
     * @return count of inserts
     */
    public long insertCount() {
        return insertCount;
    }

    /**
     * @return count of updates
     */
    public long updateCount() {
        return updateCount;
    }

    /**
     * @return count of deletes
     */
    public long deleteCount() {
        return deleteCount;
    }

    /**
     * @return count of removals of a version of a record
     */
    public long backoutCount() {
        return backoutCount;
    }

    /**
     * @return count of removals of old versions of fully mature records (records that are committed, so that older
     * ancestor versions are no longer needed)
     */
    public long purgeCount() {
        return purgeCount;
    }

    /**
     * @return count of removals of a record and all of its ancestors, for records whose deletions have been committed
     */
    public long expungeCount() {
        return expungeCount;
    }

    @Override
    public String toString() {
        return "TableStatistics{" +
                "table='" + table + '\'' +
                ", readSeqCount=" + readSeqCount +
                ", readIdxCount=" + readIdxCount +
                ", insertCount=" + insertCount +
                ", updateCount=" + updateCount +
                ", deleteCount=" + deleteCount +
                ", backoutCount=" + backoutCount +
                ", purgeCount=" + purgeCount +
                ", expungeCount=" + expungeCount +
                '}';
    }

    static TableStatisticsBuilder builder(ObjectReference table) {
        return new TableStatisticsBuilder(table);
    }

    static final class TableStatisticsBuilder {

        private final ObjectReference table;
        private long readSeqCount;
        private long readIdxCount;
        private long insertCount;
        private long updateCount;
        private long deleteCount;
        private long backoutCount;
        private long purgeCount;
        private long expungeCount;

        private TableStatisticsBuilder(ObjectReference table) {
            assert table.size() <= 2 : "table should be an object reference of at most two identifiers";
            this.table = table;
        }

        void addStatistic(int statistic, long value) {
            switch (statistic) {
            case isc_info_read_seq_count -> readSeqCount = value;
            case isc_info_read_idx_count -> readIdxCount = value;
            case isc_info_insert_count -> insertCount = value;
            case isc_info_update_count -> updateCount = value;
            case isc_info_delete_count -> deleteCount = value;
            case isc_info_backout_count -> backoutCount = value;
            case isc_info_purge_count -> purgeCount = value;
            case isc_info_expunge_count -> expungeCount = value;
            default -> System.getLogger(TableStatisticsBuilder.class.getName()).log(System.Logger.Level.DEBUG,
                    "Unexpected information item {0} with value {1}, this is likely an implementation bug.",
                    statistic, value);
            }
        }

        TableStatistics toTableStatistics() {
            return new TableStatistics(table, readSeqCount, readIdxCount, insertCount, updateCount, deleteCount,
                    backoutCount, purgeCount, expungeCount);
        }

    }
}
