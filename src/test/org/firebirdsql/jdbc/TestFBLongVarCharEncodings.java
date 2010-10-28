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

import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

/**
 * This test case tests encodings in text blobs.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBLongVarCharEncodings extends TestFBEncodings {
    
    public static final String CREATE_TABLE = 
        "CREATE TABLE test_encodings (" + 
        "  id INTEGER, " +
        "  win1250_field BLOB SUB_TYPE 1 CHARACTER SET WIN1250, " +
        "  win1251_field BLOB SUB_TYPE 1 CHARACTER SET WIN1251, " +
        "  win1252_field BLOB SUB_TYPE 1 CHARACTER SET WIN1252, " +
        "  win1253_field BLOB SUB_TYPE 1 CHARACTER SET WIN1253, " +
        "  win1254_field BLOB SUB_TYPE 1 CHARACTER SET WIN1254, " +
        "  unicode_field BLOB SUB_TYPE 1 CHARACTER SET UNICODE_FSS, " +
        "  ascii_field BLOB SUB_TYPE 1 CHARACTER SET ASCII, " +
        "  none_field BLOB SUB_TYPE 1 CHARACTER SET NONE, " +
        "  char_field BLOB SUB_TYPE 1 CHARACTER SET UNICODE_FSS, " +
        "  octets_field BLOB SUB_TYPE 1 CHARACTER SET OCTETS, " +
        "  var_octets_field BLOB SUB_TYPE 1 CHARACTER SET OCTETS, " +
        "  none_octets_field BLOB SUB_TYPE 1 CHARACTER SET NONE, " +
        "  uuid_char BLOB SUB_TYPE 1 CHARACTER SET UTF8, " + 
        "  uuid_varchar BLOB SUB_TYPE 1 CHARACTER SET UTF8 " +
        ")"
        ;

    public TestFBLongVarCharEncodings(String testName) {
        super(testName);
    }

    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }

    public void testGerman() throws Exception {
        super.testGerman();
    }

    public void testHungarian() throws Exception {
        super.testHungarian();
    }

    public void testUkrainian() throws Exception {
        super.testUkrainian();
    }
    
    public void testPadding() throws Exception {
        // test is not relevant
    }
    
    /**
     * Test whether character translation code works correctly.
     * 
     * @throws Exception if something went wrong.
     */
    public void testTranslation() throws Exception {
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        props.put("charSet", "Cp1252");
        props.put("useTranslation", "translation.hpux");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO test_encodings(" + 
                "  id, none_field) " +
                "VALUES(?, ?)");
            
            stmt.setInt(1, UNIVERSAL_TEST_ID);
            stmt.setBytes(2, TRANSLATION_TEST_BYTES);
            
            int updated = stmt.executeUpdate();
            stmt.close();
            
            assertTrue("Should insert one row", updated == 1);
            
            // 
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT none_field " + 
                "FROM test_encodings WHERE id = ?");
            
            stmt.setInt(1, UNIVERSAL_TEST_ID);
            
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            
            Reader in = rs.getCharacterStream(1);
            char[] buffer = new char[8192]; // should be enough
            
            int readChars = in.read(buffer);
            
            String str = new String(buffer, 0, readChars);
            
            assertTrue("Value should be correct.", TRANSLATION_TEST.equals(str));
            
            stmt.close();

            
        } finally {
            connection.close();
        }
    }
}