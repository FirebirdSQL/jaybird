package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.logging.Logger;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBStatement extends AbstractStatement {

    public FBStatement(GDSHelper c, int rsType, int rsConcurrency, int rsHoldability,
            FBObjectListener.StatementListener statementListener) throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener);
    }
    
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new FBDriverNotCapableException();
    }
}
