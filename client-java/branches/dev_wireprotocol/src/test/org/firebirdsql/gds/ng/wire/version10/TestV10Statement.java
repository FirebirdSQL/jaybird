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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.impl.wire.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.wire.TransactionParameterBufferImpl;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.gds.ng.StatementType;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.management.FBManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestV10Statement {

    private final FbConnectionProperties connectionInfo;
    private FbWireDatabase db;
    FBManager fbManager;

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

    @Before
    public void setUp() throws Exception {
        fbManager = defaultDatabaseSetUp();
        WireConnection gdsConnection = new WireConnection(connectionInfo, ProtocolCollection.create(new Version10Descriptor()));
        gdsConnection.socketConnect();
        db = gdsConnection.identify();
        assertEquals("Unexpected FbWireDatabase implementation", V10Database.class, db.getClass());

        DatabaseParameterBufferImp dpb = new DatabaseParameterBufferImp();
        dpb.addArgument(ISCConstants.isc_dpb_sql_dialect, 3);
        dpb.addArgument(ISCConstants.isc_dpb_user_name, DB_USER);
        dpb.addArgument(ISCConstants.isc_dpb_password, DB_PASSWORD);

        db.attach(dpb);
    }

    @Test
    public void testBasic() throws Exception {
        final FbTransaction transaction = getTransaction();
        final FbStatement statement = db.createStatement();
        statement.setTransaction(transaction);
        statement.prepare("SELECT * FROM RDB$DATABASE");

        System.out.println(statement.getFields());
        System.out.println(statement.getParameters());

        assertEquals("Unexpected StatementType", StatementType.SELECT, statement.getType());
        assertNotNull("Fields", statement.getFields());
        assertEquals("Unexpected field count", 4, statement.getFields().getCount());
        assertNotNull("Parameters", statement.getParameters());
        assertEquals("Unexpected parameter count", 0, statement.getParameters().getCount());
    }

    private FbTransaction getTransaction() throws SQLException {
        TransactionParameterBuffer tpb = new TransactionParameterBufferImpl();
        tpb.addArgument(ISCConstants.isc_tpb_read_committed);
        tpb.addArgument(ISCConstants.isc_tpb_rec_version);
        tpb.addArgument(ISCConstants.isc_tpb_write);
        tpb.addArgument(ISCConstants.isc_tpb_wait);
        return db.createTransaction(tpb);
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (db != null) {
                try {
                    db.detach();
                } catch (SQLException ex) {
                    // ignore (TODO: log)
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }
}
