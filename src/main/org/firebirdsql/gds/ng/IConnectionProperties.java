/*
 * Public Firebird Java API.
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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.DatabaseParameterBuffer;

/**
 * Connection properties for the Firebird connection.
 * <p>
 * TODO Remove overlap/duplication with {@link org.firebirdsql.jdbc.FirebirdConnectionProperties}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface IConnectionProperties extends IAttachProperties<IConnectionProperties> {

    /**
     * Value for {@code sessionTimeZone} that indicates the session time zone should not be set and use server default.
     */
    String SESSION_TIME_ZONE_SERVER = "server";
    short DEFAULT_DIALECT = 3;
    int DEFAULT_BUFFERS_NUMBER = 0;

    /**
     * @return Name or alias of the database
     */
    String getDatabaseName();

    /**
     * @param databaseName
     *         Name or alias of the database
     */
    void setDatabaseName(String databaseName);

    /**
     * Get the dialect of the client connection
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_DIALECT} if
     * the value hasn't been set yet.
     * </p>
     *
     * @return SQL dialect of the client.
     */
    short getConnectionDialect();

    /**
     * Set the dialect of the client connection
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_DIALECT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @param connectionDialect
     *         SQL dialect of the client.
     */
    void setConnectionDialect(short connectionDialect);

    /**
     * Get the page cache size.
     * <p>
     * A value of <code>0</code> indicates that the value is not set, and that
     * the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic)
     * </p>
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_BUFFERS_NUMBER} if
     * the value hasn't been set yet.
     * </p>
     *
     * @return number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    int getPageCacheSize();

    /**
     * Set the page cache size.
     * <p>
     * A value of <code>0</code> indicates that the value is not set, and that
     * the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic)
     * </p>
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_BUFFERS_NUMBER} if
     * the value hasn't been set yet.
     * </p>
     *
     * @param pageCacheSize
     *         number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    void setPageCacheSize(int pageCacheSize);

    /**
     * Set if {@link java.sql.ResultSet} should be {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT} by default.
     *
     * @param holdable
     *         <code>true</code> ResultSets are holdable, <code>false</code> (default) ResultSets are {@link
     *         java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    void setResultSetDefaultHoldable(boolean holdable);

    /**
     * Get whether ResultSets are holdable by default.
     *
     * @return <code>true</code> ResultSets by default are {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT},
     *         <code>false</code> (default), ResultSets
     *         are {@link java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    boolean isResultSetDefaultHoldable();

    /**
     * Set if {@link java.sql.ResultSetMetaData#getColumnName(int)} returns the <code>columnLabel</code> instead of the
     * <code>columnName</code>.
     * <p>
     * The default behaviour (with <code>columnLabelForName=false</code> is JDBC-compliant. The behavior for value
     * <code>true</code> is
     * to provide compatibility with tools with a wrong expectation.
     * </p>
     *
     * @param columnLabelForName
     *         <code>false</code> JDBC compliant behavior (<code>columnName</code> is returned), <code>true</code>
     *         compatibility option (<code>columnLabel</code> is returned)
     */
    void setColumnLabelForName(boolean columnLabelForName);

    /**
     * Gets the current setting of <code>columnLabelForName</code>
     *
     * @return <code>false</code> JDBC compliant behavior (<code>columnName</code> is returned), <code>true</code>
     *         compatibility option (<code>columnLabel</code> is returned)
     * @see #setColumnLabelForName(boolean)
     */
    boolean isColumnLabelForName();

    /**
     * Sets the {@code sessionTimeZone}.
     *
     * @param sessionTimeZone
     *         Firebird 4+ session time zone name (we strongly suggest to use Java compatible names only),
     *         use {@code "server"} to use server default time zone (note: conversion will use JVM default time zone)
     * @since 4.0
     */
    void setSessionTimeZone(String sessionTimeZone);

    /**
     * Get the {@code sessionTimeZone}.
     *
     * @return value for {@code sessionTimeZone}
     * @since 4.0
     */
    String getSessionTimeZone();

    /**
     * Gets the extra database parameters. This can be used to pass extra database parameters that are not directly
     * supported.
     * <p>
     * An immutable instance of <code>IConnectionProperties</code> <b>must</b> return a copy.
     * </p>
     *
     * @return DatabaseParameterBuffer instance.
     */
    DatabaseParameterBuffer getExtraDatabaseParameters();

    /**
     * @return An immutable version of this instance as an implementation of {@link IConnectionProperties}
     */
    @Override
    IConnectionProperties asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IConnectionProperties} with all properties
     * copied.
     */
    @Override
    IConnectionProperties asNewMutable();
}
