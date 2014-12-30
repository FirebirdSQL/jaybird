/*
 * $Id$
 * 
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

import java.sql.SQLException;
import java.sql.Savepoint;

/**
 * Savepoint implementation.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@SuppressWarnings("deprecation")
public class FBSavepoint implements Savepoint, FirebirdSavepoint {
    public static final String SAVEPOINT_ID_PREFIX = "svpt";

    private boolean valid = true;
    private int savepointId;
    private String name;
    private String serverId;
    
    /**
     * Create instance of this class.
     * 
     * @param id ID of the savepoint.
     */
    public FBSavepoint(int id) {
        this.savepointId = id;
        this.serverId = getSavepointServerId(id);
    }
    
    /**
     * Create instance of this class for the specified name.
     * 
     * @param name name of the savepoint.
     */
    public FBSavepoint(String name) {
        this.name = name;
        this.serverId = getSavepointServerId(name);
    }
    
    /**
     * Generate a savepoint ID for the specified savepoint counter.
     *  
     * @param counter savepoint counter.
     * @return valid savepoint ID.
     */
    private String getSavepointServerId(int counter) {
        return SAVEPOINT_ID_PREFIX + counter;
    }
    
    /**
     * Generate a savepoint ID for the specified name.
     * 
     * @param name name of the savepoint.
     * 
     * @return valid savepoint ID.
     */
    private String getSavepointServerId(String name) {
        StringBuffer sb = new StringBuffer();
        
        sb.append('"');
        
        char[] data = name.toCharArray();
        for (int i = 0; i < data.length; i++) {
            
            // we have to double quote quotes
            if (data[i] == '"') sb.append('"');
            
            sb.append(data[i]);
        }
        
        sb.append('"');
        
        return sb.toString();
    }
    
    /**
     * Get SQL server savepoint ID. This method generates correct ID for the 
     * savepoint that can be directly used in the SQL statement.
     * 
     * @return valid server-side ID for the savepoint.
     */
    String getServerSavepointId() {
        return serverId;
    }
    
    /**
     * Get ID of the savepoint.
     */
    public int getSavepointId() throws SQLException {
        if (name == null)
            return savepointId;
        else
            throw new SQLException("Savepoint is named.");
    }

    /**
     * Get name of the savepoint.
     */
    public String getSavepointName() throws SQLException {
        if (name == null)
            throw new SQLException("Savepoint is unnamed.");
        else
            return name;
    }
    
    /**
     * Check if this savepoint is named. This method is used internally to avoid
     * unnecessary exception throwing.
     * 
     * @return <code>true</code> if savepoint is named.
     */
    boolean isNamed() {
        return name == null;
    }
    
    /**
     * Check if the savepoint is valid.
     * 
     * @return <code>true</code> if savepoint is valid.
     */
    boolean isValid() {
        return valid;
    }
    
    /**
     * Make this savepoint invalid. 
     */
    void invalidate() {
        this.valid = false;
    }
    
    /**
     * Check if objects are equal. For unnamed savepoints their IDs are checked,
     * otherwise their names.
     * 
     * @param obj object to test.
     * 
     * @return <code>true</code> if <code>obj</code> is equal to this object.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBSavepoint)) return false;
        
        FBSavepoint that = (FBSavepoint)obj;
        
        return this.name == null ? 
            this.savepointId == that.savepointId : 
            this.name.equals(that.name); 
    }
    
    /**
     * Get hash code of this instance.
     */
    public int hashCode() {
        return name == null ? savepointId : name.hashCode();
    }

}
