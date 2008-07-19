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

import org.firebirdsql.gds.IscStmtHandle;

/**
 * Firebird extensions to the {@link PreparedStatement} interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public interface FirebirdPreparedStatement extends FirebirdStatement, PreparedStatement {
 
    /** A <code>SELECT</code> statement */
    public static final int TYPE_SELECT =  IscStmtHandle.TYPE_SELECT;

    /** An <code>INSERT</code> statement */
    public static final int TYPE_INSERT = IscStmtHandle.TYPE_INSERT;

    /** An <code>UPDATE</code> statement */
    public static final int TYPE_UPDATE = IscStmtHandle.TYPE_UPDATE;

    /** A <code>DELETE</code> statement */
    public static final int TYPE_DELETE = IscStmtHandle.TYPE_DELETE;

    /** A DDL statment */
    public static final int TYPE_DDL = IscStmtHandle.TYPE_DDL;

    /** A GET SEGMENT statement */
    public static final int TYPE_GET_SEGMENT = IscStmtHandle.TYPE_GET_SEGMENT;

    /** A PUT SEGMENT statement */
    public static final int TYPE_PUT_SEGMENT = IscStmtHandle.TYPE_PUT_SEGMENT;

    /** An <code>EXEC PROCEDURE</code> statement */
    public static final int TYPE_EXEC_PROCEDURE = IscStmtHandle.TYPE_EXEC_PROCEDURE;

    /** A START TRANSACTION statement */
    public static final int TYPE_START_TRANS = IscStmtHandle.TYPE_START_TRANS;

    /** A <code>COMMIT</code> statement */
    public static final int TYPE_COMMIT = IscStmtHandle.TYPE_COMMIT;

    /** A <code>ROLLBACK</code> statement */
    public static final int TYPE_ROLLBACK = IscStmtHandle.TYPE_ROLLBACK;

    /** A <code>SELECT FOR UPDATE</code> statement */
    public static final int TYPE_SELECT_FOR_UPDATE = IscStmtHandle.TYPE_SELECT_FOR_UPDATE;

    /** A <code>SET GENERATOR</code> statement */
    public static final int TYPE_SET_GENERATOR = IscStmtHandle.TYPE_SET_GENERATOR;

    /**
     * Get the execution plan of this PreparedStatement
     *
     * @return The execution plan of the statement
     */
    String getExecutionPlan() throws FBSQLException;

    /**
     * Get the statement type of this PreparedStatement.
     * The returned value will be one of the <code>TYPE_*</code> constant
     * values.
     *
     * @return The identifier for the given statement's type
     */
    int getStatementType() throws FBSQLException;
   
}
