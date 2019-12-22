/*
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

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ng.AbstractStatementTimeoutTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.util.Unstable;
import org.junit.ClassRule;

import java.sql.SQLException;

/**
 * Tests for statement timeouts with JNA statement.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@Unstable("Will fail when the test is run with a Firebird 3 or earlier fbclient")
public class JnaStatementTimeoutTest extends AbstractStatementTimeoutTest {

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return JnaDatabase.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }
}
