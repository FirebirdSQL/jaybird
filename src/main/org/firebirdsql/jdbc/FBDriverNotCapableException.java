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
package org.firebirdsql.jdbc;

import java.io.Serial;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Tell that driver is not able to serve the request due to missing capabilities.
 * 
 * @author Roman Rokytskyy
 */
public class FBDriverNotCapableException extends SQLFeatureNotSupportedException {

	@Serial
	private static final long serialVersionUID = 4813885566272454052L;

	/**
     * Create instance of this class for the specified reason.
     * 
	 * @param reason reason that will be displayed.
	 */
	public FBDriverNotCapableException(String reason) {
		super(reason, SQLStateConstants.SQL_STATE_FEATURE_NOT_SUPPORTED);
	}

	/**
	 * Create instance of this class.
	 */
	public FBDriverNotCapableException() {
		this("Not yet implemented.");
	}

}
