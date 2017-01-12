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
package org.firebirdsql.pool;

import java.sql.SQLException;

import org.firebirdsql.jdbc.FirebirdConnectionProperties;

/**
 * Configuration for the {@link org.firebirdsql.pool.FBWrappingDataSource} and
 * {@link org.firebirdsql.pool.FBConnectionPoolDataSource} objects.
 * 
 * Following properties are supported:
 * <ul>
 * <li><code>blobBufferSize</code> 
 *      size of the buffer used to transfer BLOB data.
 * 
 * <li><code>blockingTimeout</code> 
 *      time in milliseconds during which {@link javax.sql.DataSource#getConnection()} method will 
 *      block if no free connection is in pool.
 * 
 * <li><code>charSet</code>
 *      similar to <code>encoding</code>, but takes Java character set name
 *      instead of Firebird's encoding.
 * 
 * <li><code>database</code> 
 *      path to a database including the server name; for example 
 *      <code>localhost/3050:c:/path/to/database.gdb</code>.
 * 
 * <li><code>encoding</code> 
 *      character encoding for the JDBC connection.
 * 
 * <li><code>freeSize</code>
 *      read-only: gives amount of free connections in the pool, when 0, blocking
 *      will occur if <code>workingSize</code> is equal to <code>maxPoolSize</code>.
 * 
 * <li><code>isolation</code>
 *      default transaction isolation level for connections as string; possible
 *      values are:
 *      <ul>
 *      <li>TRANSACTION_READ_COMMITTED
 *      <li>TRANSACTION_REPEATABLE_READ
 *      <li>TRANSACTION_SERIALIZABLE
 *      </ul>
 * 
 * <li><code>loginTimeout</code> 
 *      property from {@link javax.sql.DataSource}, in this context is a synonym 
 *      for <code>blockingTimeout</code> (however value is specified in seconds).
 * 
 * <li><code>maxIdleTime</code> 
 *      time in milliseconds after which idle physical connection in the 
 *      pool is closed.
 * 
 * <li><code>maxStatements</code>
 *      maximum number of pooled prepared statements, if 0, pooling is switched
 *      off.
 * 
 * <li><code>maxPoolSize</code> 
 *      maximum number of physical connections that can be opened by this data 
 *      source.
 * 
 * <li><code>minPoolSize</code> 
 *      minimum number of connections that will remain open by this data source.
 * 
 * <li><code>nonStandardProperty</code>
 *      a non-standard connection parameter in form <code>name[=value]</code>.
 * 
 * <li><code>password</code> 
 *      password that is used to connect to database.
 * 
 * <li><code>pingInterval</code> 
 *      time interval during which connection will be proved for aliveness.
 * 
 * <li><code>pooling</code>
 *      allows switching pooling off.
 * 
 * <li><code>statementPooling</code>
 *      alternative way to switch statement pooling off.
 * 
 * <li><code>socketBufferSize</code> 
 *      size of the socket buffer in bytes. In some cases values used by JVM by 
 *      default are not optimal. This results in performance degradation 
 *      (especially when you transfer big BLOBs). Usually 8192 bytes provides 
 *      good results.
 * 
 * <li><code>roleName</code> 
 *      SQL role name.
 * 
 * <li><code>tpbMapping</code> 
 *      mapping of the TPB parameters to JDBC transaction isolation levels.
 * 
 * <li><code>transactionIsolationLevel</code>
 *      default transaction isolation level, number from {@link java.sql.Connection}
 *      interface.
 * 
 * <li><code>totalSize</code>
 *      total number of allocated connections.
 * 
 * <li><code>type</code> 
 *      type of connection that will be created. There are four possible types: 
 *      pure Java (or type 4), type 2 that will use Firebird client library to 
 *      connect to the database, local-mode type 2 driver, and embedded that 
 *      will use embedded engine (access to local databases). Possible values 
 *      are (case insensitive):
 *      <ul> 
 *      <li><code>"PURE_JAVA"</code> or <code>"TYPE4"</code> 
 *          for pure Java (type 4) JDBC connections;
 * 
 *      <li><code>"NATIVE"</code> or <code>"TYPE2"</code> 
 *          to use Firebird client library;
 * 
 *      <li><code>"LOCAL"</code> 
 *          to use Firebird client library in local-mode (IPC link to server);
 * 
 *      <li><code>"EMBEDDED"</code> 
 *          to use embedded engine.
 *      </ul>
 * 
 * <li><code>userName</code> 
 *      name of the user that will be used to access the database.
 * 
 * <li><code>workingSize</code>
 *      number of connections that are in use (e.g. were obtained using
 *      {@link javax.sql.DataSource#getConnection()} method, but not yet closed).
 * </ul>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public interface FirebirdPool extends FirebirdConnectionProperties, ConnectionPoolConfiguration {
    
    void restart();
    void shutdown();

    /*
     * Properties of this datasource.
     */
     int getBlockingTimeout();

     void setBlockingTimeout(int blockingTimeoutValue);

     int getMaxIdleTime();

     void setMaxIdleTime(int maxIdleTime);

     int getMaxStatements();

     void setMaxStatements(int maxStatements);

     int getMaxPoolSize();

     void setMaxPoolSize(int maxPoolSize);
    
     int getMinPoolSize();
    
     void setMinPoolSize(int minPoolSize);

     int getPingInterval();

     void setPingInterval(int pingIntervalValue);

     boolean isPooling();

     void setPooling(boolean pooling);

     boolean isStatementPooling();

     void setStatementPooling(boolean statementPooling);

     int getFreeSize() throws SQLException;

     int getWorkingSize() throws SQLException;

     int getTotalSize() throws SQLException;
}