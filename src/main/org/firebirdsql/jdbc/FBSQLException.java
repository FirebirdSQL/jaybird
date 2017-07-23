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
package org.firebirdsql.jdbc;

import java.sql.SQLException;

import javax.resource.ResourceException;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.jca.FBResourceException;
import org.firebirdsql.jca.FBXAException;

public class FBSQLException extends SQLException {

    private static final long serialVersionUID = 8157410954186424083L;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_CONN_ATTR = SQLStateConstants.SQL_STATE_INVALID_CONN_ATTR;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_NO_ROW_AVAIL = SQLStateConstants.SQL_STATE_NO_ROW_AVAIL;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_GENERAL_ERROR = SQLStateConstants.SQL_STATE_GENERAL_ERROR;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_COLUMN = SQLStateConstants.SQL_STATE_INVALID_COLUMN;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_ARG_VALUE = SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_OPTION_IDENTIFIER =
            SQLStateConstants.SQL_STATE_INVALID_OPTION_IDENTIFIER;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_PARAM_TYPE = SQLStateConstants.SQL_STATE_INVALID_PARAM_TYPE;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_WRONG_PARAM_NUM = SQLStateConstants.SQL_STATE_WRONG_PARAM_NUM;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_NO_RESULT_SET = SQLStateConstants.SQL_STATE_NO_RESULT_SET;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_CONVERSION = SQLStateConstants.SQL_STATE_INVALID_CONVERSION;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_TX_STATE = SQLStateConstants.SQL_STATE_INVALID_TX_STATE;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_STATEMENT_ID = SQLStateConstants.SQL_STATE_INVALID_STATEMENT_ID;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_CONNECTION_ERROR = SQLStateConstants.SQL_STATE_CONNECTION_ERROR;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_CONNECTION_CLOSED = SQLStateConstants.SQL_STATE_CONNECTION_CLOSED;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_CONNECTION_FAILURE = SQLStateConstants.SQL_STATE_CONNECTION_FAILURE;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_CONNECTION_FAILURE_IN_TX =
            SQLStateConstants.SQL_STATE_CONNECTION_FAILURE_IN_TX;
    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_COMM_LINK_FAILURE = SQLStateConstants.SQL_STATE_COMM_LINK_FAILURE;

    /**
     * @deprecated Use constants from {@link SQLStateConstants}. To be removed in Jaybird 4.
     */
    @Deprecated
    public static final String SQL_STATE_SYNTAX_ERROR = SQLStateConstants.SQL_STATE_SYNTAX_ERROR;

    public FBSQLException(Exception ex) {
        this("Exception. " + ex.getMessage());
        initCause(ex);
    }

    public FBSQLException(GDSException ex) {
        super(createGDSExceptionMessage(ex), defaultSQLStateIfNull(ex.getSQLState()), ex.getIntParam(), ex);
    }

    public FBSQLException(ResourceException ex) {
        super(createResourceMessage(ex), defaultSQLStateIfNull(ex.getErrorCode()), getSqlErrorCode(ex), resolveCause(ex));
        // try to unwrap wrapped GDS exception, in this case FBResourceException will never appear on the stack
    }

    public FBSQLException(String message) {
        super(message, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     *
     * @param message
     *            Exception message
     * @param ex
     *            SQLException that should be set as the 'next exception'
     * @deprecated In all most all cases use
     *             {@link #FBSQLException(String, String)} in combination with
     *             {@link #setNextException(SQLException)}.
     */
    @Deprecated
    public FBSQLException(String message, SQLException ex) {
        this(message);
        setNextException(ex);
    }

    /**
     *
     * @param message
     *            Exception message
     * @param sqlState
     *            SQL State for this exception. Replaced with
     *            {@link SQLStateConstants#SQL_STATE_GENERAL_ERROR} if null
     */
    public FBSQLException(String message, String sqlState) {
        super(message, defaultSQLStateIfNull(sqlState));
    }

    /**
     * @deprecated use {@link #getCause()} instead.
     */
    @Deprecated
    public Exception getInternalException() {
        return (Exception) getCause();
    }

    /**
     * Helper method to create message text for constructor accepting
     * ResourceException ({@link #FBSQLException(ResourceException)})
     *
     * @param ex
     *            ResourceException
     * @return Exception message
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private static String createResourceMessage(ResourceException ex) {
        Throwable cause = resolveCause(ex);
        if (cause instanceof GDSException) {
            return createGDSExceptionMessage((GDSException) cause);
        }
        return "Resource Exception. " + ex.getMessage();
    }

    /**
     * Helper method to create message text for GDSException.
     *
     * @param ex
     *            The GDSException
     * @return Message text
     */
    private static String createGDSExceptionMessage(GDSException ex) {
        return "GDS Exception. " + ex.getIntParam() + ". " + ex.getMessage();
    }

    /**
     * Helper method to get the SQL vendor code (or in the case of Firebird: the
     * isc errorcode).
     *
     * @param ex
     *            ResourceException
     * @return isc errorcode, or 0
     */
    private static int getSqlErrorCode(ResourceException ex) {
        Throwable cause = resolveCause(ex);
        if (cause instanceof GDSException) {
            return ((GDSException) cause).getIntParam();
        }
        if (cause instanceof SQLException) {
            return ((SQLException) cause).getErrorCode();
        }
        if (cause instanceof FBXAException) {
            FBXAException fbXaException = (FBXAException) cause;
            Throwable cause2 = fbXaException.getCause();
            if (cause2 instanceof SQLException) {
                return ((SQLException) cause2).getErrorCode();
            }
        }
        return 0;
    }

    /**
     * @param ex
     *            ResourceException
     * @return Non-null exception linked to FBResourceException, or the original
     *         (FB)ResourceException.
     */
    @SuppressWarnings("deprecation")
    private static Throwable resolveCause(ResourceException ex) {
        if (ex instanceof FBResourceException && ex.getLinkedException() != null) {
            return ex.getLinkedException();
        }
        return ex;
    }

    /**
     * @param sqlState
     *            SQL State value (or null)
     * @return The passed sqlState or
     *         {@link SQLStateConstants#SQL_STATE_GENERAL_ERROR} if sqlState is
     *         null.
     */
    public static String defaultSQLStateIfNull(String sqlState) {
        return sqlState != null ? sqlState : SQLStateConstants.SQL_STATE_GENERAL_ERROR;
    }
}
