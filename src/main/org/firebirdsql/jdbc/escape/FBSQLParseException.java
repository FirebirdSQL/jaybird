/*
 * Firebird Open Source JavaEE connector - JDBC driver
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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.jdbc.SQLStateConstants;

import java.sql.SQLSyntaxErrorException;

/**
 * This exception is thrown by FBEscapedParser when it cannot parse the
 * escaped syntax.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBSQLParseException extends SQLSyntaxErrorException {

    private static final long serialVersionUID = 4217078230221445003L;

    /**
     * @deprecated Use {@link SQLStateConstants#SQL_STATE_INVALID_ESCAPE_SEQ}; will be removed in Jaybird 5.
     */
    @Deprecated
    public static final String SQL_STATE_INVALID_ESCAPE_SEQ = SQLStateConstants.SQL_STATE_INVALID_ESCAPE_SEQ;

    public FBSQLParseException(String msg) {
        super(msg, SQLStateConstants.SQL_STATE_INVALID_ESCAPE_SEQ);
    }
}
