package org.firebirdsql.ds;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.jdbc.FirebirdConnection;
import org.junit.Test;

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
public class TestFBSimpleDataSource extends FBJUnit4TestBase {

    /**
     * Test for JDBC-314 : setting charSet connection property to (alias of) file.encoding system property makes prepare statement fail
     */
    @Test
    public void testJavaCharSetIsDefaultCharSet() throws Exception {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabase(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUserName(FBTestProperties.DB_USER);
        ds.setPassword(FBTestProperties.DB_PASSWORD);
        ds.setType(FBTestProperties.getGdsType().toString());
        ds.setCharSet(System.getProperty("file.encoding"));
        Connection con = null;
        try {
            con = ds.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM RDB$DATABASE");
            JdbcResourceHelper.closeQuietly(ps);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Preparing statement with property charSet equal to file.encoding should not fail");
        } finally {
            JdbcResourceHelper.closeQuietly(con);
        }
    }

    @Test
    public void defaultDisableWireCompression() throws Exception {
        assumeThat("Test only works with pure java connections", FBTestProperties.GDS_TYPE, isPureJavaType());
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabase(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUserName(FBTestProperties.DB_USER);
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
        ds.setDatabase(FBTestProperties.DB_DATASOURCE_URL);
        ds.setUserName(FBTestProperties.DB_USER);
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
}
