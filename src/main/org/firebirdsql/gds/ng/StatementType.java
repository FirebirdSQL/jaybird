// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

/**
 * Firebird statement types.
 *
 * @author Mark Rotteveel
 */
public enum StatementType {

    NONE(0),
    SELECT(ISCConstants.isc_info_sql_stmt_select) {
        @Override
        public boolean isTypeWithCursor() {
            return true;
        }
        @Override
        public boolean isTypeWithUpdateCounts() {
            // Not true for actual selects, but only for EXECUTE BLOCK and Firebird 5+ DML with RETURNING
            return true;
        }
    },
    INSERT(ISCConstants.isc_info_sql_stmt_insert) {
        @Override
        public boolean isTypeWithUpdateCounts() {
            return true;
        }
    },
    UPDATE(ISCConstants.isc_info_sql_stmt_update) {
        @Override
        public boolean isTypeWithUpdateCounts() {
            return true;
        }
    },
    DELETE(ISCConstants.isc_info_sql_stmt_delete) {
        @Override
        public boolean isTypeWithUpdateCounts() {
            return true;
        }
    },
    DDL(ISCConstants.isc_info_sql_stmt_ddl),
    GET_SEGMENT(ISCConstants.isc_info_sql_stmt_get_segment),
    PUT_SEGMENT(ISCConstants.isc_info_sql_stmt_put_segment),
    STORED_PROCEDURE(ISCConstants.isc_info_sql_stmt_exec_procedure) {
        @Override
        public boolean isTypeWithSingletonResult() {
            return true;
        }
        @Override
        public boolean isTypeWithUpdateCounts() {
            // Not true for EXECUTE PROCEDURE, but only true for EXECUTE BLOCK or DML with RETURNING
            return true;
        }
    },
    START_TRANSACTION(ISCConstants.isc_info_sql_stmt_start_trans),
    COMMIT(ISCConstants.isc_info_sql_stmt_commit),
    ROLLBACK(ISCConstants.isc_info_sql_stmt_rollback),
    SELECT_FOR_UPDATE(ISCConstants.isc_info_sql_stmt_select_for_upd) {
        @Override
        public boolean isTypeWithCursor() {
            return true;
        }
    },
    SET_GENERATOR(ISCConstants.isc_info_sql_stmt_set_generator),
    SAVE_POINT(ISCConstants.isc_info_sql_stmt_savepoint);

    /**
     * Lookup table from Firebird statement type code to StatementType value
     */
    private static final StatementType[] ID_TO_TYPE;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = ISCConstants.isc_info_sql_stmt_savepoint;

    static {
        StatementType[] tempTypes = new StatementType[MAX_VALUE + 1];
        for (StatementType type : StatementType.values()) {
            tempTypes[type.getStatementTypeCode()] = type;
        }
        ID_TO_TYPE = tempTypes;
    }

    private final int statementTypeCode;

    StatementType(int statementTypeCode) {
        this.statementTypeCode = statementTypeCode;
    }

    /**
     * @return Firebird statement type code
     */
    public int getStatementTypeCode() {
        return statementTypeCode;
    }

    /**
     * Indicates whether this statement type has a cursor.
     * <p>
     * Implementation assumes that this is the same for all Firebird versions.
     * </p>
     *
     * @return {@code true} statement type has a cursor
     */
    public boolean isTypeWithCursor() {
        return false;
    }

    /**
     * Indicates whether this statement type will produce a singleton result.
     * <p>
     * Implementation assumes that this is the same for all Firebird versions.
     * </p>
     *
     * @return {@code true} statement type will produce a singleton result
     */
    public boolean isTypeWithSingletonResult() {
        return false;
    }

    /**
     * Indicates whether this statement type can produce update counts.
     * <p>
     * Note that in some cases (e.g. an EXECUTE BLOCK) the update count might only be correct after retrieving all rows.
     * </p>
     *
     * @return {@code true} of this statement type produces an update count
     */
    public boolean isTypeWithUpdateCounts() {
        return false;
    }

    /**
     * Gets the enum value matching statementTypeCode.
     *
     * @param statementTypeCode
     *         Firebird statement type code
     * @return StatementType value matching statementTypeCode
     * @throws IllegalArgumentException
     *         If the statementTypeCode matches no enum value
     */
    public static StatementType valueOf(int statementTypeCode) {
        if (statementTypeCode < MIN_VALUE || statementTypeCode > MAX_VALUE) {
            throw new IllegalArgumentException(String.format("No StatementType with code %d", statementTypeCode));
        }
        StatementType value = ID_TO_TYPE[statementTypeCode];
        if (value == null) {
            throw new IllegalArgumentException(String.format("No StatementType with code %d", statementTypeCode));
        }
        return value;
    }
}
