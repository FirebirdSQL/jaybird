// SPDX-FileCopyrightText: Copyright 2014-2017 Mark Rotteveel
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

}
