/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.util;

import java.lang.ref.Cleaner;

/**
 * Factory for {@link Cleaner} for use within Jaybird.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
