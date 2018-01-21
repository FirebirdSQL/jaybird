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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;

/**
 * This exception is thrown when the requested type conversion cannot be
 * performed.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TypeConversionException extends FBSQLException {

    private static final long serialVersionUID = 9145386635318036933L;

    public TypeConversionException(String msg) { 
        super(msg, SQLStateConstants.SQL_STATE_INVALID_CONVERSION);
    }

    public TypeConversionException(String msg, Throwable cause) {
        super(msg, SQLStateConstants.SQL_STATE_INVALID_CONVERSION);
        initCause(cause);
    }
}
