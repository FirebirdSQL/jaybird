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


public class TestFBDatabaseMetaDataNulls extends FBTestBase {

    public TestFBDatabaseMetaDataNulls(String name) {
        super(name);
    }

    public static final String CREATE_TABLE = ""
        + "CREATE TABLE test_nulls("
        + "  id INTEGER NOT NULL PRIMARY KEY, "
        + "  char_value VARCHAR(20)"
        + ")";
    
    public static final String DROP_TABLE = 
        "DROP TABLE test_nulls";
    
    public static final String INSERT_VALUES = 
        "INSERT INTO test_nulls VALUES(?, ?)";
    
    private Connection connection;
    
    protected void setUp() throws Exception {
        
        super.setUp();

        connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
        try {
            try {
                stmt.execute(DROP_TABLE);
            } catch(SQLException ex) {
                // ignore
            }
            
            stmt.execute(CREATE_TABLE);
        } finally {
            stmt.close();
        }
        
        PreparedStatement ps = connection.prepareStatement(INSERT_VALUES);
        try {
            ps.setInt(1, 1);
            ps.setString(2, "a");
            ps.execute();
            
            ps.setInt(1, 2);
            ps.setNull(2, Types.VARCHAR);
            ps.execute();
        } finally {
            ps.close();
        }
        
    }

    protected void tearDown() throws Exception {
        
        try {
            Statement stmt = connection.createStatement();
            try {
                stmt.execute(DROP_TABLE);
            } finally {
                stmt.close();
            }
        } finally {
            connection.close();
        }
        
        super.tearDown();
    }

    public void testNullAreSortedAtStartEnd() throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        Statement stmt = connection.createStatement();
        try {

            boolean[][] sorting = new boolean[2][2];
            
            ResultSet rs;
            
            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 ASC");
            assertTrue("Should select a record", rs.next());
            sorting[0][0] = rs.getString(1) == null;
            assertTrue("Should select a record", rs.next());
            sorting[0][1] = rs.getString(1) == null;

            rs = stmt.executeQuery("SELECT char_value FROM test_nulls ORDER BY 1 DESC");
            assertTrue("Should select a record", rs.next());
            sorting[1][0] = rs.getString(1) == null;
            assertTrue("Should select a record", rs.next());
            sorting[1][1] = rs.getString(1) == null;
            
            assertTrue("nullsAreSortedAtEnd is not correct.",
                metaData.nullsAreSortedAtEnd() && (sorting[0][1] && sorting[1][1]) ||
                !metaData.nullsAreSortedAtEnd() && !(sorting[0][1] && sorting[1][1]));
      
            assertTrue("nullsAreSortedAtStart is not correct.",
              metaData.nullsAreSortedAtStart() && (sorting[0][0] && sorting[1][0]) ||
              !metaData.nullsAreSortedAtStart() && !(sorting[0][0] && sorting[1][0]));
            
            assertTrue("nullsAreSortedHigh is not correct.",
                metaData.nullsAreSortedHigh() && (sorting[0][1] && sorting[1][0]) ||
                !metaData.nullsAreSortedHigh() && (sorting[0][0] && sorting[1][1]) ||
                (!metaData.nullsAreSortedHigh() && (metaData.nullsAreSortedAtEnd() || metaData.nullsAreSortedAtStart()))
                );
            
            assertTrue("nullsAreSortedLow is not correct.",
                metaData.nullsAreSortedLow() && (sorting[0][0] && sorting[1][1]) ||
                !metaData.nullsAreSortedLow() && (metaData.nullsAreSortedAtEnd() || metaData.nullsAreSortedAtStart()));
        } finally {
            stmt.close();
        }
    }


}
