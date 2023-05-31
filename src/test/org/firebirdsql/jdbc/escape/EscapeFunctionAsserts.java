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
package org.firebirdsql.jdbc.escape;

import org.junit.jupiter.api.function.Executable;

import java.sql.SQLException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Shared asserts for JDBC escape functions.
 *
 * @author Mark Rotteveel
 * @since 5
 */
class EscapeFunctionAsserts {

    private EscapeFunctionAsserts() {
        // no instances
    }

    /**
     * Asserts the {@code executable} throws a {@link FBSQLParseException} with {@code expectedMessage}.
     *
     * @param executable
     *         Executable expected to throw a {@link FBSQLParseException}
     * @param expectedMessage
     *         Expected message
     */
    static void assertParseException(Executable executable, String expectedMessage) {
        SQLException exception = assertThrows(FBSQLParseException.class, executable);
        assertThat(exception, message(equalTo(expectedMessage)));
    }

}
