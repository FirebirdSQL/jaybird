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
package org.firebirdsql.jdbc.escape;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Test;

/**
 * Tests for support of the time and date literal escapes as defined in
 * section 13.4.2 of the JDBC 4.1 specification.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestTimeDateLiteralEscapes extends FBJUnit4TestBase {

    /**
     * Test of the {d 'yyyy-mm-dd'} escape. 
     */
    @Test
    public void testDateEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {d '2012-12-22'} FROM RDB$DATABASE");
            assertTrue("Expected one row", rs.next());
            Object column1 = rs.getObject(1);
            assertTrue("Expected result of {d escape} to be of type java.sql.Date", column1 instanceof java.sql.Date);
            assertEquals("Unexpected value for {d escape}", "2012-12-22", column1.toString());
            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
    /**
     * Test of the {t 'hh:mm:ss'} escape 
     */
    @Test
    public void testTimeEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {t '15:05:56'} FROM RDB$DATABASE");
            
            assertTrue("Expected one row", rs.next());
            Object column1 = rs.getObject(1);
            assertTrue("Expected result of {t escape} to be of type java.sql.Time", column1 instanceof java.sql.Time);
            assertEquals("Unexpected value for {t escape}", "15:05:56", column1.toString());
            
            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
    /**
     * Test of the {ts 'yyyy-mm-dd hh:mm:ss'} escape (without fractional seconds)
     */
    @Test
    public void testTimestampEscape() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {ts '2012-12-22 15:05:56'} FROM RDB$DATABASE");
            
            assertTrue("Expected one row", rs.next());
            Object column1 = rs.getObject(1);
            assertTrue("Expected result of {t escape} to be of type java.sql.Timestamp", column1 instanceof java.sql.Timestamp);
            assertEquals("Unexpected value for {ts escape}", "2012-12-22 15:05:56.0", column1.toString());
            
            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
    
    /**
     * Test of the {ts 'yyyy-mm-dd hh:mm:ss.f..'} escape (with fractional seconds)
     */
    @Test
    public void testTimestampEscapeMillisecond() throws Exception {
        Connection con = FBTestProperties.getConnectionViaDriverManager();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {ts '2012-12-22 15:05:56.123'} FROM RDB$DATABASE");
            
            assertTrue("Expected one row", rs.next());
            Object column1 = rs.getObject(1);
            assertTrue("Expected result of {t escape} to be of type java.sql.Timestamp", column1 instanceof java.sql.Timestamp);
            assertEquals("Unexpected value for {ts escape}", "2012-12-22 15:05:56.123", column1.toString());
            
            rs.close();
            stmt.close();
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }
}
