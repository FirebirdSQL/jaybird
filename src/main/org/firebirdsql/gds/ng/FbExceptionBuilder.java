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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.gds.ng.wire.crypt.FBSQLEncryptException;
import org.firebirdsql.jaybird.util.SQLExceptionChainBuilder;
import org.firebirdsql.jdbc.FBSQLExceptionInfo;
import org.firebirdsql.jdbc.SQLStateConstants;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptAlgorithmNotAvailable;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptInvalidKey;
import static org.firebirdsql.gds.JaybirdErrorCodes.jb_cryptNoCryptKeyAvailable;

/**
 * Builder for exceptions received from Firebird.
 * <p>
 * This class is not thread-safe.
 * </p>
 *
 * @author Mark Rotteveel
 */
public final class FbExceptionBuilder {

    private static final String SQLSTATE_FEATURE_NOT_SUPPORTED_PREFIX = "0A";
    private static final String SQLSTATE_SYNTAX_ERROR_PREFIX = "42";
    private static final String SQLSTATE_CONNECTION_ERROR_PREFIX = "08";

    private final List<ExceptionInformation> exceptionInfo = new ArrayList<>();
    private ExceptionInformation current;

    public FbExceptionBuilder() {
    }

    private FbExceptionBuilder(Type type, int errorCode) {
        setNextExceptionInformation(type, errorCode);
    }

    /**
     * The (next) exception is an exception.
     * <p>
     * This method and related methods can be called multiple times. This
     * builder might produce a chained exception, but could also merge exceptions
     * depending on the error code and other rules internal to this builder.
     * </p>
     *
     * @param errorCode
     *         Firebird error code
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
     * Equivalent to calling: {@code new FbExceptionBuilder().exception(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     */
    public static FbExceptionBuilder forException(int errorCode) {
        return new FbExceptionBuilder(Type.EXCEPTION, errorCode);
    }

    /**
     * Creates an exception builder for timeout exceptions with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().timeoutException(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     * @since 6
     */
    public static FbExceptionBuilder forTimeoutException(int errorCode) {
        return new FbExceptionBuilder(Type.TIMEOUT, errorCode);
    }

    /**
     * Creates an exception builder for non-transient exceptions with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().nonTransientException(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     * @since 6
     */
    public static FbExceptionBuilder forNonTransientException(int errorCode) {
        return new FbExceptionBuilder(Type.NON_TRANSIENT, errorCode);
    }

    /**
     * Creates an exception builder for non-transient connection exceptions with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().nonTransientConnectionException(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     * @since 6
     */
    public static FbExceptionBuilder forNonTransientConnectionException(int errorCode) {
        return new FbExceptionBuilder(Type.NON_TRANSIENT_CONNECT, errorCode);
    }

    /**
     * Creates an exception builder for transient exceptions with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().transientException(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     * @since 6
     */
    public static FbExceptionBuilder forTransientException(int errorCode) {
        return new FbExceptionBuilder(Type.TRANSIENT, errorCode);
    }

    /**
     * Creates an exception builder for a warning with the specified error code.
     * <p>
     * Equivalent to calling: {@code new FbExceptionBuilder().warning(errorCode); }
     * </p>
     *
     * @param errorCode
     *         Firebird error code
     * @return FbExceptionBuilder initialized with the specified error code
     */
    public static FbExceptionBuilder forWarning(int errorCode) {
        return new FbExceptionBuilder(Type.WARNING, errorCode);
    }

    /**
     * Creates an I/O write error ({@link org.firebirdsql.gds.ISCConstants#isc_net_write_err}).
     *
     * @param e
     *         exception cause
     * @return SQLException instance
     * @since 6
     */
    public static SQLException ioWriteError(IOException e) {
        class Holder {
            // Cache message and SQLSTATE to avoid constructing through builder every time
            private static final CachedMessage IO_WRITE_ERROR = CachedMessage.of(isc_net_write_err);
        }
        return new SQLNonTransientConnectionException(
                Holder.IO_WRITE_ERROR.message, Holder.IO_WRITE_ERROR.sqlState, Holder.IO_WRITE_ERROR.errorCode, e);
    }

    /**
     * Creates an I/O write error ({@link org.firebirdsql.gds.ISCConstants#isc_net_read_err}).
     *
     * @param e
     *         exception cause
     * @return SQLException instance
     * @since 6
     */
    public static SQLException ioReadError(IOException e) {
        class Holder {
            // Cache message and SQLSTATE to avoid constructing through builder every time
            private static final CachedMessage IO_READ_ERROR = CachedMessage.of(isc_net_read_err);
        }
        return new SQLNonTransientConnectionException(
                Holder.IO_READ_ERROR.message, Holder.IO_READ_ERROR.sqlState, Holder.IO_READ_ERROR.errorCode, e);
    }

    /**
     * The (next) exception is a warning.
     *
     * @param errorCode
     *         Firebird error code
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
     *         Firebird error code
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
     *         Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    @SuppressWarnings("unused")
    public FbExceptionBuilder nonTransientException(int errorCode) {
        setNextExceptionInformation(Type.NON_TRANSIENT, errorCode);
        return this;
    }

    /**
     * Force the next exception to be a {@link java.sql.SQLNonTransientConnectionException}.
     *
     * @param errorCode
     *         Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    @SuppressWarnings("unused")
    public FbExceptionBuilder nonTransientConnectionException(int errorCode) {
        setNextExceptionInformation(Type.NON_TRANSIENT_CONNECT, errorCode);
        return this;
    }

    /**
     * Force the next exception to be a {@link java.sql.SQLTransientException}.
     *
     * @param errorCode
     *         Firebird error code
     * @return this FbExceptionBuilder
     * @see #exception(int)
     */
    @SuppressWarnings("unused")
    public FbExceptionBuilder transientException(int errorCode) {
        setNextExceptionInformation(Type.TRANSIENT, errorCode);
        return this;
    }

    /**
     * Adds an integer message parameter for the exception message.
     *
     * @param parameter
     *         message parameter
     * @return this FbExceptionBuilder
     */
    public FbExceptionBuilder messageParameter(int parameter) {
        return messageParameter(Integer.toString(parameter));
    }

    /**
     * Adds two integer message parameters for the exception message.
     *
     * @param param1
     *         message parameter
     * @param param2
     *         message parameter
     * @return this FbExceptionBuilder
     * @since 5
     */
    public FbExceptionBuilder messageParameter(int param1, int param2) {
        return messageParameter(Integer.toString(param1), Integer.toString(param2));
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
     * Adds two string message parameters for the exception message.
     *
     * @param param1
     *         message parameter
     * @param param2
     *         message parameter
     * @return this FbExceptionBuilder
     * @since 5
     */
    public FbExceptionBuilder messageParameter(String param1, String param2) {
        checkExceptionInformation();
        current.addMessageParameter(param1);
        current.addMessageParameter(param2);
        return this;
    }

    /**
     * Adds an object message parameter for the exception message (applying {@code String.valueOf(parameter)}).
     *
     * @param parameter
     *         message parameter
     * @return this FbExceptionBuilder
     * @since 5
     */
    public FbExceptionBuilder messageParameter(Object parameter) {
        return messageParameter(String.valueOf(parameter));
    }

    /**
     * Adds two object message parameters for the exception message (applying {@code String.valueOf(parameter)}).
     *
     * @param param1
     *         message parameter
     * @param param2
     *         message parameter
     * @return this FbExceptionBuilder
     * @since 5
     */
    public FbExceptionBuilder messageParameter(Object param1, Object param2) {
        return messageParameter(String.valueOf(param1), String.valueOf(param2));
    }

    /**
     * Adds object message parameters for the exception message (applying {@code String.valueOf(parameter)}).
     *
     * @param params
     *         message parameters
     * @return this FbExceptionBuilder
     * @since 5
     */
    public FbExceptionBuilder messageParameter(Object... params) {
        checkExceptionInformation();
        for (int idx = 0; idx < params.length; idx++) {
            current.addMessageParameter(String.valueOf(params[idx]));
        }
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
     * <p>
     * If {@link #isEmpty()} returns {@code false}, then this will throw an {@link IllegalStateException}.
     * </p>
     *
     * @return SQLException object
     * @see #toFlatSQLException()
     */
    public SQLException toSQLException() {
        checkNonEmpty();
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        for (ExceptionInformation info : exceptionInfo) {
            chain.append(info.toSQLException());
        }
        return chain.getException();
    }

    /**
     * Set of uninteresting error codes.
     * <p>
     * This is used by {@link #toFlatSQLException()} to find a more suitable error code.
     * </p>
     */
    private static final Set<Integer> UNINTERESTING_ERROR_CODES = Set.of(0, isc_dsql_error, isc_dsql_line_col_error,
            isc_dsql_unknown_pos, isc_sqlerr, isc_dsql_command_err, isc_arith_except, isc_cancelled);

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
     * <p>
     * If {@link #isEmpty()} returns {@code false}, then this will throw an {@link IllegalStateException}.
     * </p>
     *
     * @return SQLException object
     * @see org.firebirdsql.jdbc.FBSQLExceptionInfo
     */
    public SQLException toFlatSQLException() {
        checkNonEmpty();
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

    private void checkNonEmpty() {
        if (isEmpty()) {
            throw new IllegalStateException("No information available to build an SQLException");
        }
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
    @SuppressWarnings("unused")
    public <T extends SQLException> T toFlatSQLException(Class<T> type) throws ClassCastException {
        return type.cast(toFlatSQLException());
    }

    /**
     * @return {@code true} if this builder contains exception information, {@code false} otherwise
     */
    public boolean isEmpty() {
        return exceptionInfo.isEmpty();
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
        current = new ExceptionInformation(upgradeType(type, errorCode), errorCode);
        exceptionInfo.add(current);
    }

    /**
     * Checks if a more specific exception type is possible (known and compatible) for the specified error code.
     *
     * @param type Requested exception type
     * @param errorCode Error code
     * @return Upgrade exception type (e.g. {@code (EXCEPTION, isc_login)} will upgrade to {@code NON_TRANSIENT})
     */
    private static Type upgradeType(final Type type, final int errorCode) {
        enum TypeUpgrades {
            NON_TRANSIENT(isc_wirecrypt_incompatible, isc_miss_wirecrypt, isc_wirecrypt_key, isc_wirecrypt_plugin,
                    jb_cryptNoCryptKeyAvailable, jb_cryptAlgorithmNotAvailable, jb_cryptInvalidKey, isc_login,
                    isc_net_write_err, isc_net_read_err, isc_network_error),
            TIMEOUT(isc_cfg_stmt_timeout, isc_att_stmt_timeout, isc_req_stmt_timeout);

            private final int[] errorCodes;

            TypeUpgrades(int... errorCodes) {
                Arrays.sort(errorCodes);
                this.errorCodes = errorCodes;
            }

            boolean contains(int errorCode) {
                return Arrays.binarySearch(errorCodes, errorCode) >= 0;
            }
        }

        if (type == Type.EXCEPTION) {
            if (TypeUpgrades.NON_TRANSIENT.contains(errorCode)) {
                return Type.NON_TRANSIENT;
            }
            if (TypeUpgrades.TIMEOUT.contains(errorCode)) {
                return Type.TIMEOUT;
            }
        }
        return type;
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
         * Overrides the SQL state. By default, the SQL state is decided by the errorCode.
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
     * Caches the rendered message, SQLSTATE and error code of an exception; used for internal optimization purposes.
     *
     * @param message
     *         rendered message string
     * @param sqlState
     *         SQLSTATE
     * @param errorCode
     *         vendor error code
     * @since 6
     */
    private record CachedMessage(String message, String sqlState, int errorCode) {

        /**
         * Renders the exception using {@code #toSQLException} and stores the resulting message and SQLSTATE, and
         * {@code errorCode}.
         * <p>
         * Do not use
         * </p>
         *
         * @param errorCode Firebird/Jaybird error code
         * @return cached message with the message, SQLSTATE from the generated exception, and {@code errorCode}
         */
        private static CachedMessage of(int errorCode) {
            SQLException exception = forException(errorCode).toSQLException();
            return new CachedMessage(exception.getMessage(), exception.getSQLState(), errorCode);
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
                if (sqlState != null) {
                    if (sqlState.startsWith(SQLSTATE_FEATURE_NOT_SUPPORTED_PREFIX)) {
                        // Feature not supported by Firebird
                        return new SQLFeatureNotSupportedException(message, sqlState, errorCode);
                    } else if (sqlState.startsWith(SQLSTATE_SYNTAX_ERROR_PREFIX)) {
                        return new SQLSyntaxErrorException(message, sqlState, errorCode);
                    }
                    // TODO Add support for other SQLException types
                }

                return new SQLException(message, sqlState, errorCode);
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
         * Force builder to create an exception of {@link java.sql.SQLTimeoutException} or subclass
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
                // TODO We probably want these specific exception types also for 'normal' exceptions
                return switch (errorCode) {
                case isc_wirecrypt_incompatible, isc_miss_wirecrypt, isc_wirecrypt_key, isc_wirecrypt_plugin,
                        jb_cryptNoCryptKeyAvailable, jb_cryptAlgorithmNotAvailable, jb_cryptInvalidKey ->
                        new FBSQLEncryptException(message, sqlState, errorCode);
                case isc_login -> new SQLInvalidAuthorizationSpecException(message, sqlState, errorCode);
                default -> {
                    if (sqlState != null) {
                        if (sqlState.startsWith(SQLSTATE_SYNTAX_ERROR_PREFIX)) {
                            yield new SQLSyntaxErrorException(message, sqlState, errorCode);
                        } else if (sqlState.startsWith(SQLSTATE_CONNECTION_ERROR_PREFIX)) {
                            yield new SQLNonTransientConnectionException(message, sqlState, errorCode);
                        }
                    }
                    yield new SQLNonTransientException(message, sqlState, errorCode);
                }
                };
            }
        },
        /**
         * Force builder to create exception of {@link java.sql.SQLNonTransientConnectionException} or a subclass.
         */
        NON_TRANSIENT_CONNECT(SQLStateConstants.SQL_STATE_CONNECTION_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLNonTransientConnectionException(message, sqlState, errorCode);
            }
        },
        /**
         * Force build to create exception of {@link java.sql.SQLTransientException} or a subclass.
         */
        TRANSIENT(SQLStateConstants.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(String message, String sqlState, int errorCode) {
                return new SQLTransientException(message, sqlState, errorCode);
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
