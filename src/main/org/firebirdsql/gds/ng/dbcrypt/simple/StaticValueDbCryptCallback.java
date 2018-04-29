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
import org.firebirdsql.gds.ng.dbcrypt.DbCryptData;

/**
 * Simple database encryption callback, provides a static value response to the callback
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public final class StaticValueDbCryptCallback implements DbCryptCallback {

    static final StaticValueDbCryptCallback EMPTY_RESPONSE = new StaticValueDbCryptCallback(null);

    private final byte[] staticValue;

    StaticValueDbCryptCallback(byte[] staticValue) {
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
