/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.*;

import static junit.framework.TestCase.fail;
import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Describe class <code>TestFBEncodings</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestFBEncodings extends FBJUnit4TestBase {

    private static final List<String> ENCODINGS_JAVA;
    private static final List<String> ENCODINGS_FIREBIRD;
    private static final String CREATE_TABLE_UNIVERSAL;

    static {
        List<String> encJava = new ArrayList<String>();
        List<String> encFB = new ArrayList<String>();

        encJava.add("Cp437");
        encFB.add("DOS437");
        encJava.add("Cp850");
        encFB.add("DOS850");
        encJava.add("Cp852");
        encFB.add("DOS852");
        encJava.add("Cp857");
        encFB.add("DOS857");
        encJava.add("Cp860");
        encFB.add("DOS860");
        encJava.add("Cp861");
        encFB.add("DOS861");
        encJava.add("Cp863");
        encFB.add("DOS863");
        encJava.add("Cp865");
        encFB.add("DOS865");
//      encJava.add("Cp869"); encFB.add("DOS869");

        encJava.add("Cp1250");
        encFB.add("WIN1250");
        encJava.add("Cp1251");
        encFB.add("WIN1251");
        encJava.add("Cp1252");
        encFB.add("WIN1252");
        encJava.add("Cp1253");
        encFB.add("WIN1253");
        encJava.add("Cp1254");
        encFB.add("WIN1254");

        encJava.add("ISO8859_1");
        encFB.add("ISO8859_1");
        encJava.add("ISO8859_2");
        encFB.add("ISO8859_2");
//New cs               
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
        ENCODINGS_JAVA = Collections.unmodifiableList(encJava);
        ENCODINGS_FIREBIRD = Collections.unmodifiableList(encFB);

        StringBuilder sb = new StringBuilder("CREATE TABLE test_encodings_universal (");
        sb.append("  id INTEGER ");
        for (int encN = 0; encN < ENCODINGS_JAVA.size(); encN++) {
            sb.append(',').append(ENCODINGS_JAVA.get(encN)).append("_field VARCHAR(50) CHARACTER SET ").append(ENCODINGS_FIREBIRD.get(encN));
        }
        for (int encN = 0; encN < ENCODINGS_JAVA.size(); encN++) {
            sb.append(", uc_").append(ENCODINGS_JAVA.get(encN)).append("_field VARCHAR(50) CHARACTER SET UNICODE_FSS ");
        }
        sb.append(')');

        CREATE_TABLE_UNIVERSAL = sb.toString();
    }

    //@formatter:off
    private static final String CREATE_TABLE =
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
            "  octets_field CHAR(10) CHARACTER SET OCTETS, " +
            "  var_octets_field VARCHAR(10) CHARACTER SET OCTETS, " +
            "  none_octets_field CHAR(10) CHARACTER SET NONE, " +
            "  uuid_char CHAR(36) CHARACTER SET UTF8, " +
            "  uuid_varchar CHAR(36) CHARACTER SET UTF8 " +
            ")";

    private static final String CREATE_TABLE_CYRL =
            "CREATE TABLE test_encodings_cyrl (" +
            "  id INTEGER, " +
            "  cyrl_field VARCHAR(50) CHARACTER SET CYRL COLLATE DB_RUS, " +
            "  win1251_field VARCHAR(50) CHARACTER SET WIN1251 COLLATE PXW_CYRL, " +
            "  unicode_field VARCHAR(50) CHARACTER SET UNICODE_FSS " +
            ")";
    //@formatter:on

    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }

    protected String getCreateTableStatement_cyrl() {
        return CREATE_TABLE_CYRL;
    }

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");

        Connection connection = DriverManager.getConnection(getUrl(), props);
        try {
            List<String> createTableStatements = Arrays.asList(
                    getCreateTableStatement(), getCreateTableStatement_cyrl(), CREATE_TABLE_UNIVERSAL);
            if (!supportInfoFor(connection).supportsUtf8()) {
                for (int index = 0; index < createTableStatements.size(); index++) {
                    String original = createTableStatements.get(index);
                    String modified = original.replace("CHARACTER SET UTF8", "CHARACTER SET UNICODE_FSS");
                    createTableStatements.set(index, modified);
                }
            }
            executeCreateTable(connection, getCreateTableStatement());
            executeCreateTable(connection, getCreateTableStatement_cyrl());
            executeCreateTable(connection, CREATE_TABLE_UNIVERSAL);
        } finally {
            connection.close();
        }
    }

    // "test string" in Ukrainian
    public static String UKRAINIAN_TEST_STRING =
            "\u0442\u0435\u0441\u0442\u043e\u0432\u0430 \u0441\u0442\u0440\u0456\u0447\u043a\u0430";

    private static final int UKRAINIAN_TEST_ID = 1;

    @Test
    public void testUkrainian() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings("
                            + "  id, win1251_field, unicode_field, none_field) "
                            + "VALUES(?, ?, ?, ?)");

            stmt.setInt(1, UKRAINIAN_TEST_ID);
            stmt.setString(2, UKRAINIAN_TEST_STRING);
            stmt.setString(3, UKRAINIAN_TEST_STRING);
            stmt.setString(4, UKRAINIAN_TEST_STRING);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            stmt = connection.prepareStatement(
                    "SELECT win1251_field, unicode_field, "
                            + "'" + UKRAINIAN_TEST_STRING + "' direct_sql_field "
                            + "FROM test_encodings WHERE id = ?");

            stmt.setInt(1, UKRAINIAN_TEST_ID);

            ResultSet rs = stmt.executeQuery();

            assertTrue("Should have at least one row", rs.next());

            String win1251Value = rs.getString(1);
            assertEquals("win1251_field value should be the same", UKRAINIAN_TEST_STRING, win1251Value);

            String unicodeValue = rs.getString(2);
            assertEquals("unicode_field value should be the same", UKRAINIAN_TEST_STRING, unicodeValue);

            String directSqlValue = rs.getString(3);
            assertEquals("direct_sql_field should be the same", UKRAINIAN_TEST_STRING, directSqlValue);

            assertFalse("Should have exactly one row", rs.next());

            rs.close();
            stmt.close();

            stmt = connection.prepareStatement("SELECT none_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, UKRAINIAN_TEST_ID);

            try {
                rs = stmt.executeQuery();

                rs.getString(1);

                fail("Should not be able to read none_field with special characters");
            } catch (SQLException sqlex) {
                // everything is ok
            }

            stmt.close();
        } finally {
            connection.close();
        }
    }

    // This test only demonstrate a failure in the CYRL character set
    public static byte[] CYRL_TEST_BYTES = new byte[] {
            (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3,
            (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
            (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xED, (byte) 0xEC, (byte) 0xEF
    };
    // These are the correct uppercase bytes
    public static byte[] CYRL_TEST_BYTES_UPPER = new byte[] {
            (byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
            (byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7,
            (byte) 0xC8, (byte) 0xC9, (byte) 0xCA, (byte) 0xCB,
            (byte) 0xCC, (byte) 0xCD, (byte) 0xCC, (byte) 0xCF
    };
    // These are the wrong uppercase bytes
    public static byte[] CYRL_TEST_BYTES_UPPER_WRONG = new byte[] {
            (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93,
            (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
            (byte) 0x00, (byte) 0x99, (byte) 0x9A, (byte) 0x9B,
            (byte) 0x9C, (byte) 0x9D, (byte) 0x9C, (byte) 0x9F
    };

    @Test
    public void testCyrl() throws Exception {
        final String cyrlTestString = new String(CYRL_TEST_BYTES, "Cp1251");
        final String cyrlTestStringUpper = new String(CYRL_TEST_BYTES_UPPER, "Cp1251");
        final String cyrlTestStringUpperWrong = new String(CYRL_TEST_BYTES_UPPER_WRONG, "Cp1251");

        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings_cyrl(id, cyrl_field, win1251_field, unicode_field) VALUES(?, ?, ?, ?)");

            stmt.setInt(1, 1);
            stmt.setString(2, cyrlTestString);
            stmt.setString(3, cyrlTestString);
            stmt.setString(4, cyrlTestString);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);
            //
            // Select the same case
            //
            stmt = connection.prepareStatement(
                    "SELECT cyrl_field, win1251_field, unicode_field FROM test_encodings_cyrl WHERE id = ?");

            stmt.setInt(1, 1);

            ResultSet rs = stmt.executeQuery();

            assertTrue("Should have at least one row", rs.next());

            String cyrlValue = rs.getString(1);
            String win1251Value = rs.getString(2);
            String unicodeValue = rs.getString(3);

            assertEquals("Cyrl_field and Win1251_field must be equal ", win1251Value, cyrlValue);
            assertEquals("Win1251_field and Unicode_field must be equal ", win1251Value, unicodeValue);
            assertEquals("Cyrl_field and Unicode_field must be equal ", cyrlValue, unicodeValue);

            assertFalse("Should have exactly one row", rs.next());

            rs.close();
            stmt.close();
            //
            // Select upper case
            //
            stmt = connection.prepareStatement(
                    "SELECT UPPER(cyrl_field), UPPER(win1251_field), UPPER(unicode_field) "
                            + "FROM test_encodings_cyrl WHERE id = ?");

            stmt.setInt(1, 1);

            rs = stmt.executeQuery();

            assertTrue("Should have at least one row", rs.next());

            String cyrlValueUpper = rs.getString(1);
            String win1251ValueUpper = rs.getString(2);
            String unicodeValueUpper = rs.getString(3);


            assertNotEquals("Upper(Cyrl_field) must be != Cyrl_field ", cyrlValue, cyrlValueUpper);
            assertNotEquals("Upper(Win1251_field) must be != Win1251_field ", win1251Value, win1251ValueUpper);
            // Unicode only uppercase ASCII characters (until Firebird 2.0)
            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.getDatabaseMajorVersion() < 2)
                assertEquals("Upper(unicode) must be == Unicode_field ", unicodeValue, unicodeValueUpper);

            assertEquals("Upper(win1251_field) must == upper test string ", cyrlTestStringUpper, win1251ValueUpper);
            // The CYRL charset fails because the mapping is 1251 and the uppercase
            // and lowercase functions work as if the charset is CP866
            assertEquals("Upper(cyrl_field) must be == wrong upper test string ",
                    cyrlTestStringUpperWrong, cyrlValueUpper);

            // unicode does not uppercase (until FB 2.0)

            if (metaData.getDatabaseMajorVersion() < 2)
                assertNotEquals("Upper(Unicode_field) must be != upper test string ",
                        unicodeValueUpper, cyrlTestStringUpper);

            assertFalse("Should have exactly one row", rs.next());

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

    @Test
    public void testGerman() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1252");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, win1252_field, unicode_field, none_field) VALUES(?, ?, ?, ?)");

            stmt.setInt(1, GERMAN_TEST_ID);
            stmt.setString(2, GERMAN_TEST_STRING_WIN1252);
            stmt.setString(3, GERMAN_TEST_STRING_WIN1252);
            stmt.setString(4, GERMAN_TEST_STRING_WIN1252);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            stmt = connection.prepareStatement(
                    "SELECT win1252_field, unicode_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, GERMAN_TEST_ID);

            ResultSet rs = stmt.executeQuery();

            assertTrue("Should have at least one row", rs.next());

            String win1252Value = rs.getString(1);
            assertEquals("win1252_field value should be the same", GERMAN_TEST_STRING_WIN1252, win1252Value);

            String unicodeValue = rs.getString(2);
            assertEquals("unicode_field value should be the same", GERMAN_TEST_STRING_WIN1252, unicodeValue);

            assertFalse("Should have exactly one row", rs.next());

            rs.close();
            stmt.close();

            stmt = connection.prepareStatement("SELECT none_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, GERMAN_TEST_ID);

            try {
                rs = stmt.executeQuery();

                rs.getString(1);

                fail("Should not be able to read none_field with special characters");
            } catch (SQLException sqlex) {
                // everything is ok
            }

            stmt.close();

        } finally {
            connection.close();
        }
    }

    // String in Hungarian ("\u0151r\u00FClt")
    private static final String HUNGARIAN_TEST_STRING = "\u0151\u0072\u00fc\u006c\u0074";

    public static int HUNGARIAN_TEST_ID = 3;

    @Test
    public void testHungarian() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings("
                            + "  id, win1250_field, win1251_field, win1252_field, "
                            + "  unicode_field, none_field) "
                            + "VALUES(?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, HUNGARIAN_TEST_ID);
            stmt.setString(2, HUNGARIAN_TEST_STRING);
            stmt.setString(3, UKRAINIAN_TEST_STRING);
            stmt.setString(4, GERMAN_TEST_STRING_WIN1252);
            stmt.setString(5, HUNGARIAN_TEST_STRING);
            stmt.setString(6, HUNGARIAN_TEST_STRING);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            stmt = connection.prepareStatement(
                    "SELECT win1250_field, win1251_field, win1252_field, unicode_field "
                            + "FROM test_encodings WHERE id = ?");

            stmt.setInt(1, HUNGARIAN_TEST_ID);

            ResultSet rs = stmt.executeQuery();

            assertTrue("Should have at least one row", rs.next());

            String win1250Value = rs.getString(1);
            assertEquals("win1250_field value should be the same", HUNGARIAN_TEST_STRING, win1250Value);

            String win1251Value = rs.getString(2);
            assertEquals("win1251_field value should be the same", UKRAINIAN_TEST_STRING, win1251Value);

            String win1252Value = rs.getString(3);
            assertEquals("win1252_field value should be the same", GERMAN_TEST_STRING_WIN1252, win1252Value);

            String unicodeValue = rs.getString(4);
            assertEquals("unicode_field value should be the same", HUNGARIAN_TEST_STRING, unicodeValue);

            assertFalse("Should have exactly one row", rs.next());

            rs.close();
            stmt.close();

            stmt = connection.prepareStatement("SELECT none_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, HUNGARIAN_TEST_ID);

            try {
                rs = stmt.executeQuery();

                rs.getString(1);

                fail("Should not be able to read none_field with special characters");
            } catch (SQLException sqlex) {
                // everything is ok
            }

            stmt.close();
        } finally {
            connection.close();
        }
    }

    private static final byte[] UNIVERSAL_TEST_BYTES = new byte[] {
            (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, /* (byte)0xE3, CANT MAP IN ISO_8859_3 */
            (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, /* (byte)0xE7, CANT MAP IN DOS857 */
            (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xED, (byte) 0xEC, (byte) 0xEF
    };

    public static final int UNIVERSAL_TEST_ID = 1;

    @Test
    public void testUniversal() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            StringBuilder insert = new StringBuilder("INSERT INTO test_encodings_universal VALUES(? ");
            for (int col = 0; col < ENCODINGS_JAVA.size() * 2; col++) {
                insert.append(", ?");
            }
            insert.append(')');
            PreparedStatement stmt = connection.prepareStatement(insert.toString());

            stmt.setInt(1, UNIVERSAL_TEST_ID);
            for (int col = 0; col < ENCODINGS_JAVA.size(); col++) {
                String value = new String(UNIVERSAL_TEST_BYTES, ENCODINGS_JAVA.get(col));
                stmt.setString(col + 2, value);
                stmt.setString(col + ENCODINGS_JAVA.size() + 2, value);
            }

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);
            //
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT * FROM test_encodings_universal WHERE id = ?");
            stmt.setInt(1, UNIVERSAL_TEST_ID);

            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());

            for (int col = 0; col < ENCODINGS_JAVA.size(); col++) {
                String charsetValue = rs.getString(col + 2);
                String unicodeValue = rs.getString(col + ENCODINGS_JAVA.size() + 2);

                assertEquals("charsetValue " + ENCODINGS_JAVA.get(col) + " should be the same that unicode",
                        unicodeValue, charsetValue);
                assertEquals("charsetValue " + ENCODINGS_JAVA.get(col) + " should be == string", new String(
                        UNIVERSAL_TEST_BYTES, ENCODINGS_JAVA.get(col)), charsetValue);
            }
            assertFalse("Should have exactly one row", rs.next());

            rs.close();
            stmt.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testPadding() throws Exception {
        final String testString = "test string";

        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings("
                            + "  id, char_field) "
                            + "VALUES(?, ?)");

            stmt.setInt(1, UNIVERSAL_TEST_ID);
            stmt.setString(2, testString);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            //
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT char_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, UNIVERSAL_TEST_ID);

            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());

            String str = rs.getString(1);

            assertEquals("Field length should be correct", 50, str.length());
            assertEquals("Trimmed values should be correct.", testString, str.trim());

            stmt.close();
        } finally {
            connection.close();
        }
    }

    protected static final byte[] TRANSLATION_TEST_BYTES = new byte[] {
            (byte) 0xde, (byte) 0xbd, (byte) 0xd8, (byte) 0xda, (byte) 0xdb, (byte) 0xcc, (byte) 0xce, (byte) 0xcf
    };

    protected static final String TRANSLATION_TEST = "\u00df\u00a7\u00c4\u00d6\u00dc\u00e4\u00f6\u00fc";

    /**
     * Test whether character translation code works correctly.
     *
     * @throws Exception
     *         if something went wrong.
     */
    @Test
    public void testTranslation() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");
        props.put("charSet", "Cp1252");
        props.put("useTranslation", "translation.hpux");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, none_field) VALUES(?, ?)");

            stmt.setInt(1, UNIVERSAL_TEST_ID);
            stmt.setBytes(2, TRANSLATION_TEST_BYTES);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            //
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT none_field FROM test_encodings WHERE id = ?");

            stmt.setInt(1, UNIVERSAL_TEST_ID);

            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());

            assertEquals("Value should be correct.", TRANSLATION_TEST, rs.getString(1));

            stmt.close();

        } finally {
            connection.close();
        }
    }

    protected static final byte[] OCTETS_DATA = new byte[] {
            1, 2, 3, 4, 5, 6, 0, 0, 7
    };

    // Full length of the octets fields (10 bytes)
    protected static final byte[] OCTETS_DATA_FULL_LENGTH = new byte[10];
    protected static final byte[] OCTETS_DATA_AS_NONE_FULL_LENGTH = new byte[10];

    static {
        Arrays.fill(OCTETS_DATA_FULL_LENGTH, (byte) 0);
        System.arraycopy(OCTETS_DATA, 0, OCTETS_DATA_FULL_LENGTH, 0, OCTETS_DATA.length);
        Arrays.fill(OCTETS_DATA_AS_NONE_FULL_LENGTH, (byte) 32);
        System.arraycopy(OCTETS_DATA, 0, OCTETS_DATA_AS_NONE_FULL_LENGTH, 0, OCTETS_DATA.length);
    }

    protected byte[] getOctetsFullLength() {
        return OCTETS_DATA_FULL_LENGTH;
    }

    protected byte[] getOctetsFullLengthAsNone() {
        return OCTETS_DATA_AS_NONE_FULL_LENGTH;
    }

    @Test
    public void testOctets() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("charSet", "Cp1252");

        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, octets_field, var_octets_field, none_octets_field) "
                            + "VALUES(?, ?, ?, ?)");

            stmt.setInt(1, UNIVERSAL_TEST_ID);
            stmt.setBytes(2, OCTETS_DATA);
            stmt.setBytes(3, OCTETS_DATA);
            stmt.setBytes(4, OCTETS_DATA);

            int updated = stmt.executeUpdate();
            stmt.close();

            assertEquals("Should insert one row", 1, updated);

            //
            // Test each column
            //
            stmt = connection.prepareStatement("SELECT octets_field, var_octets_field, none_octets_field "
                    + "FROM test_encodings WHERE id = ?");

            stmt.setInt(1, UNIVERSAL_TEST_ID);

            ResultSet rs = stmt.executeQuery();
            assertTrue("Should have at least one row", rs.next());

            byte[] charBytes = rs.getBytes(1);
            byte[] varcharBytes = rs.getBytes(2);
            byte[] noneBytes = rs.getBytes(3);

            assertArrayEquals("CHAR OCTETS value should be correct.", getOctetsFullLength(), charBytes);
            assertArrayEquals("VARCHAR OCTETS value should be correct.", OCTETS_DATA, varcharBytes);
            assertArrayEquals("CHAR NONE value should be correct.", getOctetsFullLengthAsNone(), noneBytes);

            stmt.close();
        } finally {
            connection.close();
        }
    }

    @Test
    public void testExecuteBlock() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            assumeTrue("Test requires EXECUTE BLOCK support", supportInfoFor(connection).supportsExecuteBlock());

            Statement stmt = connection.createStatement();
            try {
                stmt.execute("INSERT INTO test_encodings(unicode_field) VALUES('" +
                        "0123456789" +
                        "abcdefghij" +
                        "klmnopqrst" +
                        "uvwxyz____" +
                        "0123456789" +
                        "')");

                ResultSet rs = stmt.executeQuery("EXECUTE BLOCK RETURNS ( "
                        + "STR VARCHAR(3) CHARACTER SET UNICODE_FSS) "
                        + "AS BEGIN   STR = 'abc';   SUSPEND; "
                        + "END ");

                rs.next();
                System.out.println(rs.getString(1));

            } finally {
                stmt.close();
            }

        } finally {
            connection.close();
        }
    }

    @Test
    public void testCharFieldWithUTF8Encoding() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UTF8");
        Connection connection = DriverManager.getConnection(getUrl(), props);

        try {
            final String randomUUID = UUID.randomUUID().toString();
            Statement statement = connection.createStatement();
            try {
                String updateSql = "INSERT INTO test_encodings (uuid_char, uuid_varchar) VALUES ('" + randomUUID + "', '" + randomUUID + "')";
                statement.executeUpdate(updateSql);

                String sql = "SELECT uuid_char, CHAR_LENGTH(uuid_char), uuid_varchar, CHAR_LENGTH(uuid_varchar) FROM test_encodings";

                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    String uuidChar = rs.getString("uuid_char");
                    assertEquals("compare CHAR_LENGTH", rs.getInt(2), rs.getInt(4));
                    assertEquals(randomUUID.length(), rs.getInt(2));
                    assertEquals(randomUUID, rs.getString("uuid_varchar"));
                    // now it fails:
                    assertEquals(randomUUID.length(), uuidChar.length());
                    assertEquals(randomUUID, uuidChar);
                }
            } finally {
                statement.close();
            }
        } finally {
            connection.close();
        }
    }
}
