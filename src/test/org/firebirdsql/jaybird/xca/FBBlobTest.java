/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(FBBlobTest.class);

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
            log.info("C1: " + rs.getInt(1));
            Blob blobRead = rs.getBlob(2);
            InputStream is = blobRead.getBinaryStream();
            int count = 0;
            while (is.read() != -1) {
                count++;
            }
            log.info("C2 count: " + count);
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
