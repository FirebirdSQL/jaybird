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
import org.junit.Test;

import java.sql.Connection;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link org.firebirdsql.jdbc.FBBlobOutputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestFBBlobOutputStream extends FBJUnit4TestBase {

    @Test
    public void testLength() throws Exception {
        Connection conn = getConnectionViaDriverManager();
        try {
            // TODO Required because of JDBC-348
            conn.setAutoCommit(false);
            FBBlob blob = (FBBlob) conn.createBlob();
            FBBlobOutputStream stream = (FBBlobOutputStream) blob.setBinaryStream(1);

            stream.write(new byte[]{ 1, 2, 3, 4 });
            assertEquals(4, stream.length());
        } finally {
            conn.close();
        }
    }
}
