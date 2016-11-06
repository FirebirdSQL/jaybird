/*
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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jdbc.FBDriver;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * @author Ryan Baldwin
 */
public class TestSpecialEmbeddedServerUrls {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String mRelativeDatabasePath;
    private String mAbsoluteDatabasePath;
    private FBManager fbManager;
    private GDSType gdsType;

    @Before
    public void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        gdsType = GDSType.getType(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        fbManager = new FBManager(gdsType);

        fbManager.setServer("localhost");
        fbManager.setPort(5066);
        fbManager.start();

        File dbFolder = temporaryFolder.newFolder("db");

        mRelativeDatabasePath = "testES01874.fdb";
        mAbsoluteDatabasePath = new File(dbFolder, mRelativeDatabasePath).getAbsolutePath();

        fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
    }

    @After
    public void tearDown() throws Exception {
        fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        fbManager.stop();
        fbManager = null;
    }

    @Test
    public void testFBManagerWithoutSettingServerAndPort() throws Exception {
        FBManager testFBManager = new FBManager(gdsType);
        testFBManager.start();

        testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        testFBManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");

        testFBManager.stop();
    }

    @Test
    public void testFBManagerWithRelativeDatabaseFile() throws Exception {
        FBManager testFBManager = new FBManager(gdsType);
        testFBManager.setDropOnStop(true);
        try {
            testFBManager.start();

            testFBManager.createDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");
        } finally {
            testFBManager.stop();
        }
    }

    @Test
    public void testDriverManagerGetConnectionWithoutServerAndPortInUrl() throws Exception {
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:" + mAbsoluteDatabasePath +"?encoding=NONE",
                "SYSDBA", "masterkey");
        connection.close();
    }

    @Test
    public void testDriverManagerGetConnectionWithoutServerAndPortInUrlWithRelativeDatabasePath() throws Exception {
        FBManager testFBManager = new FBManager(gdsType);
        testFBManager.setDropOnStop(true);
        try {
            testFBManager.start();

            testFBManager.createDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");

            Connection connection =
                    DriverManager.getConnection("jdbc:firebirdsql:embedded:" + mRelativeDatabasePath + "?encoding=NONE",
                            "SYSDBA", "masterkey");
            connection.close();
        } finally {
            testFBManager.stop();
        }
    }
}
