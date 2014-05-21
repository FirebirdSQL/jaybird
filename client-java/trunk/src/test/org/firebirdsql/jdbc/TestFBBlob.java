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
import org.firebirdsql.gds.ISCConstants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlob}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlob extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private static final String CREATE_BLOB_TABLE =
            "CREATE TABLE test_blob(" +
            "  id INTEGER, " +
            "  bin_data BLOB " +
            ")";

    private static final String INSERT_BLOB = "INSERT INTO test_blob(id, bin_data) VALUES (?, ?)";

    private static final String SELECT_BLOB = "SELECT bin_data FROM test_blob WHERE id = ?";

    @Before
    public void createTestTable() throws SQLException {
        Connection conn = getConnectionViaDriverManager();
        try {
            executeCreateTable(conn, CREATE_BLOB_TABLE);
        } finally {
            conn.close();
        }
    }

    /**
     * Tests whether a blob created as segmented is correctly reported by {@link FBBlob#isSegmented()}.
     */
    @Test
    public void testIsSegmented_segmentedBlob() throws SQLException {
        Connection conn = getConnection(false);
        try {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });

            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);
                assertTrue("Expected a segmented blob", blob.isSegmented());
                blob.free();
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Tests whether a blob created as stream is correctly reported by {@link FBBlob#isSegmented()}.
     */
    @Test
    public void testIsSegmented_streamBlob() throws SQLException {
        Connection conn = getConnection(true);
        try {
            populateBlob(conn, new byte[]{ 1, 2, 3, 4, 5 });

            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);
                assertFalse("Expected a stream blob", blob.isSegmented());
                blob.free();
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Tests that closing a blob after opening an InputStream doesn't throw unexpected exceptions
     * <p>
     * Previously {@link FBBlob} threw a ConcurrentModificationException in this case.
     * </p>
     */
    @Test
    public void testClose_afterOpeningMultipleIS() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            populateBlob(conn, new byte[]{ 1, 2, 3, 4, 5 });
            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);

                blob.getBinaryStream();
                blob.getBinaryStream();

                blob.free();
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testPosition_byteArr_long_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.position(new byte[] { 1, 2, 3 }, 1);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testPosition_Blob_long_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();
            Blob otherBlob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.position(otherBlob, 1);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testTruncate_long_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.truncate(1);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBytes_long_byteArr_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 });
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBytes_long_byteArr_int_int_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.setBytes(1, new byte[] { 1, 2, 3, 4, 5 }, 1, 2);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBinaryStream_long_long_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(SQLFeatureNotSupportedException.class);

            blob.getBinaryStream(1, 1);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBinaryStream_calledTwice_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        // TODO Required because of JDBC-348
        conn.setAutoCommit(false);
        try {
            Blob blob = conn.createBlob();

            blob.setBinaryStream(1);

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("already open"))
            ));

            blob.setBinaryStream(1);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBinaryStream_positionZero_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        // TODO Required because of JDBC-348
        conn.setAutoCommit(false);
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_ARG_VALUE)),
                    message(containsString("before the beginning"))
            ));

            blob.setBinaryStream(0);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBinaryStream_positionBeyondStart_newBlob_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        // TODO Required because of JDBC-348
        conn.setAutoCommit(false);
        try {
            Blob blob = conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_ARG_VALUE)),
                    message(containsString("must start at position 1"))
            ));

            blob.setBinaryStream(2);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testSetBinaryStream_positionBeyondStart_existingBlob_throwsSQLFeatureNotSupported() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            populateBlob(conn, new byte[]{ 1, 2, 3, 4, 5 });
            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);

                expectedException.expect(SQLFeatureNotSupportedException.class);

                blob.setBinaryStream(2);
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBlobId_newBlob_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("No Blob ID is available in new Blob object."))
            ));

            blob.getBlobId();
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBytes_withOffset_streamBlob() throws Exception {
        Connection conn = getConnection(true);
        try {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);

                byte[] bytes = blob.getBytes(2, 4);

                assertArrayEquals("Expected array equal to original from index 1", new byte[] { 2, 3, 4, 5 }, bytes);
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBytes_withOffset_segmentedBlob_throwsSQLException() throws Exception {
        Connection conn = getConnection(false);
        try {
            populateBlob(conn, new byte[] { 1, 2, 3, 4, 5 });
            PreparedStatement select = conn.prepareStatement(SELECT_BLOB);
            select.setInt(1, 1);
            ResultSet rs = select.executeQuery();
            try {
                assertTrue("Expected a row in result set", rs.next());
                FBBlob blob = (FBBlob) rs.getBlob(1);

                expectedException.expect(allOf(
                        isA(SQLException.class),
                        message(containsString(getFbMessage(ISCConstants.isc_bad_segstr_type)))
                ));

                blob.getBytes(2, 4);
            } finally {
                closeQuietly(rs);
                closeQuietly(select);
            }
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBytes_positionZero_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(containsString("should be >= 1"))
            ));

            blob.getBytes(0, 4);
        } finally {
            closeQuietly(conn);
        }
    }

    @Test
    public void testGetBytes_positionLargerThanMaxIntValue_throwsSQLException() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            FBBlob blob = (FBBlob) conn.createBlob();

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(equalTo("Blob position is limited to 2^31 - 1 due to isc_seek_blob limitations.")),
                    sqlState(equalTo(FBSQLException.SQL_STATE_INVALID_ARG_VALUE))
            ));

            blob.getBytes(Integer.MAX_VALUE + 1L, 4);
        } finally {
            closeQuietly(conn);
        }
    }

    private void populateBlob(Connection conn, byte[] bytes) throws SQLException {
        PreparedStatement insert = conn.prepareStatement(INSERT_BLOB);
        try {
            insert.setInt(1, 1);

            insert.setBytes(2, bytes);
            insert.executeUpdate();
        } finally {
            closeQuietly(insert);
        }
    }

    private Connection getConnection(boolean useStreamBlobs) throws SQLException {
        final Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("useStreamBlobs", useStreamBlobs ? "true" : "false");
        return DriverManager.getConnection(getUrl(), connectionProperties);
    }
}
