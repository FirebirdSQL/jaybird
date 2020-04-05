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
package org.firebirdsql.jna.embedded;

import com.sun.jna.Platform;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Locates and installs a Firebird Embedded library from the class path to a temporary location.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public final class FirebirdEmbeddedLoader {

    private static final Logger log = LoggerFactory.getLogger(FirebirdEmbeddedLoader.class);
    private static final int LOAD_BUFFER_SIZE = 8192;
    private final FirebirdEmbeddedProvider firebirdEmbeddedProvider;
    private final Path targetDirectory;
    private final Path libraryEntryPoint;

    // TODO Implement more thorough temporary file management and cleanup, see com.sun.jna.Native

    private FirebirdEmbeddedLoader(FirebirdEmbeddedProvider firebirdEmbeddedProvider)
            throws FirebirdEmbeddedLoadingException {
        this.firebirdEmbeddedProvider = firebirdEmbeddedProvider;
        try {
            targetDirectory = Files.createTempDirectory("firebird-embedded");
            // Make sure we delete even if subsequent check fails
            targetDirectory.toFile().deleteOnExit();
            libraryEntryPoint = getValidatedLibraryEntryPoint();
        } catch (IOException e) {
            throw new FirebirdEmbeddedLoadingException(
                    getProviderName() + ": Could not extract Firebird Embedded to local file system: " + e, e);
        }
    }

    /**
     * Tries to find and install a Firebird Embedded library for the current platform on the classpath.
     * <p>
     * The first instance found and successfully installed in a temporary directory will be returned.
     * </p>
     *
     * @return Firebird Embedded instance information, or empty if no instance was found or installing failed
     */
    public static Optional<FirebirdEmbeddedLibrary> getFirebirdEmbeddedFromClasspath() {
        try {
            return Optional.ofNullable(findFirebirdEmbedded());
        } catch (FirebirdEmbeddedLoadingException e) {
            log.error("Could not load Firebird Embedded from the class path", e);
            return Optional.empty();
        }
    }

    /**
     * Finds a {@link FirebirdEmbeddedLibrary} on the class path for the current platform.
     *
     * @return the first embedded instance matching the current platform that installed without errors
     * @throws FirebirdEmbeddedLoadingException
     *         For errors with {@link ServiceLoader}, errors installing individual instances are not thrown
     */
    @SuppressWarnings("WhileLoopReplaceableByForEach")
    private static FirebirdEmbeddedLibrary findFirebirdEmbedded() throws FirebirdEmbeddedLoadingException {
        try {
            ServiceLoader<FirebirdEmbeddedProvider> firebirdEmbeddedProviders =
                    ServiceLoader.load(FirebirdEmbeddedProvider.class);
            Iterator<FirebirdEmbeddedProvider> iterator = firebirdEmbeddedProviders.iterator();
            // We can't use foreach here, because the services are lazily loaded, which might trigger a ServiceConfigurationError
            while (iterator.hasNext()) {
                try {
                    FirebirdEmbeddedProvider provider = iterator.next();
                    if (Platform.RESOURCE_PREFIX.equals(provider.getPlatform())) {
                        return new FirebirdEmbeddedLoader(provider).install();
                    }
                } catch (Exception | ServiceConfigurationError e) {
                    String message = "Can't load FirebirdEmbeddedProvider (skipping)";
                    log.error(message + ": " + e + "; see debug level for stacktrace");
                    log.debug(message, e);
                }
            }
            return null;
        } catch (ServiceConfigurationError | RuntimeException e) {
            throw new FirebirdEmbeddedLoadingException("Unable to install Firebird Embedded using ServiceLoader", e);
        }
    }

    /**
     * Installs the Firebird Embedded library to a temporary folder.
     * <p>
     * The temporary files and folder created by this methods are marked for deletion on exit. Deletion will not always
     * work on Windows, as some of the DLLs are not released by Firebird on shutdown, and as a result Java cannot
     * delete them on exit as the attempt to delete happens before the DLLs are released and the file handle is closed.
     * </p>
     *
     * @return Firebird embedded instance information (location on the local filesystem)
     * @throws FirebirdEmbeddedLoadingException
     *         For errors creating directories or files
     */
    private FirebirdEmbeddedLibrary install() throws FirebirdEmbeddedLoadingException {
        try {
            log.info("Extracting Firebird Embedded " + firebirdEmbeddedProvider.getVersion() + " to " + targetDirectory);
            for (String resourceName : firebirdEmbeddedProvider.getResourceList()) {
                copyResourceToTargetDirectory(resourceName);
            }
            return new FirebirdEmbeddedLibrary(libraryEntryPoint, firebirdEmbeddedProvider.getVersion());
        } catch (IOException e) {
            throw new FirebirdEmbeddedLoadingException(
                    getProviderName() + ": Could not extract Firebird Embedded to local file system: " + e, e);
        } finally {
            // Make sure the JVM cleans up the files on (normal) exit
            try (Stream<Path> tempFiles = Files.walk(targetDirectory)) {
                tempFiles
                        .map(Path::toFile)
                        .forEach(File::deleteOnExit);
            } catch (IOException e) {
                // Only log warning, and allow usage
                log.warn("Firebird Embedded files in " + targetDirectory + " could not be marked for deletion", e);
            }
        }
    }

    /**
     * Copies the resource {@code resourceName} to the target directory.
     *
     * @param resourceName
     *         relative resource name
     * @throws FirebirdEmbeddedLoadingException
     *         When the target file escapes the target directory (eg by returning an absolute path or using {@code ..},
     *         or when the resource does not exist
     * @throws IOException
     *         For exceptions creating intermediate directories or writing the resource to the target file
     */
    private void copyResourceToTargetDirectory(String resourceName)
            throws FirebirdEmbeddedLoadingException, IOException {
        Path targetFile = safeResolve(resourceName);
        Path fileParent = targetFile.getParent();
        if (!Files.isDirectory(fileParent)) {
            Files.createDirectories(fileParent);
        }

        log.debug("Saving " + resourceName + " to " + targetFile);
        try (InputStream inputStream = firebirdEmbeddedProvider.getClass().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new FirebirdEmbeddedLoadingException(
                        getProviderName() + ": File " + resourceName + " doesn't exist");
            }

            try (OutputStream outputStream = Files.newOutputStream(targetFile)) {
                byte[] buffer = new byte[LOAD_BUFFER_SIZE];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
        }
    }

    /**
     * Checks if the entry point of the library does not escape the target directory
     *
     * @return validated library entry point path (file does not necessarily exist)
     * @throws FirebirdEmbeddedLoadingException
     *         When the library entry point specified by the provider attempts to escape the target directory
     */
    private Path getValidatedLibraryEntryPoint() throws FirebirdEmbeddedLoadingException {
        return safeResolve(firebirdEmbeddedProvider.getLibraryEntryPoint());
    }

    /**
     * Resolves a relative path against the target directory, making sure the resulting path does not escape the target.
     *
     * @param relativePath
     *         Relative path to resolve
     * @return Resolved path
     * @throws FirebirdEmbeddedLoadingException
     *         When resolving {@code relativePath} against the target directory escaped the target.
     */
    private Path safeResolve(String relativePath) throws FirebirdEmbeddedLoadingException {
        Path targetFile = targetDirectory.resolve(relativePath).toAbsolutePath();
        if (targetFile.startsWith(targetDirectory)) {
            return targetFile;
        }
        throw new FirebirdEmbeddedLoadingException(
                getProviderName() + ": File " + relativePath + " escapes the target directory");
    }

    private String getProviderName() {
        return firebirdEmbeddedProvider.getClass().getName();
    }

}
