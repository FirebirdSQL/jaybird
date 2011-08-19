package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.PooledConnection;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.common.SimpleFBTestBase;

public class TestFBConnectionPoolDataSource extends FBTestBase {

    private List connections = new ArrayList();

    public TestFBConnectionPoolDataSource(String name) {
        super(name);
    }

    protected FBConnectionPoolDataSource ds;

    public void setUp() throws Exception {
        super.setUp();

        FBConnectionPoolDataSource newDs = new FBConnectionPoolDataSource();
        newDs.setType(SimpleFBTestBase.getProperty("test.gds_type", null));
        newDs.setServerName(DB_SERVER_URL);
        newDs.setPortNumber(DB_SERVER_PORT);
        newDs.setDatabaseName(getDatabasePath());
        newDs.setUser(DB_USER);
        newDs.setPassword(DB_PASSWORD);

        ds = newDs;
    }

    public void tearDown() throws Exception {
        Iterator iter = connections.iterator();
        while (iter.hasNext()) {
            PooledConnection pc = (PooledConnection) iter.next();
            try {
                pc.close();
            } catch (SQLException ex) {
            }
        }
        super.tearDown();
    }
    
    protected PooledConnection getPooledConnection() throws SQLException {
        PooledConnection pc = ds.getPooledConnection();
        connections.add(pc);
        return pc;
    }

    /**
     * Tests if the ConnectionPoolDataSource can create a PooledConnection
     * 
     * @throws SQLException
     */
    public void testDataSource_start() throws SQLException {
        getPooledConnection();
    }

    /**
     * Tests if the connection obtained from the PooledConnection can be used
     * and has expected defaults.
     * 
     * @throws SQLException
     */
    public void testConnection() throws SQLException {
        PooledConnection pc = getPooledConnection();

        Connection con = pc.getConnection();

        assertTrue("Autocommit should be true", con.getAutoCommit());
        assertTrue("Read-only should be false", !con.isReadOnly());
        assertEquals("Tx isolation level should be read committed.",
                Connection.TRANSACTION_READ_COMMITTED, con.getTransactionIsolation());

        Statement stmt = con.createStatement();

        try {
            ResultSet rs = stmt.executeQuery("SELECT cast(1 AS INTEGER) FROM rdb$database");

            assertTrue("Should select one row", rs.next());
            assertEquals("Selected value should be 1.", 1, rs.getInt(1));
        } finally {
            stmt.close();
        }
        con.close();
        assertTrue("Connection should report as being closed.", con.isClosed());
    }
}
