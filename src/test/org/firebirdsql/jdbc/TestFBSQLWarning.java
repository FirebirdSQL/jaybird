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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2.10
 */
public class TestFBSQLWarning {

    @Test
    public void getMessageShouldDisplayMessageFromConstructor() {
        final String message = "This is the message";
        final FBSQLWarning warning = new FBSQLWarning(message);

        assertEquals(message, warning.getMessage());
    }

    @Test
    public void getMessageShouldDisplayMessageFromGDSException() {
        final String message = "This is the message from GDSException";
        final FBSQLWarning warning = new FBSQLWarning(
                new GDSException(ISCConstants.isc_arg_warning, ISCConstants.isc_random, message));

        assertEquals(message, warning.getMessage());
    }
}
