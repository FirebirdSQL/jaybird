/*
 SPDX-FileCopyrightText: Copyright 2001-2003 David Jencks
 SPDX-FileCopyrightText: Copyright 2002-2003 Blas Rodriguez Somoza
 SPDX-FileCopyrightText: Copyright 2004 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2016-2025 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
*/
package org.firebirdsql.management;

import org.firebirdsql.jaybird.props.AttachmentProperties;

/**
 * API for {@link FBManager}, for creating and dropping databases.
 * <p>
 * The class {@link FBManager} provides an API to create and drop databases, including options to limit the lifetime
 * of a database
 * </p>
 * <pre>
 * // Use try-with-resources to automatically stop (close) FBManager
 * try (FBManager manager = new FBManager()) {
 *     manager.setServer("localhost");
 *     manager.setUser("sysdba");
 *     manager.setPassword("masterkey");
 *     manager.setFileName("path/to/database.fdb");
 *     manager.setPageSize(PageSizeConstants.SIZE_16K);
 *     manager.setDefaultCharacterSet("UTF8");
 *
 *     // Create database when manager is started
 *     manager.setCreateOnStart(true);
 *     // Drop database on creation if it already exists
 *     manager.setForceCreate(true);
 *     // Drop database when manager is stopped
 *     manager.setDropOnStop(true);
 *     manager.start();
 *
 *     // Use database...
 * }
 * // After end of try-with-resources, the database will be dropped
 * </pre>
 *
 * @author David Jencks
 * @version 1.0
 */
@SuppressWarnings("java:S112")
public interface FBManagerMBean extends AttachmentProperties, AutoCloseable {

    /**
     * Start this manager.
     * <p>
     * If {@link #isCreateOnStart()} is {@code true}, will attempt to create a new database specified using
     * the {@code server}, {@code port}, {@code fileName} and related properties. If the specified already exists,
     * nothing will be created unless {@link #isForceCreate()} is {@code true}. When {@code forceCreate} is {@code true}
     * an attempt will be made to drop the database.
     * </p>
     *
     * @throws IllegalStateException
     *         If this manager is not started
     * @throws Exception
     *         For failures to start or create the database
     */
    void start() throws Exception;

    /**
     * Stop this manager.
     * <p>
     * If {@link #isDropOnStop()} is {@code true}, will attempt to drop the database specified using the {@code server},
     * {@code port}, {@code fileName} and related properties,
     * </p>
     * <p>
     * If the manager is currently stopped, this method will do nothing.
     * </p>
     *
     * @throws Exception
     *         For failures to drop the database.
     */
    void stop() throws Exception;

    /**
     * Alias for {@link #stop()}
     */
    @Override
    void close() throws Exception;

    /**
     * @return Descriptive name of this manager
     */
    String getName();

    /**
     * @return Current state ({@code "Stopped"} or {@code "Started"}
     */
    String getState();

    /**
     * Set the hostname of the Firebird server.
     *
     * @param host
     *         hostname
     * @deprecated use {@link #setServerName(String)}; will not be removed for now
     */
    @Deprecated(since = "7")
    void setServer(String host);

    /**
     * @return hostname of the Firebird server (default is {@code "localhost"})
     * @deprecated use {@link #getServerName()}; will not be removed for now
     */
    @Deprecated(since = "7")
    String getServer();

    /**
     * Set the port of the Firebird server.
     *
     * @param port
     *         port of the Firebird server
     * @deprecated use {@link #setPortNumber(int)}; will not be removed for now
     */
    @Deprecated(since = "7")
    void setPort(int port);

    /**
     * @return port of the Firebird server (default is {@code 3050})
     * @deprecated use {@link #getPortNumber()}; will not be removed for now
     */
    @Deprecated(since = "7")
    int getPort();

    /**
     * @return File name or alias of the database
     */
    String getFileName();

    /**
     * Set the file name or alias of the database.
     *
     * @param fileName
     *         File name or alias of the database
     */
    void setFileName(String fileName);

    /**
     * @return username
     * @deprecated use {@link #getUser()}; will not be removed for now
     */
    @Deprecated(since = "7")
    String getUserName();

    /**
     * Set the username.
     *
     * @param userName
     *         username
     * @deprecated use {@link #setUser(String)}; will not be removed for now
     */
    @Deprecated(since = "7")
    void setUserName(String userName);

    /**
     * Set the database dialect to use when creating a new database.
     *
     * @param dialect
     *         Database dialect (1 or 3)
     * @throws IllegalArgumentException
     *         if value is not 1 or 3
     * @see #getDialect()
     */
    void setDialect(int dialect);

    /**
     * @return Database dialect (either 1 or 3), default is 3.
     */
    int getDialect();

    /**
     * Set the page size to use when creating a new database.
     * <p>
     * The value for {@code pageSize} must be one of {@link PageSizeConstants}. The default value depends on the
     * Firebird version.
     * </p>
     * <p>
     * Some values are not valid on all Firebird versions.
     * </p>
     *
     * @param pageSize
     *         The page size to be used in the created database, see {@link PageSizeConstants}
     * @see PageSizeConstants
     */
    void setPageSize(int pageSize);

    /**
     * @return The page size to be used when creating a database, or {@code -1} if the database default is used.
     */
    int getPageSize();

    /**
     * Set the default database character set to use when creating a new database.
     *
     * @param firebirdCharsetName
     *         Character set name, use Firebird names only; {@code null} will use Firebird default ({@code NONE}).
     *         Specifying an invalid name will result in an exception during database creation.
     */
    void setDefaultCharacterSet(String firebirdCharsetName);

    /**
     * @return The default character set name, {@code null} means not set.
     * @see #setDefaultCharacterSet(String)
     */
    String getDefaultCharacterSet();

    /**
     * Control force write behaviour of the created database.
     * <p>
     * Only use this method if you know what you're doing, and if you can live with data loss and database corruption.
     * In general it is advisable to use the Firebird default ({@code null} for this method).
     * </p>
     *
     * @param forceWrite
     *         {@code null} - default behaviour (force write enabled after database creation and initialization),
     *         {@code true} - enable force write at database creation,
     *         {@code false} - disable force write
     */
    void setForceWrite(Boolean forceWrite);

    /**
     * @return The forced writes configuration
     * @see #setForceWrite(Boolean)
     */
    Boolean getForceWrite();

    /**
     * Get if the database will be created when calling {@link #start()}.
     *
     * @return {@code true} when the database will be created on start, {@code false} otherwise.
     */
    boolean isCreateOnStart();

    /**
     * Set if the database will be created when calling {@link #start()}.
     *
     * @param createOnStart
     *         {@code true} to create the database on start, {@code false} to not create on start (default)
     */
    void setCreateOnStart(boolean createOnStart);

    /**
     * Get if the database will be created when calling {@link #stop()}.
     *
     * @return {@code true} to drop the database on stop, {@code false} otherwise
     */
    boolean isDropOnStop();

    /**
     * Set if the database will be created when calling {@link #stop()}.
     *
     * @param dropOnStop
     *         {@code true} to drop the database on stop, {@code false} to not drop on stop (default)
     */
    void setDropOnStop(boolean dropOnStop);

    /**
     * Get if the database will be dropped if exists when creating a database.
     *
     * @return {@code true} to drop existing database on create, {@code false} to not create a database if it exists.
     */
    boolean isForceCreate();

    /**
     * Set if the database will be dropped if exists when creating a database.
     *
     * @param forceCreate
     *         {@code true} to drop existing database on create, {@code false} to not create a database if it exists.
     */
    void setForceCreate(boolean forceCreate);

    /**
     * Create a database with the specified file name, username and password on the specified {@code server}
     * and {@code port}.
     * <p>
     * On creation, the following properties will used to configure the database: {@code dialect}, {@code pageSize},
     * {@code defaultCharacterSet}.
     * </p>
     * <p>
     * If the database already exists, and {@code forceCreate} is {@code true}, the database will be dropped. If
     * {@code false}, no database will be created.
     * </p>
     *
     * @param fileName
     *         Database file name or alias
     * @param user
     *         User name
     * @param password
     *         Password
     * @throws IllegalStateException
     *         If this manager is not started
     * @throws Exception
     *         If database creation fails.
     */
    void createDatabase(String fileName, String user, String password) throws Exception;

    /**
     * Create a database with the specified file name, username, password and role on the specified {@code server}
     * and {@code port}.
     * <p>
     * On creation, the following properties will used to configure the database: {@code dialect}, {@code pageSize},
     * {@code defaultCharacterSet}.
     * </p>
     * <p>
     * If the database already exists, and {@code forceCreate} is {@code true}, the database will be dropped. If
     * {@code false}, no database will be created.
     * </p>
     *
     * @param fileName
     *         Database file name or alias
     * @param user
     *         User name
     * @param password
     *         Password
     * @param roleName
     *         Role name (or {@code null} for no role)
     * @throws IllegalStateException
     *         If this manager is not started
     * @throws Exception
     *         If database creation fails.
     */
    void createDatabase(String fileName, String user, String password, String roleName) throws Exception;

    /**
     * Drop a database with the specified file name, username and password on the specified {@code server}
     * and {@code port}.
     *
     * @param fileName
     *         Database file name or alias
     * @param user
     *         User name
     * @param password
     *         Password
     * @throws Exception
     *         If this manager is not started or database drop fails.
     */
    void dropDatabase(String fileName, String user, String password) throws Exception;

    /**
     * Drop a database with the specified file name, username, password and role on the specified {@code server}
     * and {@code port}.
     *
     * @param fileName
     *         Database file name or alias
     * @param user
     *         User name
     * @param password
     *         Password
     * @param roleName
     *         Role name (or {@code null} for no role)
     * @throws Exception
     *         If this manager is not started or database drop fails.
     */
    void dropDatabase(String fileName, String user, String password, String roleName) throws Exception;

    /**
     * Check if a database exists with the specified file name, username and password on the specified {@code server}
     * and {@code port}.
     * <p>
     * Existence is checked by connecting to the database, so any connection error, including invalid credentials, will
     * report the database as not existing.
     * </p>
     *
     * @param fileName
     *         Database file name or alias
     * @param user
     *         User name
     * @param password
     *         Password
     * @return {@code true} if the database exists and can be connected, {@code false} if the database does not exist
     * or any other error occurred.
     * @throws Exception
     *         Currently no other exception is thrown, this may change in the future
     */
    boolean isDatabaseExists(String fileName, String user, String password) throws Exception;

}
