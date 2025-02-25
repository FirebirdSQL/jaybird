// SPDX-FileCopyrightText: Copyright 2017 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.crypt;

import java.sql.SQLInvalidAuthorizationSpecException;

/**
 * Exception that indicates encryption could not be initialized.
 * <p>
 * This exception is thrown when wire encryption cannot be initialized, for example if the current authentication
 * plugin does not support generating a session key, or if no matching cipher can be found.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class FBSQLEncryptException extends SQLInvalidAuthorizationSpecException {

    public FBSQLEncryptException(String reason, String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }
}
