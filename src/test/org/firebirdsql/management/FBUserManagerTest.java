/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.common.extension.RequireFeatureExtension;
import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.firebirdsql.common.DdlHelper.executeDDL;
import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the UserManager class which uses the Services API to display, add, delete, and modify users.
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 */
class FBUserManagerTest {

    @RegisterExtension
    @Order(1)
    static final GdsTypeExtension gdsType = GdsTypeExtension.excludes(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @RegisterExtension
    @Order(2)
    static final RequireFeatureExtension requireFeature = RequireFeatureExtension
            .withFeatureCheck(FirebirdSupportInfo::supportsSqlUserManagement, "Test requires DROP USER support")
            .build();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private static final String USER_NAME = "TESTUSER123";

    @BeforeEach
    @AfterEach
    void ensureTestUserDoesNotExist() throws SQLException {
        try (Connection connection = getConnectionViaDriverManager()) {
            executeDDL(connection, "DROP USER " + USER_NAME, ISCConstants.isc_gsec_err_rec_not_found);
        }
    }

    @Test
    void testUsers() throws Exception {
        // Initialize the UserManager.
        UserManager userManager = new FBUserManager(getGdsType());
        userManager.setServerName(DB_SERVER_URL);
        userManager.setPortNumber(DB_SERVER_PORT);
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
        final boolean supportsUserAndGroupId = getDefaultSupportInfo().supportsUserAndGroupIdInUser();
        user1.setUserId(supportsUserAndGroupId ? 222 : 0);
        user1.setGroupId(supportsUserAndGroupId ? 222 : 0);

        userManager.add(user1);
        
        // Check to make sure the user was added.
        User user2 = userManager.getUsers().get(user1.getUserName());

        assertNotNull(user2, "User 2 should not be null");
        assertEquals(user1, user2, "user1 should equal user2");

        user1.setPassword("123test");
        user1.setFirstName("Name First");
        user1.setMiddleName("Name Middle");
        user1.setLastName("Name Last");
        user1.setUserId(supportsUserAndGroupId ? 111 : 0);
        user1.setGroupId(supportsUserAndGroupId ? 111 : 0);

        userManager.update(user1);

        user2 = userManager.getUsers().get(user1.getUserName());

        assertEquals(user1, user2, "user1 should equal user2");

        userManager.delete(user1);

        user2 = userManager.getUsers().get(user1.getUserName());

        assertNull(user2, "User 2 should be null");
    }
}
