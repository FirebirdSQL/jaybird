/*
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.firebirdsql.gds.ISCConstants;

/**
 * Firebird extensions to the {@link PreparedStatement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdPreparedStatement extends FirebirdStatement, PreparedStatement {
 
    /** A <code>SELECT</code> statement */
    int TYPE_SELECT = ISCConstants.isc_info_sql_stmt_select;

    /** An <code>INSERT</code> statement */
    int TYPE_INSERT = ISCConstants.isc_info_sql_stmt_insert;

    /** An <code>UPDATE</code> statement */
    int TYPE_UPDATE = ISCConstants.isc_info_sql_stmt_update;

    /** A <code>DELETE</code> statement */
    int TYPE_DELETE = ISCConstants.isc_info_sql_stmt_delete;

    /** A DDL statment */
    int TYPE_DDL = ISCConstants.isc_info_sql_stmt_ddl;

    /** A GET SEGMENT statement */
    int TYPE_GET_SEGMENT = ISCConstants.isc_info_sql_stmt_get_segment;

    /** A PUT SEGMENT statement */
    int TYPE_PUT_SEGMENT = ISCConstants.isc_info_sql_stmt_put_segment;

    /** An <code>EXEC PROCEDURE</code> statement */
    int TYPE_EXEC_PROCEDURE = ISCConstants.isc_info_sql_stmt_exec_procedure;

    /** A START TRANSACTION statement */
    int TYPE_START_TRANS = ISCConstants.isc_info_sql_stmt_start_trans;

    /** A <code>COMMIT</code> statement */
    int TYPE_COMMIT = ISCConstants.isc_info_sql_stmt_commit;

    /** A <code>ROLLBACK</code> statement */
    int TYPE_ROLLBACK = ISCConstants.isc_info_sql_stmt_rollback;

    /** A <code>SELECT FOR UPDATE</code> statement */
    int TYPE_SELECT_FOR_UPDATE = ISCConstants.isc_info_sql_stmt_select_for_upd;

    /** A <code>SET GENERATOR</code> statement */
    int TYPE_SET_GENERATOR = ISCConstants.isc_info_sql_stmt_set_generator;

    /**
     * Get the execution plan of this PreparedStatement
     *
     * @return The execution plan of the statement
     */
    String getExecutionPlan() throws SQLException;

    /**
     * Get the detailed execution plan of this PreparedStatement
     *
     * @return The detailed execution plan of the statement
     */
    String getExplainedExecutionPlan() throws SQLException;

    /**
     * Get the statement type of this PreparedStatement.
     * The returned value will be one of the <code>TYPE_*</code> constant
     * values.
     *
     * @return The identifier for the given statement's type
     */
    int getStatementType() throws SQLException;
   
}
