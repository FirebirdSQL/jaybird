/*
 * $Id$
 *
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

import java.sql.SQLException;
import java.util.List;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBResultSet;

/**
 * ResultSet implementation to test fields.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBFieldResultSet extends FBResultSet {

	public FBFieldResultSet(XSQLVAR[] xsqlvars, List<byte[][]> rows) throws SQLException {
		super(xsqlvars, rows);
	}

	public byte[][] getCurrentRow() {
        // TODO Replace with new implementation
        throw new UnsupportedOperationException("Not implemented");
	    /*return row;*/
	}
}
