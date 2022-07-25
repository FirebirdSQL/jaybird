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
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Initial tests for Services API.
 */
class ServicesAPITest {

    @TempDir
    private Path tempDir;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String mAbsoluteDatabasePath;
    private String mAbsoluteBackupPath;
    private FBManager fbManager;
    protected String protocol;
    protected GDSType gdsType;
    protected int port = 5066;
    private FbDatabaseFactory dbFactory;
    private Path logFolder;

    @BeforeEach
    void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        dbFactory = GDSFactory.getDatabaseFactoryForType(gdsType);

        fbManager = new FBManager(gdsType);

        fbManager.setServer("localhost");
        fbManager.setPort(port);
        fbManager.start();

        Path dbFolder = tempDir.resolve("db");
        Files.createDirectories(dbFolder);
        logFolder = tempDir.resolve("log");
        Files.createDirectories(logFolder);

        mAbsoluteBackupPath = dbFolder.resolve("testES01344.fbk").toString();

        mAbsoluteDatabasePath = dbFolder.resolve("testES01344.fdb").toString();

        fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
    }

    @AfterEach
    void tearDown() throws Exception {
        fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        fbManager.stop();
    }

    @Test
    void testServicesManagerAttachAndDetach() throws SQLException {
        FbService service = dbFactory.serviceConnect(createServiceProperties());

        assertFalse(service.isAttached(), "Handle should be unattached when created.");

        service.attach();

        assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally.");

        service.close();

        assertFalse(service.isAttached(), "Handle should be detached when isc_service_detach returns normally.");
    }

    @Test
    void testBackupAndRestore() throws Exception {
        try (FbService service = attachToServiceManager()) {
            backupDatabase(service);
        }
        dropDatabase();

        try (FbService service = attachToServiceManager()) {
            restoreDatabase(service);
        }

        connectToDatabase();
    }

    private void connectToDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection(
                protocol + mAbsoluteDatabasePath + "?encoding=NONE", "SYSDBA", "masterkey");
        connection.close();
    }

    private void restoreDatabase(FbService service) throws Exception {
        startRestore(service);

        queryService(service, logFolder.resolve("restoretest.log").toString());

        assertTrue(new File(mAbsoluteDatabasePath).exists(), "Database file doesn't exist after restore !");
        if (!new File(mAbsoluteBackupPath).delete()) {
            this.log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }
    }

    private void startRestore(FbService service) throws SQLException {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_restore);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_options, ISCConstants.isc_spb_res_create);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        service.startServiceAction(serviceRequestBuffer);
    }

    private void dropDatabase() throws Exception {
        try (FBManager testFBManager = new FBManager(gdsType)) {
            testFBManager.start();
            testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        }
    }

    private void backupDatabase(FbService service) throws Exception {
        if (!new File(mAbsoluteBackupPath).delete()) {
            log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }

        startBackup(service);

        queryService(service, logFolder.resolve("backuptest.log").toString());

        assertTrue(new File(mAbsoluteBackupPath).exists(), "Backup file doesn't exist!");
    }

    private void queryService(FbService service, String outputFilename) throws Exception {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_info_svc_to_eof);

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
                    for (; numberOfBytes >= 0; numberOfBytes--)
                        file.write(byteArrayInputStream.read());
                }
            }
        }
    }

    private void startBackup(FbService service) throws SQLException {
        ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        service.startServiceAction(serviceRequestBuffer);
    }

    private FbService attachToServiceManager() throws SQLException {
        FbService service = dbFactory.serviceConnect(createServiceProperties());
        service.attach();

        assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally.");

        return service;
    }

    private IServiceProperties createServiceProperties() {
        IServiceProperties serviceProperties = new FbServiceProperties();
        serviceProperties.setUser("SYSDBA");
        serviceProperties.setPassword("masterkey");

        return serviceProperties;
    }
}
