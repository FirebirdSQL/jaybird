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

import java.util.Map;
import java.util.HashMap;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.IscStmtHandle;
import org.firebirdsql.gds.ISCConstants;

import org.firebirdsql.jdbc.AbstractStatement;
import org.firebirdsql.jdbc.FirebirdStatementInfo;

/**
 * Factory class to create <code>FirebirdStatementInfo</code> objects
 * representing a given <code>AbstractStatment</code>. 
 */
public class FirebirdStatementInfoFactory {

    private static final byte[] REQUEST =
        new byte[] { ISCConstants.isc_info_sql_get_plan,
            ISCConstants.isc_info_sql_stmt_type,
            ISCConstants.isc_info_end };

    private static final int BUFFER_SIZE = 1024;

    private GDS gds;

    private static Map factoryMap = new HashMap();

    /**
     * Factory method to retrieve a <code>FirebirdStatementInfoFactory</code>.
     *
     * @param GDS A <code>GDS</code> implementation around which the 
     *        <code>FirebirdStatementInfoFactory</code> is to be built
     */
    public synchronized static FirebirdStatementInfoFactory getInstance(GDS gds){
        if (!factoryMap.containsKey(gds)){
            factoryMap.put(gds, new FirebirdStatementInfoFactory(gds));
        }
        return (FirebirdStatementInfoFactory)factoryMap.get(gds);
    }

    private FirebirdStatementInfoFactory(GDS gds){
        this.gds = gds;
    }

    /**
     * Retrieve the statement info for a <code>Statement</code>.
     * The given statement must either be a prepared statement, or must
     * have had an SQL statement previously run within it.
     *
     * @param stmt The <code>Statement</code> for which the statement info
     *        will be retrieved
     * @throws FBSQLException if the statement info cannot be retrieved
     * @throws GDSException if an underlying database access error occurs
     */
    public FirebirdStatementInfo getStatementInfo(AbstractStatement stmt)
            throws FBSQLException, GDSException {
        
        IscStmtHandle stmtHandle = stmt.fixedStmt;

        if (stmtHandle == null){
            throw new FBSQLException("Statement info cannot be retrieved for "
                    + "a statement that is not yet prepared and has not been "
                    + "executed");
        }
      
        int bufferSize = BUFFER_SIZE;
        while (true){
            byte[] buffer = gds.iscDsqlSqlInfo(
                stmtHandle, REQUEST, bufferSize); 
            if (buffer[0] != ISCConstants.isc_info_truncated){
                return buildStatementInfo(buffer);
            } 
            bufferSize *= 2;
        }
    }

    private StatementInfo buildStatementInfo(byte [] buffer) 
            throws FBSQLException {

        if (buffer[0] == ISCConstants.isc_info_end){
            throw new FBSQLException("Statement info could not be retrieved");
        }

        StatementInfo statementInfo = new StatementInfo();

        int dataLength = -1; 
        for (int i = 0; i < buffer.length; i++){
            switch(buffer[i]){
                case ISCConstants.isc_info_sql_get_plan:
                    dataLength = gds.iscVaxInteger(buffer, ++i, 2);
                    i += 2;
                    statementInfo.executionPlan = 
                        new String(buffer, i + 1, dataLength);
                    i += dataLength - 1;
                    break;
                case ISCConstants.isc_info_sql_stmt_type:
                    dataLength = gds.iscVaxInteger(buffer, ++i, 2);
                    i += 2;
                    int stmtType = gds.iscVaxInteger(buffer, i, dataLength);
                    i += dataLength;
                    statementInfo.statementType = stmtType;
                case ISCConstants.isc_info_end:
                case 0:
                    break;
                default:
                    throw new FBSQLException("Unknown data block [" 
                            + buffer[i] + "]");
            }
        }
        return statementInfo;
    }

    class StatementInfo implements FirebirdStatementInfo {
        
        private String executionPlan;

        private int statementType;

        public String getExecutionPlan(){
            return executionPlan;
        }

        public int getStatementType(){
            return statementType;
        }
    }
}
