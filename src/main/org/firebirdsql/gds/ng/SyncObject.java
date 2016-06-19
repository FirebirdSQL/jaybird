/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object for synchronization that has an id that might be helpful during debugging.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class SyncObject {

    private static final AtomicInteger counter = new AtomicInteger();

    private final int id;

    public SyncObject() {
        id = counter.getAndIncrement();
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "SyncObject[" + id + "]";
    }
}
