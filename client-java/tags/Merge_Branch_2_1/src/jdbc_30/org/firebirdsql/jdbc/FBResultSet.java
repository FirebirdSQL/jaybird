package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.gds.impl.AbstractIscStmtHandle;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.FBObjectListener.ResultSetListener;


public class FBResultSet extends AbstractResultSet {

    public FBResultSet(GDSHelper gdsHelper, AbstractStatement fbStatement,
            AbstractIscStmtHandle stmt, ResultSetListener listener,
            boolean trimStrings, int rsType, int rsConcurrency,
            int rsHoldability, boolean cached) throws SQLException {
        super(gdsHelper, fbStatement, stmt, listener, trimStrings, rsType,
                rsConcurrency, rsHoldability, cached);
        // TODO Auto-generated constructor stub
    }

    public FBResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
        // TODO Auto-generated constructor stub
    }

    
}
