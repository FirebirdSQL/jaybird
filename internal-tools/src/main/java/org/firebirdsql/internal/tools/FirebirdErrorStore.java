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
