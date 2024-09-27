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
package org.firebirdsql.jdbc.escape;

import org.firebirdsql.jdbc.SQLStateConstants;

import java.io.Serial;
import java.sql.SQLSyntaxErrorException;

/**
 * This exception is thrown by FBEscapedParser when it cannot parse the
 * escaped syntax.
 *
 * @author Roman Rokytskyy
 */
public class FBSQLParseException extends SQLSyntaxErrorException {

    @Serial
    private static final long serialVersionUID = 4217078230221445003L;

    public FBSQLParseException(String msg) {
        super(msg, SQLStateConstants.SQL_STATE_INVALID_ESCAPE_SEQ);
    }
}
