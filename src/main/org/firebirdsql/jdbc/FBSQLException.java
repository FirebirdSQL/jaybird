/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;

import java.sql.SQLException;
import org.firebirdsql.gds.GDSException;

/**
 *@author Ken Richard
 *@created May 30, 2002
 *@see <related>
 *@version $ $
 */

public class FBSQLException extends SQLException {
    private GDSException original = null;

    public FBSQLException(GDSException ex) {
        super("GDS Exception: " + ex);
        original = ex;
    }
    
    public int getErrorCode() {
        return original.getIntParam();
    }
    
    public Exception getInternalException() {
        return original;
    }    
}
