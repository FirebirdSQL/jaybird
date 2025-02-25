/*
 SPDX-FileCopyrightText: Copyright 2001-2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2005 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2007 Gabriel Reid
 SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.jdbc;

import org.firebirdsql.ds.RootCommonDataSource;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.xca.FBConnectionRequestInfo;
import org.firebirdsql.jaybird.xca.FBManagedConnectionFactory;
import org.firebirdsql.jaybird.xca.XcaConnectionManager;
import org.firebirdsql.util.InternalApi;

import javax.sql.DataSource;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The class {@code FBDataSource} is a ConnectionFactory for jdbc Connection objects. All work is delegated to a
 * XcaConnectionManager.
 * <p>
 * This data source is for internal use inside Jaybird. For a simple data source, use
 * {@link org.firebirdsql.ds.FBSimpleDataSource}, for XA {@link org.firebirdsql.ds.FBXADataSource}.
 * </p>
 * <p>
 * If you need a standalone connection pool, consider using a connection pool implementation like HikariCP, c3p0 or
 * DBCP.
 * </p>
 *
 * @author David Jencks
 */
@InternalApi
public class FBDataSource extends RootCommonDataSource implements DataSource, Serializable {

    @Serial
    private static final long serialVersionUID = 1178461472062969634L;

    private final XcaConnectionManager cm;
    private final FBManagedConnectionFactory mcf;

    private int loginTimeout = 0;

    public FBDataSource(FBManagedConnectionFactory mcf, XcaConnectionManager cm) {
        this.mcf = mcf;
        this.cm = cm;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return cm.allocateConnection(mcf, mcf.getDefaultConnectionRequestInfo());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        //mcf makes a copy for us.
        FBConnectionRequestInfo subjectCri = mcf.getDefaultConnectionRequestInfo();
        subjectCri.setUserName(username);
        subjectCri.setPassword(password);
        return cm.allocateConnection(mcf, subjectCri);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unableToUnwrap)
                    .messageParameter(iface != null ? iface.getName() : "(null)")
                    .toSQLException();
        }

        return iface.cast(this);
    }

}
