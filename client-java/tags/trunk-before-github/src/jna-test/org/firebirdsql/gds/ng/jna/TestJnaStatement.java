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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.ng.AbstractStatementTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.junit.BeforeClass;

import java.sql.SQLException;

/**
 * Tests for JNA statement.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaStatement extends AbstractStatementTest {

    private final FbClientDatabaseFactory factory = new FbClientDatabaseFactory();

    @BeforeClass
    public static void verifyTestType() {
        // Test is for native
        // TODO assumeTrue(FBTestProperties.getGdsType().toString().equals(NativeGDSImpl.NATIVE_TYPE_NAME));
    }

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return JnaDatabase.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }
}
