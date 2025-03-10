// SPDX-FileCopyrightText: Copyright 2016-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import org.firebirdsql.util.Volatile;

/**
 * Constants with common SQLState values.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@Volatile(reason = "Constants may be removed in new major releases if no longer needed by the implementation")
public final class SQLStateConstants {

    /**
     * ISO-9075-2: warning => 01000.
     */
    public static final String SQL_STATE_WARNING = "01000";

    public static final String SQL_STATE_NO_ROW_AVAIL = "01S06";

    /**
     * ISO-9075-2: feature not supported => 0A000.
     */
    public static final String SQL_STATE_FEATURE_NOT_SUPPORTED = "0A000";

    /**
     * ISO-9075-3: CLI-specific condition => HY000.
     * <p>
     * Also
     * </p>
     * <ul>
     * <li>dynamic parameter value needed</li>
     * <li>invalid handle</li>
     * </ul>
     */
    public static final String SQL_STATE_GENERAL_ERROR = "HY000";
    /**
     * ISO-9075-3: invalid use of null pointer => HY009.
     */
    public static final String SQL_STATE_INVALID_USE_NULL = "HY009";
    /**
     * ISO-9075-3: attribute cannot be set now => HY011.
     */
    public static final String SQL_STATE_ATT_CANNOT_SET_NOW = "HY011";
    /**
     * ISO-9075-3: invalid attribute value => HY024.
     */
    public static final String SQL_STATE_INVALID_ATTR_VALUE = "HY024";
    /**
     * ISO-9075-3: invalid string length or buffer length => HY090.
     */
    public static final String SQL_STATE_INVALID_STRING_LENGTH = "HY090";
    /**
     * ISO-9075-3: invalid descriptor field identifier => H091.
     */
    public static final String SQL_STATE_INVALID_DESC_FIELD_ID = "HY091";
    /**
     * ISO-9075-3: invalid attribute identifier => HY092.
     */
    public static final String SQL_STATE_INVALID_OPTION_IDENTIFIER = "HY092";
    /**
     * ISO-9075-3: invalid parameter mode => HY105.
     */
    public static final String SQL_STATE_INVALID_PARAM_TYPE = "HY105";

    // TODO doesn't match ISO-9075-2: using clause does not match dynamic parameter specifications => 07001.
    //  maybe undefined DATA value => 0700C; however, 07001 seems to match current use by Firebird as well
    public static final String SQL_STATE_WRONG_PARAM_NUM = "07001";
    /**
     * ISO-9075-2: cursor specification cannot be executed => 07003.
     */
    public static final String SQL_STATE_INVALID_STMT_TYPE = "07003";
    /**
     * ISO-9075-2: prepared statement not a cursor specification => 07005.
     */
    public static final String SQL_STATE_NO_RESULT_SET = "07005";
    /**
     * ISO-9075-2: restricted data type attribute violation => 07006.
     */
    // maybe replace with: data type transform function violation => 0700B, invalid target type specification => 0D000,
    // or invalid data type => HY004
    public static final String SQL_STATE_INVALID_CONVERSION = "07006";

    /**
     * ISO-9075-2: invalid cursor state => 24000.
     */
    public static final String SQL_STATE_INVALID_CURSOR_STATE = "24000";

    /**
     * ISO-9075-2: invalid transaction state => 25000.
     */
    public static final String SQL_STATE_INVALID_TX_STATE = "25000";
    /**
     * ISO-9075-2: active SQL-transaction => 25001
     */
    public static final String SQL_STATE_TX_ACTIVE = "25001";

    /**
     * ISO-9075-2: invalid SQL statement name => 26000.
     */
    // better match seems: invalid SQL statement identifier => 30000; however Firebird itself uses 26000 as well
    public static final String SQL_STATE_INVALID_STATEMENT_ID = "26000";

    /**
     * ISO-9075-2: SQLSTATE class connection exception => 08
     */
    public static final String SQLSTATE_CLASS_CONNECTION_ERROR = "08";

    /**
     * ISO-9075-2: connection exception => 08000.
     */
    public static final String SQL_STATE_CONNECTION_ERROR = "08000";
    /**
     * ISO-9075-2: SQL-client unable to establish SQL-connection => 08001.
     */
    @SuppressWarnings("unused")
    public static final String SQL_STATE_CANNOT_CONNECT = "08001";
    /**
     * ISO-9075-2: connection name in use => 08002.
     */
    @SuppressWarnings("unused")
    public static final String SQL_STATE_CONNECTION_IN_USE = "08002";
    /**
     * ISO-9075-2: connection failure => 08006.
     */
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    /**
     * ISO-9075-2: transaction resolution unknown => 08007.
     */
    public static final String SQL_STATE_TX_RESOLUTION_UNKNOWN = "08007";

    /**
     * ISO-9075-2: syntax error or access rule violation => 42000.
     */
    public static final String SQL_STATE_SYNTAX_ERROR = "42000";
    // TODO Name suggests: invalid escape sequence => 22025
    public static final String SQL_STATE_INVALID_ESCAPE_SEQ = SQL_STATE_SYNTAX_ERROR;

    /**
     * ISO-9075-2: locator exception
     */
    public static final String SQL_STATE_LOCATOR_EXCEPTION = "0F000";

    private SQLStateConstants() {
        // no instances
    }
}
