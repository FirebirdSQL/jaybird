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
