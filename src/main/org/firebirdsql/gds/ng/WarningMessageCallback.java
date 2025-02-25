// SPDX-FileCopyrightText: Copyright 2013-2016 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import java.sql.SQLWarning;

/**
 * Callback interface for passing warnings.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface WarningMessageCallback {

    /**
     * Signals the warning to the callback
     *
     * @param warning
     *         Warning of type SQLWarning
     */
    void processWarning(SQLWarning warning);
}
