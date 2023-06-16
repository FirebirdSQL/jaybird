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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.DataGenerator;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.RunEnvironmentExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Demonstrates a problem backing up a database which has been created using streamed blobs(As far as my testing shows
 * it does not occur when segmented blobs are used ).
 *
 * My testing shows the following.
 *
 * The test testBacupOfBlobDataDatabase will create a database that when backed up via GBAK
 * produces the following output -
 *
 * gbak: ERROR: segment buffer length shorter than expected
 * gbak: ERROR: gds_$get_segment failed
 * gbak: Exiting before completion due to errors
 *
 * When backed up via the java code we get
 *
 * org.firebirdsql.gds.GDSException: No message for code 1 found.
 * null
 * at org.firebirdsql.ngds.GDS_Impl.native_isc_service_query(Native Method)
 * at org.firebirdsql.ngds.GDS_Impl.isc_service_query(GDS_Impl.java:1147)
 * at org.firebirdsql.ngds.TestJaybirdBlobBackupProblem.queryService(TestJaybirdBlobBackupProblem.java:177)
 * at org.firebirdsql.ngds.TestJaybirdBlobBackupProblem.backupDatabase(TestJaybirdBlobBackupProblem.java:158)
 * at org.firebirdsql.ngds.TestJaybirdBlobBackupProblem.testBacupOfBlobDataDatabase(TestJaybirdBlobBackupProblem.java:93)
 * at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
 * at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
 *
 * And the status vector returned by the call too isc_service_query looked like
 *
 * 1
 * 1
 * 335544366
 * 1
 * 336330774
 * 0
 * 0
 * 0
 * 0
 * ...
 *
 * This test runs with the embedded mode ngds gds implementation, although the primary problem of producing
 * unbackupable database when using streamed blobs is as far as my testing shows common too type2 and type4
 * modes too.
 */
class JaybirdBlobBackupProblemTest {

    @RegisterExtension
    static final RunEnvironmentExtension runEnvironment = RunEnvironmentExtension.builder()
            .requiresDbOnLocalFileSystem()
            .build();

    @TempDir
    private Path tempDir;

    private Path absoluteDatabasePath;
    private Path absoluteBackupPath;
    private FBManager fbManager;
    private FbDatabaseFactory dbFactory;

    @BeforeEach
    void setUp() throws Exception {
        dbFactory = FBTestProperties.getFbDatabaseFactory();
        fbManager = configureFBManager(createFBManager());

        Path dbFolder = tempDir.resolve("db");
        Files.createDirectories(dbFolder);

        absoluteBackupPath = dbFolder.resolve("testES01344.fbk");
        absoluteDatabasePath = dbFolder.resolve("testES01344.fdb");

        fbManager.createDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
    }

    @AfterEach
    void tearDown() throws Exception {
        fbManager.dropDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
        fbManager.stop();
    }

    @Test
    void testBackupOfEmptyDatabase() throws Exception {
        try (FbService service = attachToServiceManager()) {
            backupDatabase(service, "WithoutBlobData");
        }
    }

    @Test
    void testBackupOfBlobDataDatabase() throws Exception {
        writeSomeBlobData();
        try (FbService service = attachToServiceManager()) {
            backupDatabase(service, "WithBlobData");
        }
    }

    private void writeSomeBlobData() throws SQLException {
        Properties props = getDefaultPropertiesForConnection();
        props.setProperty("lc_ctype", "NONE");
        try (var connection = DriverManager.getConnection(getUrl(absoluteDatabasePath), props)) {
            createTheTable(connection);
            writeTheData(connection);
        }
    }

    private void createTheTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE TESTBLOB( THEBLOB BLOB  )");
        }
    }

    private void writeTheData(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO TESTBLOB(THEBLOB) VALUES(?)")) {
            statement.setBytes(1, generateBytes());

            statement.execute();
        }
    }

    private byte[] generateBytes() {
        return DataGenerator.createRandomBytes(128 * 1024);
    }

    private void backupDatabase(FbService service, String logFileSuffix) throws Exception {
        Files.deleteIfExists(absoluteBackupPath);
        Path logFolder = tempDir.resolve("log");
        Files.createDirectories(logFolder);
        Path logfile = logFolder.resolve("backuptest_" + logFileSuffix + ".log");

        startBackup(service);
        queryService(service, logfile);

        assertTrue(Files.isRegularFile(absoluteBackupPath), "Backup file doesn't exist");
    }

    private void queryService(FbService service, Path outputPath) throws Exception {
        ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_info_svc_to_eof);

        try (var file = Files.newOutputStream(outputPath)) {
            do {
                byte[] buffer = service.getServiceInfo(null, serviceRequestBuffer, 1536);
                assertEquals(ISCConstants.isc_info_svc_to_eof, buffer[0], "Expected isc_info_svc_to_eof");
                int numberOfBytes = buffer[1] & 0xff | (buffer[2] & 0xff) << 8;
                if (numberOfBytes == 0) {
                    assertEquals(ISCConstants.isc_info_end, buffer[3], "Expected ISCConstants.isc_info_end");
                    break;
                } else {
                    file.write(buffer, 3, numberOfBytes);
                }
            } while (true);
        }

        assertThat("Looks like the backup failed. See logfile " + outputPath,
                Files.readString(outputPath, StandardCharsets.ISO_8859_1),
                containsString("committing, and finishing."));
    }

    private void startBackup(FbService service) throws SQLException {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, absoluteDatabasePath.toString());
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, absoluteBackupPath.toString());

        service.startServiceAction(serviceRequestBuffer);
    }

    private FbService attachToServiceManager() throws SQLException {
        FbService service = dbFactory.serviceConnect(getDefaultServiceProperties());
        service.attach();

        assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally");

        return service;
    }

}
