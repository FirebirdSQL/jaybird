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
package org.firebirdsql.gds.ng;

/**
 * Class for holding the SQL counts (update, delete, select, insert) for a statement execution.
 * <p>
 * The {@code long} values returned from the {@code getLongXXXCount} methods should be considered as unsigned.
 * </p>
 *
 * @param updateCount
 *         record count affected by an update
 * @param deleteCount
 *         record count affected by a delete
 * @param insertCount
 *         record count affected by an insert
 * @param selectCount
 *         record count selected
 * @author Mark Rotteveel
 * @since 3.0
 */
public record SqlCountHolder(long updateCount, long deleteCount, long insertCount, long selectCount) {

    private static final SqlCountHolder EMPTY = new SqlCountHolder(0, 0, 0, 0);

    /**
     * Returns the count as an {@code int}. Values larger than {@link Integer#MAX_VALUE} are returned as 0.
     *
     * @param count The count value to convert
     * @return Converted value
     */
    private static int countAsInteger(long count) {
        if (count > Integer.MAX_VALUE) {
            return 0;
        }
        return (int) count;
    }

    /**
     * @return Update count as {@code int}, or 0 if the update count was too large.
     * @see #updateCount()
     */
    public int getIntegerUpdateCount() {
        return countAsInteger(updateCount);
    }

    /**
     * @return Number of updated records
     * @see #updateCount()
     * @deprecated Use {@link #updateCount()}, will be removed in Jaybird 7
     */
    @Deprecated(forRemoval = true, since = "6")
    public long getLongUpdateCount() {
        return updateCount;
    }

    /**
     * @return Delete count as {@code int}, or 0 if the delete count was too large.
     * @see #deleteCount()
     */
    public int getIntegerDeleteCount() {
        return countAsInteger(deleteCount);
    }

    /**
     * @return Number of deleted records
     * @see #deleteCount()
     * @deprecated Use {@link #deleteCount()}, will be removed in Jaybird 7
     */
    @Deprecated(forRemoval = true, since = "6")
    public long getLongDeleteCount() {
        return deleteCount;
    }

    /**
     * @return Insert count as {@code int}, or 0 if the insert count was too large.
     * @see #insertCount()
     */
    public int getIntegerInsertCount() {
        return countAsInteger(insertCount);
    }

    /**
     * @return Number of inserted records
     * @see #insertCount()
     * @deprecated Use {@link #insertCount()}, will be removed in Jaybird 7
     */
    @Deprecated(forRemoval = true, since = "6")
    public long getLongInsertCount() {
        return insertCount;
    }

    /**
     * @return Select count as {@code int}, or 0 if the select count was too large.
     * @see #selectCount()
     */
    public int getIntegerSelectCount() {
        return countAsInteger(selectCount);
    }

    /**
     * @return Number of selected records
     * @see #selectCount()
     * @deprecated Use {@link #selectCount()}, will be removed in Jaybird 7
     */
    @Deprecated(forRemoval = true, since = "6")
    public long getLongSelectCount() {
        return selectCount;
    }

    /**
     * @return a {@code SqlCountHolder} with all values set to 0, representing empty, or no counts
     */
    public static SqlCountHolder empty() {
        return EMPTY;
    }

}
