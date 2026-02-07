// SPDX-FileCopyrightText: Copyright 2017-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.sql.SQLInvalidAuthorizationSpecException;

/**
 * Exception that indicates encryption could not be initialised.
 * <p>
 * This exception is thrown when wire encryption cannot be initialised, for example if the current authentication
 * plugin does not support generating a session key, or if no matching cipher can be found.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class FBSQLEncryptException extends SQLInvalidAuthorizationSpecException {

    @Serial
    private static final long serialVersionUID = -9184247902589102163L;

    public FBSQLEncryptException(String reason, @Nullable String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }
}
