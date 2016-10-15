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
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLNonTransientConnectionException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * Tests for parsing done in {@link DbAttachInfo}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestDbAttachInfo {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldParseNewFormat_full() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("//myserver.here:13050/path/to/db");

        assertEquals("myserver.here", dbAttachInfo.getServer());
        assertEquals(13050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseNewFormat_noPort() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("//myserver.here/path/to/db");

        assertEquals("myserver.here", dbAttachInfo.getServer());
        assertEquals(3050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseOldFormat_full() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("myserver.here/13050:path/to/db");

        assertEquals("myserver.here", dbAttachInfo.getServer());
        assertEquals(13050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseOldFormat_noPort() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("myserver.here:path/to/db");

        assertEquals("myserver.here", dbAttachInfo.getServer());
        assertEquals(3050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseFileOnly() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("/path/to/db");

        assertEquals("localhost", dbAttachInfo.getServer());
        assertEquals(3050, dbAttachInfo.getPort());
        assertEquals("/path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseIPv6_noPort() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("//[::1]/path/to/db");

        assertEquals("::1", dbAttachInfo.getServer());
        assertEquals(3050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void shouldParseIPv6_withPort() throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString("//[::192.9.5.5]:13050/path/to/db");

        assertEquals("::192.9.5.5", dbAttachInfo.getServer());
        assertEquals(13050, dbAttachInfo.getPort());
        assertEquals("path/to/db", dbAttachInfo.getFileName());
    }

    @Test
    public void testInvalidConnectionUrl_oldFormat_hostSeparatorAtStart() throws Exception {
        checkInvalidUrl(":localhost/3050:c:/data/db/test.fdb",
                "Host separator: ':' at beginning or end");
    }

    @Test
    public void testInvalidConnectionUrl_newFormat_hostSeparatorAtStart() throws Exception {
        checkInvalidUrl("///localhost:3050/c:/data/db/test.fdb",
                "Host separator: '/' at beginning or end");
    }

    @Test
    public void testInvalidConnectionUrl_oldFormat_portSeparatorAtStart() throws Exception {
        checkInvalidUrl("/localhost/3050:c:/data/db/test.fdb",
                "Port separator: '/' at beginning or end");
    }

    @Test
    public void testInvalidConnectionUrl_newFormat_portSeparatorAtStart() throws Exception {
        checkInvalidUrl("//:localhost:3050/c:/data/db/test.fdb",
                "Port separator: ':' at beginning or end");
    }

    @Test
    public void testInvalidConnectionUrl_oldFormat_nonNumericPort() throws Exception {
        checkInvalidUrl("localhost/c:/data/db/test.fdb", "Bad port: 'c' is not a number");
    }

    @Test
    public void testInvalidConnectionUrl_newFormat_nonNumericPort() throws Exception {
        checkInvalidUrl("//localhost:c:/data/db/test.fdb", "Bad port: 'c:' is not a number");
    }

    @Test
    public void testInvalidConnectionUrl_newFormat_missingDatabaseName() throws Exception {
        checkInvalidUrl("//localhost", "null or empty database name in connection string");
    }

    @Test
    public void testInvalidConnectionUrl_ipv6_unclosed() throws Exception {
        checkInvalidUrl("//[::1/c:/data/db/test.fdb", "IPv6 address expected, missing closing ']'");
    }

    @Test
    public void testInvalidConnectionUrl_ipv6_invalidTokensAfterAddress() throws Exception {
        checkInvalidUrl("//[::1]xyz/c:/data/db/test.fdb", "Unexpected tokens 'xyz' after IPv6 address");
    }

    @Test
    public void testInvalidConnectionUrl_null() throws Exception {
        checkInvalidUrl(null, "Connection string is missing");
    }

    private void checkInvalidUrl(String invalidUrl, String expectedExceptionMessageSubString) throws Exception {
        expectedException.expect(SQLNonTransientConnectionException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_invalidConnectionString),
                message(containsString(expectedExceptionMessageSubString))));

        DbAttachInfo.parseConnectString(invalidUrl);
    }

}