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
package org.firebirdsql.management;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;

/**
 * Tests the UserManager class which uses the Services API to display, add,
 * delete, and modify users.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class TestUserManager extends FBTestBase {

    /**
     * Create and instance of this class.
     */
    public TestUserManager(String name) {
        super(name);
    }

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
        user1.setUserName("TESTUSER123");
        user1.setPassword("tes123");
        user1.setFirstName("First Name");
        user1.setMiddleName("Middle Name");
        user1.setLastName("Last Name");
        // Setting userid and groupid to 0 for Firebird 3 as it isn't supported for the SRP usermanager
        user1.setUserId(isFirebird3 ? 0 : 222);
        user1.setGroupId(isFirebird3 ? 0 : 222);

        try {
            userManager.add(user1);
        } catch(SQLException ex) {
            // 
        }
        
        // Check to make sure the user was added.
        User user2 = (User) userManager.getUsers().get(user1.getUserName());

        assertNotNull("User 2 should not be null.", user2);
        assertEquals("user1 should equal user2", user1, user2);

        user1.setPassword("123test");
        user1.setFirstName("Name First");
        user1.setMiddleName("Name Middle");
        user1.setLastName("Name Last");
        user1.setUserId(isFirebird3 ? 0 : 111);
        user1.setGroupId(isFirebird3 ? 0 : 111);

        userManager.update(user1);

        user2 = (User) userManager.getUsers().get(user1.getUserName());

        assertEquals("user1 should equal user2", user1, user2);

        userManager.delete(user1);

        user2 = (User) userManager.getUsers().get(user1.getUserName());

        assertNull("User 2 should be null", user2);
    }

    public void _testConnection() throws Exception {

        // TODO: Test use of user with database connection and sql.

    }
}
