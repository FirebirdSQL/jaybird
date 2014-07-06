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
package org.firebirdsql.management;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.gds.ISCConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

/**
 * Tests the UserManager class which uses the Services API to display, add,
 * delete, and modify users.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 */
public class TestUserManager extends FBJUnit4TestBase {

    public static final String USER_NAME = "TESTUSER123";

    @Before
    @After
    public void ensureTestUserDoesNotExist() throws SQLException {
        Connection connection = getConnectionViaDriverManager();
        try {
            assumeTrue("Test requires DROP USER support", supportInfoFor(connection).supportsSqlUserManagement());
            executeDDL(connection, "DROP USER " + USER_NAME, ISCConstants.isc_gsec_err_rec_not_found);
        } finally {
            closeQuietly(connection);
        }
    }

    @Test
    public void testUsers() throws Exception {
        boolean isFirebird3 = false;
        Connection connection = null;
        try {
            connection = getConnectionViaDriverManager();
            DatabaseMetaData dbmd = connection.getMetaData();
            isFirebird3 = dbmd.getDatabaseMajorVersion() == 3;
        } finally {
            closeQuietly(connection);
        }

        // Initialize the UserManager.
        UserManager userManager = new FBUserManager(getGdsType());
        userManager.setHost(DB_SERVER_URL);
        userManager.setPort(DB_SERVER_PORT);
        userManager.setUser(DB_USER);
        userManager.setPassword(DB_PASSWORD);

        // Add a user.
        User user1 = new FBUser();
        user1.setUserName(USER_NAME);
        user1.setPassword("tes123");
        user1.setFirstName("First");
        user1.setMiddleName("Middle");
        user1.setLastName("Last");
        // Setting userid and groupid to 0 for Firebird 3 as it isn't supported for the SRP usermanager
        user1.setUserId(isFirebird3 ? 0 : 222);
        user1.setGroupId(isFirebird3 ? 0 : 222);

        try {
            userManager.add(user1);
        } catch(SQLException ex) {
            // ignore
        }
        
        // Check to make sure the user was added.
        User user2 = userManager.getUsers().get(user1.getUserName());

        assertNotNull("User 2 should not be null.", user2);
        assertEquals("user1 should equal user2", user1, user2);

        user1.setPassword("123test");
        user1.setFirstName("Name First");
        user1.setMiddleName("Name Middle");
        user1.setLastName("Name Last");
        user1.setUserId(isFirebird3 ? 0 : 111);
        user1.setGroupId(isFirebird3 ? 0 : 111);

        userManager.update(user1);

        user2 = userManager.getUsers().get(user1.getUserName());

        assertEquals("user1 should equal user2", user1, user2);

        userManager.delete(user1);

        user2 = userManager.getUsers().get(user1.getUserName());

        assertNull("User 2 should be null", user2);
    }
}
