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

import java.sql.SQLException;



/**
 * Represents procedure call parameter.
 */
public class FBProcedureParam implements Cloneable {
    
    private boolean isParam;
    private boolean isLiteral;
    private Object value;
    private String paramValue;
    private int position;
    private int type;
    private int index = -1;
    private boolean valueSet;
    
    public FBProcedureParam() {
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone()
    {
    	try
    	{
    		return super.clone();
    	}
    	catch (CloneNotSupportedException e)
    	{
    		return null;
    	}
    }
    
    /**
     * Create a new <code>FBProcedureParam</code> instance.
     *
     * @param position The position at which this parameter is situated in 
     * the call
     * @param paramValue The value for this parameter
     */
    public FBProcedureParam(int position, String paramValue) {
        this.position = position;
        this.isLiteral = paramValue.startsWith("'") && paramValue.endsWith("'");
        this.isParam = !isLiteral && paramValue.indexOf('?') >= 0;
        this.paramValue = paramValue.trim();
    }
    
    /**
     * Check if this parameter is a variable input parameter
     *
     * @return <code>true</code> if this is an input parameter, 
     * <code>false</code> otherwise
     */
    public boolean isParam() {
        return isParam;
    }
    
    /**
     * Get the position of this parameter
     *
     * @return The index of this parameter (first index is 1)
     */
    public int getPosition() {
        return position;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * Get the variable value of this parameter
     *
     * @return The parameter value
     */
    public String getParamValue() {
    	return paramValue;    
    }
    
    /**
     * Set the value for this parameter
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set the variable value of this parameter
     *
     * @param value The value to be set
     * @throws SQLException if this parameter contains a constant value
     */
    public void setValue(Object value) throws SQLException {
        if (!isParam)
            throw new FBSQLException(
                    "Cannot set parameter, since it is constant.",
                    FBSQLException.SQL_STATE_INVALID_PARAM_TYPE);
        
        this.value = value;
        this.valueSet = true;
    }
    
    /**
     * Check if the value of this parameter has been set
     * 
     * @return <code>true</code> if the value has been set, 
     * <code>false</code> otherwise
     */
    public boolean isValueSet() {
        return valueSet;
    }
    
    /**
     * Get the SQL type of this paramater.
     *
     * @return The SQL type of this parameter
     */
    public int getType() {
        return type;
    }
    
    /**
     * Set the SQL type of this parameter
     *
     * @param type The SQL type of this parameter
     */
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FBProcedureParam)) return false;
        
        FBProcedureParam that = (FBProcedureParam)obj;
        
        return this.position == that.position &&
        this.value != null ? this.value.equals(that.value) : 
                           that.value == null;
    }
    
    public int hashCode() {
        int hashCode = 887;
        hashCode = 31 * hashCode + position;
        hashCode = 31 * hashCode + (value != null ? value.hashCode() : 0);
        return hashCode;
    }
}