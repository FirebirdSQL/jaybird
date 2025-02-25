// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

/**
 * Cursor flags.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public enum CursorFlag {

    CURSOR_TYPE_SCROLLABLE(0x1),
    ;

    private final int flagValue;

    CursorFlag(int flagValue) {
        this.flagValue = flagValue;
    }

    public int flagValue() {
        return flagValue;
    }
}
