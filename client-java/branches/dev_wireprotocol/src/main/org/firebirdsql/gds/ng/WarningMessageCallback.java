/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import java.sql.SQLWarning;

/**
 * Callback interface for passing warnings.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface WarningMessageCallback {

    /**
     * Dummy WarningMessageCallback that does nothing
     */
    WarningMessageCallback DUMMY = new WarningMessageCallback() {
        @Override
        public void processWarning(SQLWarning warning) {
        }
    };

    /**
     * Signals the warning to the callback
     *
     * @param warning
     *         Warning of type SQLWarning
     */
    void processWarning(SQLWarning warning);
}
