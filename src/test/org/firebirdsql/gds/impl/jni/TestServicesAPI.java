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
import org.firebirdsql.gds.ng.FbDatabaseFactory;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Initial tests for Services API. Currently run only against embedded server.
 * TODO: Make run against other types
 */
public class TestServicesAPI {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supports(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String mAbsoluteDatabasePath;
    private String mAbsoluteBackupPath;
    private FBManager fbManager;
    private GDSType gdsType;
    private FbDatabaseFactory dbFactory;
    private File logFolder;

    @Before
    public void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        gdsType = GDSType.getType(EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME);
        dbFactory = GDSFactory.getDatabaseFactoryForType(gdsType);

        fbManager = new FBManager(gdsType);

        fbManager.setServer("localhost");
        fbManager.setPort(5066);
        fbManager.start();

        File dbFolder = temporaryFolder.newFolder("db");
        logFolder = temporaryFolder.newFolder("log");

        mAbsoluteBackupPath = new File(dbFolder, "testES01344.fbk").getAbsolutePath();

        mAbsoluteDatabasePath = new File(dbFolder, "testES01344.fdb").getAbsolutePath();

        fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
    }

    @After
    public void tearDown() throws Exception {
        fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        fbManager.stop();
    }

    @Test
    public void testServicesManagerAttachAndDetach() throws SQLException {
        FbService service =  dbFactory.serviceConnect(createServiceProperties());

        assertFalse("Handle should be unattached when created.", service.isAttached());

        service.attach();

        assertTrue("Handle should be attached when isc_service_attach returns normally.", service.isAttached());

        service.close();

        assertFalse("Handle should be detached when isc_service_detach returns normally.", service.isAttached());
    }

    @Test
    public void testBackupAndRestore() throws Exception {
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
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:" + mAbsoluteDatabasePath + "?encoding=NONE",
                "SYSDBA", "masterkey");
        connection.close();
    }

    private void restoreDatabase(FbService service) throws Exception {
        startRestore(service);

        queryService(service, new File(logFolder, "restoretest.log").getAbsolutePath());

        assertTrue("Database file doesn't exist after restore !", new File(mAbsoluteDatabasePath).exists());
        if (!new File(mAbsoluteBackupPath).delete()) {
            this.log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }
    }

    private void startRestore(FbService service) throws SQLException {
        final ServiceRequestBuffer serviceRequestBuffer = service.createServiceRequestBuffer();
        serviceRequestBuffer.addArgument(ISCConstants.isc_action_svc_restore);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_options, ISCConstants.isc_spb_res_create);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        service.startServiceAction(serviceRequestBuffer);
    }

    private void dropDatabase() throws Exception {
        final FBManager testFBManager = new FBManager(gdsType);
        testFBManager.start();
        testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        testFBManager.stop();
    }

    private void backupDatabase(FbService service) throws Exception {
        if (!new File(mAbsoluteBackupPath).delete()) {
            log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }

        startBackup(service);

        queryService(service, new File(logFolder, "backuptest.log").getAbsolutePath());

        assertTrue("Backup file doesn't exist!", new File(mAbsoluteBackupPath).exists());
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

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        service.startServiceAction(serviceRequestBuffer);
    }

    private FbService attachToServiceManager() throws SQLException {
        FbService service =  dbFactory.serviceConnect(createServiceProperties());
        service.attach();

        assertTrue("Handle should be attached when isc_service_attach returns normally.", service.isAttached());

        return service;
    }

    private IServiceProperties createServiceProperties() {
        IServiceProperties serviceProperties = new FbServiceProperties();
        serviceProperties.setUser("SYSDBA");
        serviceProperties.setPassword("masterkey");

        return serviceProperties;
    }
}
