/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.

 */

package org.firebirdsql.jdbc;


// imports --------------------------------------




//import javax.resource.cci.Connection;--can't import, two classes with same name.
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.HashMap;
import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.jca.FBLocalTransaction;
import org.firebirdsql.jca.FBManagedConnection;
import java.util.Map;



/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */

//Note that this connection class is only a handle to a ManagedConnection.
/**
 * <P>A connection (session) with a specific
 * database. Within the context of a Connection, SQL statements are
 * executed and results are returned.
 *
 * <P>A Connection's database is able to provide information
 * describing its tables, its supported SQL grammar, its stored
 * procedures, the capabilities of this connection, and so on. This
 * information is obtained with the <code>getMetaData</code> method.
 *
 * <P><B>Note:</B> By default the Connection automatically commits
 * changes after executing each statement. If auto commit has been
 * disabled, the method <code>commit</code> must be called explicitly;
 * otherwise, database changes will not be saved.
 *
 * @see DriverManager#getConnection
 * @see Statement
 * @see ResultSet
 * @see DatabaseMetaData
 <P>
 * Methods that are new in the JDBC 2.0 API are tagged @since 1.2.
 */
public class FBConnection implements Connection/*, javax.resource.cci.Connection*/ {
    //flag that is set to true when a transaction is started automatically,
    //so the transaction may be committed automatically after a
    //statement is executed.
    private boolean autoTransaction = false;

    //Autocommit flag.  This should be left true if you are using Local or
    //XATransactions and want to execute statements outside a transaction.
    //Set it false only if you use the Connection.commit and rollback methods.
    private boolean autoCommit = true;

    FBManagedConnection mc;

    FBLocalTransaction localTransaction = null;

    FBDatabaseMetaData metaData = null;

    public FBConnection(FBManagedConnection mc) {
        this.mc = mc;
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
        return new FBStatement(this);
    }


    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @param param3 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public Statement createStatement(int param1, int param2, int param3) throws SQLException {
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
    public PreparedStatement prepareStatement(String sql)
        throws SQLException {
        return new FBPreparedStatement(this, sql);
    }


    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public PreparedStatement prepareStatement(String param1, int param2) throws SQLException {
        // TODO: implement this java.sql.Connection method
        return null;
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
    public PreparedStatement prepareStatement(String param1, int param2, int param3, int param4) throws SQLException {
        // TODO: implement this java.sql.Connection method
        return null;
    }


    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public PreparedStatement prepareStatement(String param1, int[] param2) throws SQLException {
        // TODO: implement this java.sql.Connection method
        return null;
    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @param param2 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public PreparedStatement prepareStatement(String param1, String[] param2) throws SQLException {
        // TODO: implement this java.sql.Connection method
        return null;
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
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new FBCallableStatement(this, sql);
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
    public CallableStatement prepareCall(String param1, int param2, int param3, int param4) throws SQLException {
        // TODO: implement this java.sql.Connection method
        return null;
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
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (this.autoCommit != autoCommit && inTransaction())
            try {
                getLocalTransaction().commit();
            } catch(javax.resource.ResourceException resex) {
                throw new SQLException(resex.toString());
            }

        this.autoCommit = autoCommit;
    }


    /**
     * Gets the current auto-commit state.
     *
     * @return the current state of auto-commit mode
     * @exception SQLException if a database access error occurs
     * @see #setAutoCommit
     */
    public boolean getAutoCommit() {
        return autoCommit;
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
        if (getAutoCommit())
        {
            throw new SQLException("commit called with AutoCommit true!");
        } // end of if ()

        try {
            if (inTransaction())
            {
                getLocalTransaction().commit();
            } // end of if ()
        } catch(javax.resource.ResourceException resex) {
            throw new SQLException(resex.toString());
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
        if (getAutoCommit())
        {
            throw new SQLException("rollback called with AutoCommit true!");
        } // end of if ()
        if (isClosed())
            throw new SQLException("You cannot rollback closed connection.");
        try{
            if (inTransaction())
            {
                getLocalTransaction().rollback();
            } // end of if ()
        } catch(javax.resource.ResourceException resex) {
            throw new SQLException(resex.toString());
        }
    }



    /**
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public void rollback(Savepoint param1) throws SQLException {
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
    public void close() {
        if (mc != null) {
            mc.close(this);
            mc = null;
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
    public DatabaseMetaData getMetaData() throws SQLException {
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
    public void setReadOnly(boolean readOnly) throws SQLException {
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
    public void setCatalog(String catalog) throws SQLException {
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
     * Indicates that transactions are not supported.
     */
//    int TRANSACTION_NONE       = 0;

    /**
     * Dirty reads, non-repeatable reads and phantom reads can occur.
     * This level allows a row changed by one transaction to be read
     * by another transaction before any changes in that row have been
     * committed (a "dirty read").  If any of the changes are rolled back,
     * the second transaction will have retrieved an invalid row.
     */
//    int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     * Dirty reads are prevented; non-repeatable reads and phantom
     * reads can occur.  This level only prohibits a transaction
     * from reading a row with uncommitted changes in it.
     */
//    int TRANSACTION_READ_COMMITTED   = 2;

    /**
     * Dirty reads and non-repeatable reads are prevented; phantom
     * reads can occur.  This level prohibits a transaction from
     * reading a row with uncommitted changes in it, and it also
     * prohibits the situation where one transaction reads a row,
     * a second transaction alters the row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
     */
//    int TRANSACTION_REPEATABLE_READ  = 4;

    /**
     * Dirty reads, non-repeatable reads and phantom reads are prevented.
     * This level includes the prohibitions in
     * TRANSACTION_REPEATABLE_READ and further prohibits the
     * situation where one transaction reads all rows that satisfy
     * a WHERE condition, a second transaction inserts a row that
     * satisfies that WHERE condition, and the first transaction
     * rereads for the same condition, retrieving the additional
     * "phantom" row in the second read.
     */
//    int TRANSACTION_SERIALIZABLE     = 8;

    /**
     * Attempts to change the transaction
     * isolation level to the one given.
     * The constants defined in the interface <code>Connection</code>
     * are the possible transaction isolation levels.
     *
     * <P><B>Note:</B> This method cannot be called while
     * in the middle of a transaction.
     *
     * @param level one of the TRANSACTION_* isolation values with the
     * exception of TRANSACTION_NONE; some databases may not support
     * other values
     * @exception SQLException if a database access error occurs
     * @see DatabaseMetaData#supportsTransactionIsolationLevel
     */
    public void setTransactionIsolation(int level) throws SQLException {
        switch (level) {
            case TRANSACTION_SERIALIZABLE :
                mc.setTransactionIsolation(GDS.isc_tpb_consistency);
                break;
            case TRANSACTION_REPEATABLE_READ :
                mc.setTransactionIsolation(GDS.isc_tpb_concurrency);
                break;
            case TRANSACTION_READ_COMMITTED :
                mc.setTransactionIsolation(GDS.isc_tpb_read_committed);
                break;
            default: throw new SQLException("Unsupported transaction isolation level");
        }
    }


    /**
     * Gets this Connection's current transaction isolation level.
     *
     * @return the current TRANSACTION_* mode value
     * @exception SQLException if a database access error occurs
     */
    public int getTransactionIsolation() throws SQLException {
        switch (mc.getTransactionIsolation()) {
            case GDS.isc_tpb_consistency : return TRANSACTION_SERIALIZABLE;
            case GDS.isc_tpb_concurrency : return TRANSACTION_REPEATABLE_READ;
            case GDS.isc_tpb_read_committed : return TRANSACTION_READ_COMMITTED;
            default: throw new SQLException("Unknown transaction isolation level");
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
        return null;
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
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException {
        return createStatement();
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
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                    int resultSetConcurrency) throws SQLException {
        return null;
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
    public CallableStatement prepareCall(String sql, int resultSetType,
                 int resultSetConcurrency) throws SQLException {
        return null;
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
    public void setTypeMap(java.util.Map map) throws SQLException {
        throw new SQLException("Not yet implemented");
    }

    /**
     *
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public void setHoldability(int param1) throws SQLException {
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
    public Savepoint setSavepoint() throws SQLException {
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
    public Savepoint setSavepoint(String param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");

    }

    /**
     * jdbc 3
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public void releaseSavepoint(Savepoint param1) throws SQLException {
        // TODO: implement this java.sql.Connection method
        throw new SQLException("Not yet implemented");
    }



    //-------------------------------------------
    //Borrowed from javax.resource.cci.Connection

    public FBLocalTransaction getLocalTransaction() {
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
    public Blob createBlob() throws SQLException {
        return new FBBlob(mc, 0);
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
    void ensureInTransaction() throws ResourceException
    {
        if (inTransaction())
        {
            autoTransaction = false;
            return;
        } // end of if ()
        //We have to start our own transaction
        getLocalTransaction().begin();
        autoTransaction = true;
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
    boolean willEndTransaction()
    {
        return getAutoCommit() && autoTransaction;
    }

    void checkEndTransaction() throws SQLException
    {
        if (willEndTransaction())
        {
            autoTransaction = false;
            try
            {
                getLocalTransaction().commit();
            }
            catch (ResourceException re)
            {
                throw new SQLException("Error during autocommit: " + re);
            } // end of catch

        } // end of if ()
    }


}







