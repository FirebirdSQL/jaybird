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


import java.sql.SQLData;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.sql.SQLException;

/**
 * Describe class <code>FBSQLData</code> here.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBSQLData implements SQLData {

 /**
  * Returns the fully-qualified
  * name of the SQL user-defined type that this object represents.
  * This method is called by the JDBC driver to get the name of the
  * UDT instance that is being mapped to this instance of
  * <code>SQLData</code>.
  *
  * @return the type name that was passed to the method <code>readSql</code>
  *            when this object was constructed and populated
  * @exception SQLException if there is a database access error
  * @since 1.2
  * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
  *      2.0 API</a>
  */
    public String getSQLTypeName() throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


 /**
  * Populates this object with data read from the database.
  * The implementation of the method must follow this protocol:
  * <UL>
  * <LI>It must read each of the attributes or elements of the SQL
  * type  from the given input stream.  This is done
  * by calling a method of the input stream to read each
  * item, in the order that they appear in the SQL definition
  * of the type.
  * <LI>The method <code>readSQL</code> then
  * assigns the data to appropriate fields or
  * elements (of this or other objects).
  * Specifically, it must call the appropriate <code>SQLInput.readXXX</code>
  * method(s) to do the following:
  * for a distinct type, read its single data element;
  * for a structured type, read a value for each attribute of the SQL type.
  * </UL>
  * The JDBC driver initializes the input stream with a type map
  * before calling this method, which is used by the appropriate
  * <code>SQLInput.readXXX</code> method on the stream.
  *
  * @param stream the <code>SQLInput</code> object from which to read the data for
  * the value that is being custom mapped
  * @param typeName the SQL type name of the value on the data stream
  * @exception SQLException if there is a database access error
  * @see SQLInput
  */
    public void readSQL (SQLInput stream, String typeName) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }


  /**
  * Writes this object to the given SQL data stream, converting it back to
  * its SQL value in the data source.
  * The implementation of the method must follow this protocol:<BR>
  * It must write each of the attributes of the SQL type
  * to the given output stream.  This is done by calling a
  * method of the output stream to write each item, in the order that
  * they appear in the SQL definition of the type.
  * Specifically, it must call the appropriate <code>SQLOutput.writeXXX</code>
  * method(s) to do the following:
  * for a Distinct Type, write its single data element;
  * for a Structured Type, write a value for each attribute of the SQL type.
  *
  * @param stream the <code>SQLOutput</code> object to which to write the data for
  * the value that was custom mapped
  * @exception SQLException if there is a database access error
  * @see SQLOutput
  * @since 1.2
  * @see <a href="package-summary.html#2.0 API">What Is in the JDBC
  *      2.0 API</a>
  */
    public void writeSQL (SQLOutput stream) throws  SQLException {
        throw new SQLException("Not yet implemented");
    }

}

