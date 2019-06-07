/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.management;

import org.firebirdsql.gds.impl.GDSFactory;

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
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface FBManagerMBean extends AutoCloseable {

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
     * Set the host name of the Firebird server.
     *
     * @param host
     *         Hostname
     */
    void setServer(String host);

    /**
     * @return The host name of the Firebird server (default is {@code "localhost"})
     */
    String getServer();

    /**
     * Set the port of the Firebird server.
     *
     * @param port
     *         Port of the Firebird server
     */
    void setPort(int port);

    /**
     * @return The port of the Firebird server (default is {@code 3050})
     */
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
    void setFileName(final String fileName);

    /**
     * @return GDS plugin type name (default is {@link GDSFactory#getDefaultGDSType()})
     */
    String getType();

    /**
     * Set the GDS plugin type to use.
     *
     * @param type
     *         GDS plugin type name ({@code PURE_JAVA}, {@code NATIVE}, {@code EMBEDDED}, {@code LOCAL})
     */
    void setType(String type);

    /**
     * @return User name
     */
    String getUserName();

    /**
     * Set the user name.
     *
     * @param userName
     *         User name
     */
    void setUserName(final String userName);

    /**
     * @return password
     */
    String getPassword();

    /**
     * Set the password.
     *
     * @param password
     *         Password
     */
    void setPassword(final String password);

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
    void setCreateOnStart(final boolean createOnStart);

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
    void setDropOnStop(final boolean dropOnStop);

    /**
     * Get if the database will be be dropped if exists when creating a database.
     *
     * @return {@code true} to drop existing database on create, {@code false} to not create a database if it exists.
     */
    boolean isForceCreate();

    /**
     * Set if the database will be be dropped if exists when creating a database.
     *
     * @param forceCreate
     *         {@code true} to drop existing database on create, {@code false} to not create a database if it exists.
     */
    void setForceCreate(boolean forceCreate);

    /**
     * Create a database with the specified file name, user name and password on the specified {@code server}
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
     * Drop a database with the specified file name, user name and password on the specified {@code server}
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
     * Check if a database exists with the specified file name, user name and password on the specified {@code server}
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
