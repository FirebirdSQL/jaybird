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
import javax.sql.RowSetInternal;
import javax.sql.RowSetMetaData;
import java.sql.SQLException;
import java.sql.*;
/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */



/**
 *
 * A rowset object presents itself to a reader or writer as an instance
 * of RowSetInternal.  The RowSetInternal interface contains additional
 * methods that let the reader or writer access and modify the internal
 * state of the rowset.
 *
 */

public class FBRowSetInternal implements RowSetInternal {

  /**
   * Get the parameters that were set on the rowset.
   *
   * @return an array of parameters
   * @exception SQLException if a database-access error occurs.
   */
    public Object[] getParams() throws  SQLException {
        return null;
    }


  /**
   * Get the Connection passed to the rowset.
   *
   * @return the Connection passed to the rowset, or null if none
   * @exception SQLException if a database-access error occurs.
   */
    public Connection getConnection() throws  SQLException {
        return null;
    }


  /**
   * Set the rowset's metadata.
   *
   * @param a metadata object
   * @exception SQLException if a database-access error occurs.
   */
    public void setMetaData(RowSetMetaData md) throws  SQLException {
    }


  /**
   * Returns a result set containing the original value of the rowset.
   * The cursor is positioned before the first row in the result set.
   * Only rows contained in the result set returned by getOriginal()
   * are said to have an original value.
   *
   * @return the original value of the rowset
   * @exception SQLException if a database-access error occurs.
   */
  public ResultSet getOriginal() throws  SQLException {
        return null;
    }


  /**
   * Returns a result set containing the original value of the current
   * row only.  If the current row has no original value an empty result set
   * is returned. If there is no current row an exception is thrown.
   *
   * @return the original value of the row
   * @exception SQLException if a database-access error occurs.
   */
  public ResultSet getOriginalRow() throws  SQLException {
        return null;
    }


}





