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
package org.firebirdsql.gds.ng.dbcrypt;

/**
 * Plugin for Firebird database encryption callback.
 * <p>
 * Database encryption callbacks are allowed to be stateful (eg if they require multiple callbacks to work). A new
 * callback instance is created for each authentication phase of a connection (a connection can have multiple
 * authentication phases).
 * </p>
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public interface DbCryptCallback {

    /**
     * Name of the database encryption callback.
     *
     * @return Name for identifying this callback within Jaybird.
     * @see DbCryptCallbackSpi#getDbCryptCallbackName()
     */
    String getDbCryptCallbackName();

    /**
     * Callback method to be called with the server data.
     * <p>
     * The implementation should reply with a response for the provided data. If the plugin cannot provide a response
     * (eg because the server data is invalid), use an empty reply (eg use {@link DbCryptData#EMPTY_DATA}, or construct
     * your own). The plugin should <b>not</b> throw an exception.
     * </p>
     *
     * @param serverData
     *         Data received from the server (never {@code null}).
     * @return Reply data (never {@code null}, use {@link DbCryptData#EMPTY_DATA} if there is no (valid) reply).
     */
    DbCryptData handleCallback(DbCryptData serverData);

}
