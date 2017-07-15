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
package org.firebirdsql.gds.ng.wire.crypt;

import java.sql.SQLInvalidAuthorizationSpecException;

/**
 * Exception that indicates encryption could not be initialized.
 * <p>
 * This exception is thrown when wire encryption cannot be initialized, for example if the current authentication
 * plugin does not support generating a session key, or if no matching cipher can be found.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.1
 */
public class FBSQLEncryptException extends SQLInvalidAuthorizationSpecException {

    public FBSQLEncryptException(String reason, String sqlState, int vendorCode) {
        super(reason, sqlState, vendorCode);
    }
}
