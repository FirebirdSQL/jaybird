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


// imports --------------------------------------
import javax.sql.RowSetWriter;
import javax.sql.RowSetInternal;

import java.sql.SQLException;


/**
 * Describe class <code>FBRowSetWriter</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
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
