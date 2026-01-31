// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.CommonDataSource;

import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.jspecify.annotations.Nullable;

/**
 * Root superclass for the data sources in Jaybird.
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
public abstract class RootCommonDataSource implements CommonDataSource {

    public @Nullable PrintWriter getLogWriter() throws SQLException {
        // Unused by Jaybird
        return null;
    }

    public void setLogWriter(@Nullable PrintWriter out) throws SQLException {
        // Unused by Jaybird
    }

    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new FBDriverNotCapableException("Method getParentLogger() not supported");
    }

}
