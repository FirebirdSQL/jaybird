/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

/**
 * Constants with common SQLState values.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class SQLStateConstants {

    public static final String SQL_STATE_WARNING = "01000";

    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_CONN_ATTR = "01S00";
    public static final String SQL_STATE_NO_ROW_AVAIL = "01S06";

    public static final String SQL_STATE_FEATURE_NOT_SUPPORTED = "0A000";

    public static final String SQL_STATE_GENERAL_ERROR = "HY000";
    /**
     * @deprecated will be removed in Jaybird 6 and replaced by {@code SQL_STATE_INVALID_DESC_FIELD_ID}, which will
     * be introduced in Jaybird 6
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_COLUMN = "HY002";
    /**
     * @deprecated will be removed in Jaybird 6 and replaced by {@code SQL_STATE_INVALID_USE_NULL},
     * {@code SQL_STATE_ATT_CANNOT_SET_NOW}, {@code SQL_STATE_INVALID_ATTR_VALUE} and
     * {@code SQL_STATE_INVALID_STRING_LENGTH}, which will all be introduced in Jaybird 6
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_ARG_VALUE = "HY009";
    public static final String SQL_STATE_INVALID_OPTION_IDENTIFIER = "HY092";
    public static final String SQL_STATE_INVALID_PARAM_TYPE = "HY105";

    public static final String SQL_STATE_WRONG_PARAM_NUM = "07001";
    public static final String SQL_STATE_INVALID_STMT_TYPE = "07003";
    public static final String SQL_STATE_NO_RESULT_SET = "07005";
    public static final String SQL_STATE_INVALID_CONVERSION = "07006";

    public static final String SQL_STATE_INVALID_TX_STATE = "25000";
    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_TRANSACTION_STATE = "25S01";
    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_TRANSACTION_ACTIVE = "25S02";
    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_TRANSACTION_ROLLED_BACK = "25S03";

    public static final String SQL_STATE_INVALID_STATEMENT_ID = "26000";

    public static final String SQL_STATE_CONNECTION_ERROR = "08000";
    public static final String SQL_STATE_CONNECTION_CLOSED = "08003";
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_CONNECTION_FAILURE_IN_TX = "08007";
    /**
     * @deprecated will be removed in Jaybird 6 without replacement
     */
    @Deprecated
    public static final String SQL_STATE_COMM_LINK_FAILURE = "08S01";

    public static final String SQL_STATE_SYNTAX_ERROR = "42000";
    public static final String SQL_STATE_INVALID_ESCAPE_SEQ = SQL_STATE_SYNTAX_ERROR;

    private SQLStateConstants() {
        // no instances
    }
}
