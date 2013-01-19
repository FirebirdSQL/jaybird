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
import java.io.StringReader;
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

public class TestFBClob4_0 extends FBTestBase {

    private static final String PLAIN_BLOB = "plain_blob";

    private static final String TEXT_BLOB = "text_blob";

    private static final String CREATE_TABLE = 
            "CREATE TABLE test_clob(" + 
            "  id INTEGER, " + 
               TEXT_BLOB + " BLOB SUB_TYPE TEXT, " + 
               PLAIN_BLOB + " BLOB )";

    private static final String DROP_TABLE = "DROP TABLE test_clob";

    private static final byte[] LATIN1_BYTES = new byte[] { (byte) 0xC8,
        (byte) 0xC9, (byte) 0xCA, (byte) 0xCB };

    private static final String LATIN1_TEST_STRING;

    static {
        try {
            LATIN1_TEST_STRING = new String(LATIN1_BYTES, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public TestFBClob4_0(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Connection con = getConnectionViaDriverManager();
        try {
            executeDropTable(con, DROP_TABLE);
            executeCreateTable(con, CREATE_TABLE);
        } finally {
            closeQuietly(con);
        }
    }

    public void testWriteClobUsingReader() throws Exception {
        Connection con = getEncodedConnection("ISO8859_1");
        try {
            PreparedStatement insertStmt = con.prepareStatement("INSERT INTO test_clob (" + TEXT_BLOB + ") VALUES (?)");

            insertStmt.setClob(1, new StringReader(LATIN1_TEST_STRING));
            insertStmt.execute();
            insertStmt.close();

            PreparedStatement selStatement = con.prepareStatement("SELECT " + TEXT_BLOB + " FROM test_clob");
            ResultSet rs = selStatement.executeQuery();

            if (rs.next()) {
                String result = rs.getString(1);
                assertEquals("Unexpected value for clob roundtrip", LATIN1_TEST_STRING, result);
            } else {
                fail("Expected a row");
            }
        } finally {
            closeQuietly(con);
        }
    }

    private FBConnection getEncodedConnection(String encoding) throws SQLException {
        Properties props = new Properties();
        props.putAll(getDefaultPropertiesForConnection());
        props.put("lc_ctype", encoding);
        Connection connection = DriverManager.getConnection(getUrl(), props);
        return (FBConnection) connection;
    }
}
