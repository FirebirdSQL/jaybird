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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;


/**
 * Describe class <code>TestFBEncodings</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBEncodings extends FBTestBase {
    
	 Vector encJava = new Vector();
	 Vector encFB = new Vector();

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
        "  none_field VARCHAR(50) CHARACTER SET NONE, " +
        "  char_field CHAR(50) CHARACTER SET UNICODE_FSS, " +
        "  utf8_field VARCHAR(50) CHARACTER SET UTF8 " +
        ")"
        ;

    public static String CREATE_TABLE_CYRL = 
        "CREATE TABLE test_encodings_cyrl (" + 
        "  id INTEGER, " +
        "  cyrl_field VARCHAR(50) CHARACTER SET CYRL COLLATE DB_RUS, " +
        "  win1251_field VARCHAR(50) CHARACTER SET WIN1251 COLLATE PXW_CYRL, " +
        "  unicode_field VARCHAR(50) CHARACTER SET UNICODE_FSS " +
        ")"
        ;

    public static String DROP_TABLE = 
        "DROP TABLE test_encodings";

    public static String DROP_TABLE_CYRL = 
        "DROP TABLE test_encodings_cyrl";

    public static String DROP_TABLE_UNIVERSAL = 
        "DROP TABLE test_encodings_universal";
	 
    public TestFBEncodings(String testName) {
        super(testName);
    }
    
    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }
    protected String getCreateTableStatement_cyrl() {
        return CREATE_TABLE_CYRL;
    }
    protected String getCreateTableStatement_universal() {

				encJava.add("Cp437"); encFB.add("DOS437");				
				encJava.add("Cp850"); encFB.add("DOS850");
				encJava.add("Cp852"); encFB.add("DOS852");
				encJava.add("Cp857"); encFB.add("DOS857");			
				encJava.add("Cp860"); encFB.add("DOS860");
				encJava.add("Cp861"); encFB.add("DOS861");
				encJava.add("Cp863"); encFB.add("DOS863");
				encJava.add("Cp865"); encFB.add("DOS865");
//				encJava.add("Cp869"); encFB.add("DOS869");

				encJava.add("Cp1250"); encFB.add("WIN1250");
				encJava.add("Cp1251"); encFB.add("WIN1251");
				encJava.add("Cp1252"); encFB.add("WIN1252");
				encJava.add("Cp1253"); encFB.add("WIN1253");
				encJava.add("Cp1254"); encFB.add("WIN1254");

				encJava.add("ISO8859_1"); encFB.add("ISO8859_1");
				encJava.add("ISO8859_2"); encFB.add("ISO8859_2");
// New cs				
/*				
				encJava.add("Cp737"); encFB.add("DOS737");
				encJava.add("Cp775"); encFB.add("DOS775");
				encJava.add("Cp858"); encFB.add("DOS858");
				encJava.add("Cp862"); encFB.add("DOS862");
				encJava.add("Cp864"); encFB.add("DOS864");
				encJava.add("Cp866"); encFB.add("DOS866");

				encJava.add("Cp1255"); encFB.add("WIN1255");
				encJava.add("Cp1256"); encFB.add("WIN1256");
				encJava.add("Cp1257"); encFB.add("WIN1257");

				encJava.add("ISO8859_3"); encFB.add("ISO8859_3");
				encJava.add("ISO8859_4"); encFB.add("ISO8859_4");
				encJava.add("ISO8859_5"); encFB.add("ISO8859_5");
				encJava.add("ISO8859_6"); encFB.add("ISO8859_6");
				encJava.add("ISO8859_7"); encFB.add("ISO8859_7");
				encJava.add("ISO8859_8"); encFB.add("ISO8859_8");
				encJava.add("ISO8859_9"); encFB.add("ISO8859_9");
				encJava.add("ISO8859_13"); encFB.add("ISO8859_13");
*/
        String CREATE_TABLE_UNIVERSAL = "CREATE TABLE test_encodings_universal (" + 
        "  id INTEGER ";
		  for (int encN = 0; encN < encJava.size(); encN++){
			  CREATE_TABLE_UNIVERSAL = CREATE_TABLE_UNIVERSAL + "," + (String) encJava.elementAt(encN) 
			  +"_field VARCHAR(50) CHARACTER SET "+ (String) encFB.elementAt(encN);
		  }
		  for (int encN = 0; encN < encJava.size(); encN++){
			  CREATE_TABLE_UNIVERSAL = CREATE_TABLE_UNIVERSAL + ", uc_" + (String) encJava.elementAt(encN) 
			  +"_field VARCHAR(50) CHARACTER SET UNICODE_FSS ";
		  }
		  CREATE_TABLE_UNIVERSAL = CREATE_TABLE_UNIVERSAL + ")";
        return CREATE_TABLE_UNIVERSAL;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        
        Class.forName(FBDriver.class.getName());
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        
        Connection connection = DriverManager.getConnection(getUrl(), props);
        
        Statement stmt = connection.createStatement();
        try {stmt.executeUpdate(DROP_TABLE);} catch(SQLException ex) { /*ex.printStackTrace();*/}
        try {stmt.executeUpdate(DROP_TABLE_CYRL);} catch(SQLException ex) { /*ex.printStackTrace();*/}
        try {stmt.executeUpdate(DROP_TABLE_UNIVERSAL);} catch(SQLException ex) { /*ex.printStackTrace();*/}

        stmt.executeUpdate(getCreateTableStatement());
        stmt.executeUpdate(getCreateTableStatement_cyrl());
        stmt.executeUpdate(getCreateTableStatement_universal());
        
        stmt.executeUpdate("DELETE FROM test_encodings");
        stmt.executeUpdate("DELETE FROM test_encodings_cyrl");
        stmt.executeUpdate("DELETE FROM test_encodings_universal");

        stmt.close();        
        
        connection.close();
    }

    protected void tearDown() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);
            
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(DROP_TABLE);
            stmt.close();
				stmt = connection.createStatement();
				stmt.executeUpdate(DROP_TABLE_CYRL);
				stmt.close();
        } finally {
            connection.close();      
        }
        
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
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
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
                "SELECT win1251_field, unicode_field, " + 
                "'" + UKRAINIAN_TEST_STRING + "' direct_sql_field " +  
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
                
            String directSqlValue = rs.getString(3);
            assertTrue("direct_sql_field should be the same",
                directSqlValue.equals(UKRAINIAN_TEST_STRING));
            
            assertTrue("Should have exactly one row", !rs.next());
            
            rs.close();
            stmt.close();
            
            stmt = connection.prepareStatement(
                "SELECT none_field FROM test_encodings WHERE id = ?");
                
            stmt.setInt(1, UKRAINIAN_TEST_ID);
            
            try {
                rs = stmt.executeQuery();
                
                String noneStr = rs.getString(1);
                
                assertTrue("Should not be able to read none_field " + 
                    "with special characters", false);
            } catch(SQLException sqlex) {
                // everything is ok
            }
            
            stmt.close();
        } finally {
            connection.close();
        }
    }
/*        
    public static byte[] CYRL_TEST_BYTES = new byte[] {
        (byte)0x80, (byte)0x81, (byte)0x82, (byte)0x83, 
        (byte)0x84, (byte)0x85, (byte)0x86, (byte)0x87,
        (byte)0x88, (byte)0x89, (byte)0x8A, (byte)0x8B, 
        (byte)0x8C, (byte)0x8D, (byte)0x8C, (byte)0x8F
    };
 */
	 
	 // 
	 // This test only demonstrate a failure in the CYRL character set
	 //
    public static byte[] CYRL_TEST_BYTES = new byte[] {
        (byte)0xE0, (byte)0xE1, (byte)0xE2, (byte)0xE3, 
        (byte)0xE4, (byte)0xE5, (byte)0xE6, (byte)0xE7,
        (byte)0xE8, (byte)0xE9, (byte)0xEA, (byte)0xEB, 
        (byte)0xEC, (byte)0xED, (byte)0xEC, (byte)0xEF
    };
	 // These are the correct uppercase bytes
    public static byte[] CYRL_TEST_BYTES_UPPER = new byte[] {
        (byte)0xC0, (byte)0xC1, (byte)0xC2, (byte)0xC3, 
        (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC7,
        (byte)0xC8, (byte)0xC9, (byte)0xCA, (byte)0xCB, 
        (byte)0xCC, (byte)0xCD, (byte)0xCC, (byte)0xCF
    };
	 // These are the wrong uppercase bytes
    public static byte[] CYRL_TEST_BYTES_UPPER_WRONG = new byte[] {
        (byte)0x90, (byte)0x91, (byte)0x92, (byte)0x93, 
        (byte)0x94, (byte)0x95, (byte)0x96, (byte)0x97,
        (byte)0x00, (byte)0x99, (byte)0x9A, (byte)0x9B, 
        (byte)0x9C, (byte)0x9D, (byte)0x9C, (byte)0x9F
    };
	 
    public void testCyrl() throws Exception {
        String CYRL_TEST_STRING = 
            new String(CYRL_TEST_BYTES, "Cp1251");
        String CYRL_TEST_STRING_UPPER = 
            new String(CYRL_TEST_BYTES_UPPER, "Cp1251");
        String CYRL_TEST_STRING_UPPER_WRONG = 
            new String(CYRL_TEST_BYTES_UPPER_WRONG, "Cp1251");
/*
for (int i=0; i< CYRL_TEST_BYTES.length ; i++){
	System.out.println("inic "+Integer.toHexString((int) CYRL_TEST_BYTES[i]&0xFF)+" "+((int) CYRL_TEST_BYTES[i]&0xFF));
}
*/
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO test_encodings_cyrl(" + 
                " id, cyrl_field, win1251_field, unicode_field) " +
                "VALUES(?, ?, ?, ?)");
            
            stmt.setInt(1, 1);
            stmt.setString(2, CYRL_TEST_STRING);
            stmt.setString(3, CYRL_TEST_STRING);
            stmt.setString(4, CYRL_TEST_STRING);
            
            int updated = stmt.executeUpdate();
            stmt.close();
            
            assertTrue("Should insert one row", updated == 1);
            //
            // Select the same case
            //
            stmt = connection.prepareStatement(
                "SELECT cyrl_field, win1251_field, unicode_field " + 
                "FROM test_encodings_cyrl WHERE id = ?");
                
            stmt.setInt(1, 1);
                
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should have at least one row", rs.next());
            
            String cyrlValue = rs.getString(1);
            String win1251Value = rs.getString(2);
            String unicodeValue = rs.getString(3);

            assertTrue("Cyrl_field and Win1251_field must be equal ", cyrlValue.equals(win1251Value));
            assertTrue("Win1251_field and Unicode_field must be equal ", win1251Value.equals(unicodeValue));
            assertTrue("Cyrl_field and Unicode_field must be equal ", cyrlValue.equals(unicodeValue));

            assertTrue("Should have exactly one row", !rs.next());
            
            rs.close();
            stmt.close();
            //
            // Select upper case
            //
            stmt = connection.prepareStatement(
                "SELECT UPPER(cyrl_field), UPPER(win1251_field), UPPER(unicode_field) " + 
                "FROM test_encodings_cyrl WHERE id = ?");
                
            stmt.setInt(1, 1);
                
            rs = stmt.executeQuery();
            
            assertTrue("Should have at least one row", rs.next());
            
            String cyrlValueUpper = rs.getString(1);
            byte[] cyrlUpperBytes = cyrlValueUpper.getBytes("Cp1251");
            /*
for (int i=0; i< cyrlUpperBytes.length	; i++){
	System.out.println("cyrl "+Integer.toHexString((int) cyrlUpperBytes[i]&0xFF)+" "+((int) cyrlUpperBytes[i]&0xFF));
}
*/
            String win1251ValueUpper = rs.getString(2);
            byte[] win1251UpperBytes = win1251ValueUpper.getBytes("Cp1251");
/*
for (int i=0; i< win1251UpperBytes.length	; i++){
	System.out.println("win1251 "+Integer.toHexString((int) win1251UpperBytes[i]&0xFF)+" "+((int) win1251UpperBytes[i]&0xFF));
}
*/
            String unicodeValueUpper = rs.getString(3);

            assertTrue("Upper(Cyrl_field) must be != Cyrl_field ", !cyrlValue.equals(cyrlValueUpper));
            assertTrue("Upper(Win1251_field) must be != Win1251_field ", !win1251Value.equals(win1251ValueUpper));
            // Unicode only uppercase ASCII characters (until Firebird 2.0)
            if (((FirebirdDatabaseMetaData)connection.getMetaData()).getDatabaseMajorVersion() < 2)
                assertTrue("Upper(unicode) must be == Unicode_field ", unicodeValue.equals(unicodeValueUpper));

            assertTrue("Upper(win1251_field) must == upper test string ", win1251ValueUpper.equals(CYRL_TEST_STRING_UPPER));
            // The CYRL charset fails because the mapping is 1251 and the uppercase
            // and lowercase functions work as if the charset is CP866
            assertTrue("Upper(cyrl_field) must be == wrong upper test string ", cyrlValueUpper.equals(CYRL_TEST_STRING_UPPER_WRONG));

            // unicode does not uppercase (until FB 2.0)
            
            if (((FirebirdDatabaseMetaData)connection.getMetaData()).getDatabaseMajorVersion() < 2)
                assertTrue("Upper(Unicode_field) must be != upper test string ", ! unicodeValueUpper.equals(CYRL_TEST_STRING_UPPER));

            assertTrue("Should have exactly one row", !rs.next());
            
            rs.close();
            stmt.close();
            
        } finally {
            connection.close();
        }
    }	 

    // couple of test characters in German
    public static String GERMAN_TEST_STRING_WIN1252 = 
        "Zeichen " + "\u00c4\u00e4, \u00d6\u00f6, \u00dc\u00fc und \u00df";
        
    public static int GERMAN_TEST_ID = 2;
    
    public void testGerman() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1252");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
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
                
                String noneStr = rs.getString(1);
                
                assertTrue("Should not be able to read none_field " + 
                    "with special characters", false);
            } catch(SQLException sqlex) {
                // everything is ok
            }
            
            stmt.close();
            
        } finally {
            connection.close();
        }
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
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
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
                
                String noneStr = rs.getString(1);
                
                assertTrue("Should not be able to read none_field " + 
                    "with special characters", false);
            } catch(SQLException sqlex) {
                // everything is ok
            }
            
            stmt.close();
        } finally {
            connection.close();
        }
    }    

    public static byte[] UNIVERSAL_TEST_BYTES = new byte[] {
        (byte)0xE0, (byte)0xE1, (byte)0xE2, /* (byte)0xE3, CANT MAP IN ISO_8859_3 */
        (byte)0xE4, (byte)0xE5, (byte)0xE6, /* (byte)0xE7, CANT MAP IN DOS857 */
        (byte)0xE8, (byte)0xE9, (byte)0xEA, (byte)0xEB, 
        (byte)0xEC, (byte)0xED, (byte)0xEC, (byte)0xEF
    };
	 
    public static int UNIVERSAL_TEST_ID = 1;
    
    public void testUniversal() throws Exception {
		 
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
			   String insert = "INSERT INTO test_encodings_universal VALUES(? ";
				for (int col=0; col<encJava.size()*2; col++){
					insert = insert + ",?";
				}
				insert = insert + ")";
            PreparedStatement stmt = connection.prepareStatement(insert);
            				
            stmt.setInt(1, UNIVERSAL_TEST_ID);
				for (int col=0; col<encJava.size(); col++){
					stmt.setString(col+2, new String(UNIVERSAL_TEST_BYTES,(String) encJava.elementAt(col)));
					stmt.setString(col+encJava.size()+2, new String(UNIVERSAL_TEST_BYTES,(String) encJava.elementAt(col)));
				}
								
            int updated = stmt.executeUpdate();
            stmt.close();
            
            assertTrue("Should insert one row", updated == 1);
				// 
				// Test each column
				//
				stmt = connection.prepareStatement(
                "SELECT * " + 
                "FROM test_encodings_universal WHERE id = ?");
            stmt.setInt(1, UNIVERSAL_TEST_ID);
                
            ResultSet rs = stmt.executeQuery();
				assertTrue("Should have at least one row", rs.next());

				for (int col=0; col<encJava.size(); col++){
					String charsetValue = rs.getString(col+2);
					String unicodeValue = rs.getString(col+encJava.size()+2);
					
					assertTrue("charsetValue "+encJava.elementAt(col)+" should be the same that unicode", 
                charsetValue.equals(unicodeValue));
					assertTrue("charsetValue "+encJava.elementAt(col)+" should be == string", 
                charsetValue.equals(new String(UNIVERSAL_TEST_BYTES,(String) encJava.elementAt(col))));
				}
            assertTrue("Should have exactly one row", !rs.next());
            
            rs.close();
            stmt.close();
        } finally {
            connection.close();
        }
    }    
    
    public void testPadding() throws Exception {
        
        String testString = "test string";
        
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO test_encodings(" + 
                "  id, char_field) " +
                "VALUES(?, ?)");
            
            stmt.setInt(1, UNIVERSAL_TEST_ID);
            stmt.setString(2, testString);
            
            int updated = stmt.executeUpdate();
            stmt.close();
            
            assertTrue("Should insert one row", updated == 1);
            
            // 
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT char_field " + 
                "FROM test_encodings WHERE id = ?");
            
            stmt.setInt(1, UNIVERSAL_TEST_ID);
            
            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());
            
            String str = rs.getString(1);
            
            assertTrue("Field length should be correct", str.length() == 50);
            assertTrue("Trimmed values should be correct.", testString.equals(str.trim()));
            
            stmt.close();

            
        } finally {
            connection.close();
        }
        
    }
    
    protected static final byte[] TRANSLATION_TEST_BYTES = new byte[] {
        (byte)0xde, (byte)0xbd, (byte)0xd8, (byte)0xda, (byte)0xdb, (byte)0xcc, (byte)0xce, (byte)0xcf
    };
    
    protected static final String TRANSLATION_TEST = "\u00df\u00a7\u00c4\u00d6\u00dc\u00e4\u00f6\u00fc";
    
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
            
            String str = rs.getString(1);
            
            assertTrue("Value should be correct.", TRANSLATION_TEST.equals(rs.getString(1)));
            
            stmt.close();

            
        } finally {
            connection.close();
        }
    }
    
    public void _testWrongTranslation() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        props.put("charSet", "Cp1252");
        
        Connection connection = 
            DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO test_encodings(" + 
                "  id, utf8_field) " +
                "VALUES(?, ?)");
            try {
                stmt.setInt(1, UNIVERSAL_TEST_ID);
                // stmt.setString(2, "\u00e4");
                stmt.setBytes(2, "\u00e4".getBytes("UTF-8"));
                
                int updated = stmt.executeUpdate();
                assertTrue("Should insert one row", updated == 1);
            } finally {
                stmt.close();
            }
            
        } finally {
            connection.close();
        }
        
        props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        props.put("charSet", "Cp1251");
        
        connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT utf8_field " + 
            "FROM test_encodings WHERE id = ?");
            try {
                stmt.setInt(1, UNIVERSAL_TEST_ID);
                
                ResultSet rs = stmt.executeQuery();
                assertTrue("Should have at least one row", rs.next());
                
                String str = rs.getString(1);
                
                assertTrue("Value should be correct.", "\u0434".equals(str));
            } finally {
                stmt.close();
            }
            
        } finally {
            connection.close();
        }

    }
}
