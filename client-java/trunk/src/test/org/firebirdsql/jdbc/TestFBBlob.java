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
import java.util.Properties;

import static org.firebirdsql.common.DdlHelper.executeCreateTable;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultPropertiesForConnection;
import static org.firebirdsql.common.FBTestProperties.getUrl;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlob}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlob extends FBJUnit4TestBase {

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
        final Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("useStreamBlobs", "false");
        Connection conn = DriverManager.getConnection(getUrl(), connectionProperties);
        try {
            PreparedStatement insert = conn.prepareStatement(INSERT_BLOB);
            try {
                insert.setInt(1, 1);
                insert.setBytes(2, new byte[]{ 1, 2, 3, 4, 5 });
                insert.executeUpdate();
            } finally {
                closeQuietly(insert);
            }

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
        final Properties connectionProperties = getDefaultPropertiesForConnection();
        connectionProperties.setProperty("useStreamBlobs", "true");
        Connection conn = DriverManager.getConnection(getUrl(), connectionProperties);
        try {
            PreparedStatement insert = conn.prepareStatement(INSERT_BLOB);
            try {
                insert.setInt(1, 1);
                insert.setBytes(2, new byte[]{ 1, 2, 3, 4, 5 });
                insert.executeUpdate();
            } finally {
                closeQuietly(insert);
            }

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
}
