/*
 * $Id$
 * 
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Tests FBResultSets JDBC3.0 specific functionality.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class TestFBResultSet3_0 extends TestFBResultSet {

    public TestFBResultSet3_0(String name) {
        super(name);
    }

    public void testUpdatableHoldableResultSet() throws Exception {

        connection.setAutoCommit(true);

        int recordCount = 10;
        PreparedStatement ps = connection.prepareStatement("INSERT INTO test_table("
                + "id, long_str) VALUES (?, ?)");

        try {
            for (int i = 0; i < recordCount; i++) {
                ps.setInt(1, i);
                ps.setString(2, "oldString" + i);
                ps.executeUpdate();
            }
        } finally {
            ps.close();
        }

        connection.setAutoCommit(false);

        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

        try {
            ResultSet rs = stmt.executeQuery("SELECT id, long_str FROM test_table");

            while (rs.next()) {
                rs.updateString(2, rs.getString(2) + "a");
                rs.updateRow();
                connection.commit();
            }

            int counter = 0;

            rs = stmt.executeQuery("SELECT id, long_str FROM test_table");
            while (rs.next()) {
                assertEquals("oldString" + counter + "a", rs.getString(2));
                counter++;
            }

        } finally {
            stmt.close();
        }
    }

}
