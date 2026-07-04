// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.escape;

/**
 * Handling of call escapes by {@link FBEscapedParser}.
 *
 * @since 7
 */
public enum CallEscapeHandling {

    /**
     * Call escapes are processed and converted to {@code EXECUTE PROCEDURE}.
     */
    TO_EXECUTE_PROCEDURE,
    /**
     * Call escapes are ignored and remain part of the returned statement text.
     */
    IGNORED,

}
