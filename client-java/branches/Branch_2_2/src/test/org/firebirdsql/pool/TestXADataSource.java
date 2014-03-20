/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.pool;

import java.sql.*;

import java.util.Random;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.firebirdsql.common.FBTestBase;
import org.firebirdsql.gds.ISCConstants;

/**
 * Test suite for XADataSource implementation.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class TestXADataSource extends FBTestBase {

    protected static final int DEFAULT_MIN_CONNECTIONS = 0;
    protected static final int DEFAULT_MAX_CONNECTIONS = 2;
    protected static final int DEFAULT_PING_INTERVAL = 5000;

    
    public TestXADataSource(String name) {
        super(name);
    }

    protected BasicAbstractConnectionPool pool;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        AbstractFBConnectionPoolDataSource connectionPool = FBPooledDataSourceFactory.createFBConnectionPoolDataSource();

        connectionPool.setType(getGdsType().toString());
        
        connectionPool.setDatabase(DB_DATASOURCE_URL);
        connectionPool.setMinPoolSize(DEFAULT_MIN_CONNECTIONS);
        connectionPool.setMaxPoolSize(DEFAULT_MAX_CONNECTIONS);
        connectionPool.setPingInterval(DEFAULT_PING_INTERVAL);
        
        connectionPool.setProperties(getDefaultPropertiesForConnection());
        
        this.pool = connectionPool;

        Connection con = pool.getPooledConnection().getConnection();

        try {
            Statement stmt = con.createStatement();
            try {
                stmt.executeUpdate("CREATE TABLE xa_test(a INTEGER)");
            } catch (SQLException ex) {
                if (ex.getErrorCode() != ISCConstants.isc_no_meta_update)
                        throw ex;
            } finally {
                stmt.close();
            }

        } finally {
            con.close();
        }
    }

    protected void tearDown() throws Exception {
        try {
            pool.shutdown();
        } finally {
            super.tearDown();
        }
    }

    /**
     * Test if XADataSource implementation works correctly.
     * 
     * @throws Exception
     *             if something went wrong.
     */
    public void testXAConnection() throws Exception {
        XADataSource xads = (XADataSource) pool;

        try {
            XAConnection xaConA = xads.getXAConnection();
            XAConnection xaConB = xads.getXAConnection();

            XAResource xaResA = xaConA.getXAResource();
            XAResource xaResB = xaConB.getXAResource();

            Xid xidA = new FBTestXid();
            Xid xidB = new FBTestXid(xidA.getGlobalTransactionId());

            xaResA.start(xidA, XAResource.TMNOFLAGS);
            xaResB.start(xidB, XAResource.TMNOFLAGS);

            Connection conA = xaConA.getConnection();

            try {
                Connection conB = xaConB.getConnection();
                try {

                    Statement stmtA = conA.createStatement();
                    try {
                        stmtA.execute("INSERT INTO xa_test VALUES(1)");
                    } finally {
                        stmtA.close();
                    }

                    Statement stmtB = conB.createStatement();
                    try {
                        stmtB.execute("INSERT INTO xa_test VALUES(2)");
                    } finally {
                        stmtB.close();
                    }

                } finally {
                    conB.close();
                }
            } finally {
                conA.close();
            }

            xaResA.end(xidA, XAResource.TMSUCCESS);
            xaResB.end(xidB, XAResource.TMSUCCESS);

            xaResA.prepare(xidA);
            xaResB.prepare(xidB);

            xaResA.commit(xidA, false);
            xaResB.commit(xidB, false);

            Connection conC = pool.getPooledConnection().getConnection();
            try {
                Statement stmtC = conC.createStatement();
                try {
                    ResultSet rs = stmtC
                            .executeQuery("SELECT a FROM xa_test ORDER BY a");

                    assertTrue("Should select at least one row.", rs.next());
                    assertTrue("First value should be 1", rs.getInt(1) == 1);

                    assertTrue("Should select at least two rows.", rs.next());
                    assertTrue("Second value should be 2 but is " + rs.getInt(1), rs.getInt(1) == 2);

                    assertTrue("Should select only two rows.", !rs.next());
                } finally {
                    stmtC.close();
                }
            } finally {
                conC.close();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private static class FBTestXid implements Xid {

        private static final int formatId = 0x0102;

        private byte[] globalTxId = new byte[8];

        private byte[] branchId = new byte[4];

        private FBTestXid(byte[] globalTxId, byte[] branchId) {
            Random rnd = new Random();
            if (globalTxId == null)
                rnd.nextBytes(this.globalTxId);
            else
                this.globalTxId = globalTxId;

            if (branchId == null)
                rnd.nextBytes(this.branchId);
            else
                this.branchId = branchId;
        }

        public FBTestXid(byte[] globalTxId) {
            this(globalTxId, null);
        }

        public FBTestXid() {
            this(null, null);
        }

        /**
         * Return the global transaction id of this transaction.
         */
        public byte[] getGlobalTransactionId() {
            return globalTxId;
        }

        /**
         * Return the branch qualifier of this transaction.
         */
        public byte[] getBranchQualifier() {
            return branchId;
        }

        /**
         * Return the format identifier of this transaction.
         * 
         * The format identifier augments the global id and specifies how the
         * global id and branch qualifier should be interpreted.
         */
        public int getFormatId() {
            return formatId;
        }
    }

}