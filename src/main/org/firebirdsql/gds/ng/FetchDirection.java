// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

/**
 * Direction of fetch.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public enum FetchDirection {

    /**
     * Fetch forward (to end of cursor).
     */
    FORWARD,
    /**
     * Fetch reverse (to start of cursor).
     */
    REVERSE,
    /**
     * Fetch in place (doesn't change position).
     */
    IN_PLACE,
    /**
     * Fetch direction unknown (e.g. fetch FIRST or LAST).
     */
    UNKNOWN

}
