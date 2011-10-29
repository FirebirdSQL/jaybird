/*
 * Firebird Open Source J2ee connector - jdbc driver, public Firebird-specific 
 * JDBC extensions.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.jdbc;

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public interface FirebirdParameterMetaData extends ParameterMetaData {
    /**
     * Retrieves the number of parameters in the <code>PreparedStatement</code> 
     * object for which this <code>ParameterMetaData</code> object contains
     * information.
     *
     * @return the number of parameters
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    int getParameterCount() throws SQLException;

    /**
     * Retrieves whether null values are allowed in the designated parameter.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return the nullability status of the given parameter; one of 
     *        <code>ParameterMetaData.parameterNoNulls</code>, 
     *        <code>ParameterMetaData.parameterNullable</code>, or 
     *        <code>ParameterMetaData.parameterNullableUnknown</code>
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    int isNullable(int param) throws SQLException;

    /**
     * The constant indicating that a
     * parameter will not allow <code>NULL</code> values.
     */
    int parameterNoNulls = 0;

    /**
     * The constant indicating that a
     * parameter will allow <code>NULL</code> values.
     */
    int parameterNullable = 1;

    /**
     * The constant indicating that the
     * nullability of a parameter is unknown.
     */
    int parameterNullableUnknown = 2;

    /**
     * Retrieves whether values for the designated parameter can be signed numbers.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    boolean isSigned(int param) throws SQLException;

    /**
     * Retrieves the designated parameter's number of decimal digits.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return precision
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    int getPrecision(int param) throws SQLException;

    /**
     * Retrieves the designated parameter's number of digits to right of the decimal point.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return scale
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    int getScale(int param) throws SQLException;    

    /**
     * Retrieves the designated parameter's SQL type.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return SQL type from <code>java.sql.Types</code>
     * @exception SQLException if a database access error occurs
     * @since 1.4
     * @see Types
     */
    int getParameterType(int param) throws SQLException;

    /**
     * Retrieves the designated parameter's database-specific type name.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return type the name used by the database. If the parameter type is
     * a user-defined type, then a fully-qualified type name is returned.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    String getParameterTypeName(int param) throws SQLException;

 
    /**
     * Retrieves the fully-qualified name of the Java class whose instances 
     * should be passed to the method <code>PreparedStatement.setObject</code>.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming
     *         language that would be used by the method 
     *         <code>PreparedStatement.setObject</code> to set the value 
     *         in the specified parameter. This is the class name used 
     *         for custom mapping.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    String getParameterClassName(int param) throws SQLException;

    /**
     * The constant indicating that the mode of the parameter is unknown.
     */
    int parameterModeUnknown = 0;

    /**
     * The constant indicating that the parameter's mode is IN.
     */
    int parameterModeIn = 1;

    /**
     * The constant indicating that the parameter's mode is INOUT.
     */
    int parameterModeInOut = 2;

    /**
     * The constant indicating that the parameter's mode is  OUT.
     */
    int parameterModeOut = 4;

    /**
     * Retrieves the designated parameter's mode.
     *
     * @param param the first parameter is 1, the second is 2, ...
     * @return mode of the parameter; one of 
     *        <code>ParameterMetaData.parameterModeIn</code>,
     *        <code>ParameterMetaData.parameterModeOut</code>, or
     *        <code>ParameterMetaData.parameterModeInOut</code>
     *        <code>ParameterMetaData.parameterModeUnknown</code>.
     * @exception SQLException if a database access error occurs
     * @since 1.4
     */
    int getParameterMode(int param) throws SQLException;

}