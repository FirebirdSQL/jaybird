/*
 * Public Firebird Java API.
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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */

package org.firebirdsql.gds;

/**
 * <code>isc_stmt_handle</code> describes a handle to a database statement.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface IscStmtHandle {
    
    int TYPE_UNKNOWN = -1;
    int TYPE_SELECT = ISCConstants.isc_info_sql_stmt_select;
    int TYPE_INSERT = ISCConstants.isc_info_sql_stmt_insert;
    int TYPE_UPDATE = ISCConstants.isc_info_sql_stmt_update;
    int TYPE_DELETE = ISCConstants.isc_info_sql_stmt_delete;
    int TYPE_DDL = ISCConstants.isc_info_sql_stmt_ddl;
    int TYPE_GET_SEGMENT = ISCConstants.isc_info_sql_stmt_get_segment;
    int TYPE_PUT_SEGMENT = ISCConstants.isc_info_sql_stmt_put_segment;
    int TYPE_EXEC_PROCEDURE = ISCConstants.isc_info_sql_stmt_exec_procedure;
    int TYPE_START_TRANS = ISCConstants.isc_info_sql_stmt_start_trans;
    int TYPE_COMMIT = ISCConstants.isc_info_sql_stmt_commit;
    int TYPE_ROLLBACK = ISCConstants.isc_info_sql_stmt_rollback;
    int TYPE_SELECT_FOR_UPDATE = ISCConstants.isc_info_sql_stmt_select_for_upd;
    int TYPE_SET_GENERATOR = ISCConstants.isc_info_sql_stmt_set_generator;

    /**
     * Get the input data structure that contains data that is put into
     * the statement.
     *
     * @return The input data structure
     */
    XSQLDA getInSqlda();

    /**
     * Get the output data structure that contains data that is retrieved
     * from the statement.
     *
     * @return The output data structure
     */
    XSQLDA getOutSqlda();

    /**
     * Retrieve whether all rows have been fetched of the rows selected
     * by executing this statement.
     *
     * @return <code>true</code> if all rows have been fetched,
     *         <code>false</code> otherwise
     */
    boolean isAllRowsFetched();
    
    /**
     * Get the execution plan from the statement.
     * 
     * @return execution plan or <code>null</code> if the execution plan was 
     * not fetched from the server.
     */
    String getExecutionPlan();
    
    /**
     * Get the statement type.
     * 
     * @return one of the constants defined in this interface or {@link #TYPE_UNKNOWN}
     * when no statement type was received from the server.
     */
    int getStatementType();
    
}
