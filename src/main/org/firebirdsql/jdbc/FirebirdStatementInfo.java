/*
 *
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ISCConstants;

/**
 * The <code>FirebirdStatementInfo</code> interface describes an object that
 * contains information about a <code>Statement</code> that has been prepared 
 * or executed within the database.
 */
public interface FirebirdStatementInfo {

    /** A <code>SELECT</code> statement */
    public static final int TYPE_SELECT = 
        ISCConstants.isc_info_sql_stmt_select;

    /** An <code>INSERT</code> statement */
    public static final int TYPE_INSERT =
        ISCConstants.isc_info_sql_stmt_insert;

    /** An <code>UPDATE</code> statement */
    public static final int TYPE_UPDATE =
        ISCConstants.isc_info_sql_stmt_update;

    /** A <code>DELETE</code> statement */
    public static final int TYPE_DELETE =
        ISCConstants.isc_info_sql_stmt_delete;

    /** A DDL statment */
    public static final int TYPE_DDL =
        ISCConstants.isc_info_sql_stmt_ddl;

    /** A GET SEGMENT statement */
    public static final int TYPE_GET_SEGMENT =
        ISCConstants.isc_info_sql_stmt_get_segment;

    /** A PUT SEGMENT statement */
    public static final int TYPE_PUT_SEGMENT =
        ISCConstants.isc_info_sql_stmt_put_segment;

    /** An <code>EXEC PROCEDURE</code> statement */
    public static final int TYPE_EXEC_PROCEDURE =
        ISCConstants.isc_info_sql_stmt_exec_procedure;

    /** A START TRANSACTION statement */
    public static final int TYPE_START_TRANS =
        ISCConstants.isc_info_sql_stmt_start_trans;

    /** A <code>COMMIT</code> statement */
    public static final int TYPE_COMMIT =
        ISCConstants.isc_info_sql_stmt_commit;

    /** A <code>ROLLBACK</code> statement */
    public static final int TYPE_ROLLBACK =
        ISCConstants.isc_info_sql_stmt_rollback;

    /** A <code>SELECT FOR UPDATE</code> statement */
    public static final int TYPE_SELECT_FOR_UPDATE =
        ISCConstants.isc_info_sql_stmt_select_for_upd;

    /** A <code>SET GENERATOR</code> statement */
    public static final int TYPE_SET_GENERATOR =
        ISCConstants.isc_info_sql_stmt_set_generator;


    /**
     * Get the execution plan of the related Statement
     *
     * @return The execution plan of the statement
     */
    String getExecutionPlan();

    /**
     * Get the statement type of the related Statement.
     * The returned value will be one of the <code>TYPE_*</code> constant
     * values.
     *
     * @return The identifier for the given statement's type
     */
    int getStatementType();

}
