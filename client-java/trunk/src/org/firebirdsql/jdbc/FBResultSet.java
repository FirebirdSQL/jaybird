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
import java.math.BigDecimal;
import java.util.Calendar;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Types;

import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.isc_stmt_handle;
import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.logging.Logger;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


/**
 * Describe class <code>FBResultSet</code> here.
 *
 *   @see <related>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 *   @version $ $ 
 */



public class FBResultSet implements ResultSet {

   private final Logger log = Logger.getLogger(getClass());

    protected FBFetcher fbFetcher;

    private FBConnection c;

    private XSQLVAR[] xsqlvars;

    private Object[] row = null;

    protected int rowNum = 0;
     protected int maxRows = 0;
     protected int fetchSize = 0;
     
     private boolean isEmpty = false;
     
     private boolean isBeforeFirst = false;
     private boolean isFirst = false;
     private boolean isLast = false;
     private boolean isAfterLast = false;
     
    private int wasNullColumnIndex = -1;

    //might be a bit of a kludge, or a useful feature.
    protected boolean trimStrings;

     java.sql.SQLWarning firstWarning = null;
     
    /**
     * Creates a new <code>FBResultSet</code> instance.
     *
     * @param c a <code>FBConnection</code> value
     * @param fbstatement a <code>FBStatement</code> value
     * @param stmt an <code>isc_stmt_handle</code> value
     */
    FBResultSet(FBConnection c, FBStatement fbstatement, isc_stmt_handle stmt) 
     throws SQLException {
        this.c = c;
        xsqlvars = stmt.getOutSqlda().sqlvar;
          maxRows = fbstatement.getMaxRows();
        fbFetcher = new FBStatementFetcher(this.c, fbstatement, stmt);
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
    FBResultSet(FBConnection c, FBStatement fbStatement,isc_stmt_handle stmt, boolean trimStrings) throws SQLException {
        this.c = c;
        this.trimStrings = trimStrings;
          maxRows = fbStatement.getMaxRows();
        xsqlvars = stmt.getOutSqlda().sqlvar;
        fbFetcher = new FBCachedFetcher(this.c, fbStatement,stmt);
        //use willEndTransaction rather than getAutoCommit so blobs are cached only when transactions are
        //automatically ended.  Using jca framework, getAutoCommit is always true.
        if (c.willEndTransaction()) 
        {
            FBCachedFetcher fetcher = (FBCachedFetcher)fbFetcher;
        }
    }

    FBResultSet(XSQLVAR[] xsqlvars, ArrayList rows) throws SQLException {
          maxRows = 0;
          fbFetcher = new FBCachedFetcher(rows);
        this.xsqlvars = xsqlvars;
    }



    /**
     * Moves the cursor down one row from its current position.
     * A <code>ResultSet</code> cursor is initially positioned
     * before the first row; the first call to the method
     * <code>next</code> makes the first row the current row; the
     * second call makes the second row the current row, and so on.
     *
     * <P>If an input stream is open for the current row, a call
     * to the method <code>next</code> will
     * implicitly close it. A <code>ResultSet</code> object's
     * warning chain is cleared when a new row is read.
     *
     * @return <code>true</code> if the new current row is valid;
     * <code>false</code> if there are no more rows
     * @exception SQLException if a database access error occurs
     */
    public boolean next() throws  SQLException {
         return fbFetcher.next();
    }



    /**
     * Releases this <code>ResultSet</code> object's database and
     * JDBC resources immediately instead of waiting for
     * this to happen when it is automatically closed.
     *
     * <P><B>Note:</B> A <code>ResultSet</code> object
     * is automatically closed by the
     * <code>Statement</code> object that generated it when
     * that <code>Statement</code> object is closed,
     * re-executed, or is used to retrieve the next result from a
     * sequence of multiple results. A <code>ResultSet</code> object
     * is also automatically closed when it is garbage collected.
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws  SQLException {
        fbFetcher.close();
    }


    /**
     * Reports whether
     * the last column read had a value of SQL <code>NULL</code>.
     * Note that you must first call one of the <code>getXXX</code> methods
     * on a column to try to read its value and then call
     * the method <code>wasNull</code> to see if the value read was
     * SQL <code>NULL</code>.
     *
     * @return <code>true</code> if the last column value read was SQL
     *         <code>NULL</code> and <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     */
    public boolean wasNull() throws  SQLException {
        if (wasNullColumnIndex == -1) {
            throw new SQLException("look at a column before testing null!");
        }
        if (row == null) {
            throw new SQLException("No row available for wasNull!");
        }
        return row[wasNullColumnIndex - 1] == null;
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
        /*
        if ((getXsqlvar(columnIndex).sqltype & ~1) != GDS.SQL_BLOB) {
            throw new SQLException("Wrong type for column " + columnIndex + "type should be" + getXsqlvar(columnIndex).sqltype);
        }
        setWasNullColumnIndex(columnIndex);
        if (row[columnIndex - 1] == null) {
            return null;
        }
        if (log.isDebugEnabled()) 
        {
            log.debug("retrieved blob_id: " + row[columnIndex - 1]);
        } // end of if ()
        return new FBBlob(mc, ((Long)row[columnIndex - 1]).longValue());
        //return super.getBlob(columnIndex);
        */
        
        return getField(columnIndex).getBlob();
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

    public java.sql.Date getDate(int columnIndex) throws SQLException {
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

    public java.sql.Time getTime(int columnIndex) throws SQLException {
        return getField(columnIndex).getTime();
    }

    public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException {
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
    private FBField getField(int columnIndex) throws SQLException {
         if (columnIndex> xsqlvars.length)
             throw new SQLException("invalid column index");
    
        FBField thisField = FBField.createField(getXsqlvar(columnIndex));

        if (thisField instanceof FBBlobField)
            ((FBBlobField)thisField).setConnection(c);
        else
        if (thisField instanceof FBStringField)
            ((FBStringField)thisField).setConnection(c);

        setWasNullColumnIndex(columnIndex);

        return thisField;
    }




     /**
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * a <code>java.math.BigDecimal</code> in the Java programming language.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal point
     * @return the column value; if the value is SQL <code>NULL</code>, the
     * value returned is <code>null</code>
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws  SQLException {
        return getField(columnIndex).getBigDecimal();
        /*Object obj = getObject(columnIndex);
        if (obj == null) {
            return null;
        } if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } if (obj instanceof Number) {
            return new BigDecimal(((Number) obj).doubleValue());
        } if (obj instanceof String) {
            try {
                return new BigDecimal((String) obj);
            } catch (NumberFormatException ex) {
                throw new SQLException("Number format error");
            }
        } else {
            throw new SQLException("Illegal type conversion");
            }*/
    }




    //======================================================================
    // Methods for accessing results by column name
    //======================================================================

    public String getString(String columnName) throws  SQLException {
        return getString(findColumn(columnName));
    }


    public boolean getBoolean(String columnName) throws  SQLException {
        return getBoolean(findColumn(columnName));
    }


    public byte getByte(String columnName) throws  SQLException {
        return getByte(findColumn(columnName));
    }

    public short getShort(String columnName) throws  SQLException {
        return getShort(findColumn(columnName));
    }

    public int getInt(String columnName) throws  SQLException {
        return getInt(findColumn(columnName));
    }


    public long getLong(String columnName) throws  SQLException {
        return getLong(findColumn(columnName));
    }

    public float getFloat(String columnName) throws  SQLException {
        return getFloat(findColumn(columnName));
    }


    public double getDouble(String columnName) throws  SQLException {
        return getDouble(findColumn(columnName));
    }


    /**
     * Describe <code>getBigDecimal</code> method here.
     *
     * @param columnName a <code>String</code> value
     * @param scale an <code>int</code> value
     * @return a <code>BigDecimal</code> value
     * @exception SQLException if an error occurs
     * @deprecated
     */
    public BigDecimal getBigDecimal(String columnName, int scale) throws  SQLException {
        return getBigDecimal(findColumn(columnName), scale);
    }


    public byte[] getBytes(String columnName) throws  SQLException {
        return getBytes(findColumn(columnName));
    }


    public java.sql.Date getDate(String columnName) throws  SQLException {
        return getDate(findColumn(columnName));
    }


    public java.sql.Time getTime(String columnName) throws  SQLException {
        return getTime(findColumn(columnName));
    }


    public java.sql.Timestamp getTimestamp(String columnName) throws  SQLException {
        return getTimestamp(findColumn(columnName));
    }

    public java.io.InputStream getAsciiStream(String columnName) throws  SQLException {
        return getAsciiStream(findColumn(columnName));
    }


    /**
     * Describe <code>getUnicodeStream</code> method here.
     *
     * @param columnName a <code>String</code> value
     * @return a <code>java.io.InputStream</code> value
     * @exception SQLException if an error occurs
     * @deprecated
     */
    public java.io.InputStream getUnicodeStream(String columnName) throws  SQLException {
        return getUnicodeStream(findColumn(columnName));
    }

    public java.io.InputStream getBinaryStream(String columnName) throws  SQLException {
        return getBinaryStream(findColumn(columnName));
    }



    //=====================================================================
    // Advanced features:
    //=====================================================================

    /**
     * Returns the first warning reported by calls on this
     * <code>ResultSet</code> object.
     * Subsequent warnings on this <code>ResultSet</code> object
     * will be chained to the <code>SQLWarning</code> object that
     * this method returns.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.
     *
     * <P><B>Note:</B> This warning chain only covers warnings caused
     * by <code>ResultSet</code> methods.  Any warning caused by
     * <code>Statement</code> methods
     * (such as reading OUT parameters) will be chained on the
     * <code>Statement</code> object.
     *
     * @return the first <code>SQLWarning</code> object reported or <code>null</code>
     * @exception SQLException if a database access error occurs
     */
    public SQLWarning getWarnings() throws  SQLException {
       return firstWarning;
    }


    /**
     * Clears all warnings reported on this <code>ResultSet</code> object.
     * After this method is called, the method <code>getWarnings</code>
     * returns <code>null</code> until a new warning is
     * reported for this <code>ResultSet</code> object.
     *
     * @exception SQLException if a database access error occurs
     */
    public void clearWarnings() throws  SQLException {
       firstWarning = null;
    }


    /**
     * Gets the name of the SQL cursor used by this <code>ResultSet</code>
     * object.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result set can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name. To insure that the cursor has the proper isolation
     * level to support update, the cursor's <code>select</code> statement should be
     * of the form 'select for update'. If the 'for update' clause is
     * omitted, the positioned updates may fail.
     *
     * <P>The JDBC API supports this SQL feature by providing the name of the
     * SQL cursor used by a <code>ResultSet</code> object.
     * The current row of a <code>ResultSet</code> object
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported, a
     * <code>SQLException</code> is thrown.
     *
     * @return the SQL name for this <code>ResultSet</code> object's cursor
     * @exception SQLException if a database access error occurs
     */
    public String getCursorName() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Retrieves the  number, types and properties of
     * this <code>ResultSet</code> object's columns.
     *
     * @return the description of this <code>ResultSet</code> object's columns
     * @exception SQLException if a database access error occurs
     */
    public ResultSetMetaData getMetaData() throws  SQLException {
        return new FBResultSetMetaData(xsqlvars);
    }



    /**
     * <p>Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as
     * an <code>Object</code> in the Java programming language.
     *
     * <p>This method will return the value of the given column as a
     * Java object.  The type of the Java object will be the default
     * Java object type corresponding to the column's SQL type,
     * following the mapping for built-in types specified in the JDBC
     * specification.
     *
     * <p>This method may also be used to read datatabase-specific
     * abstract data types.
     *
     * In the JDBC 2.0 API, the behavior of the method
     * <code>getObject</code> is extended to materialize
     * data of SQL user-defined types.  When a column contains
     * a structured or distinct value, the behavior of this method is as
     * if it were a call to: <code>getObject(columnIndex,
     * this.getStatement().getConnection().getTypeMap())</code>.
     *
     * @param columnName the SQL name of the column
     * @return a <code>java.lang.Object</code> holding the column value
     * @exception SQLException if a database access error occurs
     */
    public Object getObject(String columnName) throws  SQLException {
        return getObject(findColumn(columnName));
    }


    //----------------------------------------------------------------

    /**
     * Maps the given <code>ResultSet</code> column name to its
     * <code>ResultSet</code> column index.
     *
     * @param columnName the name of the column
     * @return the column index of the given column name
     * @exception SQLException if a database access error occurs
     */
    public int findColumn(String columnName) throws  SQLException {
        if (columnName == null || columnName.equals("")) {
            throw new SQLException("zero length identifiers not allowed");
        }
    columnName = columnName.toUpperCase();
        //XSQLVAR[] xsqlvars = stmt.getOutSqlda().sqlvar;
        for (int i = 0; i< xsqlvars.length; i++) {
            if (columnName.equals(xsqlvars[i].aliasname)) {
                return ++i;
            }
        }
        for (int i = 0; i< xsqlvars.length; i++) {
            if (columnName.equals(xsqlvars[i].sqlname)) {
                return ++i;
            }
        }
        throw new SQLException("column name " + columnName + " not found in result set.");
    }



    //--------------------------JDBC 2.0-----------------------------------

    //---------------------------------------------------------------------
    // Getters and Setters
    //---------------------------------------------------------------------

    /**
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object.
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param columnIndex the first column is 1, the second is 2, ...
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.io.Reader getCharacterStream(int columnIndex) throws  SQLException {
        InputStream is =  getField(columnIndex).getUnicodeStream();
        if (is==null)
            return null;
        else
            return new java.io.InputStreamReader(getField(columnIndex).getUnicodeStream());
    }


    /**
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.io.Reader</code> object.
     *
     * @return a <code>java.io.Reader</code> object that contains the column
     * value; if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @param columnName the name of the column
     * @return the value in the specified column as a <code>java.io.Reader</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.io.Reader getCharacterStream(String columnName) throws  SQLException {
        return getCharacterStream(findColumn(columnName));
    }



    /**
     * Gets the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a
     * <code>java.math.BigDecimal</code> with full precision.
     *
     * @param columnName the column name
     * @return the column value (full precision);
     * if the value is SQL <code>NULL</code>, the value returned is
     * <code>null</code> in the Java programming language.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     *
     */
    public BigDecimal getBigDecimal(String columnName) throws  SQLException {
        return getBigDecimal(findColumn(columnName));
    }


    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------

    /**
     * Indicates whether the cursor is before the first row in
     * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is before the first row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isBeforeFirst() throws  SQLException {
         return isBeforeFirst;
    }


    /**
     * Indicates whether the cursor is after the last row in
     * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is after the last row;
     * <code>false</code> if the cursor is at any other position or the
     * result set contains no rows
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isAfterLast() throws  SQLException {
        return isAfterLast;
    }


    /**
     * Indicates whether the cursor is on the first row of
     * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on the first row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isFirst() throws  SQLException {
         return isFirst;
    }


    /**
     * Indicates whether the cursor is on the last row of
     * this <code>ResultSet</code> object.
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine
     * whether the current row is the last row in the result set.
     *
     * @return <code>true</code> if the cursor is on the last row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean isLast() throws  SQLException {
       return isLast;
    }


    /**
     * Moves the cursor to the front of
     * this <code>ResultSet</code> object, just before the
     * first row. This method has no effect if the result set contains no rows.
     *
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void beforeFirst() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the end of
     * this <code>ResultSet</code> object, just after the
     * last row. This method has no effect if the result set contains no rows.
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void afterLast() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the first row in
     * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean first() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the last row in
     * this <code>ResultSet</code> object.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if there are no rows in the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean last() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on.
     *
     * @return the current row number; <code>0</code> if there is no current row
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getRow() throws  SQLException {
       return rowNum;
    }


    /**
     * Moves the cursor to the given row number in
     * this <code>ResultSet</code> object.
     *
     * <p>If the row number is positive, the cursor moves to
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on.
     *
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling the method
     * <code>absolute(-1)</code> positions the
     * cursor on the last row; calling the method <code>absolute(-2)</code>
     * moves the cursor to the next-to-last row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before the first row or after
     * the last row.
     *
     * <p><B>Note:</B> Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>. Calling <code>absolute(-1)</code>
     * is the same as calling <code>last()</code>.
     *
     * @return <code>true</code> if the cursor is on the result set;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error
     * occurs, the row is <code>0</code>, or the result set type is
     * <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean absolute( int row ) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling the method <code>relative(1)</code>
     * is different from calling the method <code>next()</code>
     * because is makes sense to call <code>next()</code> when there
     * is no current row,
     * for example, when the cursor is positioned before the first row
     * or after the last row of the result set.
     *
     * @return <code>true</code> if the cursor is on a row;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs,
     * there is no current row, or the result set type is
     * <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean relative( int rows ) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the previous row in this
     * <code>ResultSet</code> object.
     *
     * <p><B>Note:</B> Calling the method <code>previous()</code> is not the same as
     * calling the method <code>relative(-1)</code> because it
     * makes sense to call</code>previous()</code> when there is no current row.
     *
     * @return <code>true</code> if the cursor is on a valid row;
     * <code>false</code> if it is off the result set
     * @exception SQLException if a database access error
     * occurs or the result set type is <code>TYPE_FORWARD_ONLY</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean previous() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    //---------------------------------------------------------------------
    // Properties
    //---------------------------------------------------------------------

    /**
     * The constant indicating that the rows in a result set will be
     * processed in a forward direction; first-to-last.
     * This constant is used by the method <code>setFetchDirection</code>
     * as a hint to the driver, which the driver may ignore.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int FETCH_FORWARD = 1000;

    /**
     * The constant indicating that the rows in a result set will be
     * processed in a reverse direction; last-to-first.
     * This constant is used by the method <code>setFetchDirection</code>
     * as a hint to the driver, which the driver may ignore.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int FETCH_REVERSE = 1001;

    /**
     * The constant indicating that the order in which rows in a
     * result set will be processed is unknown.
     * This constant is used by the method <code>setFetchDirection</code>
     * as a hint to the driver, which the driver may ignore.
     */
    int FETCH_UNKNOWN = 1002;

    /**
     * Gives a hint as to the direction in which the rows in this
     * <code>ResultSet</code> object will be processed.
     * The initial value is determined by the
     * <code>Statement</code> object
     * that produced this <code>ResultSet</code> object.
     * The fetch direction may be changed at any time.
     *
     * @exception SQLException if a database access error occurs or
     * the result set type is <code>TYPE_FORWARD_ONLY</code> and the fetch
     * direction is not <code>FETCH_FORWARD</code>
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     * @see Statement#setFetchDirection
     */
    public void setFetchDirection(int direction) throws  SQLException {
         if (direction != java.sql.ResultSet.FETCH_FORWARD)
            throw new SQLException("can't set fetch direction");
    }


    /**
     * Returns the fetch direction for this
     * <code>ResultSet</code> object.
     *
     * @return the current fetch direction for this <code>ResultSet</code> object
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getFetchDirection() throws  SQLException {
       return java.sql.ResultSet.FETCH_FORWARD;
    }


    /**
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for this
     * <code>ResultSet</code> object.
     * If the fetch size specified is zero, the JDBC driver
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the
     * <code>Statement</code> object
     * that created the result set.  The fetch size may be changed at any time.
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs or the
     * condition <code>0 <= rows <= this.getMaxRows()</code> is not satisfied
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void setFetchSize(int rows) throws  SQLException {
         if (rows < 0)
             throw new SQLException("can't set negative fetch size");
         else if (rows > maxRows)
             throw new SQLException("can't set fetch size > maxRows");
         else
        fetchSize = rows;
    }


    /**
     *
     * Returns the fetch size for this
     * <code>ResultSet</code> object.
     *
     * @return the current fetch size for this <code>ResultSet</code> object
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getFetchSize() throws  SQLException {
        return fetchSize;
    }


    /**
     * The constant indicating the type for a <code>ResultSet</code> object
     * whose cursor may move only forward.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int TYPE_FORWARD_ONLY = 1003;

    /**
     * The constant indicating the type for a <code>ResultSet</code> object
     * that is scrollable but generally not sensitive to changes made by others.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     *
     */
    int TYPE_SCROLL_INSENSITIVE = 1004;

    /**
     * The constant indicating the type for a <code>ResultSet</code> object
     * that is scrollable and generally sensitive to changes made by others.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    int TYPE_SCROLL_SENSITIVE = 1005;

    /**
     * Returns the type of this <code>ResultSet</code> object.
     * The type is determined by the <code>Statement</code> object
     * that created the result set.
     *
     * @return <code>TYPE_FORWARD_ONLY</code>,
     * <code>TYPE_SCROLL_INSENSITIVE</code>,
     * or <code>TYPE_SCROLL_SENSITIVE</code>
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getType() throws  SQLException {
        return java.sql.ResultSet.TYPE_FORWARD_ONLY;
    }


    /**
     * The constant indicating the concurrency mode for a
     * <code>ResultSet</code> object that may NOT be updated.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     *
     */
    int CONCUR_READ_ONLY = 1007;

    /**
     * The constant indicating the concurrency mode for a
     * <code>ResultSet</code> object that may be updated.
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     *
     */
    int CONCUR_UPDATABLE = 1008;

    /**
     * Returns the concurrency mode of this <code>ResultSet</code> object.
     * The concurrency used is determined by the
     * <code>Statement</code> object that created the result set.
     *
     * @return the concurrency type, either <code>CONCUR_READ_ONLY</code>
     * or <code>CONCUR_UPDATABLE</code>
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public int getConcurrency() throws  SQLException {
        return java.sql.ResultSet.CONCUR_READ_ONLY;
    }


    //---------------------------------------------------------------------
    // Updates
    //---------------------------------------------------------------------

    /**
     * Indicates whether the current row has been updated.  The value returned
     * depends on whether or not the result set can detect updates.
     *
     * @return <code>true</code> if the row has been visibly updated
     * by the owner or another, and updates are detected
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#updatesAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowUpdated() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Indicates whether the current row has had an insertion.
     * The value returned depends on whether or not this
     * <code>ResultSet</code> object can detect visible inserts.
     *
     * @return <code>true</code> if a row has had an insertion
     * and insertions are detected; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#insertsAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowInserted() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Indicates whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether
     * or not this <code>ResultSet</code> object can detect deletions.
     *
     * @return <code>true</code> if a row was deleted and deletions are detected;
     * <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#deletesAreDetected
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public boolean rowDeleted() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Gives a nullable column a null value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code>
     * or <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateNull(int columnIndex) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBoolean(int columnIndex, boolean x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>byte</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateByte(int columnIndex, byte x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>short</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateShort(int columnIndex, short x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with an <code>int</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateInt(int columnIndex, int x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>long</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateLong(int columnIndex, long x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>float</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateFloat(int columnIndex, float x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>double</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDouble(int columnIndex, double x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.math.BigDecimal</code>
     * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>String</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateString(int columnIndex, String x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>byte</code> array value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBytes(int columnIndex, byte x[]) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDate(int columnIndex, java.sql.Date x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTime(int columnIndex, java.sql.Time x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTimestamp(int columnIndex, java.sql.Timestamp x) throws  SQLException {
        throw new SQLException("not yet implemented");
    }


    /**
     * Updates the designated column with an ascii stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateAsciiStream(int columnIndex,
               java.io.InputStream x,
               int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a binary stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBinaryStream(int columnIndex,
                java.io.InputStream x,
                int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a character stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateCharacterStream(int columnIndex,
                 java.io.Reader x,
                 int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
     *  or <code>java.sql.Types.NUMERIC</code> types,
     *  this is the number of digits after the decimal point.  For all other
     *  types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(int columnIndex, Object x, int scale) throws  SQLException {
        throw new SQLException("not yet implemented");
    }


    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(int columnIndex, Object x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>null</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateNull(String columnName) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBoolean(String columnName, boolean x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>byte</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateByte(String columnName, byte x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>short</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateShort(String columnName, short x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with an <code>int</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateInt(String columnName, int x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>long</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateLong(String columnName, long x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>float </code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateFloat(String columnName, float x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>double</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDouble(String columnName, double x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.BigDecimal</code>
     * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBigDecimal(String columnName, BigDecimal x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>String</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateString(String columnName, String x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>boolean</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * JDBC 2.0
     *
     * Updates a column with a byte array value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBytes(String columnName, byte x[]) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Date</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateDate(String columnName, java.sql.Date x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Time</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTime(String columnName, java.sql.Time x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a <code>java.sql.Timestamp</code>
     * value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateTimestamp(String columnName, java.sql.Timestamp x) throws  SQLException {
        throw new SQLException("not yet implemented");
    }


    /**
     * Updates the designated column with an ascii stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateAsciiStream(String columnName,
               java.io.InputStream x,
               int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a binary stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateBinaryStream(String columnName,
                java.io.InputStream x,
                int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with a character stream value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateCharacterStream(String columnName,
                 java.io.Reader reader,
                 int length) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale for <code>java.sql.Types.DECIMA</code>
     *  or <code>java.sql.Types.NUMERIC</code> types,
     *  this is the number of digits after the decimal point.  For all other
     *  types this value will be ignored.
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(String columnName, Object x, int scale) throws  SQLException {
        throw new SQLException("not yet implemented");
    }


    /**
     * Updates the designated column with an <code>Object</code> value.
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or
     * <code>insertRow</code> methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateObject(String columnName, Object x) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Inserts the contents of the insert row into this
     * <code>ResultSet</code> objaect and into the database.
     * The cursor must be on the insert row when this method is called.
     *
     * @exception SQLException if a database access error occurs,
     * if this method is called when the cursor is not on the insert row,
     * or if not all of non-nullable columns in
     * the insert row have been given a value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void insertRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Updates the underlying database with the new contents of the
     * current row of this <code>ResultSet</code> object.
     * This method cannot be called when the cursor is on the insert row.
     *
     * @exception SQLException if a database access error occurs or
     * if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void updateRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Deletes the current row from this <code>ResultSet</code> object
     * and from the underlying database.  This method cannot be called when
     * the cursor is on the insert row.
     *
     * @exception SQLException if a database access error occurs
     * or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void deleteRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Refreshes the current row with its most recent value in
     * the database.  This method cannot be called when
     * the cursor is on the insert row.
     *
     * <P>The <code>refreshRow</code> method provides a way for an
     * application to
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver
     * may actually refresh multiple rows at once if the fetch size is
     * greater than one.
     *
     * <P> All values are refetched subject to the transaction isolation
     * level and cursor sensitivity.  If <code>refreshRow</code> is called after
     * calling an <code>updateXXX</code> method, but before calling
     * the method <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method
     * <code>refreshRow</code> frequently will likely slow performance.
     *
     * @exception SQLException if a database access error
     * occurs or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void refreshRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Cancels the updates made to the current row in this
     * <code>ResultSet</code> object.
     * This method may be called after calling an
     * <code>updateXXX</code> method(s) and before calling
     * the method <code>updateRow</code> to roll back
     * the updates made to a row.  If no updates have been made or
     * <code>updateRow</code> has already been called, this method has no
     * effect.
     *
     * @exception SQLException if a database access error
     * occurs or if this method is called when the cursor is on the insert row
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void cancelRowUpdates() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the insert row.  The current cursor position is
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the <code>updateXXX</code> methods prior to
     * inserting the row into the result set.
     *
     * Only the <code>updateXXX</code>, <code>getXXX</code>,
     * and <code>insertRow</code> methods may be
     * called when the cursor is on the insert row.  All of the columns in
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.
     * An <code>updateXXX</code> method must be called before a
     * <code>getXXX</code> method can be called on a column value.
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void moveToInsertRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on
     * the insert row.
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public void moveToCurrentRow() throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the <code>Statement</code> object that produced this
     * <code>ResultSet</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns
     * <code>null</code>.
     *
     * @return the <code>Statment</code> object that produced
     * this <code>ResultSet</code> object or <code>null</code>
     * if the result set was produced some other way
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Statement getStatement() {
        return fbFetcher.getStatement();
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved.
     *
     * @param i the first column is 1, the second is 2, ...
     * @param map a <code>java.util.Map</code> object that contains the mapping
     * from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> in the Java programming language
     * representing the SQL value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Object getObject(int i, java.util.Map map) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Ref</code> object representing an SQL <code>REF</code> value
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Ref getRef(int i) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }



    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return a <code>Clob</code> object representing the SQL <code>CLOB</code> value in
     *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Clob getClob(int i) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language.
     *
     * @param i the first column is 1, the second is 2, ...
     * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
     *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Array getArray(int i) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Object</code>
     * in the Java programming language.
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate.
     *
     * @param columnName the name of the column from which to retrieve the value
     * @param map a <code>java.util.Map</code> object that contains the mapping
     * from SQL type names to classes in the Java programming language
     * @return an <code>Object</code> representing the SQL value in the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Object getObject(String columnName, java.util.Map map) throws  SQLException {
        return getObject(findColumn(columnName), map);
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Ref</code> object
     * in the Java programming language.
     *
     * @param columnName the column name
     * @return a <code>Ref</code> object representing the SQL <code>REF</code> value in
     *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Ref getRef(String columnName) throws  SQLException {
        return getRef(findColumn(columnName));
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Blob</code> object
     * in the Java programming language.
     *
     * @param columnName the name of the column from which to retrieve the value
     * @return a <code>Blob</code> object representing the SQL <code>BLOB</code> value in
     *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Blob getBlob(String columnName) throws  SQLException {
        return getBlob(findColumn(columnName));
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>Clob</code> object
     * in the Java programming language.
     *
     * @param columnName the name of the column from which to retrieve the value
     * @return a <code>Clob</code> object representing the SQL <code>CLOB</code>
     * value in the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Clob getClob(String columnName) throws  SQLException {
        return getClob(findColumn(columnName));
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as an <code>Array</code> object
     * in the Java programming language.
     *
     * @param columnName the name of the column from which to retrieve the value
     * @return an <code>Array</code> object representing the SQL <code>ARRAY</code> value in
     *         the specified column
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public Array getArray(String columnName) throws  SQLException {
        return getArray(findColumn(columnName));
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Date getDate(int columnIndex, Calendar cal) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Date</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the date if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column from which to retrieve the value
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the date
     * @return the column value as a <code>java.sql.Date</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Date getDate(String columnName, Calendar cal) throws  SQLException {
        return getDate(findColumn(columnName), cal);
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Time getTime(int columnIndex, Calendar cal) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Time</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the time if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the time
     * @param cal the calendar to use in constructing the time
     * @return the column value as a <code>java.sql.Time</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Time getTime(String columnName, Calendar cal) throws  SQLException {
       return getTime(findColumn(columnName), cal);
     }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the timestamp
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal) throws  SQLException {
                throw new SQLException("Not yet implemented");
    }


    /**
     * Returns the value of the designated column in the current row
     * of this <code>ResultSet</code> object as a <code>java.sql.Timestamp</code> object
     * in the Java programming language.
     * This method uses the given calendar to construct an appropriate millisecond
     * value for the timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the <code>java.util.Calendar</code> object
     * to use in constructing the date
     * @return the column value as a <code>java.sql.Timestamp</code> object;
     * if the value is SQL <code>NULL</code>,
     * the value returned is <code>null</code> in the Java programming language
     * @exception SQLException if a database access error occurs
     * @since 1.2
     * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
     *      2.0 API</a>
     */
    public java.sql.Timestamp getTimestamp(String columnName, Calendar cal) throws  SQLException {
       return getTimestamp(findColumn(columnName), cal);
     }

    //jdbc 3 methods

    /**
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     * @exception java.net.MalformedURLException <description>
     */
    public URL getURL(int param1) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     * @exception java.net.MalformedURLException <description>
     */
    public URL getURL(String param1) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }


    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateRef(int param1, Ref param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateRef(String param1, Ref param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateBlob(int param1, Blob param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateBlob(String param1, Blob param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateClob(int param1, Clob param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateClob(String param1, Clob param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateArray(int param1, Array param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    /**
     *
     * @param param1 <description>
     * @param param2 <description>
     * @exception java.sql.SQLException <description>
     */
    public void updateArray(String param1, Array param2) throws SQLException {
        // TODO: implement this java.sql.ResultSet method
        throw new SQLException("not yet implemented");
    }

    //--------------------------------------------------------------------

    interface FBFetcher {


        boolean next() throws SQLException;

        void close() throws SQLException;

        Statement getStatement();

    }

    class FBStatementFetcher implements FBFetcher {

        private FBConnection c;

        private FBStatement fbStatement;

        private isc_stmt_handle stmt;
          
        private Object[] nextRow;

        FBStatementFetcher(FBConnection c, FBStatement fbStatement, 
            isc_stmt_handle stmt) throws SQLException 
        {
            this.c = c;
            this.fbStatement = fbStatement;
            this.stmt = stmt;
            
            c.registerStatement(fbStatement);
            
            isEmpty = false;
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
            
            try {
                nextRow = c.fetch(stmt);

                if (nextRow==null)
                    isEmpty = true;
                else 
                    isBeforeFirst = true;
            }
            catch (GDSException ge) {
                throw new SQLException("fetch problem: " + ge.toString());
            }
        }

        public boolean next() throws SQLException {
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
                
            if (log.isDebugEnabled())
                log.debug("FBResultSet next - FBStatementFetcher");
                    
            if (isEmpty)
                return false;
            else if (nextRow == null || (fbStatement.maxRows!=0 && rowNum==fbStatement.maxRows)){
                isAfterLast = true;
                rowNum=0;
                return false;
            }
            else {
                try {
                    row = nextRow;
                    nextRow = c.fetch(stmt);
                    rowNum++;
                    copyToSQLVAR(row);
                    
                    if(rowNum==1)
                        isFirst=true;
                    
                    if((nextRow==null) || (fbStatement.maxRows!=0 && rowNum==fbStatement.maxRows))
                        isLast = true;
                        
                    return true;
                }
                catch (GDSException ge) {
                    throw new SQLException("fetch problem: " + ge.toString());
                }
            }
        }

        public void close() throws SQLException {
            fbStatement.closeResultSet();
        }

        public Statement getStatement() {
            return fbStatement;
        }

        private void copyToSQLVAR(Object[] row) {
            for(int i = 0; i < xsqlvars.length; i++) {
                xsqlvars[i].sqldata = row[i];
                xsqlvars[i].sqlind = row[i] == null ? -1 : 0;
            }
        }
    }

    class FBCachedFetcher  implements FBFetcher {

        ArrayList rows;

        private FBStatement fbStatement;
          
        FBCachedFetcher(FBConnection c, FBStatement fbStatement, isc_stmt_handle stmt) throws SQLException {
            Object[] localRow = null;
            
            this.fbStatement = fbStatement;
            
            isEmpty = false;
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
            rows = new ArrayList();
            
            try {
                do {
                    localRow = c.fetch(stmt);
                    if (localRow != null)
                    {
                        //ugly blob caching workaround.
                        for (int i = 0; i < localRow.length; i++)
                        {
                            boolean blobField = 
                                FBField.isType(xsqlvars[i], Types.BLOB) ||
                                FBField.isType(xsqlvars[i], Types.BINARY) ||
                                FBField.isType(xsqlvars[i], Types.LONGVARCHAR);
                                
                            if (blobField && localRow[i] != null ) 
                            {
                                FBBlobField blob = (FBBlobField)FBField.createField(xsqlvars[i]);
                                blob.setConnection(c);
                                localRow[i] = blob.getCachedObject();                                
                            } // end of if ()                            
                        } // end of for ()                        
                        rows.add(localRow);
                    }
                } while  (localRow != null && (fbStatement.maxRows==0 || rows.size()<fbStatement.maxRows));
                     if (rows.size()==0)
                         isEmpty = true;
                     else
                         isBeforeFirst = true;
                // rows.add(null);
                c.closeStatement(stmt, false);
            }
            catch (GDSException ge) {
                throw new SQLException("fetch problem: " + ge.toString());
            }
        }

        FBCachedFetcher(ArrayList rows) throws SQLException {
            this.rows = rows;
            
            isEmpty = false;
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
            
            if (rows.size()==0)
                isEmpty = true;
            else
                isBeforeFirst = true;
        }

        public boolean next() throws SQLException {
            isBeforeFirst = false;
            isFirst = false;
            isLast = false;
            isAfterLast = false;
                
            log.debug("FBResultSet next - FBCachedFetcher");
            if (isEmpty)
                return false;
            else 
            if(rowNum == rows.size()) {
                row = null;
                rowNum = 0;
                isAfterLast = true;
                return false;
            }
            else {
                rowNum++;
                
                if (rowNum == 1)
                    isFirst = true;
                if (rowNum == rows.size())
                    isLast = true;

                row = (Object[])rows.get(rowNum-1);
                
                copyToSQLVAR(row);

                return true;
            }
        }

        public void close() throws SQLException {
        }

        public Statement getStatement() {
            return fbStatement;
        }

        private void copyToSQLVAR(Object[] row) {
            for(int i = 0; i < xsqlvars.length; i++) {
                xsqlvars[i].sqldata = row[i];
                xsqlvars[i].sqlind = row[i] == null ? -1 : 0;
            }
        }
    }

    protected void setWasNullColumnIndex(int columnIndex) {
        wasNullColumnIndex = columnIndex;
    }


     protected void addWarning(java.sql.SQLWarning warning){
         if (firstWarning == null)
             firstWarning = warning;
         else{
             java.sql.SQLWarning lastWarning = firstWarning;
             while (lastWarning.getNextWarning() != null){
                 lastWarning = lastWarning.getNextWarning();
             }
             lastWarning.setNextWarning(warning);
         }
     }
}
