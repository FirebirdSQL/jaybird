package org.firebirdsql.jca;

import javax.resource.spi.*;
import javax.transaction.xa.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

//import org.firebirdsql.jca.*;
import org.firebirdsql.gds.Clumplet;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jgds.GDS_Impl;
import org.firebirdsql.management.FBManager;
import org.firebirdsql.jdbc.FBConnection;

import java.io.*;
import java.util.Properties;
import java.util.HashSet;
import java.sql.*;



import junit.framework.*;
import java.util.Arrays;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *This tests FBBlob
 */
public class TestFBBlob extends TestXABase {

    private FBConnection c;
    private FBManagedConnectionFactory mcf;
    private DataSource ds;
    private Statement s;
    private LocalTransaction t;
    private Exception ex = null;

    private int bloblength = 40960;

    
    public TestFBBlob(String name) {
        super(name);
    }
    
    public static Test suite() {

        return new TestSuite(TestFBBlob.class);
    }
    
    protected void setupTable() throws Exception {
        mcf = initMcf();
        ds = (DataSource)mcf.createConnectionFactory();
        c = (FBConnection)ds.getConnection();
        s = c.createStatement();
        t = c.getLocalTransaction();
        t.begin();
        try {
            s.execute("CREATE TABLE T1 ( C1 INTEGER not null primary key, C2 BLOB)"); 
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
    }

    protected void teardownTable() throws Exception {
        t.begin();
        s.execute("DROP TABLE T1"); 
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
    }

    protected void checkReadBlob() throws Exception {
        PreparedStatement p = c.prepareStatement("select * from T1 where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            System.out.println("C1: " + rs.getInt(1));
            Blob blobRead = rs.getBlob(2);
            //            System.out.println("blobRead blob_id: " + ((FBBlob)blobRead).getBlobId());
            InputStream is = blobRead.getBinaryStream();
            int count = 0;
            while (is.read() != -1) {
                count++;
            }
            System.out.println("C2 count: " + count);
            assert("retrieved wrong length blob: expecting " + bloblength + ", retrieved: " + count, bloblength == count);
 
        }
//        rs.close(); //should be automatic
        p.close();
    }


    public void testUseBlob() throws Exception {

        System.out.println();
        System.out.println("testUseBlob");
        setupTable();

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        Blob blob = c.createBlob();
        OutputStream os = blob.setBinaryStream(0);
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        os.write(testbuf);
        os.close();

        p.setInt(1, 1);
        p.setBlob(2, blob);
        assert("executeUpdate count != 1", p.executeUpdate() == 1);
        
        p.close();
        checkReadBlob();

        t.commit();   
        
        teardownTable();
    }

    public void testUseBlobViapsSetBinaryStream() throws Exception {

        System.out.println();
        System.out.println("testUseBlobViapsSetBinaryStream");
        setupTable();
        
        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        InputStream bais = new ByteArrayInputStream(testbuf);
        p.setBinaryStream(2, bais, bloblength);

        p.setInt(1, 1);
        assert("executeUpdate count != 1", p.executeUpdate() == 1);
        
        p.close();
        checkReadBlob();
        t.commit();   
        
        teardownTable();
        
    }
    
    public void testUseBlobViapsSetBytes() throws Exception {

        System.out.println();
        System.out.println("testUseBlobViapsSetBytes");
        setupTable();
        
        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        p.setBytes(2, testbuf);

        p.setInt(1, 1);
        assert("executeUpdate count != 1", p.executeUpdate() == 1);
        
        p.close();
        checkReadBlob();
        t.commit();   
        
        teardownTable();
        
    }
    

}
