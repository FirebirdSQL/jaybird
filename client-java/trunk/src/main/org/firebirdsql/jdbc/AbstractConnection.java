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
 * The class <code>AbstractConnection</code> is a handle to a 
 * {@link FBManagedConnection}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public abstract class AbstractConnection implements FirebirdConnection {

    //flag that is set to true when a transaction is started automatically,
    //so the transaction may be committed automatically after a
    //statement is executed.
    private boolean autoTransaction = false;
    private int transactionCount;
    
    // This flag is set tu true in close() method to indicate that this 
    // instance is invalid and cannot be used anymore
    private boolean invalid = false;


    protected FBManagedConnection mc;

    private FBLocalTransaction localTransaction = null;

    private FBDatabaseMetaData metaData = null;

    private SQLWarning firstWarning = null;
     
    // This set contains all allocated but not closed statements
    // It is used to close them before the connection is closed
    private HashSet activeStatements = new HashSet();
	 
    /**
     * Create a new AbstractConnection instance based on a
     * {@link FBManagedConnection}.
     *
     * @param mc A FBManagedConnection around which this connection is based
     */
    public AbstractConnection(FBManagedConnection mc) {
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
            throw new FBSQLException(
                "This connection is closed and cannot be used now.",
                FBSQLException.SQL_STATE_CONNECTION_CLOSED);
    }
    
    /**
     * This method should be invoked by each of the statements in the 
     * {@link Statement#close()} method. Here we remove statement from the
     * <code>activeStatements</code> set, so we do not need to close it 
     * later.
     * 
     * @param stmt statement that was closed.
     */
    void notifyStatementClosed(AbstractStatement stmt) {
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

    /**
     * Set the {@link FBManagedConnection} around which this connection is
     * based.
     * @param mc The FBManagedConnection around which this connection is based
     */
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
        return mc.getGDSHelper().getIscDBHandle();
    }

    /**
     * Get Firebird API handler (sockets/native/embeded/etc)
     * @return handler object for internal API calls
     */
    public GDS getInternalAPIHandler() {
        return mc.getGDSHelper().getInternalAPIHandler();
    }
    
    /**
     * Get database parameter buffer for this connection.
     * 
     * @return instance of {@link DatabaseParameterBuffer}.
     */
    public DatabaseParameterBuffer getDatabaseParameterBuffer() {
        return mc.getConnectionRequestInfo().getDpb();
    }

    
	public void setTransactionParameters(int isolationLevel, int[] parameters)
		throws SQLException {
        
        try {
            FBTpb tpb = mc.getTpb();
            FBTpbMapper tpbMapper = tpb.getMapper();
            
            Set tpbParams = new HashSet();
            for (int i = 0; i < parameters.length; i++) {
    			tpbParams.add(new Integer(parameters[i]));
    		}
            
            tpbMapper.setMapping(isolationLevel, tpbParams);
            
            tpb.setMapper(tpbMapper);
            
        } catch(FBResourceException ex) {
        	throw new FBSQLException(ex);   
        }
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
        return createStatement(
            ResultSet.TYPE_FORWARD_ONLY, 
            ResultSet.CONCUR_READ_ONLY
        );
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
        return prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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
    throws SQLException {
        return prepareCall(sql, 
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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
        
        DatabaseParameterBuffer dpb = getDatabaseParameterBuffer();
        
        int mode = FBEscapedParser.USE_BUILT_IN;
        
        if (dpb.hasArgument(DatabaseParameterBuffer.use_standard_udf))
            mode = FBEscapedParser.USE_STANDARD_UDF;
        
        return new FBEscapedParser(mode).parse(sql);
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
        
        synchronized(mc) {
            if (mc.autoCommit != autoCommit) {
                try {
                    if (inTransaction())
                            getLocalTransaction().internalCommit();

                    this.mc.autoCommit = autoCommit;

                } catch (ResourceException ge) {
                    throw new FBSQLException(ge);
                }
            } 
        }
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
            throw new FBSQLException("You cannot getAutomcommit on an " +
                    "unassociated closed connection.");

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
            throw new FBSQLException(
                "You cannot commit a closed connection.",
                FBSQLException.SQL_STATE_CONNECTION_CLOSED);

        if (getAutoCommit() && getInternalAPIHandler().getType() != GDSType.ORACLE_MODE)
            throw new FBSQLException("commit called with AutoCommit true!");

        synchronized(mc) {
            try {
                if (inTransaction())
                    getLocalTransaction().internalCommit();
                
            } catch(ResourceException ge) {
                throw new FBSQLException(ge);
            }
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
        if (getAutoCommit() && getInternalAPIHandler().getType() != GDSType.ORACLE_MODE)
            throw new FBSQLException("Rollback called with AutoCommit true!");

        if (isClosed())
            throw new FBSQLException(
                "You cannot rollback closed connection.",
                FBSQLException.SQL_STATE_CONNECTION_CLOSED);

        synchronized(mc) {
            try{
                if (inTransaction())
                    getLocalTransaction().internalRollback();
                
            } catch(ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
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
                
                synchronized(mc) {
                    if (!getAutoCommit()) {
                        //autocommit is always true for managed tx.
                        try {
                            if (inTransaction())
                                    getLocalTransaction().internalRollback();

                        } catch (ResourceException ge) {
                            throw new FBSQLException(ge);
                        } finally {
                            //always reset Autocommit for the next user.
                            setAutoCommit(true);
                        }
                    } // end of if ()

                    mc.close(this);
                }
                mc = null;
            }
        }
    }


    /**
     * Tests to see if a Connection is closed.
     * 
     * @return true if the connection is closed; false if it's still open
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
        mc.getGDSHelper().setReadOnly(readOnly);
    }


    /**
     * Tests to see if the connection is in read-only mode.
     *
     * @return true if connection is read-only and false otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {
        return mc.getGDSHelper().isReadOnly();
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
            throw new FBSQLException(
                    "Connection has being closed.",
                    FBSQLException.SQL_STATE_CONNECTION_CLOSED);
        
        if (getTransactionIsolation() != level) {
            synchronized(mc) {
                try {

                    if (inTransaction())
                            getLocalTransaction().internalCommit();

                    mc.getGDSHelper().setTransactionIsolation(level);

                } catch (ResourceException re) {
                    throw new FBSQLException(re);
                } 
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
            return mc.getGDSHelper().getTransactionIsolation();
        }
        catch (ResourceException e)
        {
            throw new FBSQLException(e);
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
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) 
        {
            addWarning(new FBSQLWarning("Unsupported type and/or concurrency"));
            
            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            
        }			  
          
        Statement stmt =  new FBStatement(this, resultSetType, resultSetConcurrency);
        activeStatements.add(stmt);
        return stmt;
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
		  if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
		  {
		      addWarning(new FBSQLWarning("resultSetType or resultSetConcurrency changed"));
              
              if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
                  resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
              
		  }
          
          stmt = new FBPreparedStatement(
                  this, sql, resultSetType, resultSetConcurrency);
          
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
		if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE && 
            resultSetConcurrency != ResultSet.CONCUR_READ_ONLY)
		{
            addWarning(new FBSQLWarning("resultSetType or resultSetConcurrency changed"));
            
            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            
            resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        }	
        
        stmt = new FBCallableStatement(this, sql, resultSetType, resultSetConcurrency);
        
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
     * @param map the <code>java.util.Map</code> object to install
     *        as the replacement for this <code>Connection</code>
     *        object's default type map
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC 2.0 API</a>
     */
    public synchronized void setTypeMap(Map map) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    //-------------------------------------------
    //Borrowed from javax.resource.cci.Connection

    /**
     * Returns a FBLocalTransaction instance that enables a component to 
     * demarcate resource manager local transactions on this connection.
     */
    public synchronized FBLocalTransaction getLocalTransaction() {
        synchronized(mc) {
            if (localTransaction == null) {
                localTransaction = new FBLocalTransaction(mc, this);
            }
            return localTransaction;
        }
    }

    /**
     * This non-interface method is included so you can
     * actually get a blob object to use to write new data
     * into a blob field without needing a preexisting blob
     * to modify.
    */
    public synchronized FirebirdBlob createBlob() throws SQLException {
        
        /** @todo check if this is correct code */
        if (!getAutoCommit())
            ensureInTransaction();

        return new FBBlob(this);
    }

    //package methods

    /**
     * Check if this connection is currently involved in a transaction
     */
    public boolean inTransaction() {
        return mc.getGDSHelper().inTransaction();
    }

    //for DatabaseMetaData
//    String getDatabase() {
//        return mc.getGDSHelper().getDatabase();
//    }

    String getUserName() {
        return mc.getGDSHelper().getUserName();
    }

   
    /**
     * Get the encoding that is being used for this connection.
     *
     * @return The name of the encoding used
     */
    public String getIscEncoding() {
        return mc.getGDSHelper().getIscEncoding();
    }

    /**
     * The <code>ensureInTransaction</code> method starts a local transaction
     * if a transaction is not associated with this connection.
     *
     * @return a <code>boolean</code> value, true if transaction was started.
     */
    public synchronized void ensureInTransaction() throws SQLException {
        if (autoTransaction)
            transactionCount++;
        
        synchronized (mc) {
            try {
                if (inTransaction()) {
                    // autoTransaction = false;
                    return;
                }

                //We have to start our own transaction
                getLocalTransaction().begin();
                autoTransaction = true;
                transactionCount = 1;

            } catch (ResourceException re) {
                throw new FBSQLException(re);
            }
        }
    }

    /**
     * The <code>willEndTransaction</code> method determines if the current 
     * transaction should be automatically ended when the current statement 
     * executes.
     * 
     * for use in jca contexts, autocommit is always true, and autoTransaction 
     * is true if the current transaction was started automatically.
     * 
     * Using jdbc transaction control, if autocommit is false, transactions are 
     * started automatically but not ended automatically.
     *
     * @return a <code>boolean</code> value
     */
    public synchronized boolean willEndTransaction() throws SQLException {
        return getAutoCommit() && autoTransaction;
    }

    /**
     * Ensure that the current implicit transaction is ended (if there is
     * one) with a commit. 
     *
     * @throws SQLException if a database access error occurs
     */
    public synchronized void checkEndTransaction() throws SQLException {
        checkEndTransaction(true);
    }
    
    /**
     * Ensure that the current implicit transaction is ended (if there is one),
     * either with a commit or rollback.
     *
     * @param commit if true, end the transaction with a commit, otherwise
     * end the transaction with a rollback
     * @throws SQLException if a database access error occurrs
     */
    public synchronized void checkEndTransaction(boolean commit) throws SQLException {
        if (autoTransaction)
            transactionCount--;
        
        if (willEndTransaction() && transactionCount == 0)
        {
            autoTransaction = false;
            synchronized(mc) {
                try {
                    if (commit)
                        getLocalTransaction().internalCommit();
                    else
                        getLocalTransaction().internalRollback();
                } catch (ResourceException ge) {
                    throw new FBSQLException(ge);
                }
            }

        }
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
         Iterator iter = mc.getGDSHelper().getWarnings().iterator();
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
         mc.getGDSHelper().clearWarnings();
     }
	 
	 //******** Proxies of ManagedConnection methods for jdbc methods
     
    private void checkManagedConnection() throws GDSException {
        if (mc == null)
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_req_no_trans);
    }
	 
    /**
     * Allocate a statement handle from the underlying FBManagedConnection
     * @return The newly allocated statement handle
     * @throws GDSException if an error occurs in the underlying connection
     */
    public isc_stmt_handle getAllocatedStatement() throws GDSException {
        checkManagedConnection();    
        return mc.getGDSHelper().getAllocatedStatement();
    }

    /**
     * Execute a statement based on a statement handle.
     * 
     * @param stmt The statement handle to be executed
     * @param sendOutSqlda Determine if the XSQLDA datastructure should be sent
     * to the server
     * @throws GDSException if an error occurs with the underlying connection
     */
    public void executeStatement(isc_stmt_handle stmt, boolean sendOutSqlda) throws GDSException {
        checkManagedConnection();
        if (stmt == null || !stmt.isValid())
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        mc.getGDSHelper().executeStatement(stmt,sendOutSqlda);
    }
	 	 
    /**
     * Close a statement based on a statment handle.
     *
     * @param stmt The statement to be closed
     * @param deallocate if true, the statement is deallocated, if false the 
     * statement is just closed
     * @throws GDSException if an error occurs with the underlying connection
     */
    public void closeStatement(isc_stmt_handle stmt, boolean deallocate) throws GDSException {
        checkManagedConnection();
        if (stmt == null || !stmt.isValid())
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        mc.getGDSHelper().closeStatement(stmt,deallocate);
    }	 

    /**
     * Prepare an sql statement for a given statement handle.
     * 
     * @param stmt The statement that is being prepared
     * @param sql The sql statement to prepare in the statement
     * @param describeBind Send bind information to the server
     * @throws GDSException if an error occurs with the underlying connection
     * @throws SQLException if a general database error occurs
     */
    public void prepareSQL(isc_stmt_handle stmt, String sql, boolean describeBind) throws GDSException, SQLException {
        checkManagedConnection();
        mc.getGDSHelper().prepareSQL(stmt, sql, describeBind);
    }
	 
    /**
     * Register a statement with the current transaction.
     * 
     * @param fbStatement The statement to be registered
     */
    public void registerStatement(AbstractStatement fbStatement) {
        mc.getGDSHelper().registerStatement(fbStatement.fixedStmt);
    }
	 
    /**
     * Fetch the next batch of row data from a statement handle.
     * 
     * @param stmt The underlying statement handle from which data should 
     * be fetched
     * @param fetchSize The number of rows to fetch
     * @throws GDSException if an error occurs with the underlying connection
     */
    public void fetch(isc_stmt_handle stmt, int fetchSize) throws GDSException {
        checkManagedConnection();
        if (stmt == null || !stmt.isValid())
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        mc.getGDSHelper().fetch(stmt, fetchSize);
    }
    
    /**
     * Set the cursor name to be used with the given statement handle.
     * 
     * @param stmt The statement whose cursor is being named
     * @param cursorName The name for the cursor
     * @throws GDSException if an error occurs
     */
    public void setCursorName(isc_stmt_handle stmt, String cursorName) throws GDSException {
        checkManagedConnection();
        if (stmt == null || !stmt.isValid())
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        mc.getGDSHelper().setCursorName(stmt, cursorName);
    }

    /**
     * Fetch the count information for a statement handle. The count 
     * information that is updated includes the counts for update, insert,
     * delete and select, and it is set in the handle itself.
     * 
     * @param stmt The statement handle for which count info is being fetched
     * @throws GDSException if an error occurs
     */
    public void getSqlCounts(isc_stmt_handle stmt) throws GDSException {
        checkManagedConnection();
        if (stmt == null || !stmt.isValid())
            throw new GDSException(ISCConstants.isc_bad_req_handle);
        mc.getGDSHelper().getSqlCounts(stmt);
    }

    /**
     * Get the product name for the database involved in this connection.
     * 
     * @return The name of the database product
     */
    public String getDatabaseProductName() {
        return mc.getGDSHelper().getDatabaseProductName();
    }

    /**
     * Get the version of the database involved in this connection
     *
     * @return The version of the database
     */
    public String getDatabaseProductVersion() {
        return mc.getGDSHelper().getDatabaseProductVersion();
    }

    /**
     * Get the major version number for the database involved in this 
     * connection
     * 
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        return mc.getGDSHelper().getDatabaseProductMajorVersion();
    }

    /**
     * Get the minor version number for the database involved in this
     * connection
     *
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
        return mc.getGDSHelper().getDatabaseProductMinorVersion();
    }

    /**
     * Get the blob buffer length used for this connection.
     * 
     * @return The size of the blob buffer length
     */
    public Integer getBlobBufferLength() {
        return new Integer(mc.getGDSHelper().getBlobBufferLength());
    }
	 
    /**
     * Open a blob handle with the given identifier.
     *
     * @param blob_id The id given to the blob handle
     * @param segmented If true, the blob handle will be segmented, otherwise
     * it will be a stream
     * @throws GDSException if an error occurs with the underlying connection
     */
    public isc_blob_handle openBlobHandle(long blob_id, boolean segmented) throws GDSException {
        checkManagedConnection();
        return mc.getGDSHelper().openBlobHandle(blob_id, segmented);
    }	 
	 
    /**
     * Fetch a segment of of a blob.
     * 
     * @param blob The handle to the blob for which a segment is requested
     * @param len The length of the segment that is being requested
     * @throws GDSException if an error occurs with the underlying connection
     */
    public byte[] getBlobSegment(isc_blob_handle blob, int len) throws GDSException {
        checkManagedConnection();
        return mc.getGDSHelper().getBlobSegment(blob,len);
    }
	 
    /**
     * Close a blob handle.
     *
     * @param blob The handle to the blob to be closed
     * @throws GDSException if an error occurs with the underlying connection
     */
    public void closeBlob(isc_blob_handle blob) throws GDSException {
        checkManagedConnection();
        mc.getGDSHelper().closeBlob(blob);
    }
	 
    /**
     * Create a new blob handle.
     *
     * @param segmented If true, the new blob will be segmented, otherwise
     * it will be streamed.
     * @throws GDSException if an error occurs with the underlying connection
     */
    public isc_blob_handle createBlobHandle(boolean segmented) throws GDSException {
        checkManagedConnection();
        return mc.getGDSHelper().createBlobHandle(segmented);
    }
	 
    /**
     * Put a segment data data into a blob handle.
     *
     * @param blob The blob to which data is being added
     * @param buf The data to be added to the blob
     * @throws GDSException if an error occurs with the underlying connection
     */
    public void putBlobSegment(isc_blob_handle blob, byte[] buf) throws GDSException {
        checkManagedConnection();
        mc.getGDSHelper().putBlobSegment(blob, buf);
    }

    /**
     * Get the name of the encoding that is being used by the java driver.
     *
     * @return The name of the encoding
     */
    public String getJavaEncoding() {
        return getDatabaseParameterBuffer().getArgumentAsString(
            DatabaseParameterBuffer.local_encoding);
    }
    
    /**
     * Get the path to the character mapping for this connection.
     * 
     * @return The path to the character mapping
     */
    public String getMappingPath() {
        return getDatabaseParameterBuffer().getArgumentAsString(
            DatabaseParameterBuffer.mapping_path);
    }
	 
    private AbstractPreparedStatement getStatement(String sql,HashMap statements) 
	 throws SQLException {
        AbstractPreparedStatement s = (AbstractPreparedStatement)statements.get(sql);
        if (s == null) {
            s = (AbstractPreparedStatement)prepareStatement(sql);
            statements.put(sql, s);
        }
        return s;
    }
	 
    /**
     * Execute an sql query with a given set of parameters.
     *
     * @param sql The sql statement to be used for the query
     * @param params The parameters to be used in the query
     * @param statements map of sql-&gt;AbstractStatements mappings
     * @throws SQLException if a database access error occurs
     */
    public synchronized ResultSet doQuery(String sql, List params,HashMap statements) 
	 throws SQLException
    {
        AbstractPreparedStatement s = getStatement(sql, statements);
        for (int i = 0; i < params.size(); i++) 
            s.setStringForced(i + 1, (String) params.get(i));
        
        return s.executeMetaDataQuery();
    }

    protected void finalize() throws Throwable {
        close();
    }
	 
}
