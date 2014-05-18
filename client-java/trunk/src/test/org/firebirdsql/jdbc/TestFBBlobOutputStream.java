/*
 * $Id$
 *
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.jdbc;

import org.firebirdsql.common.FBJUnit4TestBase;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobOutputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlobOutputStream extends FBJUnit4TestBase {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private Connection conn;
    private FBBlobOutputStream stream;

    public void initDefault() throws Exception {
        conn = getConnectionViaDriverManager();
        // TODO Required because of JDBC-348
        conn.setAutoCommit(false);
        FBBlob blob = (FBBlob) conn.createBlob();
        stream = (FBBlobOutputStream) blob.setBinaryStream(1);
    }

    @After
    public void tearDown() throws Exception {
        if (conn != null) conn.close();
    }

    @Test
    public void testLength() throws Exception {
        initDefault();
        stream.write(new byte[]{ 1, 2, 3, 4 });
        assertEquals(4, stream.length());
    }

    @Test
    public void testWrite_byte_throwsException() throws Exception {
        initDefault();
        expectedException.expect(allOf(
                isA(IOException.class),
                message(equalTo("FBBlobOutputStream.write(int b) not implemented")))
        );

        stream.write(1);
    }
}
