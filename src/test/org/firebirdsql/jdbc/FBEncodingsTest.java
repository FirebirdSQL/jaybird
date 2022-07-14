/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FBEncodingsTest {

    private static final List<String> ENCODINGS_JAVA;
    private static final List<String> ENCODINGS_FIREBIRD;
    private static final String CREATE_TABLE_UNIVERSAL;

    static {
        List<String> encJava = new ArrayList<>();
        List<String> encFB = new ArrayList<>();

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

        ENCODINGS_JAVA = Collections.unmodifiableList(encJava);
        ENCODINGS_FIREBIRD = Collections.unmodifiableList(encFB);

        StringBuilder sb = new StringBuilder("CREATE TABLE test_encodings_universal (");
        sb.append("  id INTEGER ");
        for (int encN = 0; encN < ENCODINGS_JAVA.size(); encN++) {
            sb.append(',').append(ENCODINGS_JAVA.get(encN)).append("_field VARCHAR(50) CHARACTER SET ").append(ENCODINGS_FIREBIRD.get(encN));
        }
        //noinspection ForLoopReplaceableByForEach
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

    @RegisterExtension
    final UsesDatabaseExtension.UsesDatabaseForEach usesDatabase = UsesDatabaseExtension.usesDatabase();

    protected String getCreateTableStatement() {
        return CREATE_TABLE;
    }

    protected String getCreateTableStatement_cyrl() {
        return CREATE_TABLE_CYRL;
    }

    @BeforeEach
    void setUp() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "NONE");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            List<String> createTableStatements = Arrays.asList(
                    getCreateTableStatement(), getCreateTableStatement_cyrl(), CREATE_TABLE_UNIVERSAL);
            if (!supportInfoFor(connection).supportsUtf8()) {
                createTableStatements.replaceAll(s -> s.replace("CHARACTER SET UTF8", "CHARACTER SET UNICODE_FSS"));
            }
            for (String ddlStatement : createTableStatements) {
                executeCreateTable(connection, ddlStatement);
            }
        }
    }

    // "test string" in Ukrainian
    private static final String UKRAINIAN_TEST_STRING =
            "\u0442\u0435\u0441\u0442\u043e\u0432\u0430 \u0441\u0442\u0440\u0456\u0447\u043a\u0430";

    private static final int UKRAINIAN_TEST_ID = 1;

    @Test
    void testUkrainian() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings("
                            + "  id, win1251_field, unicode_field, none_field) "
                            + "VALUES(?, ?, ?, ?)")) {

                stmt.setInt(1, UKRAINIAN_TEST_ID);
                stmt.setString(2, UKRAINIAN_TEST_STRING);
                stmt.setString(3, UKRAINIAN_TEST_STRING);
                stmt.setString(4, UKRAINIAN_TEST_STRING);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT win1251_field, unicode_field, '" + UKRAINIAN_TEST_STRING + "' direct_sql_field "
                            + "FROM test_encodings WHERE id = ?")) {

                stmt.setInt(1, UKRAINIAN_TEST_ID);

                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Should have at least one row");

                String win1251Value = rs.getString(1);
                assertEquals(UKRAINIAN_TEST_STRING, win1251Value, "win1251_field value should be the same");

                String unicodeValue = rs.getString(2);
                assertEquals(UKRAINIAN_TEST_STRING, unicodeValue, "unicode_field value should be the same");

                String directSqlValue = rs.getString(3);
                assertEquals(UKRAINIAN_TEST_STRING, directSqlValue, "direct_sql_field should be the same");

                assertFalse(rs.next(), "Should have exactly one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT none_field FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, UKRAINIAN_TEST_ID);

                assertThrows(SQLException.class, () -> {
                    ResultSet rs = stmt.executeQuery();
                    rs.getString(1);
                }, "Should not be able to read none_field with special characters");
            }
        }
    }

    // This test only demonstrate a failure in the CYRL character set
    private static final byte[] CYRL_TEST_BYTES = new byte[] {
            (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3,
            (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, (byte) 0xE7,
            (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xED, (byte) 0xEC, (byte) 0xEF
    };
    // These are the correct uppercase bytes
    private static final byte[] CYRL_TEST_BYTES_UPPER = new byte[] {
            (byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3,
            (byte) 0xC4, (byte) 0xC5, (byte) 0xC6, (byte) 0xC7,
            (byte) 0xC8, (byte) 0xC9, (byte) 0xCA, (byte) 0xCB,
            (byte) 0xCC, (byte) 0xCD, (byte) 0xCC, (byte) 0xCF
    };
    // These are the wrong uppercase bytes
    private static final byte[] CYRL_TEST_BYTES_UPPER_WRONG = new byte[] {
            (byte) 0x90, (byte) 0x91, (byte) 0x92, (byte) 0x93,
            (byte) 0x94, (byte) 0x95, (byte) 0x96, (byte) 0x97,
            (byte) 0x00, (byte) 0x99, (byte) 0x9A, (byte) 0x9B,
            (byte) 0x9C, (byte) 0x9D, (byte) 0x9C, (byte) 0x9F
    };

    @Test
    void testCyrl() throws Exception {
        final String cyrlTestString = new String(CYRL_TEST_BYTES, "Cp1251");
        final String cyrlTestStringUpper = new String(CYRL_TEST_BYTES_UPPER, "Cp1251");
        final String cyrlTestStringUpperWrong = new String(CYRL_TEST_BYTES_UPPER_WRONG, "Cp1251");

        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1251");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings_cyrl(id, cyrl_field, win1251_field, unicode_field) VALUES(?, ?, ?, ?)")) {

                stmt.setInt(1, 1);
                stmt.setString(2, cyrlTestString);
                stmt.setString(3, cyrlTestString);
                stmt.setString(4, cyrlTestString);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }
            //
            // Select the same case
            //
            String cyrlValue;
            String win1251Value;
            String unicodeValue;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT cyrl_field, win1251_field, unicode_field FROM test_encodings_cyrl WHERE id = ?")) {

                stmt.setInt(1, 1);

                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Should have at least one row");

                cyrlValue = rs.getString(1);
                win1251Value = rs.getString(2);
                unicodeValue = rs.getString(3);

                assertEquals(win1251Value, cyrlValue, "Cyrl_field and Win1251_field must be equal ");
                assertEquals(win1251Value, unicodeValue, "Win1251_field and Unicode_field must be equal ");
                assertEquals(cyrlValue, unicodeValue, "Cyrl_field and Unicode_field must be equal ");

                assertFalse(rs.next(), "Should have exactly one row");
            }
            //
            // Select upper case
            //
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT UPPER(cyrl_field), UPPER(win1251_field), UPPER(unicode_field) "
                            + "FROM test_encodings_cyrl WHERE id = ?")) {

                stmt.setInt(1, 1);

                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Should have at least one row");

                String cyrlValueUpper = rs.getString(1);
                String win1251ValueUpper = rs.getString(2);
                String unicodeValueUpper = rs.getString(3);

                assertNotEquals(cyrlValue, cyrlValueUpper, "Upper(Cyrl_field) must be != Cyrl_field ");
                assertNotEquals(win1251Value, win1251ValueUpper, "Upper(Win1251_field) must be != Win1251_field ");
                // Unicode only uppercase ASCII characters (until Firebird 2.0)
                DatabaseMetaData metaData = connection.getMetaData();
                if (metaData.getDatabaseMajorVersion() < 2)
                    assertEquals(unicodeValue, unicodeValueUpper, "Upper(unicode) must be == Unicode_field ");

                assertEquals(cyrlTestStringUpper, win1251ValueUpper, "Upper(win1251_field) must == upper test string ");
                // The CYRL charset fails because the mapping is 1251 and the uppercase
                // and lowercase functions work as if the charset is CP866
                assertEquals(cyrlTestStringUpperWrong, cyrlValueUpper,
                        "Upper(cyrl_field) must be == wrong upper test string");

                // unicode does not uppercase (until FB 2.0)

                if (metaData.getDatabaseMajorVersion() < 2)
                    assertNotEquals("Upper(Unicode_field) must be != upper test string ",
                            unicodeValueUpper, cyrlTestStringUpper);

                assertFalse(rs.next(), "Should have exactly one row");
            }
        }
    }

    // couple of test characters in German
    private static final String GERMAN_TEST_STRING_WIN1252 =
            "Zeichen " + "\u00c4\u00e4, \u00d6\u00f6, \u00dc\u00fc und \u00df";

    private static final int GERMAN_TEST_ID = 2;

    @Test
    void testGerman() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "WIN1252");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, win1252_field, unicode_field, none_field) VALUES(?, ?, ?, ?)")) {
                stmt.setInt(1, GERMAN_TEST_ID);
                stmt.setString(2, GERMAN_TEST_STRING_WIN1252);
                stmt.setString(3, GERMAN_TEST_STRING_WIN1252);
                stmt.setString(4, GERMAN_TEST_STRING_WIN1252);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT win1252_field, unicode_field FROM test_encodings WHERE id = ?")) {

                stmt.setInt(1, GERMAN_TEST_ID);

                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Should have at least one row");

                String win1252Value = rs.getString(1);
                assertEquals(GERMAN_TEST_STRING_WIN1252, win1252Value, "win1252_field value should be the same");

                String unicodeValue = rs.getString(2);
                assertEquals(GERMAN_TEST_STRING_WIN1252, unicodeValue, "unicode_field value should be the same");

                assertFalse(rs.next(), "Should have exactly one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT none_field FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, GERMAN_TEST_ID);

                assertThrows(SQLException.class, () -> {
                    ResultSet rs = stmt.executeQuery();

                    rs.getString(1);
                }, "Should not be able to read none_field with special characters");
            }
        }
    }

    // String in Hungarian ("\u0151r\u00FClt")
    private static final String HUNGARIAN_TEST_STRING = "\u0151\u0072\u00fc\u006c\u0074";

    private static final int HUNGARIAN_TEST_ID = 3;

    @Test
    void testHungarian() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings("
                            + "  id, win1250_field, win1251_field, win1252_field, "
                            + "  unicode_field, none_field) "
                            + "VALUES(?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, HUNGARIAN_TEST_ID);
                stmt.setString(2, HUNGARIAN_TEST_STRING);
                stmt.setString(3, UKRAINIAN_TEST_STRING);
                stmt.setString(4, GERMAN_TEST_STRING_WIN1252);
                stmt.setString(5, HUNGARIAN_TEST_STRING);
                stmt.setString(6, HUNGARIAN_TEST_STRING);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT win1250_field, win1251_field, win1252_field, unicode_field "
                            + "FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, HUNGARIAN_TEST_ID);

                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Should have at least one row");

                String win1250Value = rs.getString(1);
                assertEquals(HUNGARIAN_TEST_STRING, win1250Value, "win1250_field value should be the same");

                String win1251Value = rs.getString(2);
                assertEquals(UKRAINIAN_TEST_STRING, win1251Value, "win1251_field value should be the same");

                String win1252Value = rs.getString(3);
                assertEquals(GERMAN_TEST_STRING_WIN1252, win1252Value, "win1252_field value should be the same");

                String unicodeValue = rs.getString(4);
                assertEquals(HUNGARIAN_TEST_STRING, unicodeValue, "unicode_field value should be the same");

                assertFalse(rs.next(), "Should have exactly one row");
            }

            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT none_field FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, HUNGARIAN_TEST_ID);

                assertThrows(SQLException.class, () -> {
                    ResultSet rs = stmt.executeQuery();

                    rs.getString(1);
                }, "Should not be able to read none_field with special characters");
            }
        }
    }

    private static final byte[] UNIVERSAL_TEST_BYTES = new byte[] {
            (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, /* (byte)0xE3, CANT MAP IN ISO_8859_3 */
            (byte) 0xE4, (byte) 0xE5, (byte) 0xE6, /* (byte)0xE7, CANT MAP IN DOS857 */
            (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB,
            (byte) 0xEC, (byte) 0xED, (byte) 0xEC, (byte) 0xEF
    };

    private static final int UNIVERSAL_TEST_ID = 1;

    @Test
    void testUniversal() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            StringBuilder insert = new StringBuilder("INSERT INTO test_encodings_universal VALUES(? ");
            for (int col = 0; col < ENCODINGS_JAVA.size() * 2; col++) {
                insert.append(", ?");
            }
            insert.append(')');
            try (PreparedStatement stmt = connection.prepareStatement(insert.toString())) {
                stmt.setInt(1, UNIVERSAL_TEST_ID);
                for (int col = 0; col < ENCODINGS_JAVA.size(); col++) {
                    String value = new String(UNIVERSAL_TEST_BYTES, ENCODINGS_JAVA.get(col));
                    stmt.setString(col + 2, value);
                    stmt.setString(col + ENCODINGS_JAVA.size() + 2, value);
                }

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            //
            // Test each column
            //
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM test_encodings_universal WHERE id = ?")) {
                stmt.setInt(1, UNIVERSAL_TEST_ID);

                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next(), "Should have at least one row");

                for (int col = 0; col < ENCODINGS_JAVA.size(); col++) {
                    String charsetValue = rs.getString(col + 2);
                    String unicodeValue = rs.getString(col + ENCODINGS_JAVA.size() + 2);

                    assertEquals(unicodeValue, charsetValue,
                            "charsetValue " + ENCODINGS_JAVA.get(col) + " should be the same that unicode");
                    assertEquals(new String(UNIVERSAL_TEST_BYTES, ENCODINGS_JAVA.get(col)), charsetValue,
                            "charsetValue " + ENCODINGS_JAVA.get(col) + " should be == string");
                }
                assertFalse(rs.next(), "Should have exactly one row");
            }
        }
    }

    @Test
    void testPadding() throws Exception {
        final String testString = "test string";

        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UNICODE_FSS");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, char_field) VALUES(?, ?)")) {

                stmt.setInt(1, UNIVERSAL_TEST_ID);
                stmt.setString(2, testString);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            //
            // Test each column
            //
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT char_field FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, UNIVERSAL_TEST_ID);

                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next(), "Should have at least one row");

                String str = rs.getString(1);

                assertEquals(50, str.length(), "Field length should be correct");
                assertEquals(testString, str.trim(), "Trimmed values should be correct.");
            }
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
    void testOctets() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("charSet", "Cp1252");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO test_encodings(id, octets_field, var_octets_field, none_octets_field) "
                            + "VALUES(?, ?, ?, ?)")) {

                stmt.setInt(1, UNIVERSAL_TEST_ID);
                stmt.setBytes(2, OCTETS_DATA);
                stmt.setBytes(3, OCTETS_DATA);
                stmt.setBytes(4, OCTETS_DATA);

                int updated = stmt.executeUpdate();
                assertEquals(1, updated, "Should insert one row");
            }

            //
            // Test each column
            //
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT octets_field, var_octets_field, none_octets_field FROM test_encodings WHERE id = ?")) {
                stmt.setInt(1, UNIVERSAL_TEST_ID);

                ResultSet rs = stmt.executeQuery();
                assertTrue(rs.next(), "Should have at least one row");

                byte[] charBytes = rs.getBytes(1);
                byte[] varcharBytes = rs.getBytes(2);
                byte[] noneBytes = rs.getBytes(3);

                assertArrayEquals(getOctetsFullLength(), charBytes, "CHAR OCTETS value should be correct.");
                assertArrayEquals(OCTETS_DATA, varcharBytes, "VARCHAR OCTETS value should be correct.");
                assertArrayEquals(getOctetsFullLengthAsNone(), noneBytes, "CHAR NONE value should be correct.");
            }
        }
    }

    @Test
    void testExecuteBlock() throws Exception {
        assumeTrue(getDefaultSupportInfo().supportsExecuteBlock(), "Test requires EXECUTE BLOCK support");
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            try (Statement stmt = connection.createStatement()) {
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
                assertEquals("abc", rs.getString(1));
            }
        }
    }

    @Test
    void testCharFieldWithUTF8Encoding() throws Exception {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", "UTF8");

        try (Connection connection = DriverManager.getConnection(getUrl(), props)) {
            final String randomUUID = UUID.randomUUID().toString();
            try (Statement statement = connection.createStatement()) {
                String updateSql = "INSERT INTO test_encodings (uuid_char, uuid_varchar) VALUES ('" + randomUUID + "', '" + randomUUID + "')";
                statement.executeUpdate(updateSql);

                String sql = "SELECT uuid_char, CHAR_LENGTH(uuid_char), uuid_varchar, CHAR_LENGTH(uuid_varchar) FROM test_encodings";

                ResultSet rs = statement.executeQuery(sql);
                if (rs.next()) {
                    String uuidChar = rs.getString("uuid_char");
                    assertEquals(rs.getInt(2), rs.getInt(4), "compare CHAR_LENGTH");
                    assertEquals(randomUUID.length(), rs.getInt(2));
                    assertEquals(randomUUID, rs.getString("uuid_varchar"));
                    // now it fails:
                    assertEquals(randomUUID.length(), uuidChar.length());
                    assertEquals(randomUUID, uuidChar);
                }
            }
        }
    }
}
