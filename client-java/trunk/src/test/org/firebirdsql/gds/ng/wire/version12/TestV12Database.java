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
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.wire.*;
import org.firebirdsql.gds.ng.wire.version11.TestV11Database;
import org.firebirdsql.management.FBManager;
import org.junit.Ignore;
import org.junit.Test;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.createFBManager;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseSetUp;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseTearDown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version12.V12Database}, reuses test for V11.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV12Database extends TestV11Database {

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version12Descriptor();

    @Override
    protected AbstractFbWireDatabase createDummyDatabase() {
        return new V12Database(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);
    }

    @Override
    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version12Descriptor());
    }

    @Override
    protected Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return V12Database.class;
    }

    @Test
    @Ignore("raise is supported by V12 and higher")
    @Override
    public void testCancelOperation_raiseNotSupported() {
    }

    /**
     * Tests if {@link org.firebirdsql.gds.ng.wire.version12.V12Database#cancelOperation(int)} with value {@link org.firebirdsql.gds.ISCConstants#fb_cancel_raise}
     * doesn't throw an {@code SQLFeatureNotSupportedException}.
     * <p>
     * Actual functioning of cancel is tested through {@link org.firebirdsql.jdbc.TestFBPreparedStatement#testCancelStatement()}.
     * </p>
     */
    @Test
    public void testCancelOperation_raiseSupported() throws Exception {
        checkCancelOperationSupported(ISCConstants.fb_cancel_raise, "fb_cancel_raise");
    }

    @Test
    @Ignore("enable is supported by V12 and higher")
    @Override
    public void testCancelOperation_disableNotSupported() {
    }

    /**
     * Tests if {@link org.firebirdsql.gds.ng.wire.version12.V12Database#cancelOperation(int)} with value {@link org.firebirdsql.gds.ISCConstants#fb_cancel_disable}
     * doesn't throw an {@code SQLFeatureNotSupportedException}.
     * <p>
     * TODO: Add test to check it actually works
     * </p>
     */
    @Test
    public void testCancelOperation_disableSupported() throws Exception {
        checkCancelOperationSupported(ISCConstants.fb_cancel_disable, "fb_cancel_disable");
    }

    @Test
    @Ignore("disable is supported by V12 and higher")
    @Override
    public void testCancelOperation_enableNotSupported() {
    }

    /**
     * Tests if {@link org.firebirdsql.gds.ng.wire.version12.V12Database#cancelOperation(int)} with value {@link org.firebirdsql.gds.ISCConstants#fb_cancel_enable}
     * doesn't throw an {@code SQLFeatureNotSupportedException}.
     * <p>
     * TODO: Add test to check it actually works
     * </p>
     */
    @Test
    public void testCancelOperation_enableSupported() throws Exception {
        checkCancelOperationSupported(ISCConstants.fb_cancel_enable, "fb_cancel_enable");
    }

    private void checkCancelOperationSupported(int kind, String kindName) throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try {
            WireConnection gdsConnection = new WireConnection(getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection());
            FbWireDatabase db = null;
            try {
                gdsConnection.socketConnect();
                db = gdsConnection.identify();
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), db.getClass());
                db.attach();

                assertTrue("expected database attached", db.isAttached());

                db.cancelOperation(kind);

                assertTrue("Expected database still attached after " + kindName, db.isAttached());
                assertTrue("Expected connection still open after " + kindName, gdsConnection.isConnected());
            } finally {
                if (db != null) {
                    try {
                        db.detach();
                    } catch (SQLException ex) {
                        // ignore (TODO: log)
                    }
                }
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }
}
