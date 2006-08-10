package org.firebirdsql.jdbc.oo;

import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBParameterMetaData;

public class OOParameterMetaData extends FBParameterMetaData {

    public OOParameterMetaData(XSQLVAR[] xsqlvars, GDSHelper connection)
            throws SQLException {
        super(xsqlvars, connection);
    }

}
