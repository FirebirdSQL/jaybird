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
public class FBProcedureParam {
    
    private boolean isParam;
    private Object value;
    private int position;
    private int type;
    private int index = -1;
    
    public FBProcedureParam() {
    }
    
    public FBProcedureParam(int position, String paramValue) {
        this.position = position;
        this.isParam = "?".equals(paramValue.trim());
        this.value = paramValue.trim(); 
    }
    
    public boolean isParam() {
        return isParam;
    }
    
    public int getPosition() {
        return position;
    }
    
    public int getIndex() {
        return index;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) throws SQLException {
        if (!isParam)
            throw new FBSQLException(
                    "Cannot set parameter, since it is constant.",
                    FBSQLException.SQL_STATE_INVALID_PARAM_TYPE);
        
        this.value = value;
    }
    
    public int getType() {
        return type;
    }
    
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
}