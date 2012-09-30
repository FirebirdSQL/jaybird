/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
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
 * @param <E> Type of SQLException (definition: E extends SQLException) 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
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
     * @param root Root SQLException
     */
    public SQLExceptionChainBuilder(E root) {
        this.root = root;
    }
    
    /**
     * Appends the passed SQLException to the exception chain.
     * <p>
     * If this SQLExceptionChainBuilder does not have a root, <code>sqle</code> will be come
     * the root.
     * </p>
     * 
     * @param sqle SQLException to add to the chain.
     * @return this SQLExceptionChainBuilder
     */
    public SQLExceptionChainBuilder append(E sqle) {
        if (root == null) {
            root = sqle;
        } else {
            root.setNextException(sqle);
        }
        return this;
    }
    
    /**
     * @return <code>true</code> if this SQLExceptionChainBuilder contains at least one SQLException.
     */
    public boolean hasException() {
        return root != null;
    }
    
    /**
     * @return The root SQLException or <code>null</code> if no SQLException was added to this SQLExceptionChainBuilder
     */
    public E getException() {
        return root;
    }
}
