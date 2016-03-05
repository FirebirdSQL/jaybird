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
package org.firebirdsql.common.matchers;

import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.jdbc.FBPreparedStatement;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.*;

/**
 * Factory for {@link org.hamcrest.Matcher} instances for testing {@link java.sql.SQLException} information.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class SQLExceptionMatchers {

    /**
     * Matcher for the value of {@link java.sql.SQLException#getErrorCode()}.
     *
     * @param errorCode
     *         The expected error code
     * @return The Matcher
     */
    public static Matcher<SQLException> errorCodeEquals(final int errorCode) {
        return errorCode(equalTo(errorCode));
    }

    /**
     * Matcher for the value of {@link java.sql.SQLException#getErrorCode()}.
     *
     * @param matcher
     *         The matcher for the error code
     * @return The Matcher
     */
    public static Matcher<SQLException> errorCode(final Matcher<Integer> matcher) {
        return new FeatureMatcher<SQLException, Integer>(matcher, "error code", "error code") {
            @Override
            protected Integer featureValueOf(SQLException e) {
                return e.getErrorCode();
            }
        };
    }

    /**
     * Matcher for the value of {@link java.sql.SQLException#getSQLState()}.
     * <p>
     * Valid SQL States are 5 characters in length, this matcher does not enforce this.
     * </p>
     *
     * @param sqlState
     *         The expected SQL State
     * @return The Matcher
     * @see #sqlState(org.hamcrest.Matcher)
     */
    public static Matcher<SQLException> sqlStateEquals(final String sqlState) {
        return sqlState(equalTo(sqlState));
    }

    /**
     * Matcher for the value of {@link java.sql.SQLException#getSQLState()}.
     *
     * @param matcher
     *         Matcher for the value of the SQL State
     * @return The Matcher
     */
    public static Matcher<SQLException> sqlState(final Matcher<String> matcher) {
        return new FeatureMatcher<SQLException, String>(matcher, "SQL state", "SQL state") {
            @Override
            protected String featureValueOf(SQLException e) {
                return e.getSQLState();
            }
        };
    }

    /**
     * Matcher for the message of an Exception.
     *
     * @param matcher
     *         Matcher for the exception message
     * @return The Matcher
     */
    public static Matcher<Exception> message(final Matcher<String> matcher) {
        return new FeatureMatcher<Exception, String>(matcher, "exception message", "exception message") {
            @Override
            protected String featureValueOf(Exception e) {
                return e.getMessage();
            }
        };
    }

    /**
     * Convenience factory for matcher that checks a Firebird exception message for an exact match based on its error
     * code and message parameters.
     * <p>
     * This matcher does not check the error code itself, it just constructs and checks the message looked up
     * with the error code and populated with the parameters. The exception message must match exactly (string equals).
     * </p>
     *
     * @param fbErrorCode
     *         The Firebird error code, see {@link org.firebirdsql.gds.ISCConstants}
     * @param messageParameters
     *         The message parameters
     * @return The Matcher
     */
    public static Matcher<Exception> fbMessageEquals(int fbErrorCode, String... messageParameters) {
        return message(equalTo(getFbMessage(fbErrorCode, messageParameters)));
    }

    /**
     * Convenience factory for matcher that checks a Firebird exception message for an exact prefix match based on its
     * error code and message parameters.
     * <p>
     * This matcher does not check the error code itself, it just constructs and checks the message looked up
     * with the error code and populated with the parameters.
     * </p>
     *
     * @param fbErrorCode
     *         The Firebird error code, see {@link org.firebirdsql.gds.ISCConstants}
     * @param messageParameters
     *         The message parameters
     * @return The Matcher
     */
    public static Matcher<Exception> fbMessageStartsWith(int fbErrorCode, String... messageParameters) {
        return message(startsWith(getFbMessage(fbErrorCode, messageParameters)));
    }

    /**
     * Creates a Firebird exception message based on its error code and message parameters.
     *
     * @param fbErrorCode
     *         The Firebird error code, see {@link org.firebirdsql.gds.ISCConstants}
     * @param messageParameters
     *         The message parameters
     * @return The message
     */
    public static String getFbMessage(int fbErrorCode, String... messageParameters) {
        GDSExceptionHelper.GDSMessage message = GDSExceptionHelper.getMessage(fbErrorCode);
        message.setParameters(Arrays.asList(messageParameters));
        return message.toString();
    }

    /**
     * Convenience factory for matcher of the statement closed exception thrown by {@link org.firebirdsql.jdbc.FBStatement}
     * and descendants when the statement is closed.
     *
     * @return The Matcher
     */
    public static Matcher<SQLException> fbStatementClosedException() {
        return allOf(
                isA(SQLException.class),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID)),
                message(equalTo("Statement is already closed."))
        );
    }

    /**
     * Convenience factory for matcher of SQLException with the message {@link org.firebirdsql.jdbc.FBPreparedStatement#METHOD_NOT_SUPPORTED}
     * thrown when one of the <code>execute*(String)</code> methods of {@link java.sql.Statement} is called on
     * a {@link java.sql.PreparedStatement} or {@link java.sql.CallableStatement}.
     *
     * @return The Matcher
     */
    public static Matcher<SQLException> fbStatementOnlyMethodException() {
        return allOf(
                isA(SQLException.class),
                sqlState(equalTo(SQLStateConstants.SQL_STATE_GENERAL_ERROR)),
                message(equalTo(FBPreparedStatement.METHOD_NOT_SUPPORTED))
        );
    }
}
