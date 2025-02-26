// SPDX-FileCopyrightText: Copyright 2014-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.common.extension.RequireProtocolExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version11.V11DatabaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.firebirdsql.common.extension.RequireProtocolExtension.requireProtocolVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version12.V12Database}, reuses test for V11.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12DatabaseTest extends V11DatabaseTest {

    @RegisterExtension
    @Order(1)
    public static final RequireProtocolExtension requireProtocol = requireProtocolVersion(12);

    protected V12CommonConnectionInfo commonConnectionInfo() {
        return new V12CommonConnectionInfo();
    }

    @Test
    @Disabled("disable is supported by V12 and higher")
    @Override
    public void testCancelOperation_raiseNotSupported() {
        fail("test should be disabled");
    }

    /**
     * Tests if {@link V12Database#cancelOperation(int)} with value {@link ISCConstants#fb_cancel_raise}
     * doesn't throw an {@code SQLFeatureNotSupportedException}.
     * <p>
     * Actual functioning of cancel is tested through {@code org.firebirdsql.jdbc.FBPreparedStatementTest.testCancelStatement()}.
     * </p>
     */
    @Test
    public void testCancelOperation_raiseSupported() throws Exception {
        checkCancelOperationSupported(ISCConstants.fb_cancel_raise, "fb_cancel_raise");
    }

    @Test
    @Disabled("disable is supported by V12 and higher")
    @Override
    public void testCancelOperation_disableNotSupported() {
        fail("test should be disabled");
    }

    /**
     * Tests if {@link V12Database#cancelOperation(int)} with value {@link ISCConstants#fb_cancel_disable}
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
    @Disabled("disable is supported by V12 and higher")
    @Override
    public void testCancelOperation_enableNotSupported() {
        fail("test should be disabled");
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
        usesDatabase.createDefaultDatabase();
        try (WireDatabaseConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireDatabase db = gdsConnection.identify()) {
                assertEquals(getExpectedDatabaseType(), db.getClass(), "Unexpected FbWireDatabase implementation");
                db.attach();

                assertTrue(db.isAttached(), "expected database attached");

                db.cancelOperation(kind);

                assertTrue(db.isAttached(), () -> "Expected database still attached after " + kindName);
                assertTrue(gdsConnection.isConnected(), () -> "Expected connection still open after " + kindName);
            }
        }
    }
}
