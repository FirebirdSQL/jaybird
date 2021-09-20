package org.firebirdsql.ds;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.matchers.GdsTypeMatchers.isPureJavaType;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link FBSimpleDataSource}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FBSimpleDataSourceTest extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    /**
     * Test for JDBC-314 : setting charSet connection property to (alias of) file.encoding system property makes prepare statement fail
     */
    @Test
    public void testJavaCharSetIsDefaultCharSet() {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());
        ds.setCharSet(System.getProperty("file.encoding"));
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM RDB$DATABASE");
            JdbcResourceHelper.closeQuietly(ps);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Preparing statement with property charSet equal to file.encoding should not fail");
        }
    }

    @Test
    public void defaultDisableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertFalse("expected wire compression not in use", serverVersion.isWireCompressionUsed());
        }
    }

    @Test
    public void enableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        assumeTrue("Test requires wire compression", getDefaultSupportInfo().supportsWireCompression());
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        ds.setWireCompression(true);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(0));
            GDSServerVersion serverVersion =
                    connection.unwrap(FirebirdConnection.class).getFbDatabase().getServerVersion();
            assertTrue("expected wire compression in use", serverVersion.isWireCompressionUsed());
        }
    }

    @Test
    public void canChangeConfigAfterConnectionCreation() throws Exception {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        // possible before connecting
        ds.setBlobBufferSize(1024);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }

        // still possible after creating a connection
        ds.setBlobBufferSize(2048);
    }

    @Test
    public void cannotChangeConfigAfterConnectionCreation_usingSharedMCF() throws Exception {
        FBManagedConnectionFactory mcf = new FBManagedConnectionFactory();
        FBSimpleDataSource ds = new FBSimpleDataSource(mcf);
        ds.setDatabaseName(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUser(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());

        // possible before connecting
        ds.setBlobBufferSize(1024);

        try (Connection connection = ds.getConnection()) {
            assertTrue(connection.isValid(1000));
        }

        expectedException.expect(IllegalStateException.class);

        // not possible after creating a connection
        ds.setBlobBufferSize(2048);
    }
}
