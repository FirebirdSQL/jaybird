package org.firebirdsql.pool;

import org.firebirdsql.common.FBTestBase;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBSimpleDataSource extends FBTestBase {

    public TestFBSimpleDataSource(String name) {
        super(name);
    }

    /**
     * Test for JDBC-314 : setting charSet connection property to (alias of) file.encoding system property makes prepare statement fail
     */
    public void testJavaCharSetIsDefaultCharSet() throws Exception {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setDatabase(DB_DATASOURCE_URL);
        ds.setUserName(DB_USER);
        ds.setPassword(DB_PASSWORD);
        ds.setCharSet(System.getProperty("file.encoding"));
        Connection con = null;
        try {
            con = ds.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM RDB$DATABASE");
            closeQuietly(ps);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Preparing statement with property charSet equal to file.encoding should not fail");
        } finally {
            closeQuietly(con);
        }
    }
}
