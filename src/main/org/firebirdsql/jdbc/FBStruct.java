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
import java.sql.Struct;
import java.sql.SQLException;

/**
 *
 *   @see <related>
 *   @author David Jencks (davidjencks@earthlink.net)
 *   @version $ $
 */


/**
 * <p>The standard mapping in the Java programming language for an SQL
 * structured type. A <code>Struct</code> object contains a
 * value for each attribute of the SQL structured type that
 * it represents.
 * By default, an instance of<code>Struct</code> is valid as long as the
 * application has a reference to it.
 * @since 1.2
 * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
 *      2.0 API</a>
 */

public class FBStruct implements Struct {

  /**
   * Retrieves the SQL type name of the SQL structured type
   * that this <code>Struct</code> object represents.
   *
   * @returns the fully-qualified type name of the SQL structured
   *          type for which this <code>Struct</code> object
   *          is the generic representation
   * @exception SQLException if a database access error occurs
   */
    public String getSQLTypeName() throws  SQLException {
        return null;
    }


  /**
   * Produces the ordered values of the attributes of the SQL
   * structurec type that this <code>Struct</code> object represents.
   * This method uses the type map associated with the
   * connection for customizations of the type mappings.
   * If there is no
   * entry in the connection's type map that matches the structured
   * type that this <code>Struct</code> object represents,
   * the driver uses the standard mapping.
   * <p>
   * Conceptually, this method calls the method
   * <code>getObject</code> on each attribute
   * of the structured type and returns a Java array containing
   * the result.
   *
   * @return an array containing the ordered attribute values
   * @exception SQLException if a database access error occurs
   */
    public Object[] getAttributes() throws  SQLException {
        return null;
    }


  /**
   * Produces the ordered values of the attributes of the SQL
   * structurec type that this <code>Struct</code> object represents.
   * This method uses the given type map
   * for customizations of the type mappings.
   * If there is no
   * entry in the given type map that matches the structured
   * type that this <code>Struct</code> object represents,
   * the driver uses the standard mapping. This method never
   * uses the type map associated with the connection.
   * <p>
   * Conceptually, this method calls the method
   * <code>getObject</code> on each attribute
   * of the structured type and returns a Java array containing
   * the result.
   *
   * @param map a mapping of SQL type names to Java classes
   * @return an array containing the ordered attribute values
   * @exception SQLException if a database access error occurs
   */
    public Object[] getAttributes(java.util.Map map) throws  SQLException {
        return null;
    }

}


