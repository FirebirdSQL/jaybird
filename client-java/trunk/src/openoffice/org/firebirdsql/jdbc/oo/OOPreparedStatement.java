package org.firebirdsql.jdbc.oo;

import java.sql.*;

import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jdbc.*;
import org.firebirdsql.jdbc.FBObjectListener.BlobListener;
import org.firebirdsql.jdbc.FBObjectListener.StatementListener;

public class OOPreparedStatement extends FBPreparedStatement {

    public OOPreparedStatement(GDSHelper c, int rsType, int rsConcurrency,
            int rsHoldability, StatementListener statementListener,
            BlobListener blobListener) throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener,
                blobListener);
    }

    public OOPreparedStatement(GDSHelper c, String sql, int rsType,
            int rsConcurrency, int rsHoldability,
            StatementListener statementListener, BlobListener blobListener,
            boolean metaDataQuery, boolean standaloneConnection) throws SQLException {
        super(c, sql, rsType, rsConcurrency, rsHoldability, statementListener,
                blobListener, metaDataQuery, standaloneConnection);
    }

    public void completeStatement() throws SQLException {
        // workaround - do not close the result set, OpenOffice gets crazy

        if (!completed) notifyStatementCompleted();
    }

}
