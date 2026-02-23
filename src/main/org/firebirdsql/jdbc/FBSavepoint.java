// SPDX-FileCopyrightText: Copyright 2003-2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.util.InternalApi;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;

import static org.firebirdsql.jaybird.util.StringUtils.isNullOrBlank;

/**
 * Savepoint implementation.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * For the public API, refer to the {@link Savepoint} and {@link FirebirdSavepoint} interfaces.
 * </p>
 *
 * @author Roman Rokytskyy
 * @author Mark Rotteveel
 */
@InternalApi
public final class FBSavepoint implements FirebirdSavepoint {

    private static final String SAVEPOINT_ID_PREFIX = "SVPT";
    private final @Nullable Integer savepointId;
    private final String name;

    /**
     * Create an unnamed savepoint.
     *
     * @param savepointId
     *         ID of the savepoint
     */
    public FBSavepoint(int savepointId) {
        this.savepointId = savepointId;
        name = generateSavepointName(savepointId);
    }

    /**
     * Create a named savepoint.
     *
     * @param name
     *         name of the savepoint
     */
    public FBSavepoint(String name) {
        if (isNullOrBlank(name)) {
            throw new IllegalArgumentException("name must be non-null and non-blank");
        }
        this.savepointId = null;
        this.name = name;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBSavepoint that)) return false;
        return Objects.equals(this.savepointId, that.savepointId) &&
                this.name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "FBSavepoint[" +
                "savepointId=" + savepointId + ", " +
                "name=" + name + ']';
    }

}
