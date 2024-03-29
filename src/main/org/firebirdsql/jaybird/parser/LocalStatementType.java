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
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Statement types.
 * <p>
 * NOTE: Jaybird may take shortcuts during detection (e.g. only look at first keyword), so an invalid statement
 * might be classified anyway.
 * </p>
 * <p>
 * The types of this enum are decided by the needs of Jaybird, and do not necessarily cover all statement types.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@InternalApi
public enum LocalStatementType {
    /**
     * Null-state before detection.
     */
    UNKNOWN,
    /**
     * {@code SELECT} statement, including selectable stored procedures.
     */
    SELECT,
    /**
     * {@code EXECUTE PROCEDURE} statement.
     */
    EXECUTE_PROCEDURE,
    /**
     * {@code UPDATE} statement (or {@code UPDATE OR INSERT} before detection is complete).
     */
    UPDATE,
    /**
     * {@code DELETE} statement.
     */
    DELETE,
    /**
     * {@code INSERT} statement.
     */
    INSERT,
    /**
     * {@code UPDATE OR INSERT} statement.
     */
    UPDATE_OR_INSERT,
    /**
     * {@code MERGE} statement.
     */
    MERGE,
    /**
     * {@code COMMIT [WORK]} (i.e. without {@code RETAIN}).
     *
     * @since 6
     */
    HARD_COMMIT,
    /**
     * {@code ROLLBACK [WORK]} (i.e. without {@code RETAIN} or {@code TO SAVEPOINT ...}).
     *
     * @since 6
     */
    HARD_ROLLBACK,
    /**
     * {@code SET TRANSACTION} statement.
     *
     * @since 6
     */
    SET_TRANSACTION,
    /**
     * No specific classification applied (i.e. Jaybird is not (yet) interested in this type), or detection failed.
     */
    OTHER
}
