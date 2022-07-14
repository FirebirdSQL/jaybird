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
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
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
    static final GdsTypeExtension testTypes = GdsTypeExtension.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @TempDir
    private Path tempDir;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String mAbsoluteDatabasePath = null;
    private String mAbsoluteBackupPath = null;
    private FBManager fbManager = null;
    private FbDatabaseFactory dbFactory;

    @BeforeEach
    void setUp() {
        GDSType gdsType = FBTestProperties.getGdsType();
        dbFactory = FBTestProperties.getFbDatabaseFactory();
        try {
            fbManager = new FBManager(gdsType);

            fbManager.setServer("localhost");
            fbManager.setPort(5066);
            fbManager.start();

            Path dbFolder = tempDir.resolve("db");
            Files.createDirectories(dbFolder);

            mAbsoluteBackupPath = dbFolder.resolve("testES01344.fbk").toString();

            mAbsoluteDatabasePath = dbFolder.resolve("testES01344.fdb").toString();

            fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        } catch (Exception e) {
            log.warn("exception in setup: ", e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            fbManager.stop();
        } catch (Exception e) {
            log.warn("exception in teardown: ", e);
        }
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
        try (Connection connection = DriverManager.getConnection(
                "jdbc:firebirdsql:embedded:" + mAbsoluteDatabasePath + "?encoding=NONE", "SYSDBA", "masterkey")) {
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
        final byte[] data = new byte[128 * 1024];
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    private void backupDatabase(FbService service, String logFilePostfix) throws Exception {
        new File(mAbsoluteBackupPath).delete();
        Path logFolder = tempDir.resolve("log");
        Files.createDirectories(logFolder);
        String logfile = logFolder.resolve("backuptest_" + logFilePostfix + ".log").toString();

        startBackup(service);
        queryService(service, logfile);

        assertTrue(new File(mAbsoluteBackupPath).exists(), "Backup file doesn't exist");
    }

    private void queryService(FbService service, String outputFilename) throws Exception {
        ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_info_svc_to_eof);

        final StringBuilder stringBuffer = new StringBuilder();
        boolean finished = false;
        try (FileOutputStream file = new FileOutputStream(outputFilename)) {
            while (!finished) {
                byte[] buffer = service.getServiceInfo(null, serviceRequestBuffer, 1024);

                final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);

                // TODO Find out why unused
                final byte firstByte = (byte) byteArrayInputStream.read();

                int numberOfBytes = (short) ((byteArrayInputStream.read()) + (byteArrayInputStream.read() << 8));

                if (numberOfBytes == 0) {
                    if (byteArrayInputStream.read() != ISCConstants.isc_info_end)
                        throw new Exception("Expect ISCConstants.isc_info_end here");

                    finished = true;
                } else {
                    for (; numberOfBytes >= 0; numberOfBytes--) {
                        final byte byteToWrite = (byte) byteArrayInputStream.read();

                        file.write(byteToWrite);
                        stringBuffer.append((char) byteToWrite);
                    }
                }
            }
        }

        assertThat("Looks like the backup failed. See logfile " + outputFilename, stringBuffer.toString(),
                containsString("committing, and finishing."));
    }

    private void startBackup(FbService service) throws SQLException {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        service.startServiceAction(serviceRequestBuffer);
    }

    private FbService attachToServiceManager() throws SQLException {
        FbService service = dbFactory.serviceConnect(createServiceProperties());
        service.attach();

        assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally");

        return service;
    }

    private IServiceProperties createServiceProperties() {
        IServiceProperties serviceProperties = new FbServiceProperties();
        serviceProperties.setUser("SYSDBA");
        serviceProperties.setPassword("masterkey");

        return serviceProperties;
    }
}
