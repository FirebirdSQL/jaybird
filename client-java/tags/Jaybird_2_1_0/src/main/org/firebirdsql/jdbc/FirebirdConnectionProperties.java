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

import java.sql.SQLException;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * Connection properties for the Firebird connection. Main part of this
 * interface corresponds to the Database Parameter Buffer, but also contains
 * properties to specify default transaction parameters.
 * 
 */
public interface FirebirdConnectionProperties {
    
    /**
     * @return path to the database including the server name and the port,
     * if needed.
     */
    String getDatabase();
    
    /**
     * @param database path to the database including the server name and the 
     * port, if needed.
     */
    void setDatabase(String database);
    
    /**
     * @return type of the connection, for example, "PURE_JAVA", "LOCAL", 
     * "EMBEDDED", depends on the GDS implementations installed in the system. 
     */
    String getType();
    
    /**
     * @param type type of the connection, for example, "PURE_JAVA", "LOCAL", 
     * "EMBEDDED", depends on the GDS implementations installed in the system.
     */
    void setType(String type);

    /**
     * @return BLOB buffer size in bytes.
     */
    int getBlobBufferSize();

    /**
     * @param bufferSize
     *            size of the BLOB buffer in bytes.
     */
    void setBlobBufferSize(int bufferSize);

    /**
     * @return Character set for the connection.
     * 
     * @see #setCharSet(String)
     */
    String getCharSet();

    /**
     * @param charSet
     *            Character set for the connection. Similar to
     *            <code>encoding</code> property, but accepts Java names
     *            instead of Firebird ones.
     */
    void setCharSet(String charSet);

    /**
     * @return Character encoding for the connection.
     * 
     * @see #setEncoding(String)
     */
    String getEncoding();

    /**
     * @param encoding
     *            Character encoding for the connection. See Firebird
     *            documentation for more information.
     */
    void setEncoding(String encoding);

    /**
     * @return SQL role to use.
     */
    String getRoleName();

    /**
     * @param roleName
     *            SQL role to use.
     */
    void setRoleName(String roleName);

    /**
     * @return SQL dialect of the client.
     */
    String getSqlDialect();

    /**
     * @param sqlDialect
     *            SQL dialect of the client.
     */
    void setSqlDialect(String sqlDialect);

    /**
     * @return path to the character translation table.
     */
    String getUseTranslation();

    /**
     * @param translationPath
     *            path to the character translation table.
     */
    void setUseTranslation(String translationPath);

    /**
     * @return <code>true</code> if stream blobs should be created, otherwise
     *         <code>false</code>.
     */
    boolean isUseStreamBlobs();

    /**
     * @param useStreamBlobs
     *            <code>true</code> if stream blobs should be created,
     *            otherwise <code>false</code>.
     */
    void setUseStreamBlobs(boolean useStreamBlobs);

    /**
     * @return <code>true</code> if driver should assume that standard UDF are
     *         installed.
     */
    boolean isUseStandardUdf();

    /**
     * @param useStandardUdf
     *            <code>true</code> if driver should assume that standard UDF
     *            are installed.
     */
    void setUseStandardUdf(boolean useStandardUdf);

    /**
     * @return socket buffer size in bytes, or -1 is not specified.
     */
    int getSocketBufferSize();

    /**
     * @param socketBufferSize
     *            socket buffer size in bytes.
     */
    void setSocketBufferSize(int socketBufferSize);

    /**
     * @return <code>true</code> if the Jaybird 1.0 handling of the calendar
     *         in corresponding setters. This is also compatible with MySQL
     *         calendar treatment.
     */
    boolean isTimestampUsesLocalTimezone();

    /**
     * @param timestampUsesLocalTimezone
     *            <code>true</code> if the Jaybird 1.0 handling of the
     *            calendar in corresponding setters. This is also compatible
     *            with MySQL calendar treatment.
     */
    void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone);

    /**
     * @return name of the user that will be used when connecting to the
     *         database.
     */
    String getUserName();

    /**
     * @param userName
     *            name of the user that will be used when connecting to the
     *            database.
     */
    void setUserName(String userName);

    /**
     * @return password corresponding to the specified user name.
     */
    String getPassword();

    /**
     * @param password
     *            password corresponding to the specified user name.
     */
    void setPassword(String password);

    /**
     * @return number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    int getBuffersNumber();

    /**
     * @param buffersNumber
     *            number of cache buffers that should be allocated for this
     *            connection, should be specified for ClassicServer instances,
     *            SuperServer has a server-wide configuration parameter.
     */
    void setBuffersNumber(int buffersNumber);

    /**
     * Get the property that does not have corresponding getter method by its
     * name.
     * 
     * @param key
     *            name of the property to get.
     * 
     * @return value of the property.
     */
    String getNonStandardProperty(String key);

    /**
     * Set the property that does not have corresponding setter method.
     * 
     * @param key
     *            name of the property to set.
     * @param value
     *            value of the property.
     */
    void setNonStandardProperty(String key, String value);

    /**
     * Set the property that does not have corresponding setter method.
     * 
     * @param propertyMapping
     *            parameter value in the ?propertyName[=propertyValue]? form,
     *            this allows setting non-standard parameters using
     *            configuration files.
     */
    void setNonStandardProperty(String propertyMapping);

    /**
     * Get the database parameter buffer corresponding to the current connection
     * request information.
     * 
     * @return instance of {@link DatabaseParameterBuffer}.
     * 
     * @throws SQLException if database parameter buffer cannot be created.
     */
    DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException;

    /**
     * Get the used TPB mapping.
     * 
     * @return path to the TPB mapping.
     * 
     * @see #setTpbMapping(String)
     */
    String getTpbMapping();

    /**
     * Set path to the properties file with the TPB mapping. The path begins
     * with the protocol specification followed by the path to the resource. A
     * special protocol <code>"res:"</code> should be used to specify resource
     * in the classpath.
     * <p>
     * For the compatibility reasons, if no protocol is specified, classpath is
     * used by default.
     * <p>
     * Properties file contains a mapping between the transaction isolation
     * level (name of the constant in the {@link java.sql.Connection} interface
     * and a comma-separated list of TPB parameters.
     * 
     * @param tpbMapping
     *            path to the properties file.
     */
    void setTpbMapping(String tpbMapping);

    /**
     * Get the default transaction isolation level. This is the transaction
     * isolation level for the newly created connections.
     * 
     * @return default transaction isolation level.
     */
    int getDefaultTransactionIsolation();

    /**
     * Set the default transaction isolation level.
     * 
     * @param defaultIsolationLevel
     *            default transaction isolation level.
     */
    void setDefaultTransactionIsolation(int defaultIsolationLevel);

    /**
     * Get the default transaction isolation level as string. This method is
     * complementary to the {@link #getDefaultTransactionIsolation()}, however
     * it takes a string as parameter instead of a numeric constant.
     * 
     * @return default transaction isolation as string.
     * 
     * @see #setDefaultIsolation(String)
     */
    String getDefaultIsolation();

    /**
     * Set the default transaction isolation level as string. This method is
     * complementary to the {@link #setDefaultTransactionIsolation(int)},
     * however it takes a string as parameter instead of a numeric constant.
     * <p>
     * Following strings are allowed:
     * <ul>
     * <li><code>"TRANSACTION_READ_COMMITTED"</code> for a READ COMMITTED
     * isolation level.
     * <li><code>"TRANSACTION_REPEATABLE_READ"</code> for a REPEATABLE READ
     * isolation level.
     * <li><code>"TRANSACTION_SERIALIZABLE"</code> for a SERIALIZABLE
     * isolation level.
     * </ul>
     * 
     * @param isolation
     *            string constant representing a default isolation level.
     */
    void setDefaultIsolation(String isolation);

    /**
     * Get the transaction parameter buffer corresponding to the current
     * connection request information.
     * 
     * @param isolation
     *            transaction isolation level for which TPB should be returned.
     * 
     * @return instance of {@link TransactionParameterBuffer}.
     */
    TransactionParameterBuffer getTransactionParameters(int isolation);

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * The specified TPB is used as a default mapping for the specified
     * isolation level.
     * 
     * @param isolation
     *            transaction isolation level.
     * 
     * @param tpb
     *            instance of {@link TransactionParameterBuffer} containing
     *            transaction parameters.
     */
    void setTransactionParameters(int isolation, TransactionParameterBuffer tpb);
    
    boolean isDefaultResultSetHoldable();
    
    void setDefaultResultSetHoldable(boolean isHoldable);
}
