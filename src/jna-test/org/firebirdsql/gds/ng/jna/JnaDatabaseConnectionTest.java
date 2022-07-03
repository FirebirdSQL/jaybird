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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class JnaDatabaseConnectionTest {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsNativeOnly();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    @Test
    public void construct_clientLibraryNull_IllegalArgument() throws Exception {
        expectedException.expect(NullPointerException.class);

        new JnaDatabaseConnection(null, connectionInfo);
    }

    @Test
    public void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        JnaDatabaseConnection connection = new JnaDatabaseConnection(clientLibrary, connectionInfo);

        assertSame("Expected returned client library to be identical", clientLibrary, connection.getClientLibrary());
    }

    @Test
    public void identify_unconnected() throws Exception {
        JnaDatabaseConnection connection = new JnaDatabaseConnection(factory.getClientLibrary(), connectionInfo);

        FbDatabase db = connection.identify();

        assertFalse("Expected isAttached() to return false", db.isAttached());
        assertThat("Expected zero-valued connection handle", db.getHandle(), equalTo(0));
        assertNull("Expected version string to be null", db.getServerVersion());
        assertNull("Expected version should be null", db.getServerVersion());
    }
}
