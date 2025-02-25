// SPDX-FileCopyrightText: Copyright 2003 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
