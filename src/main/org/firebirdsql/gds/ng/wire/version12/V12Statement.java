// SPDX-FileCopyrightText: Copyright 2014-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.version11.V11Statement;

/**
 * @author Mark Rotteveel
 */
public class V12Statement extends V11Statement {

    /**
     * Creates a new instance of V12Statement for the specified database.
     *
     * @param database
     *         FbWireDatabase implementation
     */
    public V12Statement(FbWireDatabase database) {
        super(database);
    }

    @Override
    public int getMaxSqlInfoSize() {
        // Theoretically, also protocol 11 (Firebird 2.1), but not supported since before Jaybird 5
        return 65535;
    }

}
