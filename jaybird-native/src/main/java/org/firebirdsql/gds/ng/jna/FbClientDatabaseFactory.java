// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.firebirdsql.jna.fbclient.WinFbClientLibrary;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for establishing connection using the
 * Firebird native client library.
 * <p>
 * A separate factory is used for embedded: {@link FbEmbeddedDatabaseFactory}.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class FbClientDatabaseFactory extends AbstractNativeDatabaseFactory {

    private static final FbClientDatabaseFactory INSTANCE = new FbClientDatabaseFactory();
    static final String LIBRARY_NAME_FBCLIENT = "fbclient";

    private FbClientDatabaseFactory() {
        // only through getInstance()
    }

    public static FbClientDatabaseFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected FbClientLibrary createClientLibrary() {
        try {
            if (Platform.isWindows()) {
                return Native.load(LIBRARY_NAME_FBCLIENT, WinFbClientLibrary.class);
            } else {
                return Native.load(LIBRARY_NAME_FBCLIENT, FbClientLibrary.class);
            }
        } catch (RuntimeException | UnsatisfiedLinkError e) {
            throw new NativeLibraryLoadException("Could not load fbclient", e);
        }
    }

    @Override
    protected Collection<String> defaultLibraryNames() {
        return List.of(LIBRARY_NAME_FBCLIENT);
    }

}
