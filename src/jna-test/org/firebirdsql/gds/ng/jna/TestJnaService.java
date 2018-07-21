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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

/**
 * Tests for JNA service
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaService {

    // TODO Check if tests can be unified with equivalent wire protocol tests

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsNativeOnly();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbServiceProperties connectionInfo;
    {
        connectionInfo = new FbServiceProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
    }

    @Test
    public void testBasicAttach() throws Exception {
        try (JnaService service = factory.serviceConnect(connectionInfo)) {
            service.attach();

            assertTrue("Expected isAttached() to return true", service.isAttached());
            assertThat("Expected non-zero connection handle", service.getHandle(), not(equalTo(0)));
            assertNotNull("Expected version string to be not null", service.getServerVersion());
            assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION, service.getServerVersion());
        }
    }

    @Test
    public void doubleAttach() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("Already attached to a service"));

        try (JnaService service = factory.serviceConnect(connectionInfo)) {
            service.attach();

            //Second attach should throw exception
            service.attach();
        }
    }

    @Test
    public void basicStatusVectorProcessing_wrongLogin() throws Exception {
        assumeThat("Embedded on windows does not use authentication",
                FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        // set invalid password
        connectionInfo.setPassword("abcd");
        try (JnaService service = factory.serviceConnect(connectionInfo)) {

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    message(startsWith(getFbMessage(ISCConstants.isc_login))),
                    errorCode(equalTo(ISCConstants.isc_login))
            ));

            service.attach();
        }
    }

    @Test
    public void testBasicStatusVectorProcessing_wrongService() throws Exception {
        // set invalid database
        final String invalidServiceName = "doesnotexist";
        connectionInfo.setServiceName(invalidServiceName);
        try (JnaService service = factory.serviceConnect(connectionInfo)) {

            expectedException.expect(allOf(
                    isA(SQLException.class),
                    fbMessageStartsWith(ISCConstants.isc_service_att_err))
            );

            service.attach();
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
        try (JnaService service = factory.serviceConnect(connectionInfo)) {
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
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

}
