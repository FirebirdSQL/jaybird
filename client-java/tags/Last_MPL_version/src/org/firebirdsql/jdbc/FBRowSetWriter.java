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
import javax.sql.RowSetWriter;
import javax.sql.RowSetInternal;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


import java.sql.*;

/**
 * <P>An object that implements the RowSetWriter interface may be registered
 * with a RowSet object that supports the reader/writer paradigm.
 * The RowSetWriter.writeRow() method is called internally by a RowSet that supports
 * the reader/writer paradigm to write the contents of the rowset to a data source.
 */

public class FBRowSetWriter implements RowSetWriter {

  /**
    <P>This method is called to write data to the data source
    that is backing the rowset.

    * @param caller the calling rowset
    * @return true if the row was written, false if not due to a conflict
    * @exception SQLException if a database-access error occur
   */
    public boolean writeData(RowSetInternal caller) throws SQLException {
        throw new SQLException("Not yet implemented");
    }


}
