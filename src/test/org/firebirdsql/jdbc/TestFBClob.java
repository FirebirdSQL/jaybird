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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.firebirdsql.common.FBTestBase;

public class TestFBClob extends FBTestBase {

	private static final String PLAIN_BLOB = "plain_blob";

	private static final String TEXT_BLOB = "text_blob";

	private Connection connection;

	public static final String CREATE_TABLE = "" + "CREATE TABLE test_clob("
			+ "  id INTEGER, " + TEXT_BLOB + " BLOB SUB_TYPE TEXT, "
			+ PLAIN_BLOB + " BLOB )";

	public static final String DROP_TABLE = "DROP TABLE test_clob";

	public static final byte[] LATIN1_BYTES = new byte[] { (byte) 0xC8,
			(byte) 0xC9, (byte) 0xCA, (byte) 0xCB };

	public static final byte[] CP1251_BYTES = new byte[] { (byte) 0xf2,
			(byte) 0xe5, (byte) 0xf1, (byte) 0xf2, (byte) 0xee, (byte) 0xe2,
			(byte) 0xe0, (byte) 0x20, (byte) 0xf1, (byte) 0xf2, (byte) 0xf0,
			(byte) 0xb3, (byte) 0xf7, (byte) 0xea, (byte) 0xe0 };

	public static final String LATIN1_TEST_STRING;
	public static final String CP1251_TEST_STRING;

	static {
		try {
			LATIN1_TEST_STRING = new String(LATIN1_BYTES, "ISO-8859-1");
			CP1251_TEST_STRING = new String(CP1251_BYTES, "Cp1251");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public TestFBClob(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		Class.forName(FBDriver.class.getName());
		connection = getConnectionViaDriverManager();

		Statement stmt = connection.createStatement();

		try {
			stmt.execute(DROP_TABLE);
		} catch (SQLException sqle) {
			// Ignore
		}

		stmt.execute(CREATE_TABLE);

		stmt.close();
	}

	protected void tearDown() throws Exception {

		Statement stmt = connection.createStatement();

		try {
			stmt.execute(DROP_TABLE);
		} catch (SQLException sqle) {
			// Ignore
		}

		connection.close();
		super.tearDown();
	}

	public void testSimpleGetAsciiStream() throws SQLException {
		final String TEST_VALUE = "TEST_VALUE";
		addTestValues(1, TEST_VALUE, PLAIN_BLOB);
		String clobValue = null;
		Statement stmt = connection.createStatement();
		try {
			ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB
					+ " FROM test_clob");
			try {
				resultSet.next();
				Clob clob = resultSet.getClob(1);
				InputStream inputStream = clob.getAsciiStream();
				clobValue = slurpString(inputStream);
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
		assertEquals(TEST_VALUE, clobValue);
	}

	public void testNullClob() throws SQLException {
		addTestValues(1, null, PLAIN_BLOB);
		Statement stmt = connection.createStatement();
		try {
			ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB
					+ " FROM test_clob");
			try {
				resultSet.next();
				Clob clob = resultSet.getClob(1);
				assertNull(clob);
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
	}

	public void testCachedNullClob() throws SQLException {
		addTestValues(1, null, PLAIN_BLOB);
		PreparedStatement stmt = connection.prepareStatement(
				"SELECT " + PLAIN_BLOB + " FROM test_clob", 
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		try {
			ResultSet resultSet = stmt.executeQuery();
			try {
				resultSet.next();
				Clob clob = resultSet.getClob(1);
				assertNull(clob);
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
	}

	public void testSimpleGetCharacterStream() throws Exception {

		final String TEST_VALUE = "TEST_STRING";
		addTestValues(1, TEST_VALUE, PLAIN_BLOB);
		Statement stmt = connection.createStatement();
		String clobValue = null;
		try {
			ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB
					+ " FROM test_clob");
			try {
				resultSet.next();
				Clob clob = resultSet.getClob(1);
				clobValue = slurpString(clob.getCharacterStream());
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
		assertEquals(TEST_VALUE, clobValue);
	}

	public void testGetSubString() throws SQLException {
		final String TEST_VALUE = "TEST_STRING";
		addTestValues(1, TEST_VALUE, PLAIN_BLOB);
		Statement stmt = connection.createStatement();
		try {
			ResultSet resultSet = stmt.executeQuery("SELECT " + PLAIN_BLOB
					+ " FROM test_clob");
			try {
				resultSet.next();
				Clob clob = resultSet.getClob(1);

				for (int start = 1; start <= TEST_VALUE.length(); start++) {
					for (int length = 0; length <= TEST_VALUE.length()
							- (start - 1); length++) {
						String clobValue = clob.getSubString(start, length);
						assertEquals(TEST_VALUE.substring(start - 1, start - 1
								+ length), clobValue);
					}
				}
				assertEquals(TEST_VALUE, clob.getSubString(1, TEST_VALUE
						.length() * 2));
				assertEquals("", clob.getSubString(1, 0));
			} finally {
				resultSet.close();
			}
		} finally {
			stmt.close();
		}
	}

	public void testReadMultiByteCharacterClobUtfLatin1() throws Exception {
		runMultibyteReadTest(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB,
				"UTF-8");
	}

	public void testReadMultiByteCharacterClobUtfCp1251() throws Exception {
		runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
	}

	public void testReadMultiByteCharacterClobNoSubtypeUtfLatin1()
			throws Exception {
		runMultibyteReadTest(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB,
				"UTF-8");
	}

	public void testReadMultiByteCharacterClobNoSubtypeUtfCp1251()
			throws Exception {
		runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB,
				"Cp1251");
	}

	public void testReadMultiByteCharacterClobLatin1() throws Exception {
		runMultibyteReadTest(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB,
				"ISO-8859-1");
	}

	public void testReadMultiByteCharacterClobCp1251() throws Exception {
		runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB, "Cp1251");
	}

	public void testReadMultiByteCharacterClobNoSubtypeLatin1()
			throws Exception {
		runMultibyteReadTest(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB,
				"ISO-8859-1");
	}

	public void testReadMultiByteCharacterClobNoSubtypeCp1251()
			throws Exception {
		runMultibyteReadTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB,
				"Cp1251");
	}

	private void runMultibyteReadTest(String testString, String fbEncoding,
			String colName, String javaEncoding) throws Exception,
			SQLException, IOException {

		insertStringBytesViaBlobWithEncoding(testString, colName, javaEncoding,
				fbEncoding);
		char[] buffer = readClobViaCharacterStream(fbEncoding, colName,
				testString.length());
		String outputString = new String(buffer);

		assertEquals(testString, outputString);
	}

	public void testWriteMultiByteCharacterClobUtfLatin1() throws Exception {
		runMultibyteWriteTest(LATIN1_TEST_STRING, "UNICODE_FSS", TEXT_BLOB,
				"UTF-8");
	}

	public void testWriteMultiByteCharacterClobUtfCp1251() throws Exception {
		runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB,
				"Cp1251");
	}

	public void testWriteMultiByteCharacterClobPlainBlobUtfLatin1()
			throws Exception {
		runMultibyteWriteTest(LATIN1_TEST_STRING, "UNICODE_FSS", PLAIN_BLOB,
				"UTF-8");
	}

	public void testWriteMultiByteCharacterClobPlainBlobUtfCp1251()
			throws Exception {
		runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB,
				"Cp1251");
	}

	public void testWriteMultiByteCharacterClobLatin1() throws Exception {
		runMultibyteWriteTest(LATIN1_TEST_STRING, "ISO8859_1", TEXT_BLOB,
				"ISO-8859-1");
	}

	public void testWriteMultiByteCharacterClobCp1251() throws Exception {
		runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", TEXT_BLOB,
				"Cp1251");
	}

	public void testWriteMultiByteCharacterClobPlainBlobLatin1()
			throws Exception {
		runMultibyteWriteTest(LATIN1_TEST_STRING, "ISO8859_1", PLAIN_BLOB,
				"ISO-8859-1");
	}

	public void testWriteMultiByteCharacterClobPlainBlobCp1251()
			throws Exception {
		runMultibyteWriteTest(CP1251_TEST_STRING, "WIN1251", PLAIN_BLOB,
				"Cp1251");
	}

	public void testHoldableClobFromPlainBlob() throws Exception {
		runHoldableClobTest(PLAIN_BLOB, LATIN1_TEST_STRING, "UTF-8",
				"UNICODE_FSS");
	}

	public void testHoldableClobFromBlobSubtypeText() throws Exception {
		runHoldableClobTest(TEXT_BLOB, LATIN1_TEST_STRING, "ISO-8859-1",
				"ISO8859_1");
	}

	private void runHoldableClobTest(String colName, String testString,
			String javaEncoding, String fbEncoding) throws Exception,
			SQLException, IOException {
		insertStringBytesViaBlobWithEncoding(testString, colName, javaEncoding,
				fbEncoding);

		Connection unicodeConnection = getEncodedConnection(fbEncoding);
		PreparedStatement stmt = unicodeConnection.prepareStatement("SELECT "
				+ colName + " FROM test_clob", ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
		ResultSet resultSet = stmt.executeQuery();
		resultSet.next();
		Clob clob = resultSet.getClob(1);
		resultSet.close();
		stmt.close();
		unicodeConnection.close();

		Reader reader = clob.getCharacterStream();
		char[] buffer = new char[testString.length()];
		reader.read(buffer);
		reader.close();

		assertEquals(testString, new String(buffer));
	}

	private void runMultibyteWriteTest(String testString, String fbEncoding,
			String colName, String javaEncoding) throws SQLException,
			IOException, UnsupportedEncodingException {

		insertStringViaClobCharacterStream(testString, fbEncoding, colName);

		String selectString = readStringViaGetBytes(colName, javaEncoding);

		assertEquals(testString, selectString);
	}

	private String readStringViaGetBytes(String colName, String javaEncoding)
			throws SQLException, UnsupportedEncodingException {
		PreparedStatement selectStmt = connection.prepareStatement("SELECT "
				+ colName + " FROM test_clob");
		ResultSet resultSet = selectStmt.executeQuery();
		resultSet.next();
		byte[] byteBuffer = resultSet.getBytes(1);
		String selectString = new String(byteBuffer, javaEncoding);
		resultSet.close();
		selectStmt.close();
		return selectString;
	}

	private void insertStringViaClobCharacterStream(String testString,
			String fbEncoding, String colName) throws SQLException, IOException {
		FBConnection unicodeConnection = getEncodedConnection(fbEncoding);
		PreparedStatement insertStmt = unicodeConnection
				.prepareStatement("INSERT INTO test_clob (" + colName
						+ ") VALUES (?)");
		Clob insertClob = unicodeConnection.createClob();
		Writer writer = insertClob.setCharacterStream(1);
		writer.write(testString.toCharArray());
		writer.close();
		insertStmt.setClob(1, insertClob);
		insertStmt.execute();
		unicodeConnection.close();
	}

	private char[] readClobViaCharacterStream(String fbEncoding,
			String colName, int expectedLength) throws SQLException,
			IOException {
		Connection unicodeConnection = getEncodedConnection(fbEncoding);
		PreparedStatement selectStmt = unicodeConnection
				.prepareStatement("SELECT " + colName + " FROM test_clob");
		ResultSet resultSet = selectStmt.executeQuery();
		resultSet.next();

		Clob clob = resultSet.getClob(1);
		Reader reader = clob.getCharacterStream();
		char[] buffer = new char[expectedLength];
		reader.read(buffer);
		reader.close();
		selectStmt.close();
		unicodeConnection.close();
		return buffer;
	}

	private void insertStringBytesViaBlobWithEncoding(String insertString,
			String colName, String javaEncoding, String connEncoding)
			throws Exception {
		Connection localConnection = getEncodedConnection(connEncoding);

		PreparedStatement insertStmt = localConnection
				.prepareStatement("INSERT INTO test_clob (" + colName
						+ ") VALUES (?)");

		byte[] bytes = insertString.getBytes(javaEncoding);
		insertStmt.setBytes(1, bytes);

		insertStmt.execute();
		insertStmt.close();
		localConnection.close();
	}

	private FBConnection getEncodedConnection(String encoding)
			throws SQLException {
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
		StringBuffer stringBuffer = new StringBuffer();
		char[] buffer = new char[1028];
		int n = 0;
		try {
			while ((n = reader.read(buffer)) != -1) {
				stringBuffer.append(new String(buffer, 0, n));
			}
			reader.close();
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		return stringBuffer.toString();
	}

	private void addTestValues(int id, String value, String colName)
			throws SQLException {
		PreparedStatement stmt = connection
				.prepareStatement("INSERT INTO test_clob (id, " + colName
						+ ") VALUES (?, ?)");
		try {
			stmt.setInt(1, id);
			stmt.setString(2, value);
			stmt.execute();
		} finally {
			stmt.close();
		}
	}

}
