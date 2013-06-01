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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;

/**
 * Firebird statement types.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public enum FbStatementType {

    NONE(0),
    SELECT(ISCConstants.isc_info_sql_stmt_select),
    INSERT(ISCConstants.isc_info_sql_stmt_insert),
    UPDATE(ISCConstants.isc_info_sql_stmt_update),
    DELETE(ISCConstants.isc_info_sql_stmt_delete),
    DDL(ISCConstants.isc_info_sql_stmt_ddl),
    GET_SEGMENT(ISCConstants.isc_info_sql_stmt_get_segment),
    PUT_SEGMENT(ISCConstants.isc_info_sql_stmt_put_segment),
    STORED_PROCEDURE(ISCConstants.isc_info_sql_stmt_exec_procedure),
    START_TRANSACTION(ISCConstants.isc_info_sql_stmt_start_trans),
    COMMIT(ISCConstants.isc_info_sql_stmt_commit),
    ROLLBACK(ISCConstants.isc_info_sql_stmt_rollback),
    SELECT_FOR_UPDATE(ISCConstants.isc_info_sql_stmt_select_for_upd),
    SET_GENERATOR(ISCConstants.isc_info_sql_stmt_set_generator),
    SAVE_POINT(ISCConstants.isc_info_sql_stmt_savepoint);

    private final int statementTypeCode;

    private FbStatementType(int statementTypeCode) {
        this.statementTypeCode = statementTypeCode;
    }

    public int getStatementTypeCode() {
        return statementTypeCode;
    }
}
