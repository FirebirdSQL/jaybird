// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2015-2017 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds;

/**
 * A callback handler interface for event handling.
 */
public interface EventHandler {

    /**
     * Called when a database event occurs.
     * <p>
     * Implementations should take care to only perform short processing on the current thread. If longer or
     * complicated processing is necessary, please offload it to another thread or executor.
     * </p>
     *
     * @param eventHandle
     *         The event handle
     */
    void eventOccurred(EventHandle eventHandle);

}
