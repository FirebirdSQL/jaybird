/*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */

package org.firebirdsql.jdbc;

import java.sql.*;
import java.util.*;
import javax.resource.*;

import org.firebirdsql.gds.*;
import org.firebirdsql.jca.*;

/**
 * The class <code>FBConnection</code> is a handle to a FBManagedConnection.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBConnection implements Connection, FirebirdConnection
/*, javax.resource.cci.Connection
 * It is not possible to implement both Connection interfaces in one class
 * due to conflicting exception signatures.
 */ 
{
    //flag that is set to true when a transaction is started automatically,
    //so the transaction may be committed automatically after a
    //statement is executed.
    private boolean autoTransaction = false;
    
    // This flag is set tu true in close() method to indicate that this 
    // instance is invalid and cannot be used anymore
    private boolean invalid = false;


    private FBManagedConnection mc;

    private FBLocalTransaction localTransaction = null;

    private FBDatabaseMetaData metaData = null;

    private SQLWarning firstWarning = null;
     
    // This set contains all allocated but not closed statements
    // It is used to close them before the connection is closed
    private HashSet activeStatements = new HashSet();
	 
    public FBConnection(FBManagedConnection mc) {
        this.mc = mc;
    }
    
    /**
     * Check if this connection is valid. This method should be invoked before
     * executing any action in this class.
     * 
     * @throws SQLException if this connection has been closed and cannot be 
     * used anymore.
     */
    private void checkValidity() throws SQLException {
        if (invalid || isClosed())
            throw new SQLException(
                "This connection is closed and cannot be used now.");
    }
    
    /**
     * Invalidate this connection. This method makes connection unusable.
     */
/* Private and not used method    
    private void invalidate() {
        invalid = true;
    }
*/    
    /**
     * This method should be invoked by each of the statements in the 
     * {@link Statement#close()} method. Here we remove statement from the
     * <code>activeStatements</code> set, so we do not need to close it 
     * later.
     * 
     * @param stmt statement that was closed.
     */
    void notifyStatementClosed(FBStatement stmt) {
        if (!activeStatements.remove(stmt))
            throw new IllegalArgumentException(
                "Specified statement was not created by this connection.");
    }
    
    /**
     * This method closes all active statements and cleans resources.
     * 
     * @throws SQLException if at least one of the active statements failed
     * to close gracefully.
     */
    private void freeStatements() throws SQLException {
        // clone statements to avoid concurrent modification exception
        Set statements = (Set)activeStatements.clone();
        
        // iterate through the set, close statements and collect exceptions
        Iterator iter = statements.iterator();
        SQLException e = null;
        while(iter.hasNext()) {
            try {
                Statement stmt = (Statement)iter.next();
                stmt.close();
            } catch(SQLException ex) {
                if (e != null)
                    e.setNextException(ex);
                else
                    e = ex;
            }
        }
        
        // throw exception if there is any
        if (e != null) throw e;
    }

    public void setManagedConnection(FBManagedConnection mc) {
        //close any prepared statements we may have executed.
        if (this.mc != mc && metaData != null) {
            metaData.close();
            metaData = null;
        }
        this.mc = mc;
    }

    /**
     * Get connection handle for direct Firebird API access
     *
     * @return internal handle for connection
     * @exception GDSException if handle needed to be created and creation failed
     */
    public isc_db_handle getIscDBHandle() throws GDSException {
        return mc.getIscDBHandle();
    }

    /**
     * Get Firebird API handler (sockets/native/embeded/etc)
     * @return handler object for internal API calls
     */
    public GDS getInternalAPIHandler() {
        return mc.getInternalAPIHandler();
    }

    /**
     * Creates a <code>Statement</code> object for sending
     * SQL statements to the database.
     * SQL statements without parameters are normally
     * executed using Statement objects. If the same SQL statement
     * is executed many times, it is more efficient to use a
     * <code>PreparedStatement</code> object.
     *<P>
     *
     * Result sets created using the returned <code>Statement</code>
     * object will by default have forward-only type and read-only concurrency.
     *
     * @return a new Statement object
     * @exception SQLException if a database access error occurs
     */
    public synchronized Statement createStatement() throws SQLException {
        Statement stmt =  new FBStatement(this);
        activeStatements.add(stmt);
        return stmt;
    }


    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized Statement createStatement(int param1, int param2, 
        int param3) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }


    /**
     * Creates a <code>PreparedStatement</code> object for sending
     * parameterized SQL statements to the database.
     *
     * A SQL statement with or without IN parameters can be
     * pre-compiled and stored in a PreparedStatement object. This
     * object can then be used to efficiently execute this statement
     * multiple times.
     *
     * <P><B>Note:</B> This method is optimized for handling
     * parametric SQL statements that benefit from precompilation. If
     * the driver supports precompilation,
     * the method <code>prepareStatement</code> will send
     * the statement to the database for precompilation. Some drivers
     * may not support precompilation. In this case, the statement may
     * not be sent to the database until the <code>PreparedStatement</code> is
     * executed.  This has no direct effect on users; however, it does
     * affect which method throws certain SQLExceptions.
     *
     *
     * Result sets created using the returned PreparedStatement will have
     * forward-only type and read-only concurrency, by default.
     *
     * @param sql a SQL statement that may contain one or more '?' IN
     * parameter placeholders
     * @return a new PreparedStatement object containing the
     * pre-compiled statement
     * @exception SQLException if a database access error occurs
     */
    public synchronized PreparedStatement prepareStatement(String sql)
        throws SQLException {
        PreparedStatement stmt = new FBPreparedStatement(this, sql);
        activeStatements.add(stmt);
        return stmt;
    }


    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
        int param2) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
        int param2, int param3, int param4) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }


    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
        int[] param2) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized PreparedStatement prepareStatement(String param1, 
        String[] param2) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }


    /**
     * Creates a <code>CallableStatement</code> object for calling
     * database stored procedures.
     * The <code>CallableStatement</code> object provides
     * methods for setting up its IN and OUT parameters, and
     * methods for executing the call to a stored procedure.
     *
     * <P><B>Note:</B> This method is optimized for handling stored
     * procedure call statements. Some drivers may send the call
     * statement to the database when the method <code>prepareCall</code>
     * is done; others
     * may wait until the <code>CallableStatement</code> object
     * is executed. This has no
     * direct effect on users; however, it does affect which method
     * throws certain SQLExceptions.
     *
     *
     * Result sets created using the returned CallableStatement will have
     * forward-only type and read-only concurrency, by default.
     *
     * @param sql a SQL statement that may contain one or more '?'
     * parameter placeholders. Typically this  statement is a JDBC
     * function call escape string.
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement
     * @exception SQLException if a database access error occurs
     */
    public synchronized CallableStatement prepareCall(String sql) 
        throws SQLException 
    {
        CallableStatement stmt = new FBCallableStatement(this, sql);
        activeStatements.add(stmt);
        return stmt;
    }


    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @param param4 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized CallableStatement prepareCall(String param1, int param2, 
        int param3, int param4) throws SQLException 
    {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("not yet implemented");
    }


    /**
     * Converts the given SQL statement into the system's native SQL grammar.
     * A driver may convert the JDBC sql grammar into its system's
     * native SQL grammar prior to sending it; this method returns the
     * native form of the statement that the driver would have sent.
     *
     * @param sql a SQL statement that may contain one or more '?'
     * parameter placeholders
     * @return the native form of this statement
     * @exception SQLException if a database access error occurs
     */
    public synchronized String nativeSQL(String sql) throws SQLException {
        try {
            return new FBEscapedParser().parse(sql);
        } catch(FBSQLParseException pex) {
            throw new SQLException(pex.toString());
        }
    }


    /**
     * Sets this connection's auto-commit mode.
     * If a connection is in auto-commit mode, then all its SQL
     * statements will be executed and committed as individual
     * transactions.  Otherwise, its SQL statements are grouped into
     * transactions that are terminated by a call to either
     * the method <code>commit</code> or the method <code>rollback</code>.
     * By default, new connections are in auto-commit
     * mode.
     *
     * The commit occurs when the statement completes or the next
     * execute occurs, whichever comes first. In the case of
     * statements returning a ResultSet, the statement completes when
     * the last row of the ResultSet has been retrieved or the
     * ResultSet has been closed. In advanced cases, a single
     * statement may return multiple results as well as output
     * parameter values. In these cases the commit occurs when all results and
     * output parameter values have been retrieved.
     *
     * @param autoCommit true enables auto-commit; false disables
     * auto-commit.
     * @exception SQLException if a database access error occurs
     */
    public synchronized void setAutoCommit(boolean autoCommit) 
        throws SQLException 
    {
        checkValidity();
        
        if (mc.autoCommit != autoCommit)
        {
            try {
                if (inTransaction())
                {
                    getLocalTransaction().internalCommit();
                }

                this.mc.autoCommit = autoCommit;
                
                /*
                // commented out by R.Rokytskyy, we should not start
                // tx if autocommit is off. 
                if (!autoCommit) 
                {
                    getLocalTransaction().begin();
                } // end of if ()
                */
            } catch(GDSException ge) {
                throw new FBSQLException(ge);
            }
        } // end of if ()
        
    }


    /**
     * Gets the current auto-commit state.
     *
     * @return the current state of auto-commit mode
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public boolean getAutoCommit() throws SQLException {
        if (isClosed())
        {
            throw new SQLException("You cannot getAutomcommit on an unassociated closed connection.");
        }
        return mc.autoCommit;
    }


    /**
     * Makes all changes made since the previous
     * commit/rollback permanent and releases any database locks
     * currently held by the Connection. This method should be
     * used only when auto-commit mode has been disabled.
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public synchronized void commit() throws SQLException {
        if (isClosed())
        {
            throw new SQLException("You cannot commit a closed connection.");
        }
        if (getAutoCommit())
        {
            throw new SQLException("commit called with AutoCommit true!");
        } // end of if ()

        try {
            if (inTransaction())
            {
                getLocalTransaction().internalCommit();
            } // end of if ()
            // getLocalTransaction().begin();
        } catch(GDSException ge) {
            throw new FBSQLException(ge);
        }
    }


    /**
     * Drops all changes made since the previous
     * commit/rollback and releases any database locks currently held
     * by this Connection. This method should be used only when auto-
     * commit has been disabled.
     *
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public synchronized void rollback() throws SQLException {
        if (getAutoCommit())
        {
            throw new SQLException("rollback called with AutoCommit true!");
        } // end of if ()
        if (isClosed())
        {
            throw new SQLException("You cannot rollback closed connection.");
        }
        try{
            if (inTransaction())
            {
                getLocalTransaction().internalRollback();
            } // end of if ()
            // getLocalTransaction().begin();
        } catch(GDSException ge) {
            throw new FBSQLException(ge);
        }
    }



    /**
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized void rollback(Savepoint param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Rollback to savepoint not yet implemented!");
    }

    /**
     * Releases a Connection's database and JDBC resources
     * immediately instead of waiting for
     * them to be automatically released.
     *
     * <P><B>Note:</B> A Connection is automatically closed when it is
     * garbage collected. Certain fatal errors also result in a closed
     * Connection.
     *
     * @exception SQLException if a database access error occurs
     */
    public synchronized void close() throws SQLException {
        try {
            freeStatements();
        } finally {
            
            if (mc != null) {
                //if we are in a transaction started 
                //automatically because autocommit = false, roll it back.
                
                //leave managed transactions alone, they are normally
                //committed after the Connection handle is closed.
                
                if (!getAutoCommit()) 
                {
                    //autocommit is always true for managed tx.
                    try {
                        if (inTransaction())
                        {
                            getLocalTransaction().internalRollback();
                        }
                    } catch(GDSException ge) {
                        throw new FBSQLException(ge);
                    }
                    finally
                    {
                        //always reset Autocommit for the next user.
                        setAutoCommit(true);
                    }
                } // end of if ()
                
                mc.close(this);
                mc = null;
            }
        }
    }


    /**
     * Tests to see if a Connection is closed.
     *
     * @return true if the connection is closed; false if it's still open
     * @exception SQLException if a database access error occurs
     */
    public boolean isClosed() {
        return mc == null;
    }


    //======================================================================
    // Advanced features:

    /**
     * Gets the metadata regarding this connection's database.
     * A Connection's database is able to provide information
     * describing its tables, its supported SQL grammar, its stored
     * procedures, the capabilities of this connection, and so on. This
     * information is made available through a DatabaseMetaData
     * object.
     *
     * @return a DatabaseMetaData object for this Connection
     * @exception SQLException if a database access error occurs
     */
    public synchronized DatabaseMetaData getMetaData() throws SQLException {
        if (metaData == null) {
            metaData = new FBDatabaseMetaData(this);
        }
        return metaData;
    }


    /**
     * Puts this connection in read-only mode as a hint to enable
     * database optimizations.
     *
     * <P><B>Note:</B> This method cannot be called while in the
     * middle of a transaction.
     *
     * @param readOnly true enables read-only mode; false disables
     * read-only mode.
     * @exception SQLException if a database access error occurs
     */
    public synchronized void setReadOnly(boolean readOnly) throws SQLException {
        mc.setReadOnly(readOnly);
    }


    /**
     * Tests to see if the connection is in read-only mode.
     *
     * @return true if connection is read-only and false otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {
        return mc.isReadOnly();
    }


    /**
     * Sets a catalog name in order to select
     * a subspace of this Connection's database in which to work.
     * If the driver does not support catalogs, it will
     * silently ignore this request.
     *
     * @exception SQLException if a database access error occurs
     */
    public synchronized void setCatalog(String catalog) throws SQLException {
    }


    /**
     * Returns the Connection's current catalog name.
     *
     * @return the current catalog name or null
     * @exception SQLException if a database access error occurs
     */
    public String getCatalog() throws SQLException {
        return null;
    }



    /**
     * Attempts to change the transaction
     * isolation level to the one given.
     * The constants defined in the interface <code>Connection</code>
     * are the possible transaction isolation levels.
     *
     * <P>Calling this method will commit any current transaction.
     *
     * @param level one of the TRANSACTION_* isolation values with the
     * exception of TRANSACTION_NONE; some databases may not support
     * other values
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#supportsTransactionIsolationLevel
     */
    public synchronized void setTransactionIsolation(int level) 
        throws SQLException 
    {
        if (isClosed())
            throw new SQLException("Connection has being closed.");
        
        if (getTransactionIsolation() != level) 
        {
            try 
            {

                if (inTransaction())
                {
                    getLocalTransaction().internalCommit();
                }
                mc.setTransactionIsolation(level);
            }
            catch (ResourceException re)
            {
                throw new FBSQLException(re);
            }
            catch (GDSException ge)
            {
                throw new FBSQLException(ge);
            } 
        }
    }


    /**
     * Gets this Connection's current transaction isolation level.
     *
     * @return the current TRANSACTION_* mode value
     * @exception SQLException if a database access error occurs
     */
    public int getTransactionIsolation() throws SQLException {
        try 
        {
            return mc.getTransactionIsolation();
        }
        catch (ResourceException e)
        {
            throw new SQLException(e.getMessage());
        } // end of try-catch
    }


    /**
     * Returns the first warning reported by calls on this Connection.
     *
     * <P><B>Note:</B> Subsequent warnings will be chained to this
     * SQLWarning.
     *
     * @return the first SQLWarning or null
     * @exception SQLException if a database access error occurs
     */
    public SQLWarning getWarnings() throws SQLException {
        SQLWarning warning = firstWarning;
        SQLWarning iscWarning = getIscWarnings();
        
        if (warning == null)
            warning = iscWarning;
        else
        if (iscWarning != null)
            warning.setNextWarning(iscWarning);
            
        return warning;
    }


    /**
     * Clears all warnings reported for this <code>Connection</code> object.
     * After a call to this method, the method <code>getWarnings</code>
     * returns null until a new warning is
     * reported for this Connection.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() throws SQLException {
		 firstWarning = null;
         clearIscWarnings();
    }



    //--------------------------JDBC 2.0-----------------------------

    /**
     *
     * Creates a <code>Statement</code> object that will generate
     * <code>ResultSet</code> objects with the given type and concurrency.
     * This method is the same as the <code>createStatement</code> method
     * above, but it allows the default result set
     * type and result set concurrency type to be overridden.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new Statement object
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized Statement createStatement(int resultSetType, 
        int resultSetConcurrency) throws SQLException 
    {
		  if (resultSetType == ResultSet.TYPE_FORWARD_ONLY
		  && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)
		     return createStatement();
		  else{
		     addWarning(new SQLWarning("resultSetType or resultSetConcurrency changed"));
		     return createStatement();
		  }			  
    }


    /**
     *
     * Creates a <code>PreparedStatement</code> object that will generate
     * <code>ResultSet</code> objects with the given type and concurrency.
     * This method is the same as the <code>prepareStatement</code> method
     * above, but it allows the default result set
     * type and result set concurrency type to be overridden.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new PreparedStatement object containing the
     * pre-compiled SQL statement
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized PreparedStatement prepareStatement(String sql, 
        int resultSetType, int resultSetConcurrency) throws SQLException 
    {
          PreparedStatement stmt;
		  if (resultSetType == ResultSet.TYPE_FORWARD_ONLY
		  && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)
	        stmt = new FBPreparedStatement(this, sql);
		  else{
		     addWarning(new SQLWarning("resultSetType or resultSetConcurrency changed"));
	        stmt = new FBPreparedStatement(this, sql);
		  }		
          activeStatements.add(stmt);
          return stmt;
    }

    /**
     *
     * Creates a <code>CallableStatement</code> object that will generate
     * <code>ResultSet</code> objects with the given type and concurrency.
     * This method is the same as the <code>prepareCall</code> method
     * above, but it allows the default result set
     * type and result set concurrency type to be overridden.
     *
     * @param resultSetType a result set type; see ResultSet.TYPE_XXX
     * @param resultSetConcurrency a concurrency type; see ResultSet.CONCUR_XXX
     * @return a new CallableStatement object containing the
     * pre-compiled SQL statement
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized CallableStatement prepareCall(String sql, 
        int resultSetType, int resultSetConcurrency) throws SQLException 
    {
        CallableStatement stmt;
		  if (resultSetType == ResultSet.TYPE_FORWARD_ONLY
		  && resultSetConcurrency == ResultSet.CONCUR_READ_ONLY)
	        stmt =new FBCallableStatement(this, sql);
		  else{
		     addWarning(new SQLWarning("resultSetType or resultSetConcurrency changed"));
	        stmt = new FBCallableStatement(this, sql);
		  }		
          activeStatements.add(stmt);
          return stmt;
    }


    /**
     *
     * Gets the type map object associated with this connection.
     * Unless the application has added an entry to the type map,
     * the map returned will be empty.
     *
     * @return the <code>java.util.Map</code> object associated
     *         with this <code>Connection</code> object
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public Map getTypeMap() throws SQLException {
        return new HashMap();
    }


    /**
     * Installs the given type map as the type map for
     * this connection.  The type map will be used for the
     * custom mapping of SQL structured types and distinct types.
     *
     * @param the <code>java.util.Map</code> object to install
     *        as the replacement for this <code>Connection</code>
     *        object's default type map
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized void setTypeMap(Map map) throws SQLException {
        throw new SQLException("Not yet implemented");
    }

    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized void setHoldability(int param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");
    }

    /**
     *
     * jdbc 3
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public int getHoldability() throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");
    }

    /**
     *
     * jdbc 3
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized Savepoint setSavepoint() throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");

    }

    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized Savepoint setSavepoint(String param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");

    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public synchronized void releaseSavepoint(Savepoint param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");
    }



    //-------------------------------------------
    //Borrowed from javax.resource.cci.Connection

    public synchronized FBLocalTransaction getLocalTransaction() {
        if (localTransaction == null) {
            localTransaction = new FBLocalTransaction(mc, this);
        }
        return localTransaction;
    }
    /**
     * This non- interface method is included so you can
     * actually get a blob object to use to write new data
     * into a blob field without needing a preexisting blob
     * to modify.
    **/
    public synchronized FirebirdBlob createBlob() throws SQLException {
        
        /** @todo check if this is correct code */
        if (!getAutoCommit())
            ensureInTransaction();

        return new FBBlob(this);
    }

    //package methods

    public boolean inTransaction() {
        return mc.inTransaction();
    }

    //for DatabaseMetaData
    String getDatabase() {
        return mc.getDatabase();
    }

    String getUserName() {
        return mc.getUserName();
    }

    String getIscEncoding() {
        return mc.getIscEncoding();
    }

    /**
     * The <code>ensureInTransaction</code> method starts a local transaction
     * if a transaction is not associated with this connection.
     *
     * @return a <code>boolean</code> value, true if transaction was started.
     */
    synchronized void ensureInTransaction() throws SQLException
    {
		 try {
			if (inTransaction())
			{
            autoTransaction = false;
            return;
        } // end of if ()
        //We have to start our own transaction
        getLocalTransaction().begin();
        autoTransaction = true;
		 }
		 catch(ResourceException re){
//           log.warn("resource exception", re);
           throw new SQLException("ResourceException: " + re);
		 }
    }

    /**
     * The <code>willEndTransaction</code> method determines if the current transaction should be
     * automatically ended when the current statement executes.
     * for use in jca contexts, autocommit is always true, and autoTransaction is true if the current
     * transaction was started automatically.
     * Using jdbc transaction control, if autocommit is false, transactions are started automatically
     * but not ended automatically.
     *
     * @return a <code>boolean</code> value
     */
    synchronized boolean willEndTransaction() throws SQLException
    {
        return getAutoCommit() && autoTransaction;
    }

    synchronized void checkEndTransaction() throws SQLException
    {
        if (willEndTransaction())
        {
            autoTransaction = false;
            try
            {
                getLocalTransaction().internalCommit();
            }
            catch (GDSException ge)
            {
                throw new FBSQLException(ge);
            } // end of catch

        } // end of if ()
    }

	 protected synchronized void addWarning(SQLWarning warning){
		 if (firstWarning == null)
			 firstWarning = warning;
		 else{
			 SQLWarning lastWarning = firstWarning;
			 while (lastWarning.getNextWarning() != null){
				 lastWarning = lastWarning.getNextWarning();
			 }
			 lastWarning.setNextWarning(warning);
		 }
	 }
     
     /**
      * Get warnings associated with this database connection.
      * 
      * @return instance of {@link SQLWarning} that is the first warning in 
      * a linked list of warnings.
      */
     private SQLWarning getIscWarnings() {
         SQLWarning firstWarning = null;
         SQLWarning lastWarning = null;
         Iterator iter = mc.getWarnings().iterator();
         while (iter.hasNext()) {
             GDSException item = (GDSException)iter.next();
             
             FBSQLWarning warning = new FBSQLWarning(item);
             if (firstWarning == null) {
                 firstWarning = warning;
                 lastWarning = firstWarning;
             } else {
                lastWarning.setNextWarning(warning);
                lastWarning = warning;
             }
         }
         return firstWarning;
     }
     
     /**
      * Clear warnings associated with this database connection.
      */
     private void clearIscWarnings() {
         mc.clearWarnings();
     }
	 
	 //******** Proxies of ManagedConnection methods for jdbc methods
     
    private void checkManagedConnection() throws GDSException {
        if (mc == null)
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_req_no_trans);
    }
	 
    public isc_stmt_handle getAllocatedStatement() throws GDSException {
        checkManagedConnection();    
        return mc.getAllocatedStatement();
    }

    public void executeStatement(isc_stmt_handle stmt, boolean sendOutSqlda) throws GDSException {
        checkManagedConnection();
        mc.executeStatement(stmt,sendOutSqlda);
    }
	 	 
    public void closeStatement(isc_stmt_handle stmt, boolean deallocate) throws GDSException {
        checkManagedConnection();
        mc.closeStatement(stmt,deallocate);
    }	 

    public void prepareSQL(isc_stmt_handle stmt, String sql, boolean describeBind) throws GDSException {
        checkManagedConnection();
        mc.prepareSQL(stmt, sql, describeBind);
    }
	 
    public void registerStatement(FBStatement fbStatement) {
        mc.registerStatement(fbStatement);
    }
	 
    public void fetch(isc_stmt_handle stmt, int fetchSize) throws GDSException {
        checkManagedConnection();
        mc.fetch(stmt, fetchSize);
    }
    
    public void setCursorName(isc_stmt_handle stmt, String cursorName) throws GDSException {
        checkManagedConnection();
        mc.setCursorName(stmt, cursorName);
    }

    public void getSqlCounts(isc_stmt_handle stmt) throws GDSException {
        checkManagedConnection();
        mc.getSqlCounts(stmt);
    }

    public String getDatabaseProductName() {
        return mc.getDatabaseProductName();
    }

    public String getDatabaseProductVersion() {
        return mc.getDatabaseProductVersion();
    }

    public int getDatabaseProductMajorVersion() {
        return mc.getDatabaseProductMajorVersion();
    }

    public int getDatabaseProductMinorVersion() {
        return mc.getDatabaseProductMinorVersion();
    }

    public Integer getBlobBufferLength() {
        /**@todo add check if mc is not null */
        return mc.getBlobBufferLength();
    }
	 
    public isc_blob_handle openBlobHandle(long blob_id, boolean segmented) throws GDSException {
        checkManagedConnection();
        return mc.openBlobHandle(blob_id, segmented);
    }	 
	 
    public byte[] getBlobSegment(isc_blob_handle blob, int len) throws GDSException {
        checkManagedConnection();
        return mc.getBlobSegment(blob,len);
    }
	 
    public void closeBlob(isc_blob_handle blob) throws GDSException {
        checkManagedConnection();
        mc.closeBlob(blob);
    }
	 
    public isc_blob_handle createBlobHandle(boolean segmented) throws GDSException {
        checkManagedConnection();
        return mc.createBlobHandle(segmented);
    }
	 
    public void putBlobSegment(isc_blob_handle blob, byte[] buf) throws GDSException {
        checkManagedConnection();
        mc.putBlobSegment(blob, buf);
    }

    public static String getJavaEncoding(String iscEncoding) {
        return FBConnectionHelper.getJavaEncoding(iscEncoding);
    }
	 
    private PreparedStatement getStatement(String sql,HashMap statements) 
	 throws SQLException {
        PreparedStatement s = (PreparedStatement)statements.get(sql);
        if (s == null) {
            s = prepareStatement(sql);
            statements.put(sql, s);
        }
        return s;
    }
	 
    public ResultSet doQuery(String sql, List params,HashMap statements) 
	 throws SQLException
    {
        boolean ourTransaction = false;
        FBLocalTransaction trans = null;
        if (!inTransaction())
        {
            trans = getLocalTransaction();
				 
            try 
            {
                trans.internalBegin();
                ourTransaction = true;
            }
            catch (GDSException ge)
            {
                throw new FBSQLException(ge);
            }
        }
        PreparedStatement s = getStatement(sql,statements);
        for (int i = 0; i < params.size(); i++)
        {
            s.setString(i + 1, (String)params.get(i));
        }
        ResultSet rs = null;
        try
        {
            s.execute();
            rs = ((FBStatement)s).getCachedResultSet(true); //trim strings
        }
        finally
        {
            if (ourTransaction) 
            {
                try 
                {
                    trans.internalCommit();
                }
                catch (GDSException ge) 
                {
                    throw new FBSQLException(ge);
                }
            }
        }
        return rs;
    }

    protected void finalize() throws Throwable {
        close();
    }
	 
}
