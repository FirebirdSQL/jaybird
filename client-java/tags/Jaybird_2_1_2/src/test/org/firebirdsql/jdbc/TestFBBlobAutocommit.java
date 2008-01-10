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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBTestBase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Arrays;

/**
 * Describe class <code>TestFBBlobAutocommit</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBBlobAutocommit extends FBTestBase {
    public static final String CREATE_TABLE = 
        "CREATE TABLE test_blob(" + 
        "  id INTEGER, " + 
        "  bin_data BLOB, " + 
        "  char_data BLOB SUB_TYPE 1, " + 
        "  blob_data BLOB SUB_TYPE -1" + 
        ")";
        
    public static final String DROP_TABLE = 
        "DROP TABLE test_blob";
        
        
    public static final String TEST_STRING = "just a test string";
    
    public static final byte[] TEST_BYTES = TEST_STRING.getBytes();
    
    public static final int TEST_ID = 1;
        
    private Connection connection;

    
    
    public TestFBBlobAutocommit(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(FBDriver.class.getName());
        connection = getConnectionViaDriverManager();
        
        Statement stmt = connection.createStatement();
        try {
            stmt.executeUpdate(DROP_TABLE);
        }
        catch (Exception e) {}

        stmt.executeUpdate(CREATE_TABLE);
        stmt.close();
    }

    protected void tearDown() throws Exception {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(DROP_TABLE);
        stmt.close();
        connection.close();
        super.tearDown();
    }
    
    /**
     * Test if correct object types are returned from {@link ResultSet#getObject()}
     * depending on column type.
     * 
     * @throws Exception if something went wrong.
     */
    public void testFieldTypes() throws Exception {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob VALUES (?, ?, ?, ?)");
            
        stmt.setInt(1, TEST_ID);
        stmt.setBytes(2, TEST_BYTES);
        stmt.setBytes(3, TEST_BYTES);
        stmt.setBytes(4, TEST_BYTES);
        
        int inserted = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row.", inserted == 1);
        
        connection.close();
        connection = getConnectionViaDriverManager();
        
        stmt = connection.prepareStatement(
            "SELECT * FROM test_blob WHERE id = ?");
            
        stmt.setInt(1, TEST_ID);
        
        ResultSet rs = stmt.executeQuery();
        
        try {
            assertTrue("Result set should have at least 1 row", rs.next());
            
            assertTrue("ID should be the same.", rs.getInt(1) == TEST_ID);
            assertTrue("getObject() on binary field should return byte[]", 
                rs.getObject(2) instanceof byte[]);
            assertTrue("getObject() on text field should return String", 
                rs.getObject(3) instanceof String);
            assertTrue("getObject() on blob field should return java.sql.Blob", 
                rs.getObject(4) instanceof Blob);
                
            assertTrue("content of binary field should be same to written", 
                Arrays.equals(rs.getBytes(2), TEST_BYTES));
            assertTrue("content of text field should be same to written", 
                Arrays.equals(rs.getBytes(3), TEST_BYTES));
                
            assertTrue("string values should be the same",
                TEST_STRING.equals(rs.getString(3)));
            
            assertTrue("content of blob field should be same to written", 
                Arrays.equals(
                    rs.getBlob(4).getBytes(1, TEST_BYTES.length), 
                    TEST_BYTES));
            assertTrue("content of text field should be same to written", 
                Arrays.equals(rs.getBytes(4), TEST_BYTES));
            
    
            
            assertTrue("Result set should contain only one row.", !rs.next());
        } finally {
            rs.close();
            stmt.close();
        }
    }
    
    /**
     * Test if {@link PreparedStatement#setBinaryStream(int, InputStream)} stores
     * data correctly in auto-commit case.
     * 
     * @throws Exception if something went wrong.
     */
    public void testSetBinaryStream() throws Exception {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
          
        stmt.setInt(1, 2);
        stmt.setBinaryStream(2, 
            new ByteArrayInputStream(TEST_BYTES), TEST_BYTES.length);
            
        int insertedCount = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row.", insertedCount == 1);
        
        stmt = connection.prepareStatement(
            "SELECT bin_data FROM test_blob WHERE id = ?");
        stmt.setInt(1, 2);
        
        ResultSet rs = stmt.executeQuery();
        
        try {
            assertTrue("Should get at least one record.", rs.next());
            
            byte[] bytes = rs.getBytes(1);
            
            assertTrue("Should read peviously saved data", Arrays.equals(TEST_BYTES, bytes));
            assertTrue("Should have exactly one record.", !rs.next());
        } finally {
            rs.close();
            stmt.close();
        }
    }
    
    /**
     * Test if driver returns correctly empty and null blobs.
     * 
     * @throws Exception if something went wrong.
     */
    public void testEmptyOrNullBlob() throws Exception {
        try {
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
            
        try {
            stmt.setInt(1, 3);
            stmt.setBytes(2, new byte[0]);
            
            int insertedCount = stmt.executeUpdate();
            assertTrue("Should insert one row.", insertedCount == 1);
            
            stmt.setInt(1, 4);
            stmt.setNull(2, Types.BINARY);
            
            insertedCount = stmt.executeUpdate();
            assertTrue("Should insert one row.", insertedCount == 1);
        } finally {        
            stmt.close();
        }
        
        stmt = connection.prepareStatement(
            "SELECT bin_data FROM test_blob WHERE id = ?");
            
        stmt.setInt(1, 3);
        try {
            ResultSet rs = stmt.executeQuery();
            
            assertTrue("Should select at least one row.", rs.next());
            assertTrue("Result should byte[0]", Arrays.equals(rs.getBytes(1), new byte[0]));
            
            rs.close();
            
            stmt.setInt(1, 4);
            rs = stmt.executeQuery();
            
            assertTrue("Should select at least one row.", rs.next());
            assertTrue("Result should byte[0]", rs.getObject(1) == null);
            
            rs.close();
        } finally {
            stmt.close();
        }
        } catch(Exception ex) {
            ex.printStackTrace();
            
            throw ex;
        }
    }
    
    /**
     * Test if {@link PreparedStatement#setBinaryStream(int, InputStream)} stores
     * data correctly in auto-commit case.
     * 
     * @throws Exception if something went wrong.
     */
    public void testSetBlob() throws Exception {
        connection.setAutoCommit(false);
        
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO test_blob(id, bin_data) VALUES(?, ?)");
          
        connection.commit();
        
        class TempBlobImpl implements Blob {
            
            private byte[] data;
            
            TempBlobImpl(byte[] data) {
                this.data = data;
            }
            
            public long length() {
                return data.length;
            }

            public byte[] getBytes(long pos, int length) {
                int lastPos = (int)pos + length;
                
                if (lastPos > data.length)
                    lastPos = data.length;
                
                byte[] result = new byte[lastPos - (int)pos];
                System.arraycopy(data, (int)pos, result, 0, result.length);
                return result;
            }
            
            public java.io.InputStream getBinaryStream () {
                return new ByteArrayInputStream(data);
            }

            public long position(byte pattern[], long start) throws SQLException {
                throw new FBDriverNotCapableException();
            }

            public long position(Blob pattern, long start) throws SQLException {
                throw new FBDriverNotCapableException();
            }

            public int setBytes(long pos, byte[] bytes) throws SQLException {
                throw new FBDriverNotCapableException();
            }
            
            public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
                throw new FBDriverNotCapableException();
            }
            
            public java.io.OutputStream setBinaryStream(long pos) throws SQLException {
                throw new FBDriverNotCapableException();
            }

            public void truncate(long len) throws SQLException {
                throw new FBDriverNotCapableException();
            }

            public void free() throws SQLException {
                throw new FBDriverNotCapableException();
            }

            public InputStream getBinaryStream(long pos, long length) throws SQLException {
                throw new FBDriverNotCapableException();
            }
        }
        
        stmt.setInt(1, 2);
        stmt.setBlob(2, new TempBlobImpl(TEST_BYTES));
            
        int insertedCount = stmt.executeUpdate();
        stmt.close();
        
        assertTrue("Should insert one row.", insertedCount == 1);
        
        stmt = connection.prepareStatement(
            "SELECT bin_data FROM test_blob WHERE id = ?");
        stmt.setInt(1, 2);
        
        ResultSet rs = stmt.executeQuery();
        
        try {
            assertTrue("Should get at least one record.", rs.next());
            
            byte[] bytes = rs.getBytes(1);
            
            assertTrue("Should read peviously saved data", Arrays.equals(TEST_BYTES, bytes));
            assertTrue("Should have exactly one record.", !rs.next());
        } finally {
            rs.close();
            stmt.close();
        }
    }
}
