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
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.Statement;
import java.util.Properties;

/**
 * This method tests correctness of {@link FBParameterMetaData} class.
 *
 * @author <a href="mailto:skidder@users.sourceforge.net">Nickolay Samofatov</a>
 * @version 1.0
 */
public class TestFBParameterMetaData extends FBTestBase {
    
    public static String CREATE_TABLE = 
        "CREATE TABLE test_p_metadata (" + 
        "  id INTEGER, " +
        "  simple_field VARCHAR(60) CHARACTER SET WIN1251 COLLATE PXW_CYRL, " +
        "  two_byte_field VARCHAR(60) CHARACTER SET BIG_5, " +
        "  three_byte_field VARCHAR(60) CHARACTER SET UNICODE_FSS, " +
        "  long_field numeric(18,0), " + // This is BIGINT alias for FB1
        "  int_field INTEGER, " +
        "  short_field SMALLINT " +
        ")";
        
    public static final String TEST_QUERY = 
        "insert into test_p_metadata(" + 
        "simple_field, two_byte_field, three_byte_field, " + 
        "long_field, int_field, short_field) " + 
        "values (?,?,?,?,?,?)";
    
    public static String DROP_TABLE = 
        "DROP TABLE test_p_metadata";
    
    public TestFBParameterMetaData(String testName) {
        super(testName);
    }
    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        Properties props = new Properties();
        props.putAll(this.getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(this.getUrl(), props);
        
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {}

        stmt.executeUpdate(CREATE_TABLE);
        stmt.close();        
        
        connection.close();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testParameterMetaData() throws Exception {
        Properties props = new Properties();
        props.putAll(this.getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = DriverManager.getConnection(this.getUrl(), props);
        
        FirebirdPreparedStatement stmt = 
            (FirebirdPreparedStatement)connection.prepareStatement(TEST_QUERY);
        
        ParameterMetaData metaData = stmt.getParameterMetaData();
        
        assertTrue("simple_field must have size 60", 
            metaData.getPrecision(1) == 60);
            
        assertTrue("two_byte_field must have size 60", 
            metaData.getPrecision(2) == 60);

        assertTrue("three_byte_field must have size 60", 
            metaData.getPrecision(3) == 60);

        assertTrue("long_field must have precision 19", 
            metaData.getPrecision(4) == 19);

        assertTrue("int_field must have precision 10", 
            metaData.getPrecision(5) == 10);

        assertTrue("short_field must have precision 5", 
            metaData.getPrecision(6) == 5);

        stmt.close();
        connection.close();
    }
}
