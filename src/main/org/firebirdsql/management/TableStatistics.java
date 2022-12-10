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

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.Volatile;

import static java.util.Objects.requireNonNull;
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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FBTableStatisticsManager
 * @since 5
 */
@SuppressWarnings("unused")
@Volatile(reason = "Experimental")
public final class TableStatistics {

    private final String tableName;
    private final long readSeqCount;
    private final long readIdxCount;
    private final long insertCount;
    private final long updateCount;
    private final long deleteCount;
    private final long backoutCount;
    private final long purgeCount;
    private final long expungeCount;

    private TableStatistics(String tableName, long readSeqCount, long readIdxCount, long insertCount, long updateCount,
            long deleteCount, long backoutCount, long purgeCount, long expungeCount) {
        this.tableName = requireNonNull(tableName, "tableName");
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
     * @return table name
     */
    public String tableName() {
        return tableName;
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
                "tableName='" + tableName + '\'' +
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

    static TableStatisticsBuilder builder(String tableName) {
        return new TableStatisticsBuilder(tableName);
    }

    static final class TableStatisticsBuilder {

        private final String tableName;
        private long readSeqCount;
        private long readIdxCount;
        private long insertCount;
        private long updateCount;
        private long deleteCount;
        private long backoutCount;
        private long purgeCount;
        private long expungeCount;

        private TableStatisticsBuilder(String tableName) {
            this.tableName = tableName;
        }

        void addStatistic(int statistic, long value) {
            switch (statistic) {
            case isc_info_read_seq_count:
                readSeqCount = value;
                break;
            case isc_info_read_idx_count:
                readIdxCount = value;
                break;
            case isc_info_insert_count:
                insertCount = value;
                break;
            case isc_info_update_count:
                updateCount = value;
                break;
            case isc_info_delete_count:
                deleteCount = value;
                break;
            case isc_info_backout_count:
                backoutCount = value;
                break;
            case isc_info_purge_count:
                purgeCount = value;
                break;
            case isc_info_expunge_count:
                expungeCount = value;
                break;
            default: {
                Logger logger = LoggerFactory.getLogger(TableStatisticsBuilder.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("Unexpected information item, this is likely an implementation bug. "
                            + "Received item: " + statistic + " and value " + value);
                }
            }
            }
        }

        TableStatistics toTableStatistics() {
            return new TableStatistics(tableName, readSeqCount, readIdxCount, insertCount, updateCount, deleteCount,
                    backoutCount, purgeCount, expungeCount);
        }

    }
}
