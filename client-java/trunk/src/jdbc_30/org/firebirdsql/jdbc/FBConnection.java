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
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Iterator;
import java.util.LinkedList;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.jca.FBManagedConnection;

/**
 * Firebird connection class implementing JDBC 3.0 methods.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnection extends AbstractConnection {

	/**
     * Create instance of this class for the specified managed connection.
     * 
	 * @param mc managed connection.
	 */
	public FBConnection(FBManagedConnection mc) {
		super(mc);
	}
    
	/**
	 * Commit current transaction.
     * 
     * @throws SQLException if something went wrong.
	 */
	public synchronized void commit() throws SQLException {
		super.commit();
        
        invalidateSavepoints();
	}

	/**
     * Rollback current transaction.
     * 
     * @throws SQLException if something went wrong.
	 */
    public synchronized void rollback() throws SQLException {
		super.rollback();
        
        invalidateSavepoints();
	}

    
    // ---------- JDBC 3.0 stuff -----------
    
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
    private synchronized void invalidateSavepoints() {
        Iterator iter = savepoints.iterator();
        while(iter.hasNext())
            ((FBSavepoint)iter.next()).invalidate();
        
        savepoints.clear();
    }
    
}
