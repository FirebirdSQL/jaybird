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

/*
 * CVS modification log:
 * $Log$
 * Revision 1.2  2002/01/25 18:21:34  rrokytskyy
 * now FBSQLParseException extends SQLException
 *
 * Revision 1.1  2001/07/18 20:07:31  d_jencks
 * Added better GDSExceptions, new NativeSQL, and CallableStatement test from Roman Rokytskyy
 *
 */

package org.firebirdsql.jdbc;

import java.sql.SQLException;

/**
 * This exception is thrown by FBEscapedParser when it cannot parse the
 * escaped syntax.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class FBSQLParseException extends SQLException{
    public FBSQLParseException() { super(); }
    public FBSQLParseException(String msg) { super(msg); }
}
