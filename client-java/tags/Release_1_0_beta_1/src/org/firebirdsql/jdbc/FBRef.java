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
import java.sql.Ref;
import java.sql.SQLException;
import java.util.Map;


/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * The mapping in the Java programming language of an SQL <code>REF</code>
 * value, which is a reference to an
 * SQL structured type value in the database.
 * <P>
 * SQL <code>REF</code> values are stored in a special table that contains
 * instances of a referenceable SQL structured type, and each <code>REF</code>
 * value is a unique identifier for one instance in that table.
 * An SQL <code>REF</code> value may be used in place of the
 * SQL structured type it references; it may be used as either a column value in a
 * table or an attribute value in a structured type.
 * <P>
 * Because an SQL <code>REF</code> value is a logical pointer to an
 * SQL structured type, a <code>Ref</code> object is by default also a logical
 * pointer; thus, retrieving an SQL <code>REF</code> value as
 * a <code>Ref</code> object does not materialize
 * the attributes of the structured type on the client.
 * <P>
 * A <code>Ref</code> object can be saved to persistent storage and is dereferenced by
 * passing it as a parameter to an SQL statement and executing the
 * statement.
 * <P>
 * The <code>Ref</code> interface is new in the JDBC 2.0 API.
 * @see Struct
 *
 */
public class FBRef implements Ref {

  /**
   * Retrieves the fully-qualified SQL name of the SQL structured type that
   * this <code>Ref</code> object references.
   *
   * @return the fully-qualified SQL name of the referenced SQL structured type
   * @exception SQLException if a database access error occurs
   * @since 1.2
   * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
   *      2.0 API</a>
   */
    public String getBaseTypeName() throws  SQLException {
        return null;
    }

    //jdbc 3

    /**
     *
     * @param param1 <description>
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public Object getObject(Map param1) throws SQLException {
        // TODO: implement this java.sql.Ref method
        return null;
    }

    /**
     *
     * @return <description>
     * @exception java.sql.SQLException <description>
     */
    public Object getObject() throws SQLException {
        // TODO: implement this java.sql.Ref method
        return null;
    }


    /**
     *
     * @param param1 <description>
     * @exception java.sql.SQLException <description>
     */
    public void setObject(Object param1) throws SQLException {
        // TODO: implement this java.sql.Ref method
    }


}
