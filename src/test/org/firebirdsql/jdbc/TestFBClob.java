/*
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

import org.firebirdsql.common.DdlHelper;
import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.*;

public class TestFBClob extends FBJUnit4TestBase {

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

    private Connection con;

    @Before
    public void setUp() throws Exception {
        con = getConnectionViaDriverManager();
        DdlHelper.executeCreateTable(con, CREATE_TABLE);
    }

    @After
    public void tearDown() throws Exception {
        JdbcResourceHelper.closeQuietly(con);
    }

    @Test
    public void testSimpleGetAsciiStream() throws SQLException {
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
    public void testNullClob() throws SQLException {
        addTestValues(con, 1, null, PLAIN_BLOB);
        try (Statement stmt = con.createStatement();
             ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB + " FROM test_clob")) {
            resultSet.next();
            Clob clob = resultSet.getClob(1);

            assertNull(clob);
        }
    }

    @Test
    public void testCachedNullClob() throws SQLException {
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
    public void testSimpleGetCharacterStream() throws Exception {
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
    public void testSimpleGetCharacterStreamAsNClob() throws Exception {
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
    public void testGetSubString() throws SQLException {
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

    @Test
    public void testReadMultiByteCharacterClobUtfLatin1() throws Exception {
        runMultibyteReadTest(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB, "UTF-8");
    }

    @Test
    public void testReadMultiByteCharacterClobUtfCp1251() throws Exception {
        runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
    }

    @Test
    public void testReadMultiByteCharacterClobNoSubtypeUtfLatin1() throws Exception {
        runMultibyteReadTest(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB, "UTF-8");
    }

    @Test
    public void testReadMultiByteCharacterClobNoSubtypeUtfCp1251() throws Exception {
        runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251");
    }

    @Test
    public void testReadMultiByteCharacterClobLatin1() throws Exception {
        runMultibyteReadTest(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB, "ISO-8859-1");
    }

    @Test
    public void testReadMultiByteCharacterClobCp1251() throws Exception {
        runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
    }

    @Test
    public void testReadMultiByteCharacterClobNoSubtypeLatin1() throws Exception {
        runMultibyteReadTest(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB, "ISO-8859-1");
    }

    @Test
    public void testReadMultiByteCharacterClobNoSubtypeCp1251() throws Exception {
        runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251");
    }

    private void runMultibyteReadTest(String testString, String fbEncoding, String colName, String javaEncoding)
            throws Exception {
        try (Connection con = getEncodedConnection(fbEncoding)) {
            insertStringBytesViaBlobWithEncoding(con, testString, colName, javaEncoding);
            char[] buffer = readClobViaCharacterStream(con, colName, testString.length());
            String outputString = new String(buffer);

            assertEquals(testString, outputString);
        }
    }

    @Test
    public void testWriteMultiByteCharacterClobUtfLatin1() throws Exception {
        runMultibyteWriteTest(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB, "UTF-8");
    }

    @Test
    public void testWriteMultiByteCharacterClobUtfCp1251() throws Exception {
        runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
    }

    @Test
    public void testWriteMultiByteCharacterClobPlainBlobUtfLatin1() throws Exception {
        runMultibyteWriteTest(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB, "UTF-8");
    }

    @Test
    public void testWriteMultiByteCharacterClobPlainBlobUtfCp1251() throws Exception {
        runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251");
    }

    @Test
    public void testWriteMultiByteCharacterClobLatin1() throws Exception {
        runMultibyteWriteTest(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB, "ISO-8859-1");
    }

    @Test
    public void testWriteMultiByteCharacterClobCp1251() throws Exception {
        runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
    }

    @Test
    public void testWriteMultiByteCharacterClobPlainBlobLatin1() throws Exception {
        runMultibyteWriteTest(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB, "ISO-8859-1");
    }

    @Test
    public void testWriteMultiByteCharacterClobPlainBlobCp1251() throws Exception {
        runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB, "Cp1251");
    }

    @Test
    public void testHoldableClobFromPlainBlob() throws Exception {
        runHoldableClobTest(PLAIN_BLOB, LATIN1_TEST_STRING, "UTF-8", "UNICODE_FSS");
    }

    @Test
    public void testHoldableClobFromBlobSubtypeText() throws Exception {
        runHoldableClobTest(TEXT_BLOB, LATIN1_TEST_STRING, "ISO-8859-1", "ISO8859_1");
    }

    @Test
    public void testWriteClobUsingReader() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setClob(1, new StringReader(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(1);
                    assertEquals("Unexpected value for clob roundtrip", LATIN1_TEST_STRING, result);
                } else {
                    fail("Expected a row");
                }
            }
        }
    }

    @Test
    public void testWriteClobUsingReaderAsNClob() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setNClob(1, new StringReader(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(1);
                    assertEquals("Unexpected value for clob roundtrip", LATIN1_TEST_STRING, result);
                } else {
                    fail("Expected a row");
                }
            }
        }
    }

    @Test
    public void testWriteClobUsingNonFBClob() throws Exception {
        try (Connection con = getEncodedConnection("ISO8859_1")) {
            try (PreparedStatement insertStmt = con.prepareStatement(
                    "INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)")) {
                insertStmt.setClob(1, new StringClob(LATIN1_TEST_STRING));
                insertStmt.execute();
            }

            try (PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
                 ResultSet rs = selStatement.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(1);
                    assertEquals("Unexpected value for clob roundtrip", LATIN1_TEST_STRING, result);
                } else {
                    fail("Expected a row");
                }
            }
        }
    }

    @Test
    public void testReadClob_utf8_win1252() throws Exception {
        try (Connection con = getEncodedConnection("UTF8");
             PreparedStatement pstmt = con.prepareStatement("insert into test_clob (id, " + UTF8_BLOB + ") values (1, ?)")) {
            pstmt.setBytes(1, LATIN1_TEST_STRING.getBytes(StandardCharsets.UTF_8));
            pstmt.executeUpdate();
            pstmt.setString(1, "A");
            pstmt.executeUpdate();
        }

        try (Connection con = getEncodedConnection("WIN1252");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select " + UTF8_BLOB + " from test_clob where id = 1")) {
            assertTrue(rs.next());
            assertEquals(LATIN1_TEST_STRING, rs.getString(1));
        }
    }

    @Test
    public void testReadClob_utf8_none() throws Exception {
        try (Connection con = getEncodedConnection("UTF8");
             PreparedStatement pstmt = con.prepareStatement("insert into test_clob (id, " + UTF8_BLOB + ") values (1, ?)")) {
            pstmt.setBytes(1, UTF8_BYTES);
            pstmt.executeUpdate();
            pstmt.setString(1, "A");
            pstmt.executeUpdate();
        }

        try (Connection con = getEncodedConnection("NONE");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select " + UTF8_BLOB + " from test_clob where id = 1")) {
            assertTrue(rs.next());
            assertEquals(UTF8_TEST_STRING, rs.getString(1));
        }
    }

    private void runHoldableClobTest(String colName, String testString, String javaEncoding, String fbEncoding)
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

        char[] buffer = new char[testString.length()];
        try (Reader reader = clob.getCharacterStream()) {
            reader.read(buffer);
        }

        assertEquals(testString, new String(buffer));
    }

    private void runMultibyteWriteTest(String testString, String fbEncoding, String colName, String javaEncoding)
            throws Exception {
        try (Connection con = getEncodedConnection(fbEncoding)) {
            insertStringViaClobCharacterStream(con, testString, colName);

            String selectString = readStringViaGetBytes(con, colName, javaEncoding);

            assertEquals(testString, selectString);
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

    private String slurpString(InputStream inputStream) {
        return slurpString(new InputStreamReader(inputStream));
    }

    private String slurpString(Reader reader) {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int n;
        try {
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
            reader.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return sb.toString();
    }

    private void addTestValues(Connection con, int id, String value, String colName) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO test_clob (id, " + colName + ") VALUES (?, ?)")) {
            stmt.setInt(1, id);
            stmt.setString(2, value);
            stmt.execute();
        }
    }
}
