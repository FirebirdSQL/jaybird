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


import javax.sql.RowSetReader;
import javax.sql.RowSetInternal;

import java.sql.*;


/**
 * Describe class <code>FBRowSetReader</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBRowSetReader implements RowSetReader {

  /**
   * <P>Read the new contents of a rowset.  This method is invoked internally
   * by the RowSet.execute() method for rowsets that support the
   * reader/writer paradigm.
   *
   * <P>The readData() method uses the RowSet.insertRow() or RowSet.populate()
   * methods to add rows to the caller.  In general, any of the caller's
   * methods may be called by the reader with one exception, calling
   * execute() will throw an SQLException since execute may not be called
   * recursively.  Also, rowset events, such as RowSetChanged, etc. are not
   * generated by RowSet methods invoked by a reader.
   *
   * @param caller the rowset that called the reader
   * @exception SQLException if a database-access error occurs
   */
    public void readData(RowSetInternal caller) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


}
