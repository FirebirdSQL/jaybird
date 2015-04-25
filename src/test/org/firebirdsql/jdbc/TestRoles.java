/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.management.FBUser;
import org.firebirdsql.management.FBUserManager;
import org.firebirdsql.management.User;
import org.firebirdsql.management.UserManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestRoles extends FBTestBase {

    private static final String TEST_USER = "jaybirdtest1";
    private static final String TEST_PASSWORD = "password";
    private static final String CREATE_TABLE = "CREATE TABLE test ( col1 INTEGER )";
    private static final String SELECT_TABLE = "SELECT * FROM test";
    private static final String CASE_SENSITIVE_ROLE = "\"casesensitive\"";
    private static final String CREATE_CASE_SENSITIVE_ROLE = "CREATE ROLE " + CASE_SENSITIVE_ROLE;
    private static final String GRANT_TO_CASE_SENSITIVE = "GRANT ALL ON test TO ROLE " + CASE_SENSITIVE_ROLE;
    private static final String GRANT_CASE_SENSITIVE_ROLE_TO_TEST_USER = "GRANT " + CASE_SENSITIVE_ROLE + " TO " + TEST_USER;

    private UserManager userManager;
    private User user;

    public TestRoles(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
        userManager = new FBUserManager(getGdsType());
        userManager.setHost(DB_SERVER_URL);
        userManager.setPort(DB_SERVER_PORT);
        userManager.setUser(DB_USER);
        userManager.setPassword(DB_PASSWORD);
        user = new FBUser();
        user.setUserName(TEST_USER);
        user.setPassword(TEST_PASSWORD);
        userManager.add(user);

        Connection con = getConnectionViaDriverManager();
        try {
            executeCreateTable(con, CREATE_TABLE);
            executeDDL(con, CREATE_CASE_SENSITIVE_ROLE, null);
            executeDDL(con, GRANT_TO_CASE_SENSITIVE, null);
            executeDDL(con, GRANT_CASE_SENSITIVE_ROLE_TO_TEST_USER, null);
        } finally {
            closeQuietly(con);
        }
    }

    public void tearDown() throws Exception {
        try {
            if (user != null) userManager.delete(user);
        } finally {
            super.tearDown();
        }
    }

    /**
     * Tests if a case-sensitive (quoted) role works correctly if the connection dialect is not explicitly specified.
     * <p>
     * See also <a href="http://tracker.firebirdsql.org/browse/JDBC-327">JDBC-327</a>
     * </p>
     */
    public void testCaseSensitiveRoleImplicitDialect() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("user", TEST_USER);
        props.setProperty("password", TEST_PASSWORD);
        props.setProperty("sqlRole", CASE_SENSITIVE_ROLE);

        Connection con = DriverManager.getConnection(getUrl(), props);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(SELECT_TABLE);
        } finally {
            closeQuietly(stmt);
            closeQuietly(con);
        }
    }
}
