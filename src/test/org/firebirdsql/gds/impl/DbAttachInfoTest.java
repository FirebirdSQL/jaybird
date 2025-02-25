// SPDX-FileCopyrightText: Copyright 2021-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLNonTransientConnectionException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for parsing done in {@link DbAttachInfo}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class DbAttachInfoTest {

    @ParameterizedTest
    @MethodSource
    void parseConnectString(String url, String expectedServer, int expectedPort, String expectedFileName)
            throws Exception {
        DbAttachInfo dbAttachInfo = DbAttachInfo.parseConnectString(url);

        assertEquals(expectedServer, dbAttachInfo.serverName());
        assertEquals(expectedPort, dbAttachInfo.portNumber());
        assertEquals(expectedFileName, dbAttachInfo.attachObjectName());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> parseConnectString() {
        return Stream.of(
                Arguments.of("//myserver.here:13050/path/to/db", "myserver.here", 13050, "path/to/db"),
                Arguments.of("//myserver.here/path/to/db", "myserver.here", 3050, "path/to/db"),
                Arguments.of("myserver.here/13050:path/to/db", "myserver.here", 13050, "path/to/db"),
                Arguments.of("myserver.here:path/to/db", "myserver.here", 3050, "path/to/db"),
                Arguments.of("path/to/db", null, 3050, "path/to/db"),
                Arguments.of("//[::1]/path/to/db", "::1", 3050, "path/to/db"),
                Arguments.of("//[::1]:13050/path/to/db", "::1", 13050, "path/to/db"),
                Arguments.of("//localhost/", "localhost", 3050, null),
                Arguments.of("//localhost", "localhost", 3050, null),
                Arguments.of("//localhost:13050/", "localhost", 13050, null),
                Arguments.of("//localhost:13050", "localhost", 13050, null),
                Arguments.of("//localhost", "localhost", 3050, null),
                Arguments.of("C:\\path\\to\\db", null, 3050, "C:\\path\\to\\db"),
                Arguments.of("d:/path/to/db", null, 3050, "d:/path/to/db"),
                Arguments.of("//", null, 3050, null),
                Arguments.of("", null, 3050, null),
                Arguments.of("///c:/data/db/test.fdb", null, 3050, "c:/data/db/test.fdb")
        );
    }

    @ParameterizedTest
    @MethodSource
    void checkInvalidUrl(String invalidUrl, String expectedExceptionMessageSubString) {
        assertThatExceptionOfType(SQLNonTransientConnectionException.class)
                .isThrownBy(() -> DbAttachInfo.parseConnectString(invalidUrl))
                .withMessageContaining(expectedExceptionMessageSubString)
                .satisfies(e -> assertEquals(JaybirdErrorCodes.jb_invalidConnectionString, e.getErrorCode()));
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> checkInvalidUrl() {
        return Stream.of(
                Arguments.of(":localhost/3050:c:/data/db/test.fdb", "Path separator ':' at beginning"),
                Arguments.of("/localhost/3050:c:/data/db/test.fdb", "Port separator '/' at beginning or end"),
                Arguments.of("//:localhost:3050/c:/data/db/test.fdb", "Port separator ':' at beginning or end"),
                Arguments.of("localhost/c:/data/db/test.fdb", "Bad port: 'c' is not a number"),
                Arguments.of("//localhost:c:/data/db/test.fdb", "Bad port: 'c:' is not a number"),
                Arguments.of("//[::1/c:/data/db/test.fdb", "IPv6 address expected, missing closing ']'"),
                Arguments.of("//[::1]xyz/c:/data/db/test.fdb", "Unexpected tokens 'xyz/c:/data/db/test.fdb' after IPv6 address"),
                Arguments.of(null, "Connection string is missing"),
                Arguments.of("//127.0.0.1:3050://C:/db/example.FDB", "Bad port: '3050:' is not a number")
        );
    }

}