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
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.logging.Logger;

import java.io.*;
import java.util.Properties;
import java.util.HashSet;
import java.sql.*;



import junit.framework.*;
import java.util.Arrays;

/**
 *
 *   @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
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

    private int bloblength = 40960 * 10;


    public TestFBBlob(String name) {
        super(name);
    }

    protected void setupTable(String name) throws Exception {
        mcf = initMcf();
        ds = (DataSource)mcf.createConnectionFactory();
        c = (FBConnection)ds.getConnection();
        c.setAutoCommit(false);
        s = c.createStatement();
        t = c.getLocalTransaction();
        t.begin();
        try {
            s.execute("drop table " + name);
            t.commit();
            t.begin();
        }
        catch (Exception e) {
        }
        try {
            s.execute("CREATE TABLE " + name + " ( C1 INTEGER not null primary key, C2 BLOB)");
            //s.close();
        }
        catch (Exception e) {
            ex = e;
        }
        t.commit();
    }

    protected void teardownTable(String name) throws Exception {
        t.begin();
        s.execute("DROP TABLE " + name);
        s.close();
        t.commit();
        c.close();
        if (ex != null) {
            throw ex;
        }
    }

    protected void checkReadBlob(String name) throws Exception {
        PreparedStatement p = c.prepareStatement("select * from " + name + " where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1));
            Blob blobRead = rs.getBlob(2);
            // if (log != null) log.info("blobRead blob_id: " + ((FBBlob)blobRead).getBlobId());
            InputStream is = blobRead.getBinaryStream();
            int count = 0;
            while (is.read() != -1) {
                count++;
            }
            if (log != null) log.info("C2 count: " + count);
            assertTrue("retrieved wrong length blob: expecting " + bloblength + ", retrieved: " + count, bloblength == count);

        }
//        rs.close(); //should be automatic
        p.close();
    }


    public void testUseBlob() throws Exception {

        if (log != null) log.info("testUseBlob");
        setupTable("T1");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        Blob blob = c.createBlob();
        OutputStream os = ((FBBlob)blob).setBinaryStream(0);//with  jdbc 3, just blob.setBinaryStrean(0);
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        os.write(testbuf);
        os.close();

        p.setInt(1, 1);
        p.setBlob(2, blob);
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);

        p.close();
        checkReadBlob("T1");

        t.commit();

        teardownTable("T1");
    }

    public void testUseBlobViapsSetBinaryStream() throws Exception {

        if (log != null) log.info("testUseBlobViapsSetBinaryStream");
        setupTable("T2");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T2 values (?, ?)");
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        InputStream bais = new ByteArrayInputStream(testbuf);
        p.setBinaryStream(2, bais, bloblength);

        p.setInt(1, 1);
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);

        p.close();
        checkReadBlob("T2");
        t.commit();

        teardownTable("T2");

    }

    public void testUseBlobViapsSetBytes() throws Exception {

        if (log != null) log.info("testUseBlobViapsSetBytes");
        setupTable("T3");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T3 values (?, ?)");
        byte[] a = new String("a").getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        p.setBytes(2, testbuf);

        p.setInt(1, 1);
        assertTrue("executeUpdate count != 1", p.executeUpdate() == 1);

        p.close();
        checkReadBlob("T3");
        t.commit();

        teardownTable("T3");

    }


}
