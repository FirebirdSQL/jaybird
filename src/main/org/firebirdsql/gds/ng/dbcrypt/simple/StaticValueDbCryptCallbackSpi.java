// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.dbcrypt.simple;

import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallbackSpi;
import org.firebirdsql.jaybird.util.ByteArrayHelper;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * Provider for a static value response database encryption callback.
 * <p>
 * This provider takes the {@code dbCryptConfig} value to determine the static value of the response:
 * </p>
 * <ul>
 * <li>value is {@code null}: empty response</li>
 * <li>value starts with {@code "base64:"}: rest of the value is decoded to bytes using base64</li>
 * <li>value starts with {@code "base64url:"}: rest of the value is decoded to bytes using base64 URL-safe</li>
 * <li>all other values are encoded to bytes using UTF-8</li>
 * </ul>
 *
 * @author Mark Rotteveel
 * @since 3.0.4
 */
public final class StaticValueDbCryptCallbackSpi implements DbCryptCallbackSpi {

    static final String NAME = "StaticValue";
    private static final String BASE64_PREFIX = "base64:";
    private static final int BASE64_PREFIX_LENGTH = 7;
    private static final String BASE64URL_PREFIX = "base64url:";
    private static final int BASE64URL_PREFIX_LENGTH = 10;

    @Override
    public String getDbCryptCallbackName() {
        return NAME;
    }

    @Override
    public DbCryptCallback createDbCryptCallback(@Nullable String dbCryptConfig) {
        if (dbCryptConfig == null) {
            return StaticValueDbCryptCallback.EMPTY_RESPONSE;
        }

        final byte[] staticValue;
        if (dbCryptConfig.startsWith(BASE64_PREFIX)) {
            staticValue = ByteArrayHelper.fromBase64String(dbCryptConfig.substring(BASE64_PREFIX_LENGTH));
        } else if (dbCryptConfig.startsWith(BASE64URL_PREFIX)) {
            staticValue = ByteArrayHelper.fromBase64urlString(dbCryptConfig.substring(BASE64URL_PREFIX_LENGTH));
        } else {
            staticValue = dbCryptConfig.getBytes(StandardCharsets.UTF_8);
        }
        return new StaticValueDbCryptCallback(staticValue);
    }
}
