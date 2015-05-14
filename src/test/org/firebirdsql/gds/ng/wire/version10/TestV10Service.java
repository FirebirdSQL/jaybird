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
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSImpl;
import org.firebirdsql.gds.impl.jni.NativeGDSImpl;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.junit.Assert.*;

/**
 * Tests for {@link V10Service}. This test class can be sub-classed for tests running on newer protocol versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Service {

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludes(
            EmbeddedGDSImpl.EMBEDDED_TYPE_NAME,
            NativeGDSImpl.NATIVE_TYPE_NAME);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    protected static final WireServiceConnection DUMMY_CONNECTION;
    static {
        try {
            FbServiceProperties connectionInfo = new FbServiceProperties();
            connectionInfo.setEncoding("NONE");

            DUMMY_CONNECTION = new WireServiceConnection(connectionInfo);
        } catch (SQLException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version10Descriptor();

    private final FbServiceProperties connectionInfo;
    {
        connectionInfo = new FbServiceProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
        connectionInfo.setEncoding("NONE");
    }

    protected FbServiceProperties getConnectionInfo() {
        return connectionInfo;
    }

    protected V10Service createDummyService() {
        return new V10Service(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);
    }

    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version10Descriptor());
    }

    protected Class<? extends FbWireService> getExpectedDatabaseType() {
        return V10Service.class;
    }

    /**
     * Tests if attaching to an existing database works.
     */
    @Test
    public void testBasicAttach() throws Exception {
        try (WireServiceConnection gdsConnection = new WireServiceConnection(
                getConnectionInfo(), EncodingFactory.getDefaultInstance(), getProtocolCollection())) {
            gdsConnection.socketConnect();
            try (FbWireService service = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireDatabase implementation", getExpectedDatabaseType(), service.getClass());

                service.attach();
                System.out.println(service.getHandle());

                assertTrue("Expected isAttached() to return true", service.isAttached());
                assertNotNull("Expected version string to be not null", service.getServerVersion());
                assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION,
                        service.getServerVersion());
            }
        }
    }
}
