// SPDX-FileCopyrightText: Copyright 2011-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

import org.firebirdsql.jdbc.FBDriverNotCapableException;

/**
 * Root superclass for the datasources in Firebird.
 *
 * @author Mark Rotteveel
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
        throw new FBDriverNotCapableException("Method getParentLogger() not supported");
    }

}
