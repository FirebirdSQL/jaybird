package org.firebirdsql.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.firebirdsql.gds.impl.GDSHelper;

/**
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBStatement extends AbstractStatement {

	/**
	 * 
	 */
	public FBStatement(GDSHelper c, int rsType, int rsConcurrency, FBObjectListener.StatementListener statementListener) {
		super(c, rsType, rsConcurrency, statementListener);
	}

    
    /**
     * jdbc 3
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean getMoreResults(int param1) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, int param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, int[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int executeUpdate(String param1, String[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("Not yet implemented");
    }
    
    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, int param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, int[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public boolean execute(String param1, String[] param2) throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getResultSetHoldability() throws SQLException {
        // TODO: implement this java.sql.Statement method
        throw new SQLException("not yet implemented");
    }
    
}
