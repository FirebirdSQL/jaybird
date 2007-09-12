package org.firebirdsql.jdbc;

import java.sql.*;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSHelper;

class JDBC30ParameterMetaData extends FBParameterMetaData
        implements ParameterMetaData {

    public JDBC30ParameterMetaData(XSQLVAR[] xsqlvars, GDSHelper connection)
            throws SQLException {
        super(xsqlvars, connection);
    }

}