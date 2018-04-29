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
 * Service provider interface for database encryption callback plugins.
 * <p>
 * NOTE: This plugin is currently only internal to Jaybird, consider the API as unstable.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
