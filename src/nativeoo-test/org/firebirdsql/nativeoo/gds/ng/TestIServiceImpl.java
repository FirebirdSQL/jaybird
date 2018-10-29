package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ServiceRequestBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.impl.nativeoo.FbOOEmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.management.FBStatisticsManager;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import static org.firebirdsql.common.FBTestProperties.*;
import static org.firebirdsql.common.FBTestProperties.defaultDatabaseTearDown;
import static org.firebirdsql.common.FBTestProperties.getDatabasePath;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.*;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.ISCConstants.isc_info_end;
import static org.firebirdsql.gds.VaxEncoding.iscVaxInteger2;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

public class TestIServiceImpl {

    private static final String gdsType = "FBOONATIVE";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) GDSFactory.getDatabaseFactoryForType(GDSType.getType(gdsType));

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
        try (IServiceImpl service = (IServiceImpl)factory.serviceConnect(connectionInfo)) {
            service.attach();

            assertTrue("Expected isAttached() to return true", service.isAttached());
            assertNotNull("Expected version string to be not null", service.getServerVersion());
            assertNotEquals("Expected version should not be invalid", GDSServerVersion.INVALID_VERSION, service.getServerVersion());
        }
    }

    @Test
    public void doubleAttach() throws Exception {
        expectedException.expect(SQLException.class);
        expectedException.expectMessage(equalTo("Already attached to a service"));

        try (IServiceImpl service = (IServiceImpl)factory.serviceConnect(connectionInfo)) {
            service.attach();

            //Second attach should throw exception
            service.attach();
        }
    }

    @Test
    public void basicStatusVectorProcessing_wrongLogin() throws Exception {
        assumeThat("Embedded on windows does not use authentication",
                FBTestProperties.GDS_TYPE, is(not(FbOOEmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME)));
        // set invalid password
        connectionInfo.setPassword("abcd");
        try (IServiceImpl service = (IServiceImpl)factory.serviceConnect(connectionInfo)) {

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
        try (IServiceImpl service = (IServiceImpl)factory.serviceConnect(connectionInfo)) {

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
        try (IServiceImpl service = (IServiceImpl)factory.serviceConnect(connectionInfo)) {
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
                    endsWith("\n")));
        } finally {
            defaultDatabaseTearDown(fbManager);
        }
    }
}
