/*
 * $Id$
 *
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
/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 */
package org.firebirdsql.gds.impl.jni;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;
import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;

import static org.junit.Assert.assertTrue;

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
public class TestJaybirdBlobBackupProblem {

    @ClassRule
    public static final GdsTypeRule testTypes = GdsTypeRule.supports(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME);

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private String mAbsoluteDatabasePath = null;
    private String mAbsoluteBackupPath = null;
    private FBManager fbManager = null;
    private GDS gds;

    @Before
    public void setUp() throws Exception {
        GDSType gdsType = FBTestProperties.getGdsType();
        gds = GDSFactory.getGDSForType(gdsType);
        try {
            fbManager = new FBManager(gdsType);

            fbManager.setServer("localhost");
            fbManager.setPort(5066);
            fbManager.start();

            String mRelativeBackupPath = "db/" + "testES01344.fbk";
            mAbsoluteBackupPath = new File("").getCanonicalPath() + "/" + mRelativeBackupPath;

            String mRelativeDatabasePath = "db/" + "testES01344.fdb";
            mAbsoluteDatabasePath = new File("").getCanonicalPath() + "/" + mRelativeDatabasePath;

            fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        } catch (Exception e) {
            if (log != null) log.warn("exception in setup: ", e);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            fbManager.stop();
            fbManager = null;
        } catch (Exception e) {
            if (log != null) log.warn("exception in teardown: ", e);
        }
    }


    @Test
    public void testBackupOfEmptyDatabase() throws Exception {
        IscSvcHandle handle = attatchToServiceManager(gds);
        backupDatabase(gds, handle, "WithoutBlobData");
        detachFromServiceManager(gds, handle);
    }

    @Test
    public void testBacupOfBlobDataDatabase() throws Exception {
        writeSomeBlobData();
        IscSvcHandle handle = attatchToServiceManager(gds);
        backupDatabase(gds, handle, "WithBlobData");
        detachFromServiceManager(gds, handle);
    }

    private void writeSomeBlobData() throws SQLException {
        final Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:" + mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        try {
            createTheTable(connection);
            writeTheData(connection);
        } finally {
            connection.close();
        }
    }

    private void createTheTable(Connection connection) throws SQLException {
        final Statement statement = connection.createStatement();
        try {
            statement.execute("CREATE TABLE TESTBLOB( THEBLOB BLOB  )");
        } finally {
            statement.close();
        }
    }

    private void writeTheData(Connection connection) throws SQLException {
        final PreparedStatement statement = connection.prepareStatement("INSERT INTO TESTBLOB(THEBLOB) VALUES(?)");
        try {
            statement.setBytes(1, generateBytes());

            statement.execute();
        } finally {
            statement.close();
        }
    }

    private byte[] generateBytes() {
        final byte[] data = new byte[128 * 1024];
        for (int i = 0, n = data.length; i < n; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    private void backupDatabase(final GDS gds, final IscSvcHandle handle, String logFilePostfix) throws Exception {
        new File(mAbsoluteBackupPath).delete();

        startBackup(gds, handle);
        queryService(gds, handle, "log/backuptest_" + logFilePostfix + ".log");

        assertTrue("Backup file doesn't exist !", new File(mAbsoluteBackupPath).exists());
    }

    private void queryService(final GDS gds, final IscSvcHandle handle, String outputFilename) throws Exception {
        final ServiceRequestBuffer serviceRequestBuffer = gds.createServiceRequestBuffer(ISCConstants.isc_info_svc_to_eof);

        final byte[] buffer = new byte[1024];
        final StringBuilder stringBuffer = new StringBuilder();

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
                    for (; numberOfBytes >= 0; numberOfBytes--) {
                        final byte byteToWrite = (byte) byteArrayInputStream.read();

                        file.write(byteToWrite);
                        stringBuffer.append((char) byteToWrite);
                    }
                }

                file.flush();
            }
        } finally {
            file.close();
        }

        assertTrue("Looks like the backup failed. See logfile " + outputFilename, stringBuffer.toString().contains("committing, and finishing."));
    }

    private void startBackup(final GDS gds, final IscSvcHandle handle) throws GDSException {
        final ServiceRequestBuffer serviceRequestBuffer = gds.createServiceRequestBuffer(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname, mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file, mAbsoluteBackupPath);

        gds.iscServiceStart(handle, serviceRequestBuffer);
    }

    private IscSvcHandle attatchToServiceManager(GDS gds) throws GDSException {
        final ServiceParameterBuffer serviceParameterBuffer = createServiceParameterBuffer(gds);
        final IscSvcHandle handle = gds.createIscSvcHandle();

        assertTrue("Handle should be invalid when created.", handle.isNotValid());

        gds.iscServiceAttach("service_mgr", handle, serviceParameterBuffer);

        assertTrue("Handle should be valid when isc_service_attach returns normally.", handle.isValid());

        return handle;
    }

    private void detachFromServiceManager(GDS gds, IscSvcHandle handle) throws GDSException {
        if (handle.isNotValid())
            throw new Error("Handle should be valid here");

        gds.iscServiceDetach(handle);

        assertTrue("Handle should be invalid when isc_service_detach returns normally.", handle.isNotValid());
    }

    private ServiceParameterBuffer createServiceParameterBuffer(GDS gds) {
        final ServiceParameterBuffer returnValue = gds.createServiceParameterBuffer();

        returnValue.addArgument(ISCConstants.isc_spb_user_name, "SYSDBA");
        returnValue.addArgument(ISCConstants.isc_spb_password, "masterkey");

        return returnValue;
    }
}
