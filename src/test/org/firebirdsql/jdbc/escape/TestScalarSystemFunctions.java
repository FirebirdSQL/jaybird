/*
 * Firebird Open Source JavaEE connector - JDBC driver
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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.UsesDatabase;
import org.junit.ClassRule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for support of the scalar system function escapes as defined in
 * appendix D.4 of the JDBC 4.1 specification.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestScalarSystemFunctions {

    @ClassRule
    public static final UsesDatabase usesDatabase = UsesDatabase.usesDatabase();

    @Test
    public void testDatabase() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {fn DATABASE()} FROM RDB$DATABASE");
            assertTrue("Expected at least one row", rs.next());
            assertThat("Unexpected result for function escape DATABASE()",
                    rs.getString(1), endsWith(FBTestProperties.DB_NAME.toUpperCase()));
        }
    }

    @Test
    public void testIfNull() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {fn IFNULL(NULL, 'abcd')} FROM RDB$DATABASE");
            assertTrue("Expected at least one row", rs.next());
            assertEquals("Unexpected result for function escape IFNULL(NULL, 'abcd')", "abcd", rs.getString(1));
        }
    }

    @Test
    public void testUser() throws Exception {
        try (Connection con = FBTestProperties.getConnectionViaDriverManager()) {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT {fn USER()} FROM RDB$DATABASE");
            assertTrue("Expected at least one row", rs.next());
            assertEquals("Unexpected result for function escape USER()",
                    FBTestProperties.DB_USER.toUpperCase(), rs.getString(1));
        }
    }

}
