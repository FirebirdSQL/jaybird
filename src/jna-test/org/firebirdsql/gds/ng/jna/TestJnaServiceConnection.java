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
import org.firebirdsql.gds.ng.FbService;
import org.firebirdsql.gds.ng.FbServiceProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.firebirdsql.common.FBTestProperties.DB_PASSWORD;
import static org.firebirdsql.common.FBTestProperties.DB_USER;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestJnaServiceConnection {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsNativeOnly();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbServiceProperties connectionInfo;
    {
        connectionInfo = new FbServiceProperties();
        connectionInfo.setServerName(FBTestProperties.DB_SERVER_URL);
        connectionInfo.setPortNumber(FBTestProperties.DB_SERVER_PORT);
        connectionInfo.setUser(DB_USER);
        connectionInfo.setPassword(DB_PASSWORD);
    }

    @Test
    public void construct_clientLibraryNull_IllegalArgument() throws Exception {
        expectedException.expect(NullPointerException.class);

        new JnaServiceConnection(null, connectionInfo);
    }

    @Test
    public void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        JnaServiceConnection connection = new JnaServiceConnection(clientLibrary, connectionInfo);

        assertSame("Expected returned client library to be identical", clientLibrary, connection.getClientLibrary());
    }

    @Test
    public void identify_unconnected() throws Exception {
        JnaServiceConnection connection = new JnaServiceConnection(factory.getClientLibrary(), connectionInfo);

        FbService db = connection.identify();

        assertFalse("Expected isAttached() to return false", db.isAttached());
        assertThat("Expected zero-valued connection handle", db.getHandle(), equalTo(0));
        assertNull("Expected version string to be null", db.getServerVersion());
        assertNull("Expected version should be null", db.getServerVersion());
    }
}
