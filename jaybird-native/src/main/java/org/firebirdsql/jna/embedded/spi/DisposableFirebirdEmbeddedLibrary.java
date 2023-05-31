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
package org.firebirdsql.jna.embedded.spi;

/**
 * Firebird Embedded library that needs to be disposed on exit.
 * <p>
 * This can be used for additional cleanup on exit.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface DisposableFirebirdEmbeddedLibrary extends FirebirdEmbeddedLibrary {

    /**
     * Will be called by the native resource tracker (if enabled) on exit of the JVM.
     * <p>
     * Implementations that need to delete files from the file system should take into account the possibility that
     * files cannot be deleted on exit, and should try to apply a strategy to cleanup old files on the next run.
     * </p>
     */
    void dispose();

}
