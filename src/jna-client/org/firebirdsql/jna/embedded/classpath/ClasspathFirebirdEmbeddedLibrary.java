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
package org.firebirdsql.jna.embedded.classpath;

import org.firebirdsql.jna.embedded.spi.DisposableFirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLibrary;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLoadingException;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;

import java.nio.file.Path;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link FirebirdEmbeddedLibrary} that provides Firebird Embedded from a classpath resource.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public final class ClasspathFirebirdEmbeddedLibrary implements DisposableFirebirdEmbeddedLibrary {

    private final Path entryPointPath;
    private final Path rootPath;
    private final String version;

    private ClasspathFirebirdEmbeddedLibrary(Path entryPointPath, Path rootPath, String version) {
        this.entryPointPath = requireNonNull(entryPointPath, "entryPointPath");
        this.rootPath = requireNonNull(rootPath, "rootPath");
        this.version = requireNonNull(version, "version");
    }

    /**
     * Loads a Firebird Embedded library from the classpath and installs it into a temporary file location.
     *
     * @param firebirdEmbeddedProvider
     *         Firebird Embedded provider
     * @param classpathFirebirdEmbeddedResource
     *         Information to identify the classpath resources to install
     * @return Classpath Firebird Embedded library
     * @throws FirebirdEmbeddedLoadingException
     *         For errors loading the embedded library to a temporary folder
     */
    public static ClasspathFirebirdEmbeddedLibrary load(FirebirdEmbeddedProvider firebirdEmbeddedProvider,
            ClasspathFirebirdEmbeddedResource classpathFirebirdEmbeddedResource)
            throws FirebirdEmbeddedLoadingException {
        ClasspathFirebirdEmbeddedLoader loader =
                new ClasspathFirebirdEmbeddedLoader(firebirdEmbeddedProvider, classpathFirebirdEmbeddedResource);
        loader.install();
        return new ClasspathFirebirdEmbeddedLibrary(loader.getLibraryEntryPoint(), loader.getTargetDirectory(),
                firebirdEmbeddedProvider.getVersion());
    }

    @Override
    public Path getEntryPointPath() {
        return entryPointPath;
    }

    /**
     * @return root path of the Firebird Embedded installation
     */
    Path getRootPath() {
        return rootPath;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void dispose() {
        ClasspathFirebirdEmbeddedLoader.dispose(this);
    }
}
