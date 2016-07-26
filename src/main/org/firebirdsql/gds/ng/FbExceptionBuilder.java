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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.*;
import java.util.*;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Builder for exceptions received from Firebird.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public final class FbExceptionBuilder {

    private static final String SQLSTATE_FEATURE_NOT_SUPPORTED_PREFIX = "0A";
    private static final String SQLSTATE_SYNTAX_ERROR_PREFIX = "42";

    private final List<ExceptionInformation> exceptionInfo = new ArrayList<>();
    private ExceptionInformation current = null;

    /**
     * The (next) exception is an exception.
     * <p>
     * This method and related methods can be called multiple times. This
     * builder might produce a chained exception, but could also merge exceptions
     * depending on the error code and other rules internal to this builder.
     * </p>
     *
     * @param errorCode
     *         The Firebird error code
     * @return this FbExceptionBuilder
     * @see #warning(int)
     */
    public FbExceptionBuilder exception(int errorCode) {
        setNextExceptionInformation(Type.EXCEPTION, errorCode);
        return this;
    }

    /**
     * Creates an exception builder with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().error(errorCode); }
     * </p>
     *
     * @param errorCode
     *         The Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     */
    public static FbExceptionBuilder forException(int errorCode) {
        return new FbExceptionBuilder().exception(errorCode);
    }

    /**
     * Creates an exception builder for a warning with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().warning(errorCode); }
     * </p>
     *
     * @param errorCode
     *         The Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     */
    public static FbExceptionBuilder forWarning(int errorCode) {
        return new FbExceptionBuilder().warning(errorCode);
    }

    /**
     * The (next) exception is a warning.
     *
     * @param errorCode
     *         The Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    public FbExceptionBuilder warning(int errorCode) {
        setNextExceptionInformation(Type.WARNING, errorCode);
        return this;
    }

    /**
     * Force the next exception to be a {@link java.sql.SQLTimeoutException}.
     *
     * @param errorCode
     *         The Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    public FbExceptionBuilder timeoutException(int errorCode) {
        setNextExceptionInformation(Type.TIMEOUT, errorCode);
        return this;
    }

    /**
     * Force the next exception to be a {@link java.sql.SQLNonTransientException}.
     *
     * @param errorCode
     *         The Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    public FbExceptionBuilder nonTransientException(int errorCode) {
        setNextExceptionInformation(Type.NON_TRANSIENT, errorCode);
        return this;
    }

    /**
     * Force the next exception to be a {@link java.sql.SQLNonTransientConnectionException}.
     *
     * @param errorCode
     *         The Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    public FbExceptionBuilder nonTransientConnectionException(int errorCode) {
        setNextExceptionInformation(Type.NON_TRANSIENT_CONNECT, errorCode);
        return this;
    }

    /**
     * Adds an integer message parameter for the exception message.
     *
     * @param parameter
     *         Message parameter
     * @return this FbExceptionBuilder
     */
    public FbExceptionBuilder messageParameter(int parameter) {
        return messageParameter(Integer.toString(parameter));
    }

    /**
     * Adds a string message parameter for the exception message.
     *
     * @param parameter
     *         Message parameter
     * @return this FbExceptionBuilder
     */
    public FbExceptionBuilder messageParameter(String parameter) {
        checkExceptionInformation();
        current.addMessageParameter(parameter);
        return this;
    }

    /**
     * Sets the SQL state. Overriding the value derived from the Firebird error code.
     * <p>
     * SQL State is usually derived from the errorCode. Use of this
     * method is optional.
     * </p>
     *
     * @param sqlState
     *         SQL State value
     * @return this FbExceptionBuilder
     */
    public FbExceptionBuilder sqlState(String sqlState) {
        checkExceptionInformation();
        current.setSqlState(sqlState);
        return this;
    }

    /**
     * Sets the cause of the current exception.
     *
     * @param cause
     *         Throwable with the cause
     * @return this FbExceptionBuilder
     */
    public FbExceptionBuilder cause(Throwable cause) {
        checkExceptionInformation();
        current.setCause(cause);
        return this;
    }

    /**
     * Converts the builder to the appropriate SQLException instance (optionally with a chain of additional
     * exceptions).
     * <p>
     * When returning exception information from the status vector, it is advisable to use {@link #toFlatSQLException()}
     * as this applies some heuristics to get more specific error codes and flattens the message into a single
     * exception.
     * </p>
     *
     * @return SQLException object
     * @see #toFlatSQLException()
     */
    public SQLException toSQLException() {
        if (exceptionInfo.isEmpty()) return null;
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        for (ExceptionInformation info : exceptionInfo) {
            chain.append(info.toSQLException());
        }
        return chain.getException();
    }

    /**
     * Array of uninteresting error codes.
     */
    private static final Integer[] UNINTERESTING_ERROR_CODES_ARR =
            { 0, isc_dsql_error, isc_dsql_line_col_error, isc_dsql_unknown_pos, isc_sqlerr, isc_dsql_command_err,
                    isc_arith_except };

    /**
     * Set of uninteresting error codes derived from {@link #UNINTERESTING_ERROR_CODES_ARR}.
     * <p>
     * This is used by {@link #toFlatSQLException()} to find a more suitable error code.
     * </p>
     */
    private static final Set<Integer> UNINTERESTING_ERROR_CODES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(UNINTERESTING_ERROR_CODES_ARR)));

    /**
     * SQLState success is linked to some informational error message, we consider those 'not interesting' either.
     */
    private static final String SQLSTATE_SUCCESS = "00000";

    /**
     * Converts the builder to a single SQLException instance with a single exception message.
     * <p>
     * This method attempts to assign the most specific error code and SQL state to the returned exception.
     * </p>
     * <p>
     * The cause of the returned exception is set to an instance of {@link org.firebirdsql.jdbc.FBSQLExceptionInfo}
     * which contains the separate items obtained from the status vector. These items are chained together using
     * the SQLException chain.
     * </p>
     *
     * @return SQLException object
     * @see org.firebirdsql.jdbc.FBSQLExceptionInfo
     */
    public SQLException toFlatSQLException() {
        if (exceptionInfo.isEmpty()) return null;
        // We are recording the unflattened state if people need the details
        SQLExceptionChainBuilder<FBSQLExceptionInfo> chain = new SQLExceptionChainBuilder<>();
        StringBuilder fullExceptionMessage = new StringBuilder();
        ExceptionInformation interestingExceptionInfo = null;

        for (ExceptionInformation info : exceptionInfo) {
            if (interestingExceptionInfo == null
                    && !UNINTERESTING_ERROR_CODES.contains(info.errorCode)
                    && !SQLSTATE_SUCCESS.equals(info.sqlState)) {
                interestingExceptionInfo = info;
            }

            if (fullExceptionMessage.length() > 0) {
                fullExceptionMessage.append("; ");
            }
            fullExceptionMessage.append(info.toMessage());

            chain.append(info.toSQLExceptionInfo());
        }

        final ExceptionInformation firstExceptionInfo = exceptionInfo.get(0);
        if (interestingExceptionInfo == null) {
            interestingExceptionInfo = firstExceptionInfo;
        }

        fullExceptionMessage
                .append(" [SQLState:").append(interestingExceptionInfo.sqlState)
                .append(", ISC error code:").append(interestingExceptionInfo.errorCode)
                .append(']');

        /* If the type of the head of the chain is not Type.EXCEPTION we use that, not the type of the interesting
         * exception info as the head of the chain has been set explicitly to an expected exception type (eg Type.WARNING).
         */
        Type exceptionType = firstExceptionInfo.type != Type.EXCEPTION
                ? firstExceptionInfo.type
                : interestingExceptionInfo.type;
        SQLException exception = exceptionType.createSQLException(
                fullExceptionMessage.toString(), interestingExceptionInfo.sqlState, interestingExceptionInfo.errorCode);
        exception.initCause(chain.getException());
        return exception;
    }

    /**
     * Converts the builder to the appropriate SQLException instance (optionally with a chain of additional
     * exceptions) and casts to the specified type T.
     *
     * @param type
     *         Class of type T
     * @param <T>
     *         Expected exception type
     * @return SQLException of type T
     * @throws ClassCastException
     *         If the first exception created with this builder is not of the specified type
     * @see #toSQLException()
     */
    public <T extends SQLException> T toSQLException(Class<T> type) throws ClassCastException {
        return type.cast(toSQLException());
    }

    /**
     * Converts the builder to the appropriate SQLException instance and casts to the specified type T.
     *
     * @param type
     *         Class of type T
     * @param <T>
     *         Expected exception type
     * @return SQLException of type T
     * @throws ClassCastException
     *         If the first exception created with this builder is not of the specified type
     * @see #toFlatSQLException()
     */
    public <T extends SQLException> T toFlatSQLException(Class<T> type) throws ClassCastException {
        return type.cast(toFlatSQLException());
    }

    @Override
    public String toString() {
        if (current == null) return "empty";
        return exceptionInfo.toString();
    }

    /**
     * Sets the next ExceptionInformation object for the specified type.
     *
     * @param type
     *         Type of exception
     * @param errorCode
     *         The Firebird error code
     */
    private void setNextExceptionInformation(Type type, final int errorCode) {
        current = new ExceptionInformation(type, errorCode);
        exceptionInfo.add(current);
    }

    /**
     * Check if we have a current ExceptionInformation object.
     *
     * @throws IllegalStateException
     *         If current is null ({@link #warning(int)} or {@link #exception(int)} hasn't been called yet)
     */
    private void checkExceptionInformation() throws IllegalStateException {
        if (current == null) {
            throw new IllegalStateException("FbExceptionBuilder requires call to warning() or exception() first");
        }
    }

    private static final class ExceptionInformation {
        private final Type type;
        private final List<String> messageParameters = new ArrayList<>();
        private final int errorCode;
        private String sqlState;
        private Throwable cause;

        ExceptionInformation(Type type, int errorCode) {
            if (type == null) throw new IllegalArgumentException("type must not be null");
            this.type = type;
            this.errorCode = errorCode;
            sqlState = GDSExceptionHelper.getSQLState(errorCode, type.getDefaultSQLState());
        }

        /**
         * Overrides the SQL state. By default the SQL state is decided by the errorCode.
         *
         * @param sqlState
         *         New SQL state value
         * @throws IllegalArgumentException
         *         If sqlState is null or not 5 characters long
         */
        void setSqlState(String sqlState) {
            if (sqlState == null || sqlState.length() != 5) {
                throw new IllegalArgumentException("Value of sqlState must be a 5 character string");
            }
            this.sqlState = sqlState;
        }

        /**
         * Sets the cause of the exception.
         *
         * @param cause
         *         Cause of the exception
         */
        void setCause(Throwable cause) {
            this.cause = cause;
        }

        /**
         * Adds a message parameter.
         *
         * @param argument
         *         The value of the message parameter
         */
        void addMessageParameter(String argument) {
            messageParameters.add(argument);
        }

        /**
         * @return The list of message parameter values
         */
        List<String> getMessageParameters() {
            return Collections.unmodifiableList(messageParameters);
        }

        /**
         * @return The message string with the parameter substituted into the message.
         */
        String toMessage() {
            GDSExceptionHelper.GDSMessage gdsMessage = GDSExceptionHelper.getMessage(errorCode);
            gdsMessage.setParameters(getMessageParameters());
            return gdsMessage.toString();
        }

        /**
         * Converts this ExceptionInformation object into an SQLException
         *
         * @return SQLException
         */
        SQLException toSQLException() {
            String message = toMessage() + " [SQLState:" + sqlState + ", ISC error code:" + errorCode + ']';
            SQLException result = type.createSQLException(message, sqlState, errorCode);
            if (cause != null) {
                result.initCause(cause);
            }
            return result;
        }

        FBSQLExceptionInfo toSQLExceptionInfo() {
            FBSQLExceptionInfo result = new FBSQLExceptionInfo(toMessage(), sqlState, errorCode);
            if (cause != null) {
                result.initCause(cause);
            }
            return result;
        }

        @Override
        public String toString() {
            return "Type: " + type +
                    "; ErrorCode: " + errorCode +
                    "; Message: \"" + toMessage() + '"' +
                    "; SQLstate: " + sqlState +
                    "; MessageParameters: " + getMessageParameters() +
                    "; Cause: " + cause;
        }
    }

    /**
     * Type of exception.
     */
    private enum Type {
        /**
         * General {@link SQLException}, the actual type is determined by the builder.
         */
        EXCEPTION(SQLStateConstants.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                // TODO Replace with a list or chain of processors?
                if (sqlState != null && sqlState.startsWith(SQLSTATE_FEATURE_NOT_SUPPORTED_PREFIX)) {
                    // Feature not supported by Firebird
                    return new SQLFeatureNotSupportedException(message, sqlState, errorCode);
                } else if (sqlState != null && sqlState.startsWith(SQLSTATE_SYNTAX_ERROR_PREFIX)) {
                    return new SQLSyntaxErrorException(message, sqlState, errorCode);
                } else {
                    // TODO Add support for other SQLException types
                    return new SQLException(message, sqlState, errorCode);
                }
                // TODO If sqlState is 01xxx return SQLWarning any way?
            }
        },
        /**
         * Warning, exception created is of {@link SQLWarning} or a subclass
         */
        WARNING(SQLStateConstants.SQL_STATE_WARNING) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLWarning(message, sqlState, errorCode);
            }
        },
        /**
         * Force builder to create exception of {@link java.sql.SQLTimeoutException} or subclass
         */
        // TODO Specific default sqlstate for timeout?
        TIMEOUT(SQLStateConstants.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLTimeoutException(message, sqlState, errorCode);
            }
        },
        /**
         * Force builder to create exception of {@link java.sql.SQLNonTransientException}
         */
        NON_TRANSIENT(SQLStateConstants.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLNonTransientException(message, sqlState, errorCode);
            }
        },
        /**
         * Force builder to create exception of {@link java.sql.SQLNonTransientConnectionException}
         */
        NON_TRANSIENT_CONNECT(SQLStateConstants.SQL_STATE_CONNECTION_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLNonTransientConnectionException(message, sqlState, errorCode);
            }
        };

        private final String defaultSQLState;

        Type(String defaultSQLState) {
            this.defaultSQLState = defaultSQLState;
        }

        /**
         * The default SQL State for this type
         *
         * @return Default SQL State
         */
        public final String getDefaultSQLState() {
            return defaultSQLState;
        }

        /**
         * Creates an instance SQLException (or a subclass) based on this Type and additional rules based
         * on errorCode and/or SQLState.
         *
         * @param message
         *         The message text
         * @param sqlState
         *         The SQL state
         * @param errorCode
         *         The Firebird error code
         * @return Instance of SQLException (or a subclass).
         */
        public abstract SQLException createSQLException(String message, String sqlState, int errorCode);
    }
}
