// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import java.lang.ref.Cleaner;

/**
 * Factory for {@link Cleaner} for use within Jaybird.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class Cleaners {

    private static final Cleaner jbCleaner = Cleaner.create();
    private static final Cleaner.Cleanable NO_OP = () -> {};

    private Cleaners() {
        // no instances
    }

    /**
     * @return {@link Cleaner} for use within Jaybird
     */
    public static Cleaner getJbCleaner() {
        return jbCleaner;
    }

    /**
     * @return A {@link java.lang.ref.Cleaner.Cleanable} which does nothing
     */
    public static Cleaner.Cleanable getNoOp() {
        return NO_OP;
    }

}
