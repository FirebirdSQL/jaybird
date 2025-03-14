/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2012-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
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
