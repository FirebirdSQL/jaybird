// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.internal.tools;

import java.io.IOException;

/**
 * Interface for storing Firebird error information.
 *
 * @author Mark Rotteveel
 * @since 6
 */
interface FirebirdErrorStore {

    void addFirebirdError(FirebirdError firebirdError);

    /**
     * Resets this Firebird error store, clearing currently stored messages.
     */
    void reset();

    /**
     * Saves the messages to disk.
     *
     * @throws IOException
     *         For failures to write the files.
     */
    void save() throws IOException;
    
}
