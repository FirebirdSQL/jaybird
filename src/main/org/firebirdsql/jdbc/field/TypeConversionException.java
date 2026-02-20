/*
 SPDX-FileCopyrightText: Copyright 2002-2003 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc.field;

import org.firebirdsql.jdbc.SQLStateConstants;
import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.sql.SQLNonTransientException;

/**
 * This exception is thrown when the requested type conversion cannot be performed.
 *
 * @author Roman Rokytskyy
 */
@NullMarked
public class TypeConversionException extends SQLNonTransientException {

    @Serial
    private static final long serialVersionUID = 9145386635318036933L;

    public TypeConversionException(String msg) { 
        super(msg, SQLStateConstants.SQL_STATE_INVALID_CONVERSION);
    }

    public TypeConversionException(String msg, Throwable cause) {
        super(msg, SQLStateConstants.SQL_STATE_INVALID_CONVERSION, cause);
    }
}
