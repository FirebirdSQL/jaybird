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
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.jca.*;

/**
 * The class <code>AbstractConnection</code> is a handle to a 
 * {@link FBManagedConnection}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public abstract class AbstractConnection implements FirebirdConnection {

    
    // This flag is set tu true in close() method to indicate that this 
    // instance is invalid and cannot be used anymore
    private boolean invalid = false;


    protected FBManagedConnection mc;

    private FBLocalTransaction localTransaction;
    private FBDatabaseMetaData metaData;
    
    protected InternalTransactionCoordinator txCoordinator;

    private SQLWarning firstWarning;
     
    // This set contains all allocated but not closed statements
    // It is used to close them before the connection is closed
    private HashSet activeStatements = new HashSet();
    
    private int resultSetHoldability = FirebirdResultSet.CLOSE_CURSORS_AT_COMMIT;
    
    private boolean autoCommit;
	 
    /**
     * Create a new AbstractConnection instance based on a
     * {@link FBManagedConnection}.
     *
     * @param mc A FBManagedConnection around which this connection is based
     */
    public AbstractConnection(FBManagedConnection mc) {
        this.mc = mc;
        
        this.localTransaction = new FBLocalTransaction(mc, this);
        this.txCoordinator = new InternalTransactionCoordinator();
    }
    
    public FBObjectListener.StatementListener getStatementListener() {
        return txCoordinator;
    }
    
    public int getHoldability() throws SQLException {
        return this.resultSetHoldability;
    }

    public void setHoldability(int holdability) throws SQLException {
        this.resultSetHoldability = holdability;
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
                AbstractStatement stmt = (AbstractStatement)iter.next();
                stmt.close(true);
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
    public IscDbHandle getIscDBHandle() throws GDSException {
        return getGDSHelper().getIscDBHandle();
    }

    /**
     * Get Firebird API handler (sockets/native/embeded/etc)
     * @return handler object for internal API calls
     */
    public GDS getInternalAPIHandler() throws SQLException {
        try {
            return getGDSHelper().getInternalAPIHandler();
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
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
        
        TransactionParameterBuffer tpbParams = createTransactionParameterBuffer();
        
        for (int i = 0; i < parameters.length; i++) {
			tpbParams.addArgument(parameters[i]);
		}
        
        setTransactionParameters(isolationLevel, tpbParams);
	}
    
    public TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException {
        return mc.getTransactionParameters(isolationLevel);
    }

    public TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException {
        return getInternalAPIHandler().newTransactionParameterBuffer();
    }
    
    public void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException {
        if (mc.isManagedEnvironment())
            throw new FBSQLException("Cannot set transaction parameters " +
                    "in managed environment.");
        
        mc.setTransactionParameters(isolationLevel, tpb);
    }
    
    public void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException {
        try {
            if (localTransaction.inTransaction())
                throw new FBSQLException("Cannot set transaction parameters " +
                        "when transaction is already started.");
            
            mc.setTransactionParameters(tpb);
        } catch(ResourceException ex) {
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
            ResultSet.CONCUR_READ_ONLY,
            resultSetHoldability
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
        
        if (dpb.hasArgument(DatabaseParameterBufferExtension.USE_STANDARD_UDF))
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

        if (this.autoCommit == autoCommit) 
            return;
        
        InternalTransactionCoordinator.AbstractTransactionCoordinator coordinator;
        if (autoCommit)
            coordinator = new InternalTransactionCoordinator.AutoCommitCoordinator(this, localTransaction);
        else
            coordinator = new InternalTransactionCoordinator.LocalTransactionCoordinator(this, localTransaction);
        
        txCoordinator.setCoordinator(coordinator);
        this.autoCommit = autoCommit;
    }

    public void setManagedEnvironment(boolean managedConnection) throws SQLException {
        checkValidity();
        
        InternalTransactionCoordinator.AbstractTransactionCoordinator coordinator;
        
        if (managedConnection) {
            coordinator = new InternalTransactionCoordinator.ManagedTransactionCoordinator(this);
            this.autoCommit = false;
        } else {
            coordinator = new InternalTransactionCoordinator.AutoCommitCoordinator(this, localTransaction);
            this.autoCommit = true;
        }
         
        txCoordinator.setCoordinator(coordinator);
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

        return this.autoCommit;
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

        txCoordinator.commit();
        invalidateSavepoints();
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

        if (isClosed())
            throw new FBSQLException(
                "You cannot rollback closed connection.",
                FBSQLException.SQL_STATE_CONNECTION_CLOSED);

        txCoordinator.rollback();
        invalidateSavepoints();
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
        try {
            freeStatements();
        } finally {
            
            if (mc != null) {
                //if we are in a transaction started 
                //automatically because autocommit = false, roll it back.
                
                //leave managed transactions alone, they are normally
                //committed after the Connection handle is closed.
                
                if (!getAutoCommit() && localTransaction.inTransaction()) {
                    //autocommit is always true for managed tx.
                    try {
                        txCoordinator.rollback();
                    } finally {
                        setAutoCommit(true);
                    }
                }

                mc.close(this);
                mc = null;
            }
        }
        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
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
        try {
            if (metaData == null) 
                metaData = new FBDatabaseMetaData(this);
            
            
            return metaData;
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
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
        try {
            if (localTransaction.inTransaction() && !mc.isManagedEnvironment())
                throw new FBSQLException("Calling setReadOnly(boolean) method " +
                        "is not allowed when transaction is already started.");
            
            mc.setReadOnly(readOnly);
        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
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
            throw new FBSQLException(
                    "Connection has being closed.",
                    FBSQLException.SQL_STATE_CONNECTION_CLOSED);
        
        try {

            if (!getAutoCommit() && !mc.isManagedEnvironment())
                txCoordinator.commit();

            mc.setTransactionIsolation(level);

        } catch (ResourceException re) {
            throw new FBSQLException(re);
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
            int resultSetConcurrency) throws SQLException {
        return createStatement(resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }
    
    /**
     * Creates a <code>Statement</code> object that will generate
     * <code>ResultSet</code> objects with the given type, concurrency,
     * and holdability.
     * This method is the same as the <code>createStatement</code> method
     * above, but it allows the default result set
     * type, concurrency, and holdability to be overridden.
     *
     * @param resultSetType one of the following <code>ResultSet</code> 
     *        constants:
     *         <code>ResultSet.TYPE_FORWARD_ONLY</code>, 
     *         <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
     *         <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
     * @param resultSetConcurrency one of the following <code>ResultSet</code> 
     *        constants:
     *         <code>ResultSet.CONCUR_READ_ONLY</code> or
     *         <code>ResultSet.CONCUR_UPDATABLE</code>
     * @param resultSetHoldability one of the following <code>ResultSet</code> 
     *        constants:
     *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
     *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
     * @return a new <code>Statement</code> object that will generate
     *         <code>ResultSet</code> objects with the given type,
     *         concurrency, and holdability
     * @exception SQLException if a database access error occurs
     *            or the given parameters are not <code>ResultSet</code> 
     *            constants indicating type, concurrency, and holdability
     * @see ResultSet
     * @since 1.4
     */
    public synchronized Statement createStatement(int resultSetType, 
        int resultSetConcurrency, int resultSetHoldability) throws SQLException 
    {
        if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
            addWarning(new FBSQLWarning("Unsupported type and/or concurrency"));
            
            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
        }			  
          
        checkHoldability(resultSetType, resultSetHoldability);
        
        try {
            Statement stmt = new FBStatement(getGDSHelper(), resultSetType,
                    resultSetConcurrency, resultSetHoldability, txCoordinator);
            
            activeStatements.add(stmt);
            return stmt;
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Check whether result set type and holdability are compatible.
     * 
     * @param resultSetType desired result set type.
     * @param resultSetHoldability desired result set holdability.
     * 
     * @return new holdability, compatible with result set type.
     * 
     * @throws SQLException if specified result set type and holdability are
     * not compatibe.
     */
    private void checkHoldability(int resultSetType, int resultSetHoldability) throws SQLException {
        boolean holdable = 
            resultSetHoldability == FirebirdResultSet.HOLD_CURSORS_OVER_COMMIT;
        
        boolean notScrollable = resultSetType != ResultSet.TYPE_SCROLL_INSENSITIVE;

        if (holdable && notScrollable) 
            throw new FBDriverNotCapableException(
                    "Holdable cursors are supported only " +
                    "for scrollable insensitive result sets.");
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
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareStatement(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    public synchronized PreparedStatement prepareStatement(String sql, 
        int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException 
    {
          PreparedStatement stmt;
		  if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
		  {
		      addWarning(new FBSQLWarning("resultSetType or resultSetConcurrency changed"));
              
              if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE)
                  resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
              
		  }
          
          checkHoldability(resultSetType, resultSetHoldability);
          
          try {
              stmt = new FBPreparedStatement(
                      getGDSHelper(), sql, resultSetType, resultSetConcurrency, 
                      resultSetHoldability, txCoordinator);
              
              activeStatements.add(stmt);
              return stmt;
              
          } catch(GDSException ex) {
              throw new FBSQLException(ex);
          }
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
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }
            
    public synchronized CallableStatement prepareCall(String sql, 
        int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException 
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

        checkHoldability(resultSetType, resultSetHoldability);
        
        try {
            stmt = new FBCallableStatement(getGDSHelper(), sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability, txCoordinator);
            activeStatements.add(stmt);
            return stmt;
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
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
    
    
    /*
     * Savepoint stuff.  
     */
    
    private int savepointCounter = 0;
    private LinkedList savepoints = new LinkedList();

    private int getNextSavepointCounter() {
        return savepointCounter++;
    }
    
    /**
     * Creates an unnamed savepoint in the current transaction and 
     * returns the new <code>Savepoint</code> object that represents it.
     *
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     */
    public synchronized Savepoint setSavepoint() throws SQLException {
        FBSavepoint savepoint = new FBSavepoint(getNextSavepointCounter());
        
        setSavepoint(savepoint);
        
        savepoints.addLast(savepoint);
        
        return savepoint;
    }
    
    /**
     * Set the savepoint on the server.
     * 
     * @param savepoint savepoint to set.
     * 
     * @throws SQLException if something went wrong.
     */
    private void setSavepoint(FBSavepoint savepoint) throws SQLException {
        if (getAutoCommit())
            throw new SQLException("Connection.setSavepoint() method cannot " + 
                    "be used in auto-commit mode.");

        try {
            txCoordinator.ensureTransaction();
            
            getGDSHelper().executeImmediate("SAVEPOINT " + savepoint.getServerSavepointId());
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Creates a savepoint with the given name in the current transaction
     * and returns the new <code>Savepoint</code> object that represents it.
     *
     * @param name a <code>String</code> containing the name of the savepoint
     * @return the new <code>Savepoint</code> object
     * @exception SQLException if a database access error occurs
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     */
    public synchronized Savepoint setSavepoint(String name) throws SQLException {
        FBSavepoint savepoint = new FBSavepoint(name);
        
        setSavepoint(savepoint);
        
        return savepoint;
    }
    
    /**
     * Undoes all changes made after the given <code>Savepoint</code> object
     * was set. 
     * <P>
     * This method should be used only when auto-commit has been disabled.
     *
     * @param savepoint the <code>Savepoint</code> object to roll back to
     * @exception SQLException if a database access error occurs,
     *            the <code>Savepoint</code> object is no longer valid,
     *            or this <code>Connection</code> object is currently in
     *            auto-commit mode
     * @see Savepoint
     * @see #rollback
     */
    public synchronized void rollback(Savepoint savepoint) throws SQLException {
        
        if (getAutoCommit())
            throw new SQLException("Connection.setSavepoint() method cannot " + 
                    "be used in auto-commit mode.");
        
        if (!(savepoint instanceof FBSavepoint))
            throw new SQLException(
                    "Specified savepoint was not obtained from this connection.");
        
        FBSavepoint fbSavepoint = (FBSavepoint)savepoint;
        
        if (!fbSavepoint.isValid())
            throw new SQLException("Savepoint is no longer valid.");
        
        try {
            getGDSHelper().executeImmediate(
                    "ROLLBACK TO " + fbSavepoint.getServerSavepointId());
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    /**
     * Removes the given <code>Savepoint</code> object from the current 
     * transaction. Any reference to the savepoint after it have been removed 
     * will cause an <code>SQLException</code> to be thrown.
     *
     * @param savepoint the <code>Savepoint</code> object to be removed
     * @exception SQLException if a database access error occurs or
     *            the given <code>Savepoint</code> object is not a valid 
     *            savepoint in the current transaction
     */
    public synchronized void releaseSavepoint(Savepoint savepoint) throws SQLException {
        
        if (getAutoCommit())
            throw new SQLException("Connection.setSavepoint() method cannot " + 
                    "be used in auto-commit mode.");
        
        if (!(savepoint instanceof FBSavepoint))
            throw new SQLException(
                    "Specified savepoint was not obtained from this connection.");
        
        FBSavepoint fbSavepoint = (FBSavepoint)savepoint;
        
        if (!fbSavepoint.isValid())
            throw new SQLException("Savepoint is no longer valid.");

        try {
            getGDSHelper().executeImmediate(
                    "RELEASE SAVEPOINT " + fbSavepoint.getServerSavepointId() + " ONLY");
        } catch (GDSException ex) {
            throw new FBSQLException(ex);
        }
        
        fbSavepoint.invalidate();
        
        savepoints.remove(fbSavepoint);
    }

    /**
     * Invalidate all savepoints.
     */
    protected synchronized void invalidateSavepoints() {
        Iterator iter = savepoints.iterator();
        while(iter.hasNext())
            ((FBSavepoint)iter.next()).invalidate();
        
        savepoints.clear();
    }    

    //-------------------------------------------
    //Borrowed from javax.resource.cci.Connection

    /**
     * Returns a FBLocalTransaction instance that enables a component to 
     * demarcate resource manager local transactions on this connection.
     */
    public synchronized FBLocalTransaction getLocalTransaction() {
        return localTransaction;
    }

    /**
     * This non-interface method is included so you can
     * actually get a blob object to use to write new data
     * into a blob field without needing a preexisting blob
     * to modify.
    */
    public synchronized FirebirdBlob createBlob() throws SQLException {
        try {
            return new FBBlob(getGDSHelper(), txCoordinator);
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }

    //package methods

    /**
     * Check if this connection is currently involved in a transaction
     */
    public boolean inTransaction() throws SQLException {
        try {
            return getGDSHelper().inTransaction();
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
        }
    }
   
    /**
     * Get the encoding that is being used for this connection.
     *
     * @return The name of the encoding used
     */
    public String getIscEncoding() throws SQLException {
        try {
            return getGDSHelper().getIscEncoding();
        } catch(GDSException ex) {
            throw new FBSQLException(ex);
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
     private SQLWarning getIscWarnings() throws SQLException {
         try {
             SQLWarning firstWarning = null;
             SQLWarning lastWarning = null;
             Iterator iter = getGDSHelper().getWarnings().iterator();
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
         } catch(GDSException ex) {
             throw new FBSQLException(ex);
         }
     }
     
     /**
      * Clear warnings associated with this database connection.
      */
     private void clearIscWarnings() throws SQLException {
         try {
             getGDSHelper().clearWarnings();
         } catch(GDSException ex) {
             throw new FBSQLException(ex);
         }
     }
	 
    public GDSHelper getGDSHelper() throws GDSException {
        if (mc == null)
            throw new GDSException(ISCConstants.isc_arg_gds, ISCConstants.isc_req_no_trans);

        return mc.getGDSHelper();
    }
    
    protected void finalize() throws Throwable {
        close();
    }
	 
}
