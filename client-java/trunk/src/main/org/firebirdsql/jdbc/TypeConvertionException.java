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
 * Revision 1.1  2002/08/29 13:41:05  d_jencks
 * Changed to lgpl only license.  Moved driver to subdirectory to make build system more consistent.
 *
 * Revision 1.1  2002/02/19 19:30:18  rrokytskyy
 * added FBField and related stuff
 *
 */

package org.firebirdsql.jdbc;

import java.sql.SQLException;
/**
 * This exception is thrown when the requested type conversion cannot be
 * performed.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TypeConvertionException extends SQLException {
    public TypeConvertionException() { super();}
    public TypeConvertionException(String msg) { super(msg);}
}
