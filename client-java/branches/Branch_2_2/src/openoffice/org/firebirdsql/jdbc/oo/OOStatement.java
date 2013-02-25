package org.firebirdsql.jdbc.oo;

import java.sql.SQLException;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;

public class OOStatement extends FBStatement {

    public OOStatement(GDSHelper c, int rsType, int rsConcurrency,
            int rsHoldability, StatementListener statementListener)
            throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener);
    }

    public void completeStatement() throws SQLException {
        // workaround - do not close the result set, OpenOffice gets crazy
        if (!completed) notifyStatementCompleted();
    }
}