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
import java.sql.DriverManager;
import java.sql.SQLException;

import org.firebirdsql.common.FBTestBase;

/**
 * Tests the UserManager class which uses the Services API to display, add,
 * delete, and modify users.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine </a>
 */
public class TestUserManager extends FBTestBase {

    private FBManager fbManager;

    /**
     * Create and instance of this class.
     */
    public TestUserManager(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        fbManager = createFBManager();
        fbManager.setServer("localhost");
        // fbManager.setPort(3060);
        fbManager.start();

        fbManager.setForceCreate(true);
        fbManager.createDatabase(getDatabasePath(), DB_USER, DB_PASSWORD);
    }

    private String getDatabasePath() {
        return DB_PATH + "/" + DB_NAME + ".fdb";
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:firebirdsql:localhost:"
                + getDatabasePath(), DB_USER, DB_PASSWORD);
    }

    protected void tearDown() throws Exception {
        fbManager.stop();
        super.tearDown();
    }

    public void testUsers() throws Exception {
        // TODO Implement a test that adds a 2 users, then display's all users.
        // Delete a user, and display's all users.
        // Attempt to display the deleted user.
        // Display the remaining user.
        // Modify the remaining user.
        // Display all users.
        // Delete remaining user.
    }
}
