// SPDX-FileCopyrightText: Copyright 2018-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.dbcrypt.simple;

import org.firebirdsql.gds.ng.dbcrypt.DbCryptCallback;
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;
import org.jspecify.annotations.Nullable;

/**
 * Simple database encryption callback, provides a static value response to the callback
 *
 * @author Mark Rotteveel
 * @since 3.0.4
 */
public final class StaticValueDbCryptCallback implements DbCryptCallback {

    static final StaticValueDbCryptCallback EMPTY_RESPONSE = new StaticValueDbCryptCallback(null);

    private final byte @Nullable [] staticValue;

    StaticValueDbCryptCallback(byte @Nullable [] staticValue) {
        this.staticValue = staticValue;
    }

    @Override
    public String getDbCryptCallbackName() {
        return StaticValueDbCryptCallbackSpi.NAME;
    }

    @Override
    public DbCryptData handleCallback(DbCryptData serverData) {
        return DbCryptData.createReply(staticValue);
    }
    
}
