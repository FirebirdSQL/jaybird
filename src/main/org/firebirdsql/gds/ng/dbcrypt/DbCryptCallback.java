// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
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
 * @author Mark Rotteveel
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
