/*
 * Firebird Open Source JDBC Driver
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
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.jaybird.fb.constants.SpbItems;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.JdbcResourceHelper.closeQuietly;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isEmbeddedType;
import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for JNA service
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
class JnaServiceTest {

    // TODO Check if tests can be unified with equivalent wire protocol tests

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbServiceProperties connectionInfo = getDefaultServiceProperties();

    @Test
    void testBasicAttach() throws Exception {
        try (JnaService service = factory.serviceConnect(connectionInfo)) {
            service.attach();

            assertTrue(service.isAttached(), "Expected isAttached() to return true");
            assertNotEquals(0, service.getHandle(), "Expected non-zero connection handle");
            assertNotNull(service.getServerVersion(), "Expected version string to be not null");
            assertNotEquals(GDSServerVersion.INVALID_VERSION, service.getServerVersion(), "Expected version should not be invalid");
        }
    }

    @Test
    void doubleAttach() throws Exception {
        try (JnaService service = factory.serviceConnect(connectionInfo)) {
            service.attach();

            //Second attach should throw exception
            SQLException exception = assertThrows(SQLException.class, service::attach,
                    "Second attach should throw exception");
            assertThat(exception, message(equalTo("Already attached to a service")));
        }
    }

    @Test
    void basicStatusVectorProcessing_wrongLogin() throws Exception {
        assumeThat("Embedded on windows does not use authentication",
                FBTestProperties.GDS_TYPE, not(isEmbeddedType()));
        // set invalid password
        connectionInfo.setPassword("abcd");
        JnaService service = factory.serviceConnect(connectionInfo);
        try {
            SQLException exception = assertThrows(SQLException.class, service::attach);
            assertThat(exception, allOf(
                    message(startsWith(getFbMessage(ISCConstants.isc_login))),
                    errorCode(equalTo(ISCConstants.isc_login))));
        } finally {
            closeQuietly(service);
        }
    }

    @Test
    void testBasicStatusVectorProcessing_wrongService() throws Exception {
        assumeTrue(getDefaultSupportInfo().isVersionBelow(4, 0), "Incorrect service name ignored in Firebird 4+");
        // set invalid database
        final String invalidServiceName = "doesnotexist";
        connectionInfo.setServiceName(invalidServiceName);
        JnaService service = factory.serviceConnect(connectionInfo);
        try {
            SQLException exception = assertThrows(SQLException.class, service::attach);
            assertThat(exception, fbMessageStartsWith(ISCConstants.isc_service_att_err));
        } finally {
            closeQuietly(service);
        }
    }

    /**
     * Test for service action.
     * <p>
     * Replicates the behavior of {@link FBStatisticsManager#getHeaderPage()}.
     * </p>
     */
    @Test
    void testStartServiceAction() throws Exception {
        FBManager fbManager = createFBManager();
        defaultDatabaseSetUp(fbManager);
        try (JnaService service = factory.serviceConnect(connectionInfo)) {
            service.attach();

            ServiceRequestBuffer actionSrb = service.createServiceRequestBuffer();
            actionSrb.addArgument(isc_action_svc_db_stats);
            actionSrb.addArgument(SpbItems.isc_spb_dbname, getDatabasePath());
            actionSrb.addArgument(SpbItems.isc_spb_options, isc_spb_sts_hdr_pages);

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
                    containsString("*END*\n")));
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }

}
