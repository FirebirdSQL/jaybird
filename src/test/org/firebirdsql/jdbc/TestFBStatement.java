package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.firebirdsql.common.FBTestBase;

public class TestFBStatement extends FBTestBase {
    
    private Connection con;

    public TestFBStatement(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();

        Class.forName(FBDriver.class.getName());
        con = this.getConnectionViaDriverManager();

        Statement stmt = con.createStatement();
        
        try {
            
        } finally {
            closeQuietly(stmt);
        } 
    }

    protected void tearDown() throws Exception {
        Statement stmt = con.createStatement();
        try {
        
        } finally {
            closeQuietly(stmt);
        }
        
        closeQuietly(con);

        super.tearDown();
    }
    
    /**
     * Closing a statement twice should not result in an Exception.
     * 
     * @throws SQLException
     */
    public void testDoubleClose() throws SQLException {
        Statement stmt = con.createStatement();
        stmt.close();
        stmt.close();
    }
}
