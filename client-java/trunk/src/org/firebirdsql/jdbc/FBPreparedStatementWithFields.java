/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

package org.firebirdsql.jdbc;

import java.sql.*;
import java.io.InputStream;

import org.firebirdsql.gds.XSQLVAR;

public class FBPreparedStatementWithFields extends FBPreparedStatement {

    FBPreparedStatementWithFields(FBConnection c, String sql) throws SQLException {
        super(c, sql);
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        //super.setBinaryStream(parameterIndex, inputStream, length);
        getField(parameterIndex).setBinaryStream(inputStream, length);
    }
    
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getField(parameterIndex).setBytes(x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getField(parameterIndex).setBoolean(x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        getField(parameterIndex).setByte(x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        getField(parameterIndex).setDate(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        getField(parameterIndex).setDouble(x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        getField(parameterIndex).setFloat(x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        getField(parameterIndex).setInteger(x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        getField(parameterIndex).setLong(x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        getField(parameterIndex).setObject(x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        getField(parameterIndex).setShort(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        getField(parameterIndex).setString(x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        getField(parameterIndex).setTime(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getField(parameterIndex).setTimestamp(x);
    }

    /**
     * Returns the XSQLVAR structure for the specified column.
     */
    protected XSQLVAR getXsqlvar(int columnIndex) {
        return fixedStmt.getInSqlda().sqlvar[columnIndex - 1];
    }

    /**
     * Factory method for the field access objects
     */
    protected FBField getField(int columnIndex) throws SQLException {
        FBField thisField = FBField.createField(getXsqlvar(columnIndex));

        if (thisField instanceof FBBlobField)
            ((FBBlobField)thisField).setConnection(c);

        return thisField;
    }

    protected boolean internalExecute(boolean sendOutParams) throws SQLException {
        if (c.getAutoCommit()) {
            
            XSQLVAR[] inVars = fixedStmt.getInSqlda().sqlvar;
            
            for(int i = 0; i < inVars.length; i++) {
                if (FBField.isType(inVars[i], Types.BLOB)) {
                    FBBlobField blobField = (FBBlobField)getField(i + 1);
                    blobField.flushCachedData();
                }
            }
        }
        return super.internalExecute(sendOutParams);
    }
    
    
}