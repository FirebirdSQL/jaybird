// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common.assertions;

import org.assertj.core.util.CanIgnoreReturnValue;
import org.junit.jupiter.api.function.Executable;

import java.sql.SQLException;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbStatementClosedException;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbStatementOnlyMethodException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Common assertions for SQLException.
 *
 * @author Mark Rotteveel
 */
public class SQLExceptionAssertions {

    /**
     * Assert that {@code executable} throws a statement closed exception.
     *
     * @param executable
     *         Executable that should trigger the statement closed exception
     * @return the exception for further assertions
     */
    @CanIgnoreReturnValue
    public static SQLException assertThrowsFbStatementClosed(Executable executable) {
        SQLException exception = assertThrows(SQLException.class, executable);
        assertThat(exception, fbStatementClosedException());
        return exception;
    }

    /**
     * Assert that {@code executable} throws a statement only exception (meaning that a method is only supported on
     * {@code Statement}, not {@code PreparedStatement} or {@code CallableStatement}).
     *
     * @param executable
     *         Executable that should trigger the statement closed exception
     * @return the exception for further assertions
     */
    @CanIgnoreReturnValue
    public static SQLException assertThrowsFbStatementOnlyMethod(Executable executable) {
        SQLException exception = assertThrows(SQLException.class, executable);
        assertThat(exception, fbStatementOnlyMethodException());
        return exception;
    }

}
