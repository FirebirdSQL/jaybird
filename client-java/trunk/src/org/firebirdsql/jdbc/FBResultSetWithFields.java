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

import java.util.ArrayList;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jca.FBManagedConnection;
import java.sql.*;
import java.math.BigDecimal;
import java.io.InputStream;
import java.io.Reader;

public class FBResultSetWithFields extends FBResultSet {

    FBConnection c;
    
    FBResultSetWithFields(FBConnection c, FBStatement fbstatement,
        isc_stmt_handle stmt)
    {
        super(c.mc, fbstatement, stmt);
        this.c = c;
    }

    /**
     * Creates an instance of this class and caches complete result set for
     * later use. This constructor should be used only in auto-commit case.
     * 
     * @param c active database connection
     * @param stmt statement handle
     * @param trimStrings <code>true</code> if we should trim strings (used 
     * in {@link FBDatabaseMetaData} class).
     * @throws SQLException if database access error occurs
     */
    FBResultSetWithFields(FBConnection c, isc_stmt_handle stmt,
        boolean trimStrings)
    throws SQLException {
        super(c.mc, stmt, trimStrings);
        
        this.c = c;
        
        if (c.getAutoCommit()) {
            FBCachedFetcher fetcher = (FBCachedFetcher)fbFetcher;
            for(int i = 0; i < fetcher.rows.size(); i++) {
                cacheBlobIfPresent(
                    (Object[])((FBCachedFetcher)fbFetcher).rows.get(i));
            }
        }
    }

    FBResultSetWithFields(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
        super(xsqlvars, rows);
    }

    void cacheBlobIfPresent(Object[] row) throws SQLException {
        if (row == null)
            return;

        for(int i = 0; i < row.length; i++) {
            XSQLVAR xsqlvar = new XSQLVAR();

            xsqlvar.aliasname = xsqlvars[i].aliasname;
            xsqlvar.ownname = xsqlvars[i].ownname;
            xsqlvar.relname = xsqlvars[i].relname;
            xsqlvar.sqllen = xsqlvars[i].sqllen;
            xsqlvar.sqlname = xsqlvars[i].sqlname;
            xsqlvar.sqltype = xsqlvars[i].sqltype;
            xsqlvar.sqlsubtype = xsqlvars[i].sqlsubtype;

            xsqlvar.sqldata = row[i];
            xsqlvar.sqlind = row[i] != null ? 0 : -1;

            // if this is BLOB field and we're in autocommit, get the cached copy
            if (FBField.isType(xsqlvar, Types.BLOB) && row[i] != null) {
                FBBlobField blob = (FBBlobField)FBField.createField(xsqlvar);
                blob.setConnection(c);
                row[i] = blob.getCachedObject();
            }
        }
    }


    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return getField(columnIndex).getAsciiStream();
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return getField(columnIndex).getBigDecimal();
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return getField(columnIndex).getBinaryStream();
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return super.getBlob(columnIndex);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return getField(columnIndex).getBoolean();
    }

    public byte getByte(int columnIndex) throws SQLException {
        return getField(columnIndex).getByte();
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return getField(columnIndex).getBytes();
    }

    public Date getDate(int columnIndex) throws SQLException {
        return getField(columnIndex).getDate();
    }

    public double getDouble(int columnIndex) throws SQLException {
        return getField(columnIndex).getDouble();
    }

    public float getFloat(int columnIndex) throws SQLException {
        return getField(columnIndex).getFloat();
    }

    public int getInt(int columnIndex) throws SQLException {
        return getField(columnIndex).getInt();
    }

    public long getLong(int columnIndex) throws SQLException {
        return getField(columnIndex).getLong();
    }

    public Object getObject(int columnIndex) throws SQLException {
        return getField(columnIndex).getObject();
    }

    public short getShort(int columnIndex) throws SQLException {
        return getField(columnIndex).getShort();
    }

    public String getString(int columnIndex) throws SQLException {
        if (trimStrings) {
            String result = getField(columnIndex).getString();
            return result != null ? result.trim() : null;
        } else
            return getField(columnIndex).getString();
    }

    public Time getTime(int columnIndex) throws SQLException {
        return getField(columnIndex).getTime();
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return getField(columnIndex).getTimestamp();
    }

    /**
     * @deprecated
     */
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return getField(columnIndex).getUnicodeStream();
    }

    /**
     * Returns the XSQLVAR structure for the specified column.
     */
    protected XSQLVAR getXsqlvar(int columnIndex) {
        return xsqlvars[columnIndex - 1];
    }

    /**
     * Factory method for the field access objects
     */
    protected FBField getField(int columnIndex) throws SQLException {
        FBField thisField = FBField.createField(getXsqlvar(columnIndex));

        if (thisField instanceof FBBlobField)
            ((FBBlobField)thisField).setConnection(c);

        setWasNullColumnIndex(columnIndex);

        return thisField;
    }

}