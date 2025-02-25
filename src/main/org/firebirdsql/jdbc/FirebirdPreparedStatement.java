// SPDX-FileCopyrightText: Copyright 2003-2004 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2019 Vasiliy Yashkov
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;

/**
 * Firebird extensions to the {@link PreparedStatement} interface.
 * 
 * @author Roman Rokytskyy
 */
@SuppressWarnings("unused")
public interface FirebirdPreparedStatement extends FirebirdStatement, PreparedStatement {
 
    /** A {@code SELECT} or multi-row DML with {@code RETURNING} statement. */
    int TYPE_SELECT = ISCConstants.isc_info_sql_stmt_select;

    /** An {@code INSERT} statement. */
    int TYPE_INSERT = ISCConstants.isc_info_sql_stmt_insert;

    /** An {@code UPDATE} statement. */
    int TYPE_UPDATE = ISCConstants.isc_info_sql_stmt_update;

    /** A {@code DELETE} statement. */
    int TYPE_DELETE = ISCConstants.isc_info_sql_stmt_delete;

    /** A DDL statement. */
    int TYPE_DDL = ISCConstants.isc_info_sql_stmt_ddl;

    /** A GET SEGMENT statement. */
    int TYPE_GET_SEGMENT = ISCConstants.isc_info_sql_stmt_get_segment;

    /** A PUT SEGMENT statement. */
    int TYPE_PUT_SEGMENT = ISCConstants.isc_info_sql_stmt_put_segment;

    /** An {@code EXECUTE PROCEDURE} or singleton DML with {@code RETURNING} statement. */
    int TYPE_EXEC_PROCEDURE = ISCConstants.isc_info_sql_stmt_exec_procedure;

    /** A {@code SET TRANSACTION} statement. */
    int TYPE_START_TRANS = ISCConstants.isc_info_sql_stmt_start_trans;

    /** A {@code COMMIT} statement. */
    int TYPE_COMMIT = ISCConstants.isc_info_sql_stmt_commit;

    /** A {@code ROLLBACK} statement. */
    int TYPE_ROLLBACK = ISCConstants.isc_info_sql_stmt_rollback;

    /** A {@code SELECT FOR UPDATE} statement. */
    int TYPE_SELECT_FOR_UPDATE = ISCConstants.isc_info_sql_stmt_select_for_upd;

    /** A {@code SET GENERATOR} statement. */
    int TYPE_SET_GENERATOR = ISCConstants.isc_info_sql_stmt_set_generator;

    /**
     * A {@code SAVEPOINT} statement.
     *
     * @since 6
     */
    int TYPE_SAVEPOINT = ISCConstants.isc_info_sql_stmt_savepoint;

    /**
     * Get the statement type of this PreparedStatement.
     * <p>
     * The returned value is one of the {@code TYPE_*} constant values defined in this interface.
     * </p>
     *
     * @return The identifier for the given statement's type
     */
    int getStatementType() throws SQLException;
   
}
