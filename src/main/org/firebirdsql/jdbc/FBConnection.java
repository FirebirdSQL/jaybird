/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferExtension;
import org.firebirdsql.gds.impl.GDSHelper;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBLocalTransaction;
import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jca.FirebirdLocalTransaction;
import org.firebirdsql.jdbc.escape.FBEscapedParser;
import org.firebirdsql.jdbc.escape.FBEscapedParser.EscapeParserMode;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

import javax.resource.ResourceException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import static org.firebirdsql.gds.impl.DatabaseParameterBufferExtension.USE_FIREBIRD_AUTOCOMMIT;

/**
 * The class <code>FBConnection</code> is a handle to a 
 * {@link FBManagedConnection}.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBConnection implements FirebirdConnection, Synchronizable {

    private static final Logger log = LoggerFactory.getLogger(FBConnection.class);

    private static final String GET_CLIENT_INFO_SQL = "SELECT "
                + "    rdb$get_context('USER_SESSION', ?) session_context "
                + "  , rdb$get_context('USER_TRANSACTION', ?) tx_context " 
                + "FROM rdb$database";

    private static final String SET_CLIENT_INFO_SQL = "SELECT "
                + "  rdb$set_context('USER_SESSION', ?, ?) session_context " 
                + "FROM rdb$database";

    protected FBManagedConnection mc;

    private FBLocalTransaction localTransaction;
    private FBDatabaseMetaData metaData;
    
    protected final InternalTransactionCoordinator txCoordinator;

    private SQLWarning firstWarning;
     
    // This set contains all allocated but not closed statements
    // It is used to close them before the connection is closed
    protected final Set<Statement> activeStatements = Collections.synchronizedSet(new HashSet<Statement>());
    
    private int resultSetHoldability = ResultSet.CLOSE_CURSORS_AT_COMMIT;

    private StoredProcedureMetaData storedProcedureMetaData;
    private FBEscapedParser escapedParser;
	 
    /**
     * Create a new AbstractConnection instance based on a
     * {@link FBManagedConnection}.
     *
     * @param mc A FBManagedConnection around which this connection is based
     */
    public FBConnection(FBManagedConnection mc) {
        this.mc = mc;
        
        txCoordinator = new InternalTransactionCoordinator(this);
        
        FBConnectionRequestInfo cri = mc.getConnectionRequestInfo();

        resultSetHoldability = cri.hasArgument(DatabaseParameterBufferExtension.RESULT_SET_HOLDABLE)
                ? ResultSet.HOLD_CURSORS_OVER_COMMIT
                : ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    public FBObjectListener.StatementListener getStatementListener() {
        return txCoordinator;
    }
    
    public int getHoldability() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return resultSetHoldability;
        }
    }

    public void setHoldability(int holdability) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            this.resultSetHoldability = holdability;
        }
    }

    /**
     * Check if this connection is valid. This method should be invoked before
     * executing any action in this class.
     * 
     * @throws SQLException if this connection has been closed and cannot be 
     * used anymore.
     */
    protected void checkValidity() throws SQLException {
        if (isClosed()) {
            throw new FBSQLException("This connection is closed and cannot be used now.",
                SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
        }
    }
    
    /**
     * This method should be invoked by each of the statements in the 
     * {@link Statement#close()} method. Here we remove statement from the
     * <code>activeStatements</code> set, so we do not need to close it 
     * later.
     * 
     * @param stmt statement that was closed.
     */
    void notifyStatementClosed(FBStatement stmt) {
        if (!activeStatements.remove(stmt)) {
            if (stmt instanceof FBPreparedStatement && ((FBPreparedStatement) stmt).isParamSet == null) {
                // Close was likely triggered by finalizer of a prepared statement that failed on prepare in
                // the constructor: Do not log warning
                return;
            }
            log.warn("Specified statement was not created by this connection: " + stmt);
        }
    }
    
    /**
     * This method closes all active statements and cleans resources.
     * 
     * @throws SQLException if at least one of the active statements failed
     * to close gracefully.
     */
    protected void freeStatements() throws SQLException {
        // copy statements to avoid concurrent modification exception
        List<Statement> statements = new ArrayList<>(activeStatements);
        
        // iterate through the set, close statements and collect exceptions
        SQLExceptionChainBuilder<SQLException> chain = new SQLExceptionChainBuilder<>();
        for (Statement stmt : statements) {
            try {
                stmt.close();
            } catch(SQLException ex) {
                chain.append(ex);
            }
        }
        
        // throw exception if there is any
        if (chain.hasException()) throw chain.getException();
    }

    /**
     * Set the {@link FBManagedConnection} around which this connection is
     * based.
     * @param mc The FBManagedConnection around which this connection is based
     */
    public void setManagedConnection(FBManagedConnection mc) {
        synchronized (getSynchronizationObject()) {
            //close any prepared statements we may have executed.
            if (this.mc != mc && metaData != null) {
                try {
                    metaData.close();
                } finally {
                    metaData = null;
                }
            }
            this.mc = mc;
        }
    }
    
    public FBManagedConnection getManagedConnection() {
        synchronized (getSynchronizationObject()) {
            return mc;
        }
    }

    /**
     * Get connection handle for direct Firebird API access
     *
     * @return internal handle for connection
     * @throws SQLException
     *         if handle needed to be created and creation failed
     */
    @Override
    public FbDatabase getFbDatabase() throws SQLException {
        return getGDSHelper().getCurrentDatabase();
    }

    /**
     * Get database parameter buffer for this connection.
     * 
     * @return instance of {@link DatabaseParameterBuffer}.
     */
    public DatabaseParameterBuffer getDatabaseParameterBuffer() {
        return mc != null ? mc.getConnectionRequestInfo().getDpb() : null;
    }

    @Deprecated
	public void setTransactionParameters(int isolationLevel, int[] parameters) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            TransactionParameterBuffer tpbParams = createTransactionParameterBuffer();

            for (int parameter : parameters) {
                tpbParams.addArgument(parameter);
            }

            setTransactionParameters(isolationLevel, tpbParams);
        }
	}
    
    public TransactionParameterBuffer getTransactionParameters(int isolationLevel) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return mc.getTransactionParameters(isolationLevel);
        }
    }

    public TransactionParameterBuffer createTransactionParameterBuffer() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return getFbDatabase().createTransactionParameterBuffer();
        }
    }
    
    public void setTransactionParameters(int isolationLevel, TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (mc.isManagedEnvironment()) {
                throw new FBSQLException("Cannot set transaction parameters in managed environment.");
            }

            mc.setTransactionParameters(isolationLevel, tpb);
        }
    }
    
    public void setTransactionParameters(TransactionParameterBuffer tpb) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            try {
                if (getLocalTransaction().inTransaction()) {
                    throw new FBSQLException("Cannot set transaction parameters when transaction is already started.");
                }

                mc.setTransactionParameters(tpb);
            } catch (ResourceException ex) {
                throw new FBSQLException(ex);
            }
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
    public Statement createStatement() throws SQLException {
        return createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, resultSetHoldability);
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
    public PreparedStatement prepareStatement(String sql) throws SQLException {
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
    public CallableStatement prepareCall(String sql) throws SQLException {
        return prepareCall(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
    }
    
    public Blob createBlob() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return new FBBlob(getGDSHelper(), txCoordinator);
        }
    }
    
    public Clob createClob() throws SQLException {
        FBBlob blob = (FBBlob)createBlob();
        return new FBClob(blob);
    }
    
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type STRUCT not supported");
    }
    
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type ARRAY not yet supported");
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
    public String nativeSQL(String sql) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return getEscapedParser().parse(sql);
        }
    }
    
    /**
     * Returns the FBEscapedParser instance for this connection.
     * 
     * @return Instance of FBEscapedParser
     */
    protected FBEscapedParser getEscapedParser() {
        if (escapedParser == null) {
            DatabaseParameterBuffer dpb = getDatabaseParameterBuffer();
            EscapeParserMode mode = dpb.hasArgument(DatabaseParameterBufferExtension.USE_STANDARD_UDF)
                    ? EscapeParserMode.USE_STANDARD_UDF
                    : EscapeParserMode.USE_BUILT_IN;
            escapedParser = new FBEscapedParser(mode);
        }
        return escapedParser;
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
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit() == autoCommit) {
                return;
            }

            // FIXME : Behavior in switch might be wrong, see also setSavePoint
            txCoordinator.switchTransactionCoordinator(autoCommit);
        }
    }

    protected void setTransactionCoordinator(boolean managedConnection, boolean autoCommit) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            txCoordinator.setTransactionCoordinator(managedConnection, autoCommit);
        }
    }

    public void setManagedEnvironment(boolean managedConnection) throws SQLException {
        synchronized (getSynchronizationObject()) {
            setTransactionCoordinator(managedConnection, true);
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
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException("You cannot getAutoCommit on an unassociated closed connection.");
            }
            return txCoordinator.getAutoCommit();
        }
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
    public void commit() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException(
                        "You cannot commit a closed connection.",
                        SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new FBSQLException("Connection enlisted in distributed transaction", SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            txCoordinator.commit();
            invalidateTransactionLifetimeObjects();
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
    public void rollback() throws SQLException {
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                throw new FBSQLException(
                        "You cannot rollback closed connection.",
                        SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
            }

            if (mc.inDistributedTransaction()) {
                throw new FBSQLException("Connection enlisted in distributed transaction", SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            txCoordinator.rollback();
            invalidateTransactionLifetimeObjects();
        }
    }
    
    /**
     * Invalidate everything that should only last for the lifetime of the current transaction.
     */
    protected void invalidateTransactionLifetimeObjects(){
    	invalidateSavepoints();
    	storedProcedureMetaData = null;
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
    public void close() throws SQLException {
        SQLExceptionChainBuilder<SQLException> chainBuilder = new SQLExceptionChainBuilder<>();
        synchronized (getSynchronizationObject()) {
            if (log.isTraceEnabled()) {
                log.trace("Connection closed requested at", new RuntimeException("Connection close logging"));
            }
            try {
                freeStatements();
                if (metaData != null) metaData.close();
            } catch (SQLException e) {
                chainBuilder.append(e);
            } finally {
                metaData = null;
                if (mc != null) {
                    // leave managed transactions alone, they are normally
                    // committed after the Connection handle is closed.
                    if (!mc.inDistributedTransaction()) {
                        try {
                            txCoordinator.handleConnectionClose();
                        } catch (SQLException e) {
                            chainBuilder.append(e);
                        } finally {
                            try {
                                setAutoCommit(true);
                            } catch (SQLException e) {
                                if (!SQLStateConstants.SQL_STATE_CONNECTION_CLOSED.equals(e.getSQLState())) {
                                    chainBuilder.append(e);
                                }
                            }
                        }
                    }

                    mc.close(this);
                    mc = null;
                }
            }
        }
        if (chainBuilder.hasException()) {
            throw chainBuilder.getException();
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
    
    public boolean isValid(int timeout) throws SQLException {
        if (timeout < 0) {
            throw new SQLException("Timeout should be >= 0", SQLStateConstants.SQL_STATE_INVALID_ARG_VALUE);
        }
        synchronized (getSynchronizationObject()) {
            if (isClosed()) {
                return false;
            }
            // TODO Check if we can set the connection timeout temporarily for this call
            if (timeout != 0) {
                addWarning(new SQLWarning(
                        String.format("Connection.isValid does not support non-zero timeouts, timeout value %d has been ignored",
                                timeout)));
            }
            try {
                getFbDatabase().getDatabaseInfo(
                        new byte[] { ISCConstants.isc_info_ods_version, ISCConstants.isc_info_end }, 10);
                return true;
            } catch (SQLException ex) {
                return false;
            }
        }
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
    public DatabaseMetaData getMetaData() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (metaData == null)
                metaData = new FBDatabaseMetaData(this);
            return metaData;
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
    public void setReadOnly(boolean readOnly) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            try {
                if (getLocalTransaction().inTransaction() && !mc.isManagedEnvironment())
                    throw new FBSQLException("Calling setReadOnly(boolean) method " +
                            "is not allowed when transaction is already started.");

                mc.setReadOnly(readOnly);
            } catch (ResourceException ex) {
                throw new FBSQLException(ex);
            }
        }
    }


    /**
     * Tests to see if the connection is in read-only mode.
     *
     * @return true if connection is read-only and false otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean isReadOnly() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return mc.isReadOnly();
        }
    }


    /**
     * Sets a catalog name in order to select
     * a subspace of this Connection's database in which to work.
     * If the driver does not support catalogs, it will
     * silently ignore this request.
     *
     * @exception SQLException if a database access error occurs
     */
    public void setCatalog(String catalog) throws SQLException {
        checkValidity();
    }


    /**
     * Returns the Connection's current catalog name.
     *
     * @return the current catalog name or null
     * @exception SQLException if a database access error occurs
     */
    public String getCatalog() throws SQLException {
        checkValidity();
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
    public void setTransactionIsolation(int level) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();

            try {
                if (!getAutoCommit() && !mc.isManagedEnvironment())
                    txCoordinator.commit();

                mc.setTransactionIsolation(level);

            } catch (ResourceException re) {
                throw new FBSQLException(re);
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            try {
                return mc.getTransactionIsolation();
            } catch (ResourceException e) {
                throw new FBSQLException(e);
            }
        }
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            return firstWarning;
        }
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
        synchronized (getSynchronizationObject()) {
            checkValidity();
            firstWarning = null;
        }
    }

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
    
    public Statement createStatement(int resultSetType,
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
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT &&
                    resultSetType == ResultSet.TYPE_FORWARD_ONLY) {

                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            checkHoldability(resultSetType, resultSetHoldability);

            Statement stmt = new FBStatement(getGDSHelper(), resultSetType, resultSetConcurrency, resultSetHoldability,
                    txCoordinator);

            activeStatements.add(stmt);
            return stmt;
        }
    }

    /**
     * Check whether result set type and holdability are compatible.
     * 
     * @param resultSetType desired result set type.
     * @param resultSetHoldability desired result set holdability.
     * 
     * @throws SQLException if specified result set type and holdability are
     * not compatibe.
     */
    private void checkHoldability(int resultSetType, int resultSetHoldability) throws SQLException {
        boolean holdable = 
            resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT;
        
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
    public PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareStatement(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    /**
     * Creates a <code>PreparedStatement</code> object that will generate
     * <code>ResultSet</code> objects with the given type, concurrency,
     * and holdability.
     * <P>
     * This method is the same as the <code>prepareStatement</code> method
     * above, but it allows the default result set
     * type, concurrency, and holdability to be overridden.
     *
     * @param sql a <code>String</code> object that is the SQL statement to
     *            be sent to the database; may contain one or more '?' IN
     *            parameters
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
     * @return a new <code>PreparedStatement</code> object, containing the
     *         pre-compiled SQL statement, that will generate
     *         <code>ResultSet</code> objects with the given type,
     *         concurrency, and holdability
     * @exception SQLException if a database access error occurs, this 
     * method is called on a closed connection 
     *            or the given parameters are not <code>ResultSet</code> 
     *            constants indicating type, concurrency, and holdability
      * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method or this method is not supported for the specified result 
     * set type, result set holdability and result set concurrency.
     * @see ResultSet
     * @since 1.4
     */
    public PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        
        return prepareStatement(sql, resultSetType, resultSetConcurrency,
            resultSetHoldability, false, false);
    }
    
    protected PreparedStatement prepareMetaDataStatement(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
        
        return prepareStatement(sql, resultSetType, resultSetConcurrency,
            resultSetHoldability, true, false);
    }
    
    /**
     * Creates a default <code>PreparedStatement</code> object that has
     * the capability to retrieve auto-generated keys. The given constant
     * tells the driver whether it should make auto-generated keys
     * available for retrieval.  This parameter is ignored if the SQL statement
     * is not an <code>INSERT</code> statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     * <P>
     * <B>Note:</B> This method is optimized for handling
     * parametric SQL statements that benefit from precompilation. If
     * the driver supports precompilation,
     * the method <code>prepareStatement</code> will send
     * the statement to the database for precompilation. Some drivers
     * may not support precompilation. In this case, the statement may
     * not be sent to the database until the <code>PreparedStatement</code> 
     * object is executed.  This has no direct effect on users; however, it does
     * affect which methods throw certain SQLExceptions.
     * <P>
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>. 
     * The holdability of the created result sets can be determined by 
     * calling {@link #getHoldability}.
     *
     * @param sql an SQL statement that may contain one or more '?' IN
     *        parameter placeholders
     * @param autoGeneratedKeys a flag indicating whether auto-generated keys 
     *        should be returned; one of
     *        <code>Statement.RETURN_GENERATED_KEYS</code> or
     *        <code>Statement.NO_GENERATED_KEYS</code>  
     * @return a new <code>PreparedStatement</code> object, containing the
     *         pre-compiled SQL statement, that will have the capability of
     *         returning auto-generated keys
     * @exception SQLException if a database access error occurs, this
     *  method is called on a closed connection 
     *         or the given parameter is not a <code>Statement</code>
     *         constant indicating whether auto-generated keys should be
     *         returned
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method with a constant of Statement.RETURN_GENERATED_KEYS
     * @since 1.4
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException {
    	
    	checkValidity();
        if (autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS) {
            checkAutoGeneratedKeysSupport();
        }
        
        GeneratedKeysQuery query = new GeneratedKeysQuery(sql, autoGeneratedKeys);
        return prepareStatement(query);
    }

    /**
     * Creates a default <code>PreparedStatement</code> object capable
     * of returning the auto-generated keys designated by the given array.
     * This array contains the indexes of the columns in the target
     * table that contain the auto-generated keys that should be made
     * available.  The driver will ignore the array if the SQL statement
     * is not an <code>INSERT</code> statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     *<p>
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code> object. This
     * object can then be used to efficiently execute this statement
     * multiple times.
     * <P>
     * <B>Note:</B> This method is optimized for handling
     * parametric SQL statements that benefit from precompilation. If
     * the driver supports precompilation,
     * the method <code>prepareStatement</code> will send
     * the statement to the database for precompilation. Some drivers
     * may not support precompilation. In this case, the statement may
     * not be sent to the database until the <code>PreparedStatement</code> 
     * object is executed.  This has no direct effect on users; however, it does
     * affect which methods throw certain SQLExceptions.
     * <P>
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>. 
     * The holdability of the created result sets can be determined by 
     * calling {@link #getHoldability}.
     *
     * @param sql an SQL statement that may contain one or more '?' IN
     *        parameter placeholders
     * @param columnIndexes an array of column indexes indicating the columns
     *        that should be returned from the inserted row or rows 
     * @return a new <code>PreparedStatement</code> object, containing the
     *         pre-compiled statement, that is capable of returning the
     *         auto-generated keys designated by the given array of column
     *         indexes
     * @exception SQLException if a database access error occurs 
     * or this method is called on a closed connection
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     *
     * @since 1.4
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException {
    	checkValidity();
        checkAutoGeneratedKeysSupport();

        GeneratedKeysQuery query = new GeneratedKeysQuery(sql, columnIndexes);
        return prepareStatement(query);
    }

    /**
     * Creates a default <code>PreparedStatement</code> object capable
     * of returning the auto-generated keys designated by the given array.
     * This array contains the names of the columns in the target
     * table that contain the auto-generated keys that should be returned.
     * The driver will ignore the array if the SQL statement
     * is not an <code>INSERT</code> statement, or an SQL statement able to return
     * auto-generated keys (the list of such statements is vendor-specific).
     * <P>
     * An SQL statement with or without IN parameters can be
     * pre-compiled and stored in a <code>PreparedStatement</code> object. This
     * object can then be used to efficiently execute this statement
     * multiple times.
     * <P>
     * <B>Note:</B> This method is optimized for handling
     * parametric SQL statements that benefit from precompilation. If
     * the driver supports precompilation,
     * the method <code>prepareStatement</code> will send
     * the statement to the database for precompilation. Some drivers
     * may not support precompilation. In this case, the statement may
     * not be sent to the database until the <code>PreparedStatement</code> 
     * object is executed.  This has no direct effect on users; however, it does
     * affect which methods throw certain SQLExceptions.
     * <P>
     * Result sets created using the returned <code>PreparedStatement</code>
     * object will by default be type <code>TYPE_FORWARD_ONLY</code>
     * and have a concurrency level of <code>CONCUR_READ_ONLY</code>. 
     * The holdability of the created result sets can be determined by 
     * calling {@link #getHoldability}.
     *
     * @param sql an SQL statement that may contain one or more '?' IN
     *        parameter placeholders
     * @param columnNames an array of column names indicating the columns
     *        that should be returned from the inserted row or rows 
     * @return a new <code>PreparedStatement</code> object, containing the
     *         pre-compiled statement, that is capable of returning the
     *         auto-generated keys designated by the given array of column
     *         names
     * @exception SQLException if a database access error occurs 
     * or this method is called on a closed connection
     * @exception java.sql.SQLFeatureNotSupportedException if the JDBC driver does not support
     * this method
     *
     * @since 1.4
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException {
        checkValidity();
        checkAutoGeneratedKeysSupport();

        GeneratedKeysQuery query = new GeneratedKeysQuery(sql, columnNames);
        return prepareStatement(query);
    }
    
    /**
     * Prepares a statement for generated keys.
     * 
     * @param query AbstractGeneratedKeysQuery instance
     * @return PreparedStatement object
     * @throws SQLException if a database access error occurs 
     * or this method is called on a closed connection
     */
    private PreparedStatement prepareStatement(AbstractGeneratedKeysQuery query) throws SQLException {
        if (query.generatesKeys()) {
            return prepareStatement(query.getQueryString(), 
                    ResultSet.TYPE_FORWARD_ONLY, 
                    ResultSet.CONCUR_READ_ONLY, 
                    ResultSet.CLOSE_CURSORS_AT_COMMIT, 
                    false, true);
        } else {
            return prepareStatement(query.getQueryString());
        }
    }
    
    /**
     * Helper method to check support of autoGeneratedKeys
     * 
     * @throws SQLException If the feature is not supported, or if an error occurred retrieving the server version.
     */
    protected void checkAutoGeneratedKeysSupport() throws SQLException {
        GDSHelper gdsHelper = getGDSHelper();
        if (gdsHelper.compareToVersion(2, 0) < 0) {
            throw new FBDriverNotCapableException("This version of Firebird does not support retrieving generated keys (support was added in Firebird 2.0)");
        }
    }

    protected PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability, boolean metaData, boolean generatedKeys) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT
                    && resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            checkHoldability(resultSetType, resultSetHoldability);

            FBObjectListener.StatementListener coordinator = txCoordinator;
            if (metaData)
                coordinator = new InternalTransactionCoordinator.MetaDataTransactionCoordinator(txCoordinator);

            FBObjectListener.BlobListener blobCoordinator = metaData ? null : txCoordinator;

            PreparedStatement stmt = new FBPreparedStatement(getGDSHelper(), sql, resultSetType, resultSetConcurrency, resultSetHoldability,
                            coordinator, blobCoordinator, metaData, false, generatedKeys);

            activeStatements.add(stmt);
            return stmt;
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
    public CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency) throws SQLException {
        return prepareCall(sql, resultSetType, resultSetConcurrency, this.resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (resultSetHoldability == ResultSet.HOLD_CURSORS_OVER_COMMIT
                    && resultSetType == ResultSet.TYPE_FORWARD_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeUpgradeReasonHoldability)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            } else if (resultSetType == ResultSet.TYPE_SCROLL_SENSITIVE) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_resultSetTypeDowngradeReasonScrollSensitive)
                        .toFlatSQLException(SQLWarning.class));
                resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            }

            if (resultSetConcurrency != ResultSet.CONCUR_READ_ONLY) {
                addWarning(FbExceptionBuilder
                        .forWarning(JaybirdErrorCodes.jb_concurrencyResetReadOnlyReasonStoredProcedure)
                        .toSQLException(SQLWarning.class));
                resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
            }

            checkHoldability(resultSetType, resultSetHoldability);

            if (storedProcedureMetaData == null) {
                storedProcedureMetaData = StoredProcedureMetaDataFactory.getInstance(this);
            }

            FBCallableStatement stmt = new FBCallableStatement(getGDSHelper(), sql, resultSetType, resultSetConcurrency, resultSetHoldability,
                            storedProcedureMetaData, txCoordinator, txCoordinator);
            activeStatements.add(stmt);

            return stmt;
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
    public Map<String,Class<?>> getTypeMap() throws SQLException {
    	return new HashMap<>();
    }

    public void setTypeMap(Map<String,Class<?>> map) throws SQLException {
        throw new FBDriverNotCapableException();
    }

    private Set<String> clientInfoPropNames = new HashSet<>();
    
    /*
     * Savepoint stuff.  
     */
    
    private final AtomicInteger savepointCounter = new AtomicInteger();
    private final List<FBSavepoint> savepoints = new ArrayList<>();

    private int getNextSavepointCounter() {
        return savepointCounter.getAndIncrement();
    }
    
    public Savepoint setSavepoint() throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            FBSavepoint savepoint = new FBSavepoint(getNextSavepointCounter());
            setSavepoint(savepoint);

            return savepoint;
        }
    }
    
    /**
     * Set the savepoint on the server.
     * 
     * @param savepoint savepoint to set.
     * 
     * @throws SQLException if something went wrong.
     */
    private void setSavepoint(FBSavepoint savepoint) throws SQLException {
        if (getAutoCommit()) {
            throw new SQLException("Connection.setSavepoint() method cannot be used in auto-commit mode.",
                    SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
        }

        if (mc.inDistributedTransaction()) {
            throw new SQLException("Connection enlisted in distributed transaction",
                    SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
        }

        txCoordinator.ensureTransaction();

        getGDSHelper().executeImmediate("SAVEPOINT " + savepoint.getServerSavepointId());
        savepoints.add(savepoint);
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            FBSavepoint savepoint = new FBSavepoint(name);
            setSavepoint(savepoint);

            return savepoint;
        }
    }
    
    public void rollback(Savepoint savepoint) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.rollback(Savepoint) method cannot be used in auto-commit mode.",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            // TODO The error message and actual condition do not match
            if (!(savepoint instanceof FBSavepoint)) {
                throw new SQLException("Specified savepoint was not obtained from this connection.");
            }

            if (mc.inDistributedTransaction()) {
                throw new SQLException("Connection enlisted in distributed transaction",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            FBSavepoint fbSavepoint = (FBSavepoint) savepoint;

            if (!fbSavepoint.isValid())
                throw new SQLException("Savepoint is no longer valid.");

            getGDSHelper().executeImmediate("ROLLBACK TO " + fbSavepoint.getServerSavepointId());
        }
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        synchronized (getSynchronizationObject()) {
            checkValidity();
            if (getAutoCommit()) {
                throw new SQLException("Connection.releaseSavepoint() method cannot be used in auto-commit mode.",
                        SQLStateConstants.SQL_STATE_INVALID_TX_STATE);
            }

            // TODO The error message and actual condition do not match
            if (!(savepoint instanceof FBSavepoint)) {
                throw new SQLException("Specified savepoint was not obtained from this connection.");
            }

            FBSavepoint fbSavepoint = (FBSavepoint) savepoint;

            if (!fbSavepoint.isValid()) {
                throw new SQLException("Savepoint is no longer valid.");
            }

            getGDSHelper().executeImmediate("RELEASE SAVEPOINT " + fbSavepoint.getServerSavepointId() + " ONLY");

            fbSavepoint.invalidate();

            savepoints.remove(fbSavepoint);
        }
    }

    /**
     * Invalidate all savepoints.
     */
    protected void invalidateSavepoints() {
        synchronized (getSynchronizationObject()) {
            for (FBSavepoint savepoint : savepoints) {
                savepoint.invalidate();
            }

            savepoints.clear();
        }
    }    

    //-------------------------------------------
    //Borrowed from javax.resource.cci.Connection

    /**
     * Returns a FBLocalTransaction instance that enables a component to 
     * demarcate resource manager local transactions on this connection.
     */
    public FirebirdLocalTransaction getLocalTransaction() {
        synchronized (getSynchronizationObject()) {
            if (localTransaction == null)
                localTransaction = new FBLocalTransaction(mc, this);

            return localTransaction;
        }
    }

    // java.sql.Wrapper interface
    
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(FBConnection.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());
        
        return iface.cast(this);
    }

    public void setSchema(String schema) throws SQLException {
        // Ignore: no schema support
        checkValidity();
    }

    public String getSchema() throws SQLException {
        checkValidity();
        return null;
    }

    //package methods

    /**
     * Check if this connection is currently involved in a transaction
     */
    public boolean inTransaction() throws SQLException {
        return getGDSHelper().inTransaction();
    }
   
    /**
     * Get the encoding that is being used for this connection.
     *
     * @return The name of the encoding used
     */
    public String getIscEncoding() throws SQLException {
        return getGDSHelper().getIscEncoding();
    }

	 public void addWarning(SQLWarning warning){
         synchronized (getSynchronizationObject()) {
             // TODO: Find way so this method can be protected (or less visible) again.
             if (firstWarning == null)
                 firstWarning = warning;
             else {
                 firstWarning.setNextWarning(warning);
             }
         }
	 }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #createClob()}.
     * </p>
     */
    public NClob createNClob() throws SQLException {
        return (NClob) createClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        checkValidity();
        throw new FBDriverNotCapableException("Type SQLXML not supported");
    }

    public GDSHelper getGDSHelper() throws SQLException {
        if (mc == null)
            // TODO Right error code?
            throw new FbExceptionBuilder().exception(ISCConstants.isc_req_no_trans).toSQLException();

        return mc.getGDSHelper();
    }

    @Override
    public boolean isUseFirebirdAutoCommit() {
        DatabaseParameterBuffer dpb = getDatabaseParameterBuffer();
        return dpb != null && dpb.hasArgument(USE_FIREBIRD_AUTOCOMMIT);
    }
    
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * Checks if client info is supported.
     *
     * @throws SQLException If the client info is not supported, or if there is no database connection.
     */
    protected void checkClientInfoSupport() throws SQLException {
        if (!getFbDatabase().getServerVersion().isEqualOrAbove(2, 0)) {
            throw new FBDriverNotCapableException(
                    "Required functionality (RDB$SET_CONTEXT()) only available in Firebird 2.0 or higher");
        }
    }
    
    public Properties getClientInfo() throws SQLException {
        checkValidity();
        checkClientInfoSupport();

        Properties result = new Properties();
        try (PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL)) {
            for (String propName : clientInfoPropNames) {
                result.put(propName, getClientInfo(stmt, propName));
            }
        }
    
        return result;
    }

    public String getClientInfo(String name) throws SQLException {
        checkValidity();
        checkClientInfoSupport();

        try (PreparedStatement stmt = prepareStatement(GET_CLIENT_INFO_SQL)) {
            return getClientInfo(stmt, name);
        }
    }

    protected String getClientInfo(PreparedStatement stmt, String name) throws SQLException {
        stmt.clearParameters();
    
        stmt.setString(1, name);
        stmt.setString(2, name);

        try (ResultSet rs = stmt.executeQuery()) {
            if (!rs.next())
                return null;

            String sessionContext = rs.getString(1);
            String transactionContext = rs.getString(2);

            if (transactionContext != null)
                return transactionContext;
            else if (sessionContext != null)
                return sessionContext;
            else
                return null;

        }
    
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        SQLExceptionChainBuilder<SQLClientInfoException> chain = new SQLExceptionChainBuilder<>();
        try {
            checkValidity();
            checkClientInfoSupport();

            try (PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL)) {
                for (String propName : properties.stringPropertyNames()) {
                    String propValue = properties.getProperty(propName);

                    try {
                        setClientInfo(stmt, propName, propValue);
                    } catch (SQLClientInfoException ex) {
                        chain.append(ex);
                    }
                }
            }
    
        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }
    
        if (chain.hasException())
            throw chain.getException();
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        try {
            checkValidity();
            checkClientInfoSupport();

            try (PreparedStatement stmt = prepareStatement(SET_CLIENT_INFO_SQL)) {
                setClientInfo(stmt, name, value);
            }
        } catch (SQLException ex) {
            throw new SQLClientInfoException(ex.getMessage(), ex.getSQLState(), null, ex);
        }
    }

    protected void setClientInfo(PreparedStatement stmt, String name, String value) throws SQLException {
        try {
            stmt.clearParameters();
            stmt.setString(1, name);
            stmt.setString(2, value);
    
            ResultSet rs = stmt.executeQuery();
            if (!rs.next())
                throw new FBDriverConsistencyCheckException(
                        "Expected result from RDB$SET_CONTEXT call");
    
            // needed, since the value is set on fetch!!!
            rs.getInt(1);
    
        } catch (SQLException ex) {
            throw new SQLClientInfoException(null, ex);
        }
    }

    public void abort(Executor executor) throws SQLException {
        // TODO Write implementation
        checkValidity();
        throw new FBDriverNotCapableException();
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        // TODO Write implementation
        checkValidity();
        throw new FBDriverNotCapableException();
    }

    public int getNetworkTimeout() throws SQLException {
        // TODO Write implementation
        checkValidity();
        return 0;
    }

    @Override
    public final Object getSynchronizationObject() {
        final FBManagedConnection managedConnection = mc;
        if (managedConnection != null) {
            return managedConnection.getSynchronizationObject();
        } else {
            return this;
        }
    }

    protected class GeneratedKeysQuery extends AbstractGeneratedKeysQuery {
        protected GeneratedKeysQuery(String sql, int autoGeneratedKeys) throws SQLException {
            super(sql, autoGeneratedKeys);
        }
        
        protected GeneratedKeysQuery(String sql, int[] columnIndexes) throws SQLException {
            super(sql, columnIndexes);
        }
        
        protected GeneratedKeysQuery(String sql, String[] columnNames) throws SQLException {
            super(sql, columnNames);
        }

        @Override
        DatabaseMetaData getDatabaseMetaData() throws SQLException {
            return getMetaData();
        }
    }
	 
}
