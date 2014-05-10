/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.rules.TestTypeRule;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

/**
 * Initial tests for Services API. Currently run only against embedded server.
 * TODO: Make run against other types
 */
public class TestServicesAPI {

    @Rule
    public final TestTypeRule testType = TestTypeRule.supports(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME);

    private final Logger log = LoggerFactory.getLogger(getClass(), true);

    private String mAbsoluteDatabasePath;
    private String mAbsoluteBackupPath;
    private FBManager fbManager;
    private GDSType gdsType;
    private GDS gds;

    @Before
    public void setUp() throws Exception {
        Class.forName(FBDriver.class.getName());
        gdsType = GDSType.getType("EMBEDDED");
        gds = GDSFactory.getGDSForType(gdsType);

        fbManager = new FBManager(gdsType);

        fbManager.setServer("localhost");
        fbManager.setPort(5066);
        fbManager.start();

        String mRelativeBackupPath = "db/testES01344.fbk";
        mAbsoluteBackupPath = new File(".", mRelativeBackupPath).getAbsolutePath();

        String mRelativeDatabasePath = "db/testES01344.fdb";
        mAbsoluteDatabasePath = new File(".", mRelativeDatabasePath).getAbsolutePath();

        fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
    }

    @After
    public void tearDown() throws Exception {
        fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        fbManager.stop();
        fbManager = null;
    }

    @Test
    public void testServicesManagerAttachAndDetach() throws GDSException {
        final ServiceParameterBuffer serviceParameterBuffer = createServiceParameterBuffer();
        final IscSvcHandle handle = gds.createIscSvcHandle();

        assertTrue("Handle should be invalid when created.", handle.isNotValid());

        gds.iscServiceAttach("service_mgr", handle, serviceParameterBuffer);

        assertTrue("Handle should be valid when isc_service_attach returns normally.", handle.isValid());

        gds.iscServiceDetach(handle);

        assertTrue("Handle should be invalid when isc_service_detach returns normally.", handle.isNotValid());
    }

    @Test
    public void testBackupAndRestore() throws Exception {
        IscSvcHandle handle = attachToServiceManager();
        backupDatabase(handle);
        detachFromServiceManager(handle);
        dropDatabase();

        handle = attachToServiceManager();
        restoreDatabase(handle);
        detachFromServiceManager(handle);

        connectToDatabase();
    }

    private void connectToDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:" + mAbsoluteDatabasePath,
                "SYSDBA", "masterkey");
        connection.close();
    }

    private void restoreDatabase(IscSvcHandle handle) throws Exception {
        startRestore(handle);

        queryService(handle, "log/restoretest.log");

        assertTrue("Database file doesn't exist after restore !", new File(mAbsoluteDatabasePath).exists());
        if (!new File(mAbsoluteBackupPath).delete()) {
            log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }
    }

    private void startRestore(IscSvcHandle handle) throws GDSException {
        final ServiceRequestBuffer serviceRequestBuffer = gds
                .createServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_options, ISCConstants.isc_spb_res_create);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname, mAbsoluteBackupPath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteDatabasePath);

        gds.iscServiceStart(handle, serviceRequestBuffer);
    }

    private void dropDatabase() throws Exception {
        final FBManager testFBManager = new FBManager(gdsType);
        testFBManager.start();
        testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        testFBManager.stop();
    }

    private void backupDatabase(final IscSvcHandle handle) throws Exception {
        if (!new File(mAbsoluteBackupPath).delete()) {
            log.debug("Unable to delete file " + mAbsoluteBackupPath);
        }

        startBackup(handle);

        queryService(handle, "log/backuptest.log");

        assertTrue("Backup file doesn't exist!", new File(mAbsoluteBackupPath).exists());
    }

    private void queryService(final IscSvcHandle handle, String outputFilename) throws Exception {
        final ServiceRequestBuffer serviceRequestBuffer = gds
                .createServiceRequestBuffer(ISCConstants.isc_info_svc_to_eof);
        final byte[] buffer = new byte[1024];
        boolean finished = false;

        final FileOutputStream file = new FileOutputStream(outputFilename);
        try {
            while (!finished) {
                gds.iscServiceQuery(handle, null, serviceRequestBuffer, buffer);
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

                file.flush();
            }
        } finally {
            file.close();
        }
    }

    private void startBackup(final IscSvcHandle handle) throws GDSException {
        final ServiceRequestBuffer serviceRequestBuffer = gds
                .createServiceRequestBuffer(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        gds.iscServiceStart(handle, serviceRequestBuffer);
    }

    private IscSvcHandle attachToServiceManager() throws GDSException {
        final ServiceParameterBuffer serviceParameterBuffer = createServiceParameterBuffer();
        final IscSvcHandle handle = gds.createIscSvcHandle();

        assertTrue("Handle should be invalid when created.", handle.isNotValid());

        gds.iscServiceAttach("service_mgr", handle, serviceParameterBuffer);

        assertTrue("Handle should be valid when isc_service_attach returns normally.", handle.isValid());

        return handle;
    }

    private void detachFromServiceManager(IscSvcHandle handle) throws GDSException {
        if (handle.isNotValid())
            throw new Error("Handle should be valid here");

        gds.iscServiceDetach(handle);

        assertTrue("Handle should be invalid when isc_service_detach returns normally.", handle.isNotValid());
    }

    private ServiceParameterBuffer createServiceParameterBuffer() {
        final ServiceParameterBuffer returnValue = gds.createServiceParameterBuffer();

        returnValue.addArgument(ISCConstants.isc_spb_user_name, "SYSDBA");
        returnValue.addArgument(ISCConstants.isc_spb_password, "masterkey");

        return returnValue;
    }
}
