// SPDX-FileCopyrightText: Copyright 2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
