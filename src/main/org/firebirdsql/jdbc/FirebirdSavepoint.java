// SPDX-FileCopyrightText: Copyright 2005 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2011-2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jdbc;

import java.sql.Savepoint;

/**
 * Firebird-specific extensions to the {@link java.sql.Savepoint} interface.
 */
public interface FirebirdSavepoint extends Savepoint {
    /* Empty interface retained for backwards compatibility and potential future extension */
}
