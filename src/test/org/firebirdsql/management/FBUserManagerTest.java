/*
 SPDX-FileCopyrightText: Copyright 2004-2005 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2005-2006 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
 * @author Steven Jardine
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
        UserManager userManager = configureDefaultServiceProperties(new FBUserManager(getGdsType()));

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
