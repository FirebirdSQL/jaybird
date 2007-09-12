package org.firebirdsql.jdbc;

import java.sql.*;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSHelper;

class JDBC40ParameterMetaData extends FBParameterMetaData
        implements ParameterMetaData {

    public JDBC40ParameterMetaData(XSQLVAR[] xsqlvars, GDSHelper connection)
            throws SQLException {
        super(xsqlvars, connection);
    }

    // java.sql.Wrapper interface
    
    public boolean isWrapperFor(Class iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBDatabaseMetaData.class);
    }

    public Object unwrap(Class iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return this;
    }
}