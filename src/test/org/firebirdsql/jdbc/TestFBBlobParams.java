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

/**
 * Describe class <code>TestFBBlobParam</code> here.
 *
 * @version 1.0
 */
public class TestFBBlobParams extends BaseFBTest {
    public static final String CREATE_TABLE = 
        "CREATE TABLE ClassMap(" + 
        "  oid INTEGER NOT NULL, " + 
        "  className BLOB SUB_TYPE 1, " + 
        "  mapping BLOB SUB_TYPE 1, " +
        "  codebase BLOB SUB_TYPE 1, " + 
        " PRIMARY KEY (oid) " +
        ")";
        
    public static final String DROP_TABLE = 
        "DROP TABLE ClassMap";
        
    public TestFBBlobParams(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(FBDriver.class.getName());
        Connection connection = DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
        
        java.sql.Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {
            // e.printStackTrace();
        }

        stmt.executeUpdate(CREATE_TABLE);
        stmt.close();
        connection.close();
    }

    protected void tearDown() throws Exception {
        Connection connection = DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
        
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();
        super.tearDown();
    }
    
    /**
     * Test if we can pass string as blob param. This test is not 100% correctly
     * coded, but it tries to reproduce bug
     * 
     * @throws Exception if something went wrong.
     */
    public void testParams() throws Exception {
        Connection connection = DriverManager.getConnection(DB_DRIVER_URL, DB_INFO);
        connection.setAutoCommit(false);
        
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT ClassMap.oid,classname,mapping,codebase FROM ClassMap WHERE classname=?;"
            );
    
            ps.setObject(1, PreparedStatement.class.getName());
    
            ResultSet rs = ps.executeQuery();
            rs.next();
            
        } catch(Exception e) {
            assertTrue("There should be no exceptions.", false);
        } finally {
            connection.close();
        }
    }
}
