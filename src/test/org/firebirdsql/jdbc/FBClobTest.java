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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.*;

class FBClobTest {

    private static final String PLAIN_BLOB = "plain_blob";
    private static final String TEXT_BLOB = "text_blob";
    private static final String UTF8_BLOB = "utf8_blob";
    private static final String ISO8859_1_BLOB = "iso8859_1_blob";

    private static final String CREATE_TABLE =
            "CREATE TABLE test_clob(" +
                    "  id INTEGER, " +
                    TEXT_BLOB + " BLOB SUB_TYPE TEXT, " +
                    PLAIN_BLOB + " BLOB, " +
                    UTF8_BLOB + " BLOB SUB_TYPE TEXT CHARACTER SET UTF8, " +
                    ISO8859_1_BLOB + " BLOB SUB_TYPE TEXT CHARACTER SET ISO8859_1 )";

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            CREATE_TABLE);

    private static final byte[] LATIN1_BYTES = new byte[] { (byte) 0xC8,
            (byte) 0xC9, (byte) 0xCA, (byte) 0xCB };

    private static final byte[] CP1251_BYTES = new byte[] { (byte) 0xf2,
            (byte) 0xe5, (byte) 0xf1, (byte) 0xf2, (byte) 0xee, (byte) 0xe2,
            (byte) 0xe0, (byte) 0x20, (byte) 0xf1, (byte) 0xf2, (byte) 0xf0,
            (byte) 0xb3, (byte) 0xf7, (byte) 0xea, (byte) 0xe0 };

    private static final byte[] UTF8_BYTES;

    private static final String LATIN1_TEST_STRING;
    private static final String CP1251_TEST_STRING;
    private static final String UTF8_TEST_STRING =
            "\u16a0\u16a1\u16a2\u16a3\u16a4\u16a5\u16a6\u16a7\u16a8\u16a9\u16aa\u16ab\u16ac\u16ad\u16ae\u16af";

    static {
        try {
            LATIN1_TEST_STRING = new String(LATIN1_BYTES, StandardCharsets.ISO_8859_1);
            CP1251_TEST_STRING = new String(CP1251_BYTES, "Cp1251");
            UTF8_BYTES = UTF8_TEST_STRING.getBytes(StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Connection con;

    @BeforeAll
    static void setupAll() throws Exception {
        con = getConnectionViaDriverManager();
    }

    @BeforeEach
    void setup() throws Exception {
        try (Statement stmt = con.createStatement()) {
            stmt.execute("delete from test_clob");
        } finally {
            con.setAutoCommit(true);
        }
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        try {
            con.close();
        } finally {
            con = null;
        }
    }

    @Test
    void testSimpleGetAsciiStream() throws Exception {
        final String TEST_VALUE = "TEST_VALUE";
        addTestValues(con, 1, TEST_VALUE, PLAIN_BLOB);

        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);
            InputStream inputStream = clob.getAsciiStream();
            String clobValue = slurpString(inputStream);

            assertEquals(TEST_VALUE, clobValue);
        }
    }

    @Test
    void testNullClob() throws SQLException {
        addTestValues(con, 1, null, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);

            assertNull(clob);
        }
    }

    @Test
    void testCachedNullClob() throws SQLException {
        addTestValues(con, 1, null, PLAIN_BLOB);
        try (PreparedStatement stmt = con.prepareStatement("SELECT " + PLAIN_BLOB + " FROM test_clob",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
             ResultSet resultSet = stmt.executeQuery()) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);
            assertNull(clob);
        }
    }

    @Test
    void testSimpleGetCharacterStream() throws Exception {
        final String TEST_VALUE = "TEST_STRING";
        addTestValues(con, 1, TEST_VALUE, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);
            String clobValue = slurpString(clob.getCharacterStream());

            assertEquals(TEST_VALUE, clobValue);
        }
    }

    @Test
    void testSimpleGetCharacterStreamAsNClob() throws Exception {
        final String TEST_VALUE = "TEST_STRING";
        addTestValues(con, 1, TEST_VALUE, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            NClob clob = resultSet.getNClob(1);
            String clobValue = slurpString(clob.getCharacterStream());

            assertEquals(TEST_VALUE, clobValue);
        }
    }

    @Test
    void testGetSubString() throws SQLException {
        final String TEST_VALUE = "TEST_STRING";
        addTestValues(con, 1, TEST_VALUE, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);

            for (int start = 1; start <= TEST_VALUE.length(); start++) {
                for (int length = 0; length <= TEST_VALUE.length() - (start - 1); length++) {
                    String clobValue = clob.getSubString(start, length);
                    assertEquals(TEST_VALUE.substring(start - 1, start - 1 + length), clobValue);
                }
            }
            assertEquals(TEST_VALUE, clob.getSubString(1, TEST_VALUE.length() * 2));
            assertEquals("", clob.getSubString(1, 0));
        }
    }

    @ParameterizedTest
    @MethodSource
    void testMultibyteRead(String testString, String fbEncoding, String colName, String javaEncoding)
            throws Exception {
        try (Connection con = getEncodedConnection(fbEncoding)) {
            insertStringBytesViaBlobWithEncoding(con, testString, colName, javaEncoding);
            char[] buffer = readClobViaCharacterStream(con, colName, testString.length());
            String outputString = new String(buffer);

            assertEquals(testString, outputString);
        }
    }

    static Stream<Arguments> testMultibyteRead() {
        return Stream.of(
                Arguments.of(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB, "UTF-8"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB, "UTF-8"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB, "ISO-8859-1"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB, "ISO-8859-1"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251"));
    }

    @ParameterizedTest
    @MethodSource
    void testMultibyteWrite(String testString, String fbEncoding, String colName, String javaEncoding)
            throws Exception {
        try (Connection con = getEncodedConnection(fbEncoding)) {
            insertStringViaClobCharacterStream(con, testString, colName);

            String selectString = readStringViaGetBytes(con, colName, javaEncoding);

            assertEquals(testString, selectString);
        }
    }

    static Stream<Arguments> testMultibyteWrite() {
        return Stream.of(
                Arguments.of(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB, "UTF-8"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB, "UTF-8"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB, "ISO-8859-1"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251"),
                Arguments.of(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB, "ISO-8859-1"),
                Arguments.of(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251"));
    }

    @ParameterizedTest
    @MethodSource
    void testHoldableClob(String colName, String testString, String javaEncoding, String fbEncoding)
            throws Exception {
        Clob clob;
        try (Connection con = getEncodedConnection(fbEncoding)) {
            insertStringBytesViaBlobWithEncoding(con, testString, colName, javaEncoding);

            try (PreparedStatement stmt = con.prepareStatement("SELECT " + colName + " FROM test_clob",
                    ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
                 ResultSet resultSet = stmt.executeQuery()) {
                resultSet.next();
                clob = resultSet.getClob(1);
            }
        }

        String receivedValue;
        try (Reader reader = clob.getCharacterStream()) {
            receivedValue = slurpString(reader);
        }

        assertEquals(testString, receivedValue);
    }

    static Stream<Arguments> testHoldableClob() {
        return Stream.of(
                Arguments.of(PLAIN_BLOB, LATIN1_TEST_STRING, "UTF-8", "UNICODE_FSS"),
                Arguments.of(TEXT_BLOB, LATIN1_TEST_STRING, "ISO-8859-1", "ISO8859_1"));
    }

    @Test
    void testWriteClobUsingReader() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setClob(1, new StringReader(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(LATIN1_TEST_STRING, rs.getString(1), "Unexpected value for clob roundtrip");
            }
        }
    }

    @Test
    void testWriteClobUsingReaderAsNClob() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setNClob(1, new StringReader(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(LATIN1_TEST_STRING, rs.getString(1), "Unexpected value for clob roundtrip");
            }
        }
    }

    @Test
    void testWriteClobUsingNonFBClob() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setClob(1, new StringClob(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                assertTrue(rs.next(), "Expected a row");
                assertEquals(LATIN1_TEST_STRING, rs.getString(1), "Unexpected value for clob roundtrip");
            }
        }
    }

    @Test
    void testReadClob_utf8_win1252() throws Exception {
        try (Connection con = getEncodedConnection("UTF8");
             PreparedStatement pstmt = con.prepareStatement(
                     "insert into test_clob (id, " + UTF8_BLOB + ") values (1, ?)")) {
            pstmt.setBytes(1, LATIN1_TEST_STRING.getBytes(StandardCharsets.UTF_8));
            pstmt.execute();
            pstmt.setString(1, "A");
            pstmt.execute();
        }

        try (Connection con = getEncodedConnection("WIN1252");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select " + UTF8_BLOB + " from test_clob where id = 1")) {
            assertTrue(rs.next());
            assertEquals(LATIN1_TEST_STRING, rs.getString(1));
        }
    }

    @Test
    void testReadClob_utf8_none() throws Exception {
        try (Connection con = getEncodedConnection("UTF8");
             PreparedStatement pstmt = con.prepareStatement(
                     "insert into test_clob (id, " + UTF8_BLOB + ") values (1, ?)")) {
            pstmt.setBytes(1, UTF8_BYTES);
            pstmt.execute();
            pstmt.setString(1, "A");
            pstmt.execute();
        }

        try (Connection con = getEncodedConnection("NONE");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select " + UTF8_BLOB + " from test_clob where id = 1")) {
            assertTrue(rs.next());
            assertEquals(UTF8_TEST_STRING, rs.getString(1));
        }
    }

    @Test
    void testSQLExceptionAfterFree() throws Exception {
        final String TEST_VALUE = "TEST_STRING";
        addTestValues(con, 1, TEST_VALUE, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);
            clob.free();

            assertThrows(SQLException.class, clob::getCharacterStream);
            assertThrows(SQLException.class, () -> clob.getCharacterStream(1, 1));
            assertThrows(SQLException.class, () -> { try (Writer ignored = clob.setCharacterStream(1)) { } });
        }
    }

    private String readStringViaGetBytes(Connection con, String colName, String javaEncoding) throws SQLException,
            UnsupportedEncodingException {
        try (PreparedStatement selectStmt = con.prepareStatement("SELECT " + colName + " FROM test_clob");
             ResultSet resultSet = selectStmt.executeQuery()) {
            resultSet.next();
            byte[] byteBuffer = resultSet.getBytes(1);
            return new String(byteBuffer, javaEncoding);
        }
    }

    private void insertStringViaClobCharacterStream(Connection con, String testString, String colName)
            throws SQLException, IOException {
        try (PreparedStatement insertStmt = con.prepareStatement(
                "INSERT INTO test_clob (" + colName + ") VALUES (?)")) {
            Clob insertClob = con.createClob();
            try (Writer writer = insertClob.setCharacterStream(1)) {
                writer.write(testString.toCharArray());
            }
            insertStmt.setClob(1, insertClob);
            insertStmt.execute();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private char[] readClobViaCharacterStream(Connection con, String colName, int expectedLength) throws SQLException,
            IOException {
        try (PreparedStatement selectStmt = con.prepareStatement("SELECT " + colName + " FROM test_clob")) {
            ResultSet resultSet = selectStmt.executeQuery();
            resultSet.next();

            Clob clob = resultSet.getClob(1);
            try (Reader reader = clob.getCharacterStream()) {
                char[] buffer = new char[expectedLength];
                reader.read(buffer);
                return buffer;
            }
        }
    }

    private void insertStringBytesViaBlobWithEncoding(Connection con, String insertString, String colName,
            String javaEncoding) throws Exception {
        try (PreparedStatement insertStmt = con.prepareStatement(
                "INSERT INTO test_clob (" + colName + ") VALUES (?)")) {
            byte[] bytes = insertString.getBytes(javaEncoding);
            insertStmt.setBytes(1, bytes);

            insertStmt.execute();
        }
    }

    private FBConnection getEncodedConnection(String encoding) throws SQLException {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", encoding);
        Connection connection = DriverManager.getConnection(getUrl(), props);
        return (FBConnection) connection;
    }

    private String slurpString(InputStream inputStream) throws IOException {
        return slurpString(new InputStreamReader(inputStream));
    }

    private String slurpString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int n;
        try (Reader temp = reader) {
            while ((n = temp.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
        } 
        return sb.toString();
    }

    @SuppressWarnings("SameParameterValue")
    private void addTestValues(Connection con, int id, String value, String colName) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO test_clob (id, " + colName + ") VALUES (?, ?)")) {
            stmt.setInt(1, id);
            stmt.setString(2, value);
            stmt.execute();
        }
    }
}
