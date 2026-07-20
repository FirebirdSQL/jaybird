// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
    UNKNOWN(LocalStatementClass.UNKNOWN),
    /**
     * {@code SELECT} statement, including selectable stored procedures.
     */
    SELECT(LocalStatementClass.DML),
    /**
     * {@code EXECUTE PROCEDURE} statement.
     */
    EXECUTE_PROCEDURE(LocalStatementClass.DML),
    /**
     * {@code CALL} statement.
     *
     * @since 7
     */
    CALL(LocalStatementClass.DML),
    /**
     * JDBC call escape without return parameter (i.e. {@code {call procname ...}}).
     * <p>
     * Callable statement V2 only.
     * </p>
     *
     * @since 7
     */
    JDBC_CALL_ESCAPE(LocalStatementClass.DML),
    /**
     * JDBC call escape with return parameter (i.e. {@code {?=call procname ...}}).
     * <p>
     * Callable statement V2 only.
     * </p>
     *
     * @since 7
     */
    JDBC_CALL_RETURN_ESCAPE(LocalStatementClass.DML),
    /**
     * JDBC escape (probably CALL escape) after {@code USING ... DO}, we can't handle this the same as a bare call
     * escape.
     * <p>
     * Callable statement V2 only.
     * </p>
     *
     * @since 7
     */
    JDBC_ESCAPE_AFTER_USING(LocalStatementClass.DML),
    /**
     * {@code UPDATE} statement (or {@code UPDATE OR INSERT} before detection is complete).
     */
    UPDATE(LocalStatementClass.DML),
    /**
     * {@code DELETE} statement.
     */
    DELETE(LocalStatementClass.DML),
    /**
     * {@code INSERT} statement.
     */
    INSERT(LocalStatementClass.DML),
    /**
     * {@code UPDATE OR INSERT} statement.
     */
    UPDATE_OR_INSERT(LocalStatementClass.DML),
    /**
     * {@code MERGE} statement.
     */
    MERGE(LocalStatementClass.DML),
    /**
     * {@code COMMIT [WORK]} (i.e. without {@code RETAIN}).
     *
     * @since 6
     */
    HARD_COMMIT(LocalStatementClass.TRANSACTION_BOUNDARY),
    /**
     * {@code ROLLBACK [WORK]} (i.e. without {@code RETAIN} or {@code TO SAVEPOINT ...}).
     *
     * @since 6
     */
    HARD_ROLLBACK(LocalStatementClass.TRANSACTION_BOUNDARY),
    /**
     * {@code SET TRANSACTION} statement.
     *
     * @since 6
     */
    SET_TRANSACTION(LocalStatementClass.TRANSACTION_BOUNDARY),
    /**
     * No specific classification applied (i.e. Jaybird is not (yet) interested in this type), or detection failed.
     */
    OTHER(LocalStatementClass.UNKNOWN);

    private final LocalStatementClass statementClass;

    LocalStatementType(LocalStatementClass statementClass) {
        this.statementClass = statementClass;
    }

    /**
     * Class of the statement.
     *
     * @return statement class
     * @since 6
     */
    public LocalStatementClass statementClass() {
        return statementClass;
    }

}
