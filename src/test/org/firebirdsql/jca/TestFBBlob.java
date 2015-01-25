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

import org.firebirdsql.jdbc.AbstractConnection;

import javax.resource.spi.LocalTransaction;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

/**
 * Describe class <code>TestFBBlob</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class TestFBBlob extends TestXABase {

    private AbstractConnection c;
    private LocalTransaction t;
    private Exception ex = null;

    private int bloblength = 40960 * 10;

    public TestFBBlob(String name) {
        super(name);
    }

    protected void setupTable(String name) throws Exception {
        FBManagedConnectionFactory mcf = initMcf();
        DataSource ds = (DataSource) mcf.createConnectionFactory();
        c = (AbstractConnection) ds.getConnection();
        Statement s = c.createStatement();
        t = c.getLocalTransaction();
        t.begin();
        try {
            s.execute("CREATE TABLE " + name + " ( C1 INTEGER not null primary key, C2 BLOB)");
            s.close();
        } catch (Exception e) {
            ex = e;
        }
        t.commit();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            c.close();
            if (ex != null) {
                throw ex;
            }
        } finally {
            super.tearDown();
        }
    }

    protected void checkReadBlob(String name) throws Exception {
        PreparedStatement p = c.prepareStatement("select * from " + name + " where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            if (log != null) log.info("C1: " + rs.getInt(1));
            Blob blobRead = rs.getBlob(2);
            InputStream is = blobRead.getBinaryStream();
            int count = 0;
            while (is.read() != -1) {
                count++;
            }
            if (log != null) log.info("C2 count: " + count);
            assertEquals("retrieved wrong length blob: expecting " + bloblength + ", retrieved: " + count, bloblength, count);
        }
        p.close();
    }


    public void testUseBlob() throws Exception {
        setupTable("T1");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        Blob blob = c.createBlob();
        OutputStream os = blob.setBinaryStream(1);
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        os.write(testbuf);
        os.close();

        p.setInt(1, 1);
        p.setBlob(2, blob);
        assertEquals("executeUpdate count != 1", 1, p.executeUpdate());

        p.close();
        checkReadBlob("T1");

        t.commit();
    }

    public void testUseBlobViapsSetBinaryStream() throws Exception {
        setupTable("T2");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T2 values (?, ?)");
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        InputStream bais = new ByteArrayInputStream(testbuf);
        p.setBinaryStream(2, bais, bloblength);

        p.setInt(1, 1);
        assertEquals("executeUpdate count != 1", 1, p.executeUpdate());

        p.close();
        checkReadBlob("T2");
        t.commit();
    }

    public void testUseBlobViapsSetBytes() throws Exception {
        setupTable("T3");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T3 values (?, ?)");
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[bloblength];
        Arrays.fill(testbuf, a[0]);
        p.setBytes(2, testbuf);

        p.setInt(1, 1);
        assertEquals("executeUpdate count != 1", 1, p.executeUpdate());

        p.close();
        checkReadBlob("T3");
        t.commit();
    }
}
