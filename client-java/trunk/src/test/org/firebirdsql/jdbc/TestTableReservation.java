/*
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

import java.sql.*;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;


public class TestTableReservation extends FBTestBase {
    
    protected static final int READ_COMMITTED = TransactionParameterBuffer.READ_COMMITTED;
    protected static final int CONCURRENCY = TransactionParameterBuffer.CONCURRENCY;
    protected static final int CONSISTENCY = TransactionParameterBuffer.CONSISTENCY;
    
    protected static final int LOCK_READ = TransactionParameterBuffer.LOCK_READ;
    protected static final int LOCK_WRITE = TransactionParameterBuffer.LOCK_WRITE;
    
    protected static final int SHARED = TransactionParameterBuffer.SHARED;
    protected static final int PROTECTED = TransactionParameterBuffer.PROTECTED;
    protected static final int EXCLUSIVE = TransactionParameterBuffer.EXCLUSIVE;

    public TestTableReservation(String name) {
        super(name);
    }

    protected static final String CREATE_TABLE_1 = ""
        + "CREATE TABLE table_1("
        + "  ID INTEGER NOT NULL PRIMARY KEY"
        + ")"
        ;
    
    protected static final String CREATE_TABLE_2 = ""
        + "CREATE TABLE table_2("
        + "  ID INTEGER NOT NULL PRIMARY KEY"
        + ")"
        ;
    
    protected static final String DROP_TABLE_1 = ""
        + "DROP TABLE table_1"
        ;
    
    protected static final String DROP_TABLE_2 = ""
        + "DROP TABLE table_2"
        ;
    
    protected static final String INSERT_TABLE_1 = ""
        + "INSERT INTO table_1 VALUES(?)"
        ;
    
    protected static final String INSERT_TABLE_2 = ""
        + "INSERT INTO table_2 VALUES(?)"
        ;
    
    protected static final String SELECT_TABLE_1 = ""
        + "SELECT id FROM table_1 WHERE id = ?"
        ;
    
    protected static final String SELECT_TABLE_2 = ""
        + "SELECT id FROM table_2 WHERE id = ?"
        ;
    
    protected FirebirdConnection connection1;
    protected FirebirdConnection connection2;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Connection connection = getConnectionViaDriverManager();
        try {
            executeCreateTable(connection, CREATE_TABLE_1);
            executeCreateTable(connection, CREATE_TABLE_2);
        } finally {
            connection.close();
        }
        
        connection1 = getConnectionViaDriverManager();
        connection1.setAutoCommit(false);
        
        connection2 = getConnectionViaDriverManager();
        connection2.setAutoCommit(false);
    }

    protected void tearDown() throws Exception {
        
        try {
            try {
                connection2.close();
            } finally {
                connection1.close();
            }
        } finally {
            Connection connection = getConnectionViaDriverManager();
            try {
                executeDropTable(connection, DROP_TABLE_1);
                executeDropTable(connection, DROP_TABLE_2);
            } finally {
                connection.close();
            }
    
            
            super.tearDown();
        }
    }

    protected void execute(Connection connection, String sql, Object[] params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            boolean query = stmt.execute();
            if (query) {
                ResultSet rs = stmt.getResultSet();
                while(rs.next()) {
                    Object dummy = rs.getObject(1);
                }
            }
        } finally {
            stmt.close();
        }
    }
    
    protected void prepareTPB(FirebirdConnection connection, 
            int isolationLevel, int lockMode, String tableName, int lockType, boolean readOnly) 
    throws SQLException {
        TransactionParameterBuffer tpb = connection.createTransactionParameterBuffer();
        
        // specify new isolation level
        tpb.addArgument(isolationLevel);
        if (isolationLevel == TransactionParameterBuffer.READ_COMMITTED)
            tpb.addArgument(TransactionParameterBuffer.REC_VERSION);
        
        tpb.addArgument(!readOnly ? TransactionParameterBuffer.WRITE : TransactionParameterBuffer.READ);
        tpb.addArgument(TransactionParameterBuffer.NOWAIT);
        
        tpb.addArgument(lockMode, tableName);
        tpb.addArgument(lockType);
        
        connection.setTransactionParameters(tpb);
    }

    public void testProtectedWriteProtectedWrite() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", PROTECTED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_WRITE, "TABLE_1", PROTECTED, false);
            
            execute(connection1, INSERT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }
    
    public void testProtectedWriteProtectedRead() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", PROTECTED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", PROTECTED, false);
            
            execute(connection1, INSERT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    public void testProtectedWriteSharedWrite() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", PROTECTED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_WRITE, "TABLE_1", SHARED, false);
            
            execute(connection1, INSERT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }
    
    public void testProtectedWriteSharedRead() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", PROTECTED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", SHARED, false);
            
            execute(connection1, INSERT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    public void testSharedWriteSharedRead() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", SHARED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", SHARED, false);
            
            execute(connection1, INSERT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }
    
    public void testSharedReadSharedRead() throws SQLException {
        prepareTPB(connection1, CONSISTENCY, LOCK_READ, "TABLE_1", SHARED, false);
        prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", SHARED, false);
        
        execute(connection1, SELECT_TABLE_1, new Object[] {new Integer(1)});
        execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
    }

    public void testProtectedReadSharedRead() throws SQLException {
        prepareTPB(connection1, CONSISTENCY, LOCK_READ, "TABLE_1", PROTECTED, false);
        prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", SHARED, false);
        
        execute(connection1, SELECT_TABLE_1, new Object[] {new Integer(1)});
        execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
    }
    
    public void testSharedWriteSharedWrite() throws SQLException {
        prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", SHARED, false);
        prepareTPB(connection2, CONSISTENCY, LOCK_WRITE, "TABLE_1", SHARED, false);
        
        execute(connection1, SELECT_TABLE_1, new Object[] {new Integer(1)});
        execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
    }
    
    public void testSharedWriteProtectedRead() throws SQLException {
        try {
            prepareTPB(connection1, CONSISTENCY, LOCK_WRITE, "TABLE_1", SHARED, false);
            prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", PROTECTED, false);
            
            execute(connection1, SELECT_TABLE_1, new Object[] {new Integer(1)});
            execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
            
            fail();
        } catch(SQLException ex) {
            int fbErrorCode = ex.getErrorCode();
            assertEquals(ISCConstants.isc_lock_conflict, fbErrorCode);
        }
    }

    public void testProtectedReadProtectedRead() throws SQLException {
        prepareTPB(connection1, CONSISTENCY, LOCK_READ, "TABLE_1", PROTECTED, false);
        prepareTPB(connection2, CONSISTENCY, LOCK_READ, "TABLE_1", PROTECTED, false);
        
        execute(connection1, SELECT_TABLE_1, new Object[] {new Integer(1)});
        execute(connection2, SELECT_TABLE_1, new Object[] {new Integer(1)});
    }

}
