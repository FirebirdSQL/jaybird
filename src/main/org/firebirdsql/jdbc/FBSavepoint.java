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
package org.firebirdsql.jdbc;

import java.sql.SQLException;

/**
 * Savepoint implementation.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBSavepoint implements FirebirdSavepoint {

    private static final String SAVEPOINT_ID_PREFIX = "SVPT";

    private final boolean named;
    private final int savepointId;
    private final String name;
    private boolean valid = true;

    /**
     * Create instance of this class.
     *
     * @param savepointId
     *         ID of the savepoint.
     */
    public FBSavepoint(int savepointId) {
        this(false, savepointId, getSavepointServerId(savepointId));
    }

    /**
     * Create instance of this class for the specified name.
     *
     * @param name
     *         name of the savepoint.
     */
    public FBSavepoint(String name) {
        this(true, -1, name);
    }

    private FBSavepoint(boolean named, int savepointId, String name) {
        this.named = named;
        this.savepointId = savepointId;
        this.name = name;
    }

    /**
     * Generate a savepoint name for the specified savepoint id.
     *
     * @param savePointId
     *         savepoint id.
     * @return valid savepoint name.
     */
    private static String getSavepointServerId(int savePointId) {
        if (savePointId >= 0) {
            return SAVEPOINT_ID_PREFIX + savePointId;
        }
        return SAVEPOINT_ID_PREFIX + '_' + Math.abs(savePointId);
    }

    /**
     * Get server savepoint name.
     * <p>
     * This method generates correct name for the savepoint that can be used in the SQL statement after
     * dialect-appropriate quoting.
     * </p>
     *
     * @return valid server-side name for the savepoint.
     */
    String getServerSavepointId() {
        return name;
    }

    @Override
    public int getSavepointId() throws SQLException {
        if (named) {
            throw new SQLException("Savepoint is named.");
        }
        return savepointId;
    }

    @Override
    public String getSavepointName() throws SQLException {
        if (!named) {
            throw new SQLException("Savepoint is unnamed.");
        }
        return name;
    }

    /**
     * Check if the savepoint is valid.
     *
     * @return {@code true} if savepoint is valid.
     */
    boolean isValid() {
        return valid;
    }

    /**
     * Make this savepoint invalid.
     */
    void invalidate() {
        this.valid = false;
    }

    /**
     * Check if objects are equal. For unnamed savepoints their IDs are checked, otherwise their names.
     *
     * @param obj
     *         object to test.
     * @return {@code true} if {@code obj} is equal to this object.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBSavepoint)) return false;

        FBSavepoint that = (FBSavepoint) obj;

        return this.named == that.named &&
                this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
