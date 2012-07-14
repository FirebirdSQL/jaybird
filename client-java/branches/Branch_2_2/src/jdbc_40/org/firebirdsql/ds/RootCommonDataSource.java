/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

import org.firebirdsql.jdbc.FBDriverNotCapableException;

/**
 * Root superclass for the datasources in Firebird.
 * <p>
 * Used to workaround incompatibilities introduced for JDBC 3.0 and earlier by JDBC 4.1 (getParentLogger).
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public abstract class RootCommonDataSource implements CommonDataSource {

    public PrintWriter getLogWriter() throws SQLException {
        // Unused by Jaybird
        return null;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        // Unused by Jaybird
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new FBDriverNotCapableException();
    }

}
