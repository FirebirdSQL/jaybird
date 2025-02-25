// SPDX-FileCopyrightText: Copyright 2021-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.util.InternalApi;

/**
 * Class of {@link LocalStatementType}.
 * <p>
 * The types of this enum are decided by the needs of Jaybird, and do not necessarily cover all statement classes.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 6
 */
@InternalApi
public enum LocalStatementClass {

    DML,
    TRANSACTION_BOUNDARY,
    UNKNOWN

}
