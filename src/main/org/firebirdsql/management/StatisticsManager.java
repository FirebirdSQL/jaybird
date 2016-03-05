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
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 */
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
     * Request statistics on 
     */

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
     * Invoking this method is equivalent to the behaviour of {@code gstat -t <table name>} on the command-line.
     * </p>
     *
     * @param tableNames
     *         array of table names to analyze.
     * @throws SQLException
     *         if something went wrong.
     */
    void getTableStatistics(String[] tableNames) throws SQLException;
}
