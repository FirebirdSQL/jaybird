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
package org.firebirdsql.gds.ng.wire.version10;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseSetUp;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseTearDown;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.impl.wire.DatabaseParameterBufferImp;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbException;
import org.firebirdsql.gds.ng.SimpleWarningMessageCallback;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.GenericResponse;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.management.FBManager;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class TestV10Database {
    
    private final FbConnectionProperties connectionInfo;
    {
        connectionInfo = new FbConnectionProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setDatabaseName(FBTestProperties.getDatabasePath());
    }
    
    @BeforeClass
    public static void verifyTestType() {
        // Test irrelevant for embedded
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(EmbeddedGDSImpl.EMBEDDED_TYPE_NAME));
        // Test irrelevant for native
        assumeTrue(!FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    /**
     * Test if processResponse does not throw an exception if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessResponse_noException() throws Exception {
        V10Database db = new V10Database(null, null);

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        try {
            db.processResponse(genericResponse);
        } catch (FbException ex) {
            fail("Expected no FbException to be thrown");
        }
    }

    /**
     * Test if processResponse throws the exception in the response if the
     * exception is not a warning.
     */
    @Test
    public void testProcessResponse_exception() throws Exception {
        V10Database db = new V10Database(null, null);

        FbException exception = new FbException(ISCConstants.isc_arg_gds, ISCConstants.isc_numeric_out_of_range);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

        try {
            db.processResponse(genericResponse);
            fail("Expected the registered exception to be thrown");
        } catch (FbException ex) {
            assertEquals("Unexpected exception caught", exception, ex);
        }
    }

    /**
     * Test if processResponse does not throw an exception if the response
     * contains an exception that is warning.
     */
    @Test
    public void testProcessResponse_warning() throws Exception {
        V10Database db = new V10Database(null, null);

        FbException exception = new FbException(ISCConstants.isc_arg_warning, ISCConstants.isc_numeric_out_of_range);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);

        try {
            db.processResponse(genericResponse);
        } catch (FbException ex) {
            fail("Expected no FbException to be thrown");
        }
    }

    /**
     * Test if no warning is registered with the callback if the response does
     * not contain an exception.
     */
    @Test
    public void testProcessReponseWarnings_noException() throws Exception {
        V10Database db = new V10Database(null, null);
        SimpleWarningMessageCallback callback = new SimpleWarningMessageCallback();
        db.setWarningMessageCallback(callback);

        GenericResponse genericResponse = new GenericResponse(-1, -1, null, null);
        db.processResponseWarnings(genericResponse);

        List<FbException> warnings = callback.getWarnings();
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
    }

    /**
     * Test if no warning is registered with the callback if the response
     * contains an exception that is not a warning.
     */
    @Test
    public void testProcessReponseWarnings_exception() throws Exception {
        V10Database db = new V10Database(null, null);
        SimpleWarningMessageCallback callback = new SimpleWarningMessageCallback();
        db.setWarningMessageCallback(callback);

        FbException exception = new FbException(ISCConstants.isc_arg_gds, ISCConstants.isc_numeric_out_of_range);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, exception);
        db.processResponseWarnings(genericResponse);

        List<FbException> warnings = callback.getWarnings();
        assertEquals("Expected no warnings to be registered", 0, warnings.size());
    }

    /**
     * Test if a warning is registered with the callback if the response
     * contains an exception that is a warning.
     */
    @Test
    public void testProcessResponseWarnings_warning() throws Exception {
        V10Database db = new V10Database(null, null);
        SimpleWarningMessageCallback callback = new SimpleWarningMessageCallback();
        db.setWarningMessageCallback(callback);

        FbException warning = new FbException(ISCConstants.isc_arg_warning, ISCConstants.isc_numeric_out_of_range);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, warning);
        db.processResponseWarnings(genericResponse);

        List<FbException> warnings = callback.getWarnings();

        assertEquals("Unexpected warnings registered or no warnings registered", Arrays.asList(warning), warnings);
    }

    /**
     * Test if processing the response warning works even if no warning callback is registered.
     */
    @Test
    public void testProcessResponseWarnings_warning_noCallback() throws Exception {
        V10Database db = new V10Database(null, null);

        FbException warning = new FbException(ISCConstants.isc_arg_warning, ISCConstants.isc_numeric_out_of_range);
        GenericResponse genericResponse = new GenericResponse(-1, -1, null, warning);
        try {
            db.processResponseWarnings(genericResponse);
        } catch (Exception ex) {
            fail("Expected no exception");
        }
    }
    
    /**
     * Tests if attaching to an existing database works.
     */
    @Test
    public void testBasicAttach() throws Exception {
        FBManager fbManager = defaultDatabaseSetUp();
        try {
            WireConnection gdsConnection = new WireConnection(connectionInfo, ProtocolCollection.create(new Version10Descriptor()));
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());
                
                DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
                dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
                dpb.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
                dpb.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
                
                db.attach(dpb, gdsConnection.getDatabaseName());
                System.out.println(db.getHandle());
                
                assertTrue("Expected isAttached() to return true", db.isAttached());
                assertNotNull("Expected version string to be not null", db.getVersionString());
            } finally {
                if (db != null) {
                    try {
                        db.detach();
                    } catch (FbException ex) {
                        // ignore (TODO: log)
                    }
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }
    
    /**
     * Tests if attaching to a non-existent database results in an exception
     */
    @Test
    public void testAttach_NonExistentDatabase() throws Exception {
        WireConnection gdsConnection = new WireConnection(connectionInfo, ProtocolCollection.create(new Version10Descriptor()));
        FbWireDatabase db = null;
        try {
            gdsConnection.socketConnect();
            db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());
            
            DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
            dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
            dpb.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
            dpb.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
            
            db.attach(dpb, gdsConnection.getDatabaseName());
            fail("Expected the attach to fail because the database doesn't exist");
        } catch (FbException e) {
            // TODO Is this actually the right SQLState?
            assertEquals("Expected SQLState for 'Client unable to establish connection' (08001)", "08001", e.getSQLState());
            // TODO Seems to be the least specific error, deeper in there is a more specific 335544734 (isc_io_open_err)
            assertEquals("Expected isc_io_error (335544344)", ISCConstants.isc_io_error, e.getFbErrorCode());
        }
        assertFalse(gdsConnection.isConnected());
    }

    /**
     * Tests creating and subsequently dropping a database
     */
    @Test
    public void testBasicCreateAndDrop() throws Exception {
        WireConnection gdsConnection = new WireConnection(connectionInfo, ProtocolCollection.create(new Version10Descriptor()));
        FbWireDatabase db = null;
        File dbFile = new File(gdsConnection.getDatabaseName());
        try {
            gdsConnection.socketConnect();
            db = gdsConnection.identify();
            assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());
            
            DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
            dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
            dpb.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
            dpb.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);
            
            db.createDatabase(dpb, gdsConnection.getDatabaseName());
            assertTrue(db.isAttached());
            assertTrue(gdsConnection.isConnected());
            assertTrue(dbFile.exists());

            db.dropDatabase();
            assertFalse(gdsConnection.isConnected());
            assertFalse(dbFile.exists());
        } finally {
            if (gdsConnection.isConnected()) {
                gdsConnection.disconnect();
            }
            if (dbFile != null && dbFile.exists()) {
                dbFile.delete();
            }
        }
    }
}
