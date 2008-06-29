package org.firebirdsql.jdbc;

import java.sql.*;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBStatement extends AbstractStatement {

	public FBStatement(GDSHelper c, int rsType, int rsConcurrency,
            int rsHoldability,
            FBObjectListener.StatementListener statementListener)
            throws SQLException {
        super(c, rsType, rsConcurrency, rsHoldability, statementListener);
    }

	
	//--------------------------------------------------------------------------
	// JDBC 3.0
	//--------------------------------------------------------------------------
	
//    public ResultSet getGeneratedKeys() throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, int param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, int[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public int executeUpdate(String param1, String[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//    
//    public boolean execute(String param1, int param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//
//    public boolean execute(String param1, int[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }
//
//    public boolean execute(String param1, String[] param2) throws SQLException {
//        throw new FBDriverNotCapableException();
//    }

    //--------------------------------------------------------------------------
    // JDBC 4.0
    //--------------------------------------------------------------------------

    public boolean isPoolable() throws SQLException {
        return false;
    }

    public void setPoolable(boolean poolable) throws SQLException {
        // ignore the hint
    }

    public boolean isClosed() {
        return super.isClosed();
    }
    
    
    // java.sql.Wrapper interface
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBStatement.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new FBDriverNotCapableException();
        
        return (T)this;
    }
}
