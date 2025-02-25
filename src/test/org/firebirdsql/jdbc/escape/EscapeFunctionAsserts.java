// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
