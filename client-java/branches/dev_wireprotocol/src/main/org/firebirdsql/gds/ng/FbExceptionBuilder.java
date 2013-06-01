/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.GDSExceptionHelper;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FBSQLWarning;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final List<ExceptionInformation> exceptionInfo = new ArrayList<ExceptionInformation>();
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

    public FbExceptionBuilder cause(Throwable cause) {
        checkExceptionInformation();
        current.setCause(cause);
        return this;
    }

    /**
     * Converts the builder to the appropriate SQLException instance (optionally with a chain of additional
     * exceptions).
     *
     * @return SQLException object
     */
    public SQLException toSQLException() {
        if (exceptionInfo.isEmpty()) return null;
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<SQLException>();
        for (ExceptionInformation info : exceptionInfo) {
            // TODO: Reorder or coalesce uninformative exceptions like those with error 335544569, see also JDBC-221
            // TODO: Reordering/coalesce may need to be part of setNextExceptionInformation instead
            chain.append(info.toSQLException());
        }
        return chain.getException();
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
        private final List<String> messageParameters = new ArrayList<String>();
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
            SQLException result = type.createSQLException(toMessage(), sqlState, errorCode);
            if (cause != null) {
                result.initCause(cause);
            }
            return result;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Type: ").append(type);
            sb.append("; ErrorCode: ").append(errorCode);
            sb.append("; Message: \"").append(toMessage()).append('"');
            sb.append("; SQLstate: ").append(sqlState);
            sb.append("; MessageParameters: ").append(getMessageParameters());
            sb.append("; Cause: ").append(cause);
            return sb.toString();
        }
    }

    /**
     * Type of exception.
     */
    private enum Type {
        /**
         * General {@link SQLException}, the actual type is determined by the builder.
         */
        EXCEPTION(FBSQLException.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                if (sqlState != null && sqlState.startsWith(SQLSTATE_FEATURE_NOT_SUPPORTED_PREFIX)) {
                    // Feature not supported by Firebird
                    return new SQLFeatureNotSupportedException(message, sqlState, errorCode);
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
        WARNING(FBSQLWarning.SQL_STATE_WARNING) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLWarning(message, sqlState, errorCode);
            }
        },
        /**
         * Force builder to create exception of {@link java.sql.SQLTimeoutException} or subclass
         */
        // TODO Specific default sqlstate for timeout?
        TIMEOUT(FBSQLException.SQL_STATE_GENERAL_ERROR) {
            @Override
            public SQLException createSQLException(final String message, final String sqlState, final int errorCode) {
                return new SQLTimeoutException(message, sqlState, errorCode);
            }
        };

        private final String defaultSQLState;

        private Type(String defaultSQLState) {
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
