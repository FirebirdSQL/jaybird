/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * The <code>long</code> values returned from the <code>getLongXXXCount</code> methods should be considered as unsigned
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class SqlCountHolder {

    private final long updateCount;
    private final long deleteCount;
    private final long insertCount;
    private final long selectCount;

    public SqlCountHolder(final long updateCount, final long deleteCount, final long insertCount, final long selectCount) {
        this.updateCount = updateCount;
        this.deleteCount = deleteCount;
        this.insertCount = insertCount;
        this.selectCount = selectCount;
    }

    /**
     * Returns the count as an <code>int</code>. Values larger than {@link Integer#MAX_VALUE} are returned as 0.
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
     * @return Update count as <code>int</code>, or 0 if the update count was too large.
     * @see #getLongUpdateCount()
     */
    public int getIntegerUpdateCount() {
        return countAsInteger(updateCount);
    }

    /**
     * @return Number of updated records
     */
    public long getLongUpdateCount() {
        return updateCount;
    }

    /**
     * @return Delete count as <code>int</code>, or 0 if the delete count was too large.
     * @see #getLongDeleteCount()
     */
    public int getIntegerDeleteCount() {
        return countAsInteger(deleteCount);
    }

    /**
     * @return Number of deleted records
     */
    public long getLongDeleteCount() {
        return deleteCount;
    }

    /**
     * @return Insert count as <code>int</code>, or 0 if the insert count was too large.
     * @see #getLongInsertCount()
     */
    public int getIntegerInsertCount() {
        return countAsInteger(insertCount);
    }

    /**
     * @return Number of inserted records
     */
    public long getLongInsertCount() {
        return insertCount;
    }

    /**
     * @return Select count as <code>int</code>, or 0 if the select count was too large.
     * @see #getLongSelectCount()
     */
    public int getIntegerSelectCount() {
        return countAsInteger(selectCount);
    }

    /**
     * @return Number of selected records
     */
    public long getLongSelectCount() {
        return selectCount;
    }
}
