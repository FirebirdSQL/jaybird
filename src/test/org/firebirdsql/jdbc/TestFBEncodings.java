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
 * Describe class <code>TestFBEncodings</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBEncodings extends BaseFBTest {
    
    public static String CREATE_TABLE = 
        "CREATE TABLE test_encodings (" + 
        "  id INTEGER, " +
        "  win1250_field VARCHAR(50) CHARACTER SET WIN1250, " +
        "  win1251_field VARCHAR(50) CHARACTER SET WIN1251, " +
        "  win1252_field VARCHAR(50) CHARACTER SET WIN1252, " +
        "  win1253_field VARCHAR(50) CHARACTER SET WIN1253, " +
        "  win1254_field VARCHAR(50) CHARACTER SET WIN1254, " +
        "  unicode_field VARCHAR(50) CHARACTER SET UNICODE_FSS, " +
        "  ascii_field VARCHAR(50) CHARACTER SET ASCII, " +
        "  none_field VARCHAR(50) CHARACTER SET NONE " +
        ")";
        
    public static String DROP_TABLE = 
        "DROP TABLE test_encodings";
    
    public TestFBEncodings(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "NONE");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
        
        java.sql.Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {}

        try {
            stmt.executeUpdate(CREATE_TABLE);
            stmt.close();        
        } catch(Exception ex) {
        }
        
        try {
            stmt.executeUpdate("DELETE FROM test_encodings");
            stmt.close();        
        } catch(Exception ex) {
        }
        
        connection.close();
    }

    protected void tearDown() throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "NONE");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);
            
        java.sql.Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();      
        
        super.tearDown();
    }
    
    
    // "test string" in Ukrainian ("тестова стрічка")
    public static String UKRAINIAN_TEST_STRING = 
        //"\u00f2\u00e5\u00f1\u00f2\u00ee\u00e2\u00e0 " +
        "\u0442\u0435\u0441\u0442\u043e\u0432\u0430 " + 
        //"\u00f1\u00f2\u00f0\u00b3\u00f7\u00ea\u00e0";
        "\u0441\u0442\u0440\u0456\u0447\u043a\u0430";
        
    public static byte[] UKRAINIAN_TEST_BYTES = new byte[] {
        (byte)0xf2, (byte)0xe5, (byte)0xf1, (byte)0xf2, 
        (byte)0xee, (byte)0xe2, (byte)0xe0, (byte)0x20,
        (byte)0xf1, (byte)0xf2, (byte)0xf0, (byte)0xb3, 
        (byte)0xf7, (byte)0xea, (byte)0xe0
    };
    
    public static String UKRAINIAN_TEST_STRING_WIN1251;
        
    public static int UKRAINIAN_TEST_ID = 1;
    
    public void testUkrainian() throws Exception {
        UKRAINIAN_TEST_STRING_WIN1251 = 
            new String(UKRAINIAN_TEST_BYTES, "Cp1251");
        
        assertTrue("Strings should be equal.", 
            UKRAINIAN_TEST_STRING.equals(UKRAINIAN_TEST_STRING_WIN1251));
        
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "WIN1251");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);

        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_encodings(" + 
            "  id, win1251_field, unicode_field, none_field) " +
            "VALUES(?, ?, ?, ?)");
        
        stmt.setInt(1, UKRAINIAN_TEST_ID);
        stmt.setString(2, UKRAINIAN_TEST_STRING_WIN1251);
        stmt.setString(3, UKRAINIAN_TEST_STRING_WIN1251);
        stmt.setString(4, UKRAINIAN_TEST_STRING_WIN1251);
        
        int updated = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row", updated == 1);
        
        stmt = connection.prepareStatement(
            "SELECT win1251_field, unicode_field " + 
            "FROM test_encodings WHERE id = ?");
            
        stmt.setInt(1, UKRAINIAN_TEST_ID);
            
        ResultSet rs = stmt.executeQuery();
        
        assertTrue("Should have at least one row", rs.next());
        
        String win1251Value = rs.getString(1);
        assertTrue("win1251_field value should be the same", 
            win1251Value.equals(UKRAINIAN_TEST_STRING));
            
        String unicodeValue = rs.getString(2);
        assertTrue("unicode_field value should be the same", 
            unicodeValue.equals(UKRAINIAN_TEST_STRING));
            
        assertTrue("Should have exactly one row", !rs.next());
        
        rs.close();
        stmt.close();
        
        stmt = connection.prepareStatement(
            "SELECT none_field FROM test_encodings WHERE id = ?");
        	
        stmt.setInt(1, UKRAINIAN_TEST_ID);
        
        try {
            rs = stmt.executeQuery();
            
            assertTrue("Should not be able to read none_field " + 
                "with special characters", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
        
        stmt.close();
        
        connection.close();
    }


    // couple of test characters in German
    public static String GERMAN_TEST_STRING_WIN1252 = 
        "Zeichen " + "\u00c4\u00e4, \u00d6\u00f6, \u00dc\u00fc und \u00df";
        
    public static int GERMAN_TEST_ID = 2;
    
    public void testGerman() throws Exception {
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "WIN1252");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);

        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_encodings(" + 
            "  id, win1252_field, unicode_field, none_field) " +
            "VALUES(?, ?, ?, ?)");
        
        stmt.setInt(1, GERMAN_TEST_ID);
        stmt.setString(2, GERMAN_TEST_STRING_WIN1252);
        stmt.setString(3, GERMAN_TEST_STRING_WIN1252);
        stmt.setString(4, GERMAN_TEST_STRING_WIN1252);
        
        int updated = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row", updated == 1);
        
        stmt = connection.prepareStatement(
            "SELECT win1252_field, unicode_field " + 
            "FROM test_encodings WHERE id = ?");
            
        stmt.setInt(1, GERMAN_TEST_ID);
            
        ResultSet rs = stmt.executeQuery();
        
        assertTrue("Should have at least one row", rs.next());
        
        String win1252Value = rs.getString(1);
        assertTrue("win1252_field value should be the same", 
            win1252Value.equals(GERMAN_TEST_STRING_WIN1252));
            
        String unicodeValue = rs.getString(2);
        assertTrue("unicode_field value should be the same", 
            unicodeValue.equals(GERMAN_TEST_STRING_WIN1252));
            
        assertTrue("Should have exactly one row", !rs.next());
        
        rs.close();
        stmt.close();
        
        stmt = connection.prepareStatement(
            "SELECT none_field FROM test_encodings WHERE id = ?");
        	
        stmt.setInt(1, GERMAN_TEST_ID);
        
        try {
            rs = stmt.executeQuery();
            
            assertTrue("Should not be able to read none_field " + 
                "with special characters", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
        
        stmt.close();
        
        connection.close();
    }
    
    // String in Hungarian ("\u0151r\u00FClt")
    public static String HUNGARIAN_TEST_STRING = 
        "\u0151\u0072\u00fc\u006c\u0074";
        
    public static byte[] HUNGARIAN_TEST_BYTES = new byte[] {
        (byte)0xf5, (byte)0x72, (byte)0xfc, (byte)0x6c, (byte)0x74
    };
    
    public static String HUNGARIAN_TEST_STRING_WIN1250;
    public static String HUNGARIAN_NATIVE_UNICODE;
        
    public static int HUNGARIAN_TEST_ID = 3;
    
    public void testHungarian() throws Exception {
        HUNGARIAN_TEST_STRING_WIN1250 = 
            new String(HUNGARIAN_TEST_BYTES, "Cp1250");
            
        HUNGARIAN_NATIVE_UNICODE = new String(HUNGARIAN_TEST_BYTES);

        assertTrue("Strings should be equal.", 
            HUNGARIAN_TEST_STRING.equals(HUNGARIAN_TEST_STRING_WIN1250));
        
        java.util.Properties props = new java.util.Properties();
        props.putAll(DB_INFO);
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(DB_DRIVER_URL, props);

        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_encodings(" + 
            "  id, win1250_field, win1251_field, win1252_field, " + 
            "  unicode_field, none_field) " +
            "VALUES(?, ?, ?, ?, ?, ?)");
        
        stmt.setInt(1, HUNGARIAN_TEST_ID);
        stmt.setString(2, HUNGARIAN_TEST_STRING_WIN1250);
        stmt.setString(3, UKRAINIAN_TEST_STRING);
        stmt.setString(4, GERMAN_TEST_STRING_WIN1252);
        stmt.setString(5, HUNGARIAN_TEST_STRING);
        stmt.setString(6, HUNGARIAN_TEST_STRING_WIN1250);
        
        int updated = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row", updated == 1);
        
        stmt = connection.prepareStatement(
            "SELECT win1250_field, win1251_field, win1252_field, unicode_field " + 
            "FROM test_encodings WHERE id = ?");
            
        stmt.setInt(1, HUNGARIAN_TEST_ID);
            
        ResultSet rs = stmt.executeQuery();
        
        assertTrue("Should have at least one row", rs.next());
        
        String win1250Value = rs.getString(1);
        assertTrue("win1250_field value should be the same", 
            win1250Value.equals(HUNGARIAN_TEST_STRING));

        String win1251Value = rs.getString(2);
        assertTrue("win1251_field value should be the same", 
            win1251Value.equals(UKRAINIAN_TEST_STRING));

        String win1252Value = rs.getString(3);
        assertTrue("win1252_field value should be the same", 
            win1252Value.equals(GERMAN_TEST_STRING_WIN1252));
            
        String unicodeValue = rs.getString(4);
        assertTrue("unicode_field value should be the same", 
            unicodeValue.equals(HUNGARIAN_TEST_STRING));

        assertTrue("Should have exactly one row", !rs.next());
        
        rs.close();
        stmt.close();
        
        stmt = connection.prepareStatement(
            "SELECT none_field FROM test_encodings WHERE id = ?");
        	
        stmt.setInt(1, HUNGARIAN_TEST_ID);
        
        try {
            rs = stmt.executeQuery();
            
            assertTrue("Should not be able to read none_field " + 
                "with special characters", false);
        } catch(SQLException sqlex) {
            // everything is ok
        }
        
        stmt.close();
        
        connection.close();
    }    
}
