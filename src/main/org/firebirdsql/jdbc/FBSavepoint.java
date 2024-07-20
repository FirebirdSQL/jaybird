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
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;

/**
 * Savepoint implementation.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link java.sql.Savepoint} and {@link FirebirdSavepoint} interfaces.
 * </p>
 *
 * @param savepointId
 *         numeric savepoint id (must be non-{@code null} if {@code name} is {@code null}).
 * @param name
 *         savepoint name (must be non-{@code null} if {@code savepointId} is {@code null})
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@InternalApi
public record FBSavepoint(Integer savepointId, String name) implements FirebirdSavepoint {

    private static final String SAVEPOINT_ID_PREFIX = "SVPT";

    public FBSavepoint {
        if (savepointId == null && name == null) {
            throw new NullPointerException("savepointId and name cannot both be null");
        } else if (name == null) {
            name = generateSavepointName(savepointId);
        } else if (savepointId != null) {
            throw new IllegalArgumentException("savepointId cannot be non-null if name is not null");
        } else if (name.isBlank()) {
            throw new IllegalArgumentException("name must be non-blank");
        }
    }

    /**
     * Create an unnamed savepoint.
     *
     * @param savepointId
     *         ID of the savepoint
     */
    public FBSavepoint(int savepointId) {
        this(savepointId, null);
    }

    /**
     * Create a named savepoint.
     *
     * @param name
     *         name of the savepoint
     */
    public FBSavepoint(String name) {
        this(null, name);
    }

    /**
     * Generate a savepoint name for the specified savepoint id.
     *
     * @param savePointId
     *         savepoint id
     * @return valid savepoint name
     */
    private static String generateSavepointName(int savePointId) {
        if (savePointId >= 0) {
            return SAVEPOINT_ID_PREFIX + savePointId;
        }
        return SAVEPOINT_ID_PREFIX + '_' + Math.abs(savePointId);
    }

    @Override
    public int getSavepointId() throws SQLException {
        if (savepointId == null) {
            throw new SQLException("Savepoint is named");
        }
        return savepointId;
    }

    @Override
    public String getSavepointName() throws SQLException {
        if (savepointId != null) {
            throw new SQLException("Savepoint is unnamed");
        }
        return name;
    }

    String toSavepointStatement(QuoteStrategy quoteStrategy) {
        return "SAVEPOINT " + quoteStrategy.quoteObjectName(name);
    }

    String toRollbackStatement(QuoteStrategy quoteStrategy) {
        return "ROLLBACK TO " + quoteStrategy.quoteObjectName(name);
    }

    String toReleaseStatement(QuoteStrategy quoteStrategy) {
        return "RELEASE SAVEPOINT " + quoteStrategy.quoteObjectName(name) + " ONLY";
    }

}
