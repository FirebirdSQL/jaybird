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
package org.firebirdsql.util;

import java.sql.SQLException;

/**
 * Helper class for building {@link java.sql.SQLException} chains.
 * <p>
 * <b>NOTE</b>: This class is not thread-safe; an instance should only be used on
 * a single thread or with proper external synchronisation.
 * </p>
 *
 * @param <E>
 *         type of SQLException (definition: {@code E extends SQLException})
 * @author Mark Rotteveel
 * @since 2.2
 */
@InternalApi
public final class SQLExceptionChainBuilder<E extends SQLException> {

    private E root;

    /**
     * Create SQLExceptionChainBuilder
     */
    public SQLExceptionChainBuilder() {
        this(null);
    }

    /**
     * Create SQLExceptionChainBuilder with the specified root exception.
     *
     * @param root
     *         root SQLException
     */
    public SQLExceptionChainBuilder(E root) {
        this.root = root;
    }

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
    public SQLExceptionChainBuilder<E> append(E sqle) {
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
    public SQLExceptionChainBuilder<E> addFirst(E sqle) {
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
    public E getException() {
        return root;
    }
}
