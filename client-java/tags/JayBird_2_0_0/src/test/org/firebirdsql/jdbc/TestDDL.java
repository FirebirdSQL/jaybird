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

import org.firebirdsql.common.FBTestBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This test case checks if DDL statements are executed correctly.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestDDL extends FBTestBase {
    public static final String CREATE_MAIN_TABLE = ""
        + "CREATE TABLE main_table (" 
        + "  id INTEGER NOT NULL PRIMARY KEY"
        + ")"
        ;

    public static final String CREATE_DETAIL_TABLE = ""
        + "CREATE TABLE detail_table("
        + "  main_id INTEGER NOT NULL, "
        + "  some_data VARCHAR(20)"
        + ")"
        ;

    public static final String ADD_FOREIGN_KEY = ""
        + "ALTER TABLE detail_table ADD FOREIGN KEY(main_id) "
        + "REFERENCES main_table(id) ON DELETE CASCADE"
        ;
        
    public static final String DROP_DETAIL_TABLE = ""
        + "DROP TABLE detail_table"
        ;
        
    public static final String DROP_MAIN_TABLE = ""
        + "DROP TABLE main_table"
        ;
    
    public TestDDL(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(FBDriver.class.getName());
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void tryDrop(Connection connection, String sql) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch(SQLException sqlex) {
            // do nothing
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch(SQLException sqlex) {
                    // do nothing
                }
        }
    }
    
    private void executeUpdate(Connection connection, String sql) throws SQLException {
        Statement stmt = null;
        
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    public void testFKWithAutoCommit() throws Exception {
        Connection connection = null;
        try {
            connection = getConnectionViaDriverManager();
            
            tryDrop(connection, DROP_DETAIL_TABLE);
            tryDrop(connection, DROP_MAIN_TABLE);
            
            executeUpdate(connection, CREATE_MAIN_TABLE);
            executeUpdate(connection, CREATE_DETAIL_TABLE);
            
            try {
                executeUpdate(connection, ADD_FOREIGN_KEY);
            } catch(SQLException sqlex) {
                assertTrue("Should add foreign key constraint.", false);
            } 

            tryDrop(connection, DROP_DETAIL_TABLE);
            tryDrop(connection, DROP_MAIN_TABLE);
            
        } finally {
            if (connection != null)
                connection.close();
        }
    }
    
    public void testFKWithTx() throws Exception {
        Connection connection = null;
        try {
            connection = getConnectionViaDriverManager();
            connection.setAutoCommit(false);
            
            tryDrop(connection, DROP_DETAIL_TABLE);
            connection.commit();
            
            tryDrop(connection, DROP_MAIN_TABLE);
            connection.commit();
            
            executeUpdate(connection, CREATE_MAIN_TABLE);
            connection.commit();
            
            executeUpdate(connection, CREATE_DETAIL_TABLE);
            connection.commit();
            
            try {
                executeUpdate(connection, ADD_FOREIGN_KEY);
                connection.commit();
            } catch(SQLException sqlex) {
                assertTrue("Should add foreign key constraint.", false);
            } 
            
            connection.setAutoCommit(true);

            tryDrop(connection, DROP_DETAIL_TABLE);
            tryDrop(connection, DROP_MAIN_TABLE);
            
        } finally {
            if (connection != null)
                connection.close();
        }
    }
    
    public void testFKMixed() throws Exception {
        Connection connection = null;
        try {
            connection = getConnectionViaDriverManager();
            
            tryDrop(connection, DROP_DETAIL_TABLE);
            tryDrop(connection, DROP_MAIN_TABLE);
            
            executeUpdate(connection, CREATE_MAIN_TABLE);
            executeUpdate(connection, CREATE_DETAIL_TABLE);
            
            try {
                executeUpdate(connection, ADD_FOREIGN_KEY);
            } catch(SQLException sqlex) {
                sqlex.printStackTrace();
                assertTrue("Should add foreign key constraint.", false);
            } 
            
            connection.setAutoCommit(false);
            
            tryDrop(connection, DROP_DETAIL_TABLE);
            
            try {
                // Here it will fail, but should not, 
                // everything is correct from the programmers point of view
                connection.commit();
            } catch(SQLException sqlex) {
                connection.setAutoCommit(true);
                tryDrop(connection, DROP_DETAIL_TABLE);
            }
            
            connection.setAutoCommit(false);
            
            tryDrop(connection, DROP_MAIN_TABLE);
            
            try {
                // Here it will fail, but should not, 
                // everything is correct from the programmers point of view
                connection.commit();
            } catch(SQLException sqlex) {
                connection.setAutoCommit(true);
                tryDrop(connection, DROP_MAIN_TABLE);
            }
            
        } finally {
            if (connection != null)
                connection.close();
        }
    }
}
