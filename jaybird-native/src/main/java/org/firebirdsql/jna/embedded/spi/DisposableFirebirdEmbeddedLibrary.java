// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
