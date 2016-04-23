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

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ServiceParameterBuffer;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.IServiceProperties;
import org.firebirdsql.gds.ng.wire.AbstractFbWireService;
import org.firebirdsql.gds.ng.wire.FbWireService;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link V10Service}. This test class can be sub-classed for tests running on newer protocol versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Service {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(10);

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludesNativeOnly();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final V10CommonConnectionInfo commonConnectionInfo;

    public TestV10Service() {
        this(new V10CommonConnectionInfo());
    }

    protected TestV10Service(V10CommonConnectionInfo commonConnectionInfo) {
        this.commonConnectionInfo = commonConnectionInfo;
    }

    protected final IServiceProperties getConnectionInfo() {
        return commonConnectionInfo.getServiceConnectionInfo();
    }

    protected final AbstractFbWireService createDummyService() throws SQLException {
        return commonConnectionInfo.createDummyService();
    }

    protected final ProtocolCollection getProtocolCollection() {
        return commonConnectionInfo.getProtocolCollection();
    }

    protected final Class<? extends FbWireService> getExpectedServiceType() {
        return commonConnectionInfo.getExpectedServiceType();
    }

    /**
     * Tests if attaching to a service works.
     * <p>
     * Includes a test of {@link FbService#getServiceInfo(ServiceParameterBuffer, ServiceRequestBuffer, int)} (which is
     * called to populate the server version after attach).
     * </p>
     */
    @Test
    public void testBasicAttach() throws Exception {
        try (WireServiceConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireService service = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireService implementation", getExpectedServiceType(), service.getClass());
                service.attach();

                assertTrue("Expected isAttached() to return true", service.isAttached());
                assertNotNull("Expected version string to be not null", service.getServerVersion());
                assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION,
                        service.getServerVersion());
            }
        }
    }

    /**
     * Test for service action.
     * <p>
     * Replicates the behavior of {@link FBStatisticsManager#getHeaderPage()}.
     * </p>
     */
    @Test
    public void testStartServiceAction() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (WireServiceConnection gdsConnection = createConnection()) {
            gdsConnection.socketConnect();
            try (FbWireService service = gdsConnection.identify()) {
                assertEquals("Unexpected FbWireService implementation", getExpectedServiceType(), service.getClass());
                service.attach();

                ServiceRequestBuffer actionSrb = service.createServiceRequestBuffer();
                actionSrb.addArgument(isc_action_svc_db_stats);
                actionSrb.addArgument(isc_spb_dbname, getDatabasePath());
                actionSrb.addArgument(isc_spb_options, isc_spb_sts_hdr_pages);

                service.startServiceAction(actionSrb);

                ServiceRequestBuffer infoSrb = service.createServiceRequestBuffer();
                infoSrb.addArgument(isc_info_svc_to_eof);
                int bufferSize = 1024;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                boolean processing = true;
                while (processing) {
                    byte[] buffer = service.getServiceInfo(null, infoSrb, bufferSize);

                    switch (buffer[0]) {
                    case isc_info_svc_to_eof:
                        int dataLength = iscVaxInteger2(buffer, 1);
                        if (dataLength == 0) {
                            if (buffer[3] != isc_info_end) {
                                throw new SQLException("Unexpected end of stream reached.");
                            } else {
                                processing = false;
                                break;
                            }
                        }
                        bos.write(buffer, 3, dataLength);
                        break;
                    case isc_info_truncated:
                        bufferSize = bufferSize * 2;
                        break;
                    case isc_info_end:
                        processing = false;
                        break;
                    }
                }
                String headerPage = service.getEncoding().decodeFromCharset(bos.toByteArray());
                assertThat("Expected database header page content", headerPage, allOf(
                        startsWith("\nDatabase"),
                        containsString("Database header page information"),
                        endsWith("*END*\n")));
            }
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

    private WireServiceConnection createConnection() throws SQLException {
        return new WireServiceConnection(getConnectionInfo(),
                EncodingFactory.getPlatformDefault(), getProtocolCollection());
    }
}
