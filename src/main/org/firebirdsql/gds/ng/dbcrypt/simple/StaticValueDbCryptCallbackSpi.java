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
package org.firebirdsql.gds.ng.dbcrypt.simple;

import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallbackSpi;
import org.firebirdsql.util.ByteArrayHelper;

import java.nio.charset.StandardCharsets;

/**
 * Provider for a static value response database encryption callback.
 * <p>
 * This provider takes the {@code dbCryptConfig} value to determine the static value of the response:
 * </p>
 * <ul>
 * <li>value is {@code null}: empty response</li>
 * <li>value starts with {@code "base64:"}: rest of the value is decoded to bytes using base64</li>
 * <li>all other values are encoded to bytes using UTF-8</li>
 * </ul>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public final class StaticValueDbCryptCallbackSpi implements DbCryptCallbackSpi {

    static final String NAME = "StaticValue";
    private static final String BASE64_PREFIX = "base64:";
    private static final int BASE64_PREFIX_LENGTH = 7;

    @Override
    public String getDbCryptCallbackName() {
        return NAME;
    }

    @Override
    public DbCryptCallback createDbCryptCallback(String dbCryptConfig) {
        if (dbCryptConfig == null) {
            return StaticValueDbCryptCallback.EMPTY_RESPONSE;
        }

        final byte[] staticValue;
        if (dbCryptConfig.startsWith(BASE64_PREFIX)) {
            staticValue = ByteArrayHelper.fromBase64String(dbCryptConfig.substring(BASE64_PREFIX_LENGTH));
        } else {
            staticValue = dbCryptConfig.getBytes(StandardCharsets.UTF_8);
        }
        return new StaticValueDbCryptCallback(staticValue);
    }
}
