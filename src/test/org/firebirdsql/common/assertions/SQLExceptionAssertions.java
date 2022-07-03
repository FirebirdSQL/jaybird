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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
