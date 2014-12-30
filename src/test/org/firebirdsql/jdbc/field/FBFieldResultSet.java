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
package org.firebirdsql.jdbc.field;

import java.sql.SQLException;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBResultSet;

/**
 * ResultSet implementation to test fields.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBFieldResultSet extends FBResultSet {

	public FBFieldResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
		super(xsqlvars, rows);
	}
	
	public byte[][] getCurrentRow() {
	    return row;
	}
}
