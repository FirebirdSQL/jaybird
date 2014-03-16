package org.firebirdsql.common.matchers;

import org.firebirdsql.gds.GDSExceptionHelper;
import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;

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
     * @param errorCode The expected error code
     * @return The Matcher
     */
    @Factory
    public static Matcher<SQLException> errorCodeEquals(final int errorCode) {
        return errorCode(equalTo(errorCode));
    }

    /**
     * Matcher for the value of {@link java.sql.SQLException#getErrorCode()}.
     *
     * @param matcher The matcher for the error code
     * @return The Matcher
     */
    @Factory
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
     * @param sqlState The expected SQL State
     * @return The Matcher
     * @see #sqlState(org.hamcrest.Matcher)
     */
    @Factory
    public static Matcher<SQLException> sqlStateEquals(final String sqlState) {
        return sqlState(equalTo(sqlState));
    }

    /**
     * Matcher for the value of {@link java.sql.SQLException#getSQLState()}.
     *
     * @param matcher Matcher for the value of the SQL State
     * @return The Matcher
     */
    @Factory
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
     * @param matcher Matcher for the exception message
     * @return The Matcher
     */
    @Factory
    public static Matcher<Exception> message(final Matcher<String> matcher) {
        return new FeatureMatcher<Exception, String>(matcher, "exception message", "exception message") {
            @Override
            protected String featureValueOf(Exception e) {
                return e.getMessage();
            }
        };
    }

    /**
     * Convenience factory for matcher that checks a Firebird exception message based on its error code
     * and message parameters.
     * <p>
     * This matcher does not check the error code itself, it just constructs and checks the message looked up
     * with the error code and populated with the parameters
     * </p>
     *
     * @param fbErrorCode The Firebird error code, see {@link org.firebirdsql.gds.ISCConstants}
     * @param messageParameters The message parameters
     * @return The Matcher
     */
    @Factory
    public static Matcher<Exception> fbMessageEquals(int fbErrorCode, String... messageParameters) {
        GDSExceptionHelper.GDSMessage message = GDSExceptionHelper.getMessage(fbErrorCode);
        message.setParameters(Arrays.asList(messageParameters));
        return message(equalTo(message.toString()));
    }
}
