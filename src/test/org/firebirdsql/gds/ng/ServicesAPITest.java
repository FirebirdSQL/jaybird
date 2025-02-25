// SPDX-FileCopyrightText: Copyright 2003 Ryan Baldwin
// SPDX-FileCopyrightText: Copyright 2003-2006 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicesAPITest {

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
    private Path logFolder;

    @BeforeEach
    void setUp() throws Exception {
        dbFactory = getFbDatabaseFactory();
        fbManager = configureFBManager(createFBManager());

        Path dbFolder = tempDir.resolve("db");
        Files.createDirectories(dbFolder);
        logFolder = tempDir.resolve("log");
        Files.createDirectories(logFolder);

        absoluteBackupPath = dbFolder.resolve("testES01344.fbk");
        absoluteDatabasePath = dbFolder.resolve("testES01344.fdb");

        fbManager.createDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            fbManager.dropDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
        } finally {
            fbManager.stop();
        }
    }

    @Test
    void testServicesManagerAttachAndDetach() throws SQLException {
        FbService service = dbFactory.serviceConnect(getDefaultServiceProperties());
        try {
            assertFalse(service.isAttached(), "Handle should be unattached when created.");

            service.attach();

            assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally.");

            service.close();

            assertFalse(service.isAttached(), "Handle should be detached when isc_service_detach returns normally.");
        } finally {
            if (service.isAttached()) {
                // cannot use try-with-resources: an already closed service object throws an exception (intentionally)
                service.close();
            }
        }
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
        Properties props = getDefaultPropertiesForConnection();
        try (var connection = DriverManager.getConnection(getUrl(absoluteDatabasePath), props)) {
            assertTrue(connection.isValid(5));
        }
    }

    private void restoreDatabase(FbService service) throws Exception {
        startRestore(service);

        queryService(service, logFolder.resolve("restoretest.log"));

        assertTrue(Files.isRegularFile(absoluteDatabasePath), "Database file doesn't exist after restore");
        Files.deleteIfExists(absoluteBackupPath);
    }

    private void startRestore(FbService service) throws SQLException {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_restore);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_options, ISCConstants.isc_spb_res_create);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, absoluteDatabasePath.toString());
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, absoluteBackupPath.toString());

        service.startServiceAction(serviceRequestBuffer);
    }

    private void dropDatabase() throws Exception {
        fbManager.dropDatabase(absoluteDatabasePath.toString(), DB_USER, DB_PASSWORD);
    }

    private void backupDatabase(FbService service) throws Exception {
        Files.deleteIfExists(absoluteBackupPath);

        startBackup(service);

        queryService(service, logFolder.resolve("backuptest.log"));

        assertTrue(Files.isRegularFile(absoluteBackupPath), "Backup file doesn't exist");
    }

    private void queryService(FbService service, Path outputPath) throws Exception {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_info_svc_to_eof);

        try (var file = Files.newOutputStream(outputPath)) {
            do {
                byte[] buffer = service.getServiceInfo(null, serviceRequestBuffer, 1024);
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
    }

    private void startBackup(FbService service) throws SQLException {
        ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(SpbItems.isc_spb_verbose);
        serviceRequestBuffer.addArgument(SpbItems.isc_spb_dbname, absoluteDatabasePath.toString());
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, absoluteBackupPath.toString());

        service.startServiceAction(serviceRequestBuffer);
    }

    private FbService attachToServiceManager() throws SQLException {
        FbService service = dbFactory.serviceConnect(getDefaultServiceProperties());
        service.attach();

        assertTrue(service.isAttached(), "Handle should be attached when isc_service_attach returns normally.");

        return service;
    }

}
