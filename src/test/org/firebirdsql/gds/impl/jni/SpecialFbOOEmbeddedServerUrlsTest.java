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

import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;

public class SpecialFbOOEmbeddedServerUrlsTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @TempDir
    private Path tempDir;

    private String mRelativeDatabasePath;
    private String mAbsoluteDatabasePath;
    private FBManager fbManager;
    private GDSType gdsType;

    @BeforeEach
    void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        gdsType = GDSType.getType(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        fbManager = new FBManager(gdsType);

        fbManager.setServer("localhost");
        fbManager.setPort(5066);
        fbManager.start();

        Path dbFolder = tempDir.resolve("db");
        Files.createDirectories(dbFolder);

        mRelativeDatabasePath = "testES01874.fdb";
        mAbsoluteDatabasePath = dbFolder.resolve(mRelativeDatabasePath).toString();

        fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
    }

    @AfterEach
    void tearDown() throws Exception {
        fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        fbManager.stop();
        fbManager = null;

        cleanUpFile(mRelativeDatabasePath);
    }

    @Test
    void testFBManagerWithoutSettingServerAndPort() throws Exception {
        try (FBManager testFBManager = new FBManager(gdsType)) {
            testFBManager.start();

            testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            testFBManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        }
    }

    @Test
    void testFBManagerWithRelativeDatabaseFile() throws Exception {
        try (FBManager testFBManager = new FBManager(gdsType)) {
            testFBManager.setDropOnStop(true);
            testFBManager.start();

            testFBManager.createDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");
        }
    }

    @Test
    void testDriverManagerGetConnectionWithoutServerAndPortInUrl() throws Exception {
        Connection connection = DriverManager.getConnection(
                "jdbc:firebirdsql:fboo:embedded:" + mAbsoluteDatabasePath +"?encoding=NONE", "SYSDBA", "masterkey");
        connection.close();
    }

    @Test
    void testDriverManagerGetConnectionWithoutServerAndPortInUrlWithRelativeDatabasePath() throws Exception {
        try (FBManager testFBManager = new FBManager(gdsType)) {
            testFBManager.setDropOnStop(true);
            testFBManager.start();

            testFBManager.createDatabase(mRelativeDatabasePath, "SYSDBA", "masterkey");

            Connection connection = DriverManager.getConnection(
                    "jdbc:firebirdsql:fboo:embedded:" + mRelativeDatabasePath + "?encoding=NONE", "SYSDBA", "masterkey");
            connection.close();
        }
    }

    private static void cleanUpFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        Files.deleteIfExists(filePath);
    }
}
