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
package org.firebirdsql.gds.ng;

import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link WarningMessageCallback} for testing.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class SimpleWarningMessageCallback implements WarningMessageCallback {

    private final List<SQLWarning> warnings = new ArrayList<>();

    @Override
    public void processWarning(SQLWarning warning) {
        warnings.add(warning);
    }

    /**
     * @return List with received warnings (direct access)
     */
    public List<SQLWarning> getWarnings() {
        return warnings;
    }
}
