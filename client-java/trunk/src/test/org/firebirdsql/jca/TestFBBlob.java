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
package org.firebirdsql.jca;

import javax.resource.spi.LocalTransaction;
import javax.sql.DataSource;

import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBBlob;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Arrays;

/**
 * Describe class <code>TestFBBlob</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBBlob extends TestXABase {

    private AbstractConnection c;
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
        c = (AbstractConnection)ds.getConnection();
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
        OutputStream os = ((FBBlob)blob).setBinaryStream(1);//with  jdbc 3, just blob.setBinaryStrean(0);
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
