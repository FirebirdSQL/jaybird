package org.firebirdsql.ds;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.JdbcResourceHelper;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.Assert.fail;

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
}
