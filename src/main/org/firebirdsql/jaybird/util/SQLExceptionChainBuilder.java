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
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Helper class for building {@link java.sql.SQLException} chains.
 * <p>
 * <b>NOTE</b>: This class is not thread-safe; an instance should only be used on
 * a single thread or with proper external synchronisation.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
@NullMarked
public final class SQLExceptionChainBuilder {

    private @Nullable SQLException root;

    /**
     * Appends the passed SQLException to the exception chain.
     * <p>
     * If this SQLExceptionChainBuilder does not have a root, {@code sqle} will be come the root.
     * </p>
     *
     * @param sqle
     *         SQLException to add to the chain.
     * @return this SQLExceptionChainBuilder
     */
    public SQLExceptionChainBuilder append(SQLException sqle) {
        if (root == null) {
            root = sqle;
        } else {
            root.setNextException(sqle);
        }
        return this;
    }

    /**
     * Adds the passed SQLException as the first exception in the chain.
     * <p>
     * If this SQLExceptionChainBuilder already has a root, that root will be chained to {@code sqle}, and {@code sqle}
     * becomes the new root.
     * </p>
     *
     * @param sqle
     *         SQLException to add to the chain
     * @return this SQLExceptionChainBuilder
     * @since 5
     */
    public SQLExceptionChainBuilder addFirst(SQLException sqle) {
        SQLException originalRoot = root;
        if (originalRoot != null) {
            sqle.setNextException(originalRoot);
        }
        root = sqle;
        return this;
    }

    /**
     * @return {@code true} if this SQLExceptionChainBuilder contains at least one SQLException.
     */
    public boolean hasException() {
        return root != null;
    }

    /**
     * @return the root SQLException or {@code null} if no SQLException was added to this SQLExceptionChainBuilder
     */
    public @Nullable SQLException getException() {
        return root;
    }

    /**
     * @return the root SQLException or empty if no SQLException was added to this SQLExceptionChainBuilder
     */
    public @NonNull Optional<SQLException> optException() {
        return Optional.ofNullable(root);
    }

    /**
     * Throws the root SQLException stored in the chain, if present; otherwise does nothing.
     *
     * @throws SQLException
     *         the root SQLException in this chain builder
     * @since 6
     */
    public void throwIfPresent() throws SQLException {
        SQLException root = this.root;
        if (root != null) throw root;
    }

}
