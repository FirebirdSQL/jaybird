// SPDX-FileCopyrightText: Copyright 2018 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.dbcrypt;

/**
 * Service provider interface for database encryption callback plugins.
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0.4
 */
public interface DbCryptCallbackSpi {

    /**
     * Name of the database encryption callback.
     * <p>
     * This name is for identification and selection purposes. As the name will be used in connection properties, we
     * suggest to use relatively simple/short names, but make sure it is unique enough to prevent name conflicts.
     * Consider using something like {@code <company-or-author>.<name>}.
     * </p>
     *
     * @return Name for identifying this callback within Jaybird.
     */
    String getDbCryptCallbackName();

    /**
     * Creates the database encryption callback with a configuration string.
     * <p>
     * The configuration string of the {@code dbCryptConfig} connection property is plugin specific, but we suggest the
     * following conventions:
     * </p>
     * <ul>
     * <li>For binary data, use prefix {@code base64:} to indicate the rest of the string is base64-encoded</li>
     * <li>Avoid use of {@code &}, {@code ;} or {@code :}, or 'hide' this by using base64 encoding; this is necessary to
     * avoid existing limitations in the parsing of connection properties that are added directly to the URL (we
     * hope to address this in the future), and to allow support for other prefixes similar to {@code base64:}</li>
     * </ul>
     *
     * @param dbCryptConfig
     *         Configuration string from connection properties, or {@code null} if absent
     * @return Database encryption callback
     */
    DbCryptCallback createDbCryptCallback(String dbCryptConfig);

}
