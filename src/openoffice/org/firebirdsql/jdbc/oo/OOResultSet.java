package org.firebirdsql.jdbc.oo;

import java.sql.SQLException;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.AbstractStatement;
import org.firebirdsql.jdbc.FBResultSet;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;

public class OOResultSet extends FBResultSet {

    public OOResultSet(GDSHelper gdsHelper, AbstractStatement fbStatement,
            AbstractIscStmtHandle stmt, ResultSetListener listener,
            boolean metaDataQuery, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        super(gdsHelper, fbStatement, stmt, listener, metaDataQuery, rsType,
                rsConcurrency, rsHoldability, cached);
    }

    public OOResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
    }
}
