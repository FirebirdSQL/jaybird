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

package org.firebirdsql.jdbc.parser;

import org.firebirdsql.util.InternalApi;

/**
 * DML statement types.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
@InternalApi
public enum DmlStatementType {
    /**
     * Null-state before detection.
     */
    UNKNOWN,
    /**
     * {@code UPDATE} statement (or {@code UPDATE OR INSERT} before detection is complete)
     */
    UPDATE,
    /**
     * {@code DELETE} statement
     */
    DELETE,
    /**
     * {@code INSERT} statement
     */
    INSERT,
    /**
     * {@code UPDATE OR INSERT} statement
     */
    UPDATE_OR_INSERT,
    /**
     * {@code MERGE} statement
     */
    MERGE,
    /**
     * Any other statement (e.g. {@code SELECT}, DDL or management statements).
     */
    OTHER
}
