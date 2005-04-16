/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.ngds;

import junit.framework.TestCase;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Initial tests for Services API. Currently run only against embedded server.
 *
 */
public class TestServicesAPI extends TestCase
    {
     protected final Logger log = LoggerFactory.getLogger(getClass(),true);

    public TestServicesAPI(String s)
        {
        super(s);
        }

    protected void setUp() throws Exception
        {
        try
            {
            Class.forName(FBDriver.class.getName());

            fbManager = new FBManager(GDSType.NATIVE_EMBEDDED);

            fbManager.setServer("localhost");
            fbManager.setPort(5066);
            fbManager.start();

            mRelativeBackupPath = "db/" + "testES01344.fbk";
            mAbsoluteBackupPath = new File("").getCanonicalPath() + "/"+mRelativeBackupPath;

            mRelativeDatabasePath = "db/" + "testES01344.fdb";
            mAbsoluteDatabasePath = new File("").getCanonicalPath() + "/"+mRelativeDatabasePath;

            fbManager.createDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in setup of " + getName() + ": ", e);
            } // end of try-catch
        }

    protected void tearDown() throws Exception
        {
        try
            {
            fbManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
            fbManager.stop();
            fbManager = null;
            }
        catch (Exception e)
            {
            if (log!=null) log.warn("exception in teardown of " + getName() + ": ", e);
            } // end of try-catch
        }

    public void testServicesManagerAttachAndDetach() throws GDSException
        {
        final GDS gds = GDSFactory.getGDSForType(GDSType.NATIVE_EMBEDDED);

        final ServiceParameterBuffer serviceParameterBuffer = createServiceParameterBuffer(gds);

        final IscSvcHandle handle = gds.createIscSvcHandle();

        assertTrue("Handle should be invalid when created.", handle.isNotValid());

        gds.iscServiceAttach( "service_mgr", handle, serviceParameterBuffer );

        assertTrue("Handle should be valid when isc_service_attach returns normally.", handle.isValid());

        gds.iscServiceDetach(handle);

        assertTrue("Handle should be invalid when isc_service_detach returns normally.", handle.isNotValid());
        }


    public void testBackupAndRestore() throws Exception
        {
        final GDS gds = GDSFactory.getGDSForType(GDSType.NATIVE_EMBEDDED);

        IscSvcHandle handle = attatchToServiceManager(gds);

        backupDatabase(gds, handle);

        detachFromServiceManager(gds, handle);

        dropDatabase(gds);

        handle = attatchToServiceManager(gds);

        restoreDatabase(gds, handle);

        detachFromServiceManager(gds, handle);


        connectToDatabase();
        }


    private void connectToDatabase() throws SQLException
        {
        Connection connection = DriverManager.getConnection("jdbc:firebirdsql:embedded:"+mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        connection.close();
        }

    private void restoreDatabase(GDS gds, IscSvcHandle handle) throws Exception, IOException
        {
        startRestore(gds, handle);

        queryService(gds, handle, "log/restoretest.log");

        assertTrue("Database file doesent exist after restore !", new File(mAbsoluteDatabasePath).exists());
        new File(mAbsoluteBackupPath).delete();
        }

    private void startRestore(GDS gds, IscSvcHandle handle) throws GDSException
        {
        final ServiceRequestBuffer serviceRequestBuffer = gds.createServiceRequestBuffer(ISCConstants.isc_action_svc_restore);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_options,   ISCConstants.isc_spb_res_create);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname,    mAbsoluteBackupPath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file,    mAbsoluteDatabasePath);

        gds.iscServiceStart( handle, serviceRequestBuffer );
        }

    private void dropDatabase(GDS gds) throws Exception
        {
        final FBManager testFBManager = new FBManager(GDSType.NATIVE_EMBEDDED);
        testFBManager.start();
        testFBManager.dropDatabase(mAbsoluteDatabasePath, "SYSDBA", "masterkey");
        testFBManager.stop();
        }

    private void backupDatabase(final GDS gds, final IscSvcHandle handle) throws Exception, IOException
        {
        new File(mAbsoluteBackupPath).delete();

        startBackup(gds, handle);

        queryService(gds, handle, "log/backuptest.log");

        assertTrue("Backup file doesent exist !", new File(mAbsoluteBackupPath).exists());
        }

    private void queryService(final GDS gds, final IscSvcHandle handle, String outputFilename) throws Exception, IOException
        {
        final ServiceRequestBuffer serviceRequestBuffer = gds.createServiceRequestBuffer(ISCConstants.isc_info_svc_to_eof);

        final byte[] buffer = new byte[1024];

        boolean finished = false;

        final FileOutputStream file = new FileOutputStream(outputFilename);

        while(finished==false)
            {
            gds.iscServiceQuery(handle, null, serviceRequestBuffer, buffer);

            final ByteArrayInputStream byteArrayInputStream  = new ByteArrayInputStream(buffer);

            final byte firstByte = (byte) byteArrayInputStream.read();

            int numberOfBytes = (short)((byteArrayInputStream.read() << 0) + (byteArrayInputStream.read() << 8));

            if(numberOfBytes==0)
                {
                 if(byteArrayInputStream.read() != ISCConstants.isc_info_end)
                     throw new Exception("Expect ISCConstants.isc_info_end here");

                finished = true;
                }
            else
                {
                for(; numberOfBytes >= 0; numberOfBytes--)
                    file.write(byteArrayInputStream.read());
                }

            file.flush();
            }


        }

    private void startBackup(final GDS gds, final IscSvcHandle handle) throws GDSException
        {
        final ServiceRequestBuffer serviceRequestBuffer = gds.createServiceRequestBuffer(ISCConstants.isc_action_svc_backup);

        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_verbose);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_dbname,    mAbsoluteDatabasePath);
        serviceRequestBuffer.addArgument(ISCConstants.isc_spb_bkp_file,  mAbsoluteBackupPath);

        gds.iscServiceStart( handle, serviceRequestBuffer );
        }

    private IscSvcHandle attatchToServiceManager(GDS gds) throws GDSException
        {
        final ServiceParameterBuffer serviceParameterBuffer = createServiceParameterBuffer(gds);

        final IscSvcHandle handle = gds.createIscSvcHandle();

        assertTrue("Handle should be invalid when created.", handle.isNotValid());

        gds.iscServiceAttach( "service_mgr", handle, serviceParameterBuffer );

        assertTrue("Handle should be valid when isc_service_attach returns normally.", handle.isValid());

        return handle;
        }

    private void detachFromServiceManager(GDS gds, IscSvcHandle handle) throws GDSException
        {
        if( handle.isNotValid() )
            throw new Error("Handle should be valid here");

        gds.iscServiceDetach(handle);

        assertTrue("Handle should be invalid when isc_service_detach returns normally.", handle.isNotValid());
        }


    private ServiceParameterBuffer createServiceParameterBuffer(GDS gds)
        {
        final ServiceParameterBuffer returnValue = gds.createServiceParameterBuffer();

        returnValue.addArgument(ISCConstants.isc_spb_user_name, "SYSDBA");
        returnValue.addArgument(ISCConstants.isc_spb_password,  "masterkey");

        return returnValue;
        }


    private String mRelativeDatabasePath = null;
    private String mAbsoluteDatabasePath = null;

    private String mRelativeBackupPath = null;
    private String mAbsoluteBackupPath = null;

    private FBManager fbManager = null;
    }
