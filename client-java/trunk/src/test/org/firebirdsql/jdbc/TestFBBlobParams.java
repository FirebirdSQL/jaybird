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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * Describe class <code>TestFBBlobParam</code> here.
 *
 * @version 1.0
 */
public class TestFBBlobParams extends FBTestBase {
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
        Connection connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
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
        Connection connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
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
        Connection connection = getConnectionViaDriverManager();
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
    
    /**
     * Test case that reproduces problem when using UPPER function with text
     * Blobs.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void testUpperAndBlobParam() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        connection.setAutoCommit(false);

        try {
            
            Statement stmt = connection.createStatement();
            try {
                stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
            } finally {
                stmt.close();
            }
    
            connection.commit();
            
            PreparedStatement ps = connection.prepareStatement(
                "SELECT oid FROM ClassMap WHERE UPPER(classname) LIKE ?"
            );
            
            try {
                ps.setString(1, "TEST");

                ResultSet rs = ps.executeQuery();
                assertTrue("Should find at least one row.", rs.next());
                assertTrue("OID value should be correct.",
                    "1".equals(rs.getString(1)));
                assertTrue("Only one row should be selected", !rs.next());
            } finally {
                ps.close();
            }

        } finally {
            connection.close();
        }
    }

    /**
     * Test case that reproduces problem when using equal sign with text
     * Blobs.
     * 
     * @throws java.lang.Exception if something went wrong.
     */
    public void testEqualityInBlobParam() throws Exception {
        Connection connection = getConnectionViaDriverManager();
        connection.setAutoCommit(false);

        try {

            Statement stmt = connection.createStatement();
            try {
                stmt.execute("INSERT INTO ClassMap(oid, classname) VALUES (1, 'test')");
            } finally {
                stmt.close();
            }

            connection.commit();

            PreparedStatement ps = connection.prepareStatement(
                "SELECT oid FROM ClassMap WHERE classname = ?"
            );

            try {
                ps.setString(1, "test");

                ResultSet rs = ps.executeQuery();
                assertTrue("Should find at least one row.", rs.next());
                assertTrue("OID value should be correct.",
                    "1".equals(rs.getString(1)));
                assertTrue("Only one row should be selected", !rs.next());
            } finally {
                ps.close();
            }

        } finally {
            connection.close();
        }
    }

}
