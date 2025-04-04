/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2012-2023 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static org.firebirdsql.common.FBTestProperties.createDefaultMcf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FBBlobTest {

    private final System.Logger log = System.getLogger(FBBlobTest.class.getName());

    private static final int BLOB_LENGTH = 40960 * 10;

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    private FBConnection c;
    private FBLocalTransaction t;

    private void setupTable(String name) throws Exception {
        FBManagedConnectionFactory mcf = createDefaultMcf();
        DataSource ds = mcf.createConnectionFactory();
        c = (FBConnection) ds.getConnection();
        Statement s = c.createStatement();
        t = c.getLocalTransaction();
        t.begin();
        s.execute("CREATE TABLE " + name + " ( C1 INTEGER not null primary key, C2 BLOB)");
        t.commit();
    }

    @AfterEach
    void cleanUp() throws Exception {
        c.close();
    }

    private void checkReadBlob(String name) throws Exception {
        PreparedStatement p = c.prepareStatement("select * from " + name + " where C1 = ?");
        p.setInt(1, 1);
        ResultSet rs = p.executeQuery();
        while (rs.next()) {
            log.log(System.Logger.Level.DEBUG, "C1: {0}", rs.getInt(1));
            Blob blobRead = rs.getBlob(2);
            InputStream is = blobRead.getBinaryStream();
            int count = 0;
            while (is.read() != -1) {
                count++;
            }
            log.log(System.Logger.Level.DEBUG, "C2 count: {0}", count);
            assertEquals(BLOB_LENGTH, count,
                    "retrieved wrong length blob: expecting " + BLOB_LENGTH + ", retrieved: " + count);
        }
        p.close();
    }

    @Test
    void testUseBlob() throws Exception {
        setupTable("T1");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T1 values (?, ?)");
        Blob blob = c.createBlob();
        OutputStream os = blob.setBinaryStream(1);
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[BLOB_LENGTH];
        Arrays.fill(testbuf, a[0]);
        os.write(testbuf);
        os.close();

        p.setInt(1, 1);
        p.setBlob(2, blob);
        assertEquals(1, p.executeUpdate(), "executeUpdate count != 1");

        p.close();
        checkReadBlob("T1");

        t.commit();
    }

    @Test
    void testUseBlobViapsSetBinaryStream() throws Exception {
        setupTable("T2");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T2 values (?, ?)");
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[BLOB_LENGTH];
        Arrays.fill(testbuf, a[0]);
        InputStream bais = new ByteArrayInputStream(testbuf);
        p.setBinaryStream(2, bais, BLOB_LENGTH);

        p.setInt(1, 1);
        assertEquals(1, p.executeUpdate(), "executeUpdate count != 1");

        p.close();
        checkReadBlob("T2");
        t.commit();
    }

    @Test
    void testUseBlobViapsSetBytes() throws Exception {
        setupTable("T3");

        t.begin();
        PreparedStatement p = c.prepareStatement("insert into T3 values (?, ?)");
        byte[] a = "a".getBytes();
        byte[] testbuf = new byte[BLOB_LENGTH];
        Arrays.fill(testbuf, a[0]);
        p.setBytes(2, testbuf);

        p.setInt(1, 1);
        assertEquals(1, p.executeUpdate(), "executeUpdate count != 1");

        p.close();
        checkReadBlob("T3");
        t.commit();
    }

}
