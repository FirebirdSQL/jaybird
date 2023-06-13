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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.extension.GdsTypeExtension;
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
import java.sql.DriverManager;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ryan Baldwin
 */
class SpecialEmbeddedServerUrlsTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @TempDir
    private Path tempDir;

    private String relativeDatabasePath;
    private Path absoluteDatabasePath;
    private FBManager fbManager;

    @BeforeEach
    void setUp() throws Exception {
        fbManager = configureFBManager(createFBManager());

        Path dbFolder = tempDir.resolve("db");
        Files.createDirectories(dbFolder);

        relativeDatabasePath = "testES01874.fdb";
        absoluteDatabasePath = dbFolder.resolve(relativeDatabasePath);

        fbManager.createDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
    }

    @AfterEach
    void tearDown() throws Exception {
        fbManager.dropDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
        fbManager.stop();

        cleanUpFile(relativeDatabasePath);
    }

    @Test
    void testFBManagerWithoutSettingServerAndPort() throws Exception {
        try (FBManager testFBManager = createFBManager()) {
            testFBManager.start();

            testFBManager.dropDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
            testFBManager.createDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
        }
    }

    @Test
    void testFBManagerWithRelativeDatabaseFile() throws Exception {
        try (FBManager testFBManager = createFBManager()) {
            testFBManager.setDropOnStop(true);
            testFBManager.start();

            testFBManager.createDatabase(relativeDatabasePath, DB_USER, DB_PASSWORD);
        }
    }

    @Test
    void testDriverManagerGetConnectionWithoutServerAndPortInUrl() throws Exception {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "NONE");
        try (var connection = DriverManager.getConnection(getUrl(absoluteDatabasePath), props)) {
            assertTrue(connection.isValid(1000));
        }
    }

    @Test
    void testDriverManagerGetConnectionWithoutServerAndPortInUrlWithRelativeDatabasePath() throws Exception {
        try (FBManager testFBManager = createFBManager()) {
            testFBManager.setDropOnStop(true);
            testFBManager.start();

            testFBManager.createDatabase(relativeDatabasePath, DB_USER, DB_PASSWORD);

            Properties props = getDefaultPropertiesForConnection();
            props.setProperty("lc_ctype", "NONE");
            try (var connection = DriverManager.getConnection(getUrl(absoluteDatabasePath), props)) {
                assertTrue(connection.isValid(1000));
            }
        }
    }

    private static void cleanUpFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        Files.deleteIfExists(filePath);
    }
}
