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

package org.firebirdsql.jdbc.rowset;



import javax.sql.RowSetMetaData;
import java.sql.SQLException;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.AbstractConnection;
import org.firebirdsql.jdbc.FBResultSetMetaData;


/**
 * Describe class <code>FBRowSetMetaData</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBRowSetMetaData extends FBResultSetMetaData implements RowSetMetaData {

    FBRowSetMetaData(XSQLVAR[] xsqlvars, AbstractConnection connection) throws SQLException {
        super(xsqlvars, connection);
    }

  /**
   * Set the number of columns in the RowSet.
   *
   * @param columnCount number of columns.
   * @exception SQLException if a database-access error occurs.
   */
    public void setColumnCount(int columnCount) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify whether the is column automatically numbered, thus read-only.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either true or false.
   *
   * @default is false.
   * @exception SQLException if a database-access error occurs.
   */
    public void setAutoIncrement(int columnIndex, boolean property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify whether the column is case sensitive.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either true or false.
   *
   * @default is false.
   * @exception SQLException if a database-access error occurs.
   */
    public void setCaseSensitive(int columnIndex, boolean property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify whether the column can be used in a where clause.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either true or false.
   *
   * @default is false.
   * @exception SQLException if a database-access error occurs.
   */
    public void setSearchable(int columnIndex, boolean property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify whether the column is a cash value.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either true or false.
   *
   * @default is false.
   * @exception SQLException if a database-access error occurs.
   */
    public void setCurrency(int columnIndex, boolean property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify whether the column's value can be set to NULL.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either one of columnNoNulls, columnNullable or columnNullableUnknown.
   *
   * @default is columnNullableUnknown.
   * @exception SQLException if a database-access error occurs.
   */
    public void setNullable(int columnIndex, int property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Speicfy whether the column is a signed number.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param property is either true or false.
   *
   * @default is false.
   * @exception SQLException if a database-access error occurs.
   */
    public void setSigned(int columnIndex, boolean property) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's normal max width in chars.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param size size of the column
   *
   * @exception SQLException if a database-access error occurs.
   */
    public void setColumnDisplaySize(int columnIndex, int size) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the suggested column title for use in printouts and
   * displays, if any.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param label the column title
   * @exception SQLException if a database-access error occurs.
   */
    public void setColumnLabel(int columnIndex, String label) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column name.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param columnName the column name
   * @exception SQLException if a database-access error occurs.
   */
    public void setColumnName(int columnIndex, String columnName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's table's schema, if any.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param schemaName the schema name
   * @exception SQLException if a database-access error occurs.
   */
    public void setSchemaName(int columnIndex, String schemaName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's number of decimal digits.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param precision number of decimal digits.
   * @exception SQLException if a database-access error occurs.
   */
    public void setPrecision(int columnIndex, int precision) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's number of digits to right of the decimal point.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param scale number of digits to right of decimal point.
   * @exception SQLException if a database-access error occurs.
   */
    public void setScale(int columnIndex, int scale) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's table name, if any.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param tableName column's table name.
   * @exception SQLException if a database-access error occurs.
   */
    public void setTableName(int columnIndex, String tableName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's table's catalog name, if any.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param catalogName column's catalog name.
   * @exception SQLException if a database-access error occurs.
   */
    public void setCatalogName(int columnIndex, String catalogName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's SQL type.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param SQLType column's SQL type.
   * @exception SQLException if a database-access error occurs.
   * @see Types
   */
    public void setColumnType(int columnIndex, int SQLType) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
   * Specify the column's data source specific type name, if any.
   *
   * @param column the first column is 1, the second is 2, ...
   * @param typeName data source specific type name.
   * @exception SQLException if a database-access error occurs.
   */
    public void setColumnTypeName(int columnIndex, String typeName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


}





