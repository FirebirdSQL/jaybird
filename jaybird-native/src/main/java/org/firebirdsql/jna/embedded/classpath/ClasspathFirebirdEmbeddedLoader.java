// SPDX-FileCopyrightText: Copyright 2020-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jna.embedded.classpath;

import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedLoadingException;
import org.firebirdsql.jna.embedded.spi.FirebirdEmbeddedProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Locates and installs a Firebird Embedded library from the class path to a temporary location.
 *
 * @author Mark Rotteveel
 * @since 5
 */
final class ClasspathFirebirdEmbeddedLoader {

    private static final System.Logger log = System.getLogger(ClasspathFirebirdEmbeddedLoader.class.getName());
    private static final int LOAD_BUFFER_SIZE = 8192;
    private static final String TEMP_FOLDER_PREFIX = "firebird-embedded";
    private static final String DELETION_MARKER_SUFFIX = ".jaybird_x";
    
    private final FirebirdEmbeddedProvider firebirdEmbeddedProvider;
    private final ClasspathFirebirdEmbeddedResource classpathFirebirdEmbeddedResource;
    private final Path targetDirectory;
    private final Path libraryEntryPoint;

    /**
     * Creates a loader to install Firebird Embedded from the classpath to a temporary folder.
     *
     * @param firebirdEmbeddedProvider
     *         Firebird Embedded provider instance
     * @param classpathFirebirdEmbeddedResource
     *         Information to identify the classpath resources to install
     * @throws FirebirdEmbeddedLoadingException
     *         For errors creating the temporary folder, or if the entry point tries to escape the temporary folder
     */
    ClasspathFirebirdEmbeddedLoader(FirebirdEmbeddedProvider firebirdEmbeddedProvider,
            ClasspathFirebirdEmbeddedResource classpathFirebirdEmbeddedResource)
            throws FirebirdEmbeddedLoadingException {
        this.firebirdEmbeddedProvider = firebirdEmbeddedProvider;
        this.classpathFirebirdEmbeddedResource = classpathFirebirdEmbeddedResource;
        cleanupOldTemporaryFiles();
        try {
            targetDirectory = Files.createTempDirectory(TEMP_FOLDER_PREFIX);
            // Make sure we delete even if subsequent check fails
            targetDirectory.toFile().deleteOnExit();
            libraryEntryPoint = getValidatedLibraryEntryPoint();
        } catch (IOException e) {
            throw new FirebirdEmbeddedLoadingException(
                    getProviderName() + ": Could not create temporary folder for Firebird Embedded: " + e, e);
        }
    }

    Path getTargetDirectory() {
        return targetDirectory;
    }

    Path getLibraryEntryPoint() {
        return libraryEntryPoint;
    }

    /**
     * Installs the Firebird Embedded library to a temporary folder.
     * <p>
     * The temporary files and folder created by this methods are marked for deletion on exit. Deletion will not always
     * work on Windows, as some of the DLLs are not released by Firebird on shutdown, and as a result Java cannot
     * delete them on exit as the attempt to delete happens before the DLLs are released and the file handle is closed.
     * </p>
     *
     * @throws FirebirdEmbeddedLoadingException
     *         For errors creating directories or files
     */
    void install() throws FirebirdEmbeddedLoadingException {
        try {
            log.log(INFO, "Extracting Firebird Embedded {0} to {1}", firebirdEmbeddedProvider.getVersion(),
                    targetDirectory);
            for (String resourceName : classpathFirebirdEmbeddedResource.getResourceList()) {
                copyResourceToTargetDirectory(resourceName);
            }
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
                log.log(WARNING, "Firebird Embedded files in %s could not be marked for deletion"
                        .formatted(targetDirectory), e);
            }
        }
    }

    /**
     * Copies the resource {@code resourceName} to the target directory.
     *
     * @param resourceName
     *         relative resource name
     * @throws FirebirdEmbeddedLoadingException
     *         When the target file escapes the target directory (e.g. by returning an absolute path or using
     *         {@code ..}, or when the resource does not exist
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

        log.log(TRACE, "Saving {0} to {1}", resourceName,  targetFile);
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
        return safeResolve(classpathFirebirdEmbeddedResource.getLibraryEntryPoint());
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

    /**
     * Cleans up the temporary files of a classpath Firebird Embedded library.
     *
     * @param classpathFirebirdEmbeddedLibrary
     *         Classpath Firebird Embedded library to identify temporary folder
     */
    static void dispose(ClasspathFirebirdEmbeddedLibrary classpathFirebirdEmbeddedLibrary) {
        boolean allDeleted;
        Path pathToDelete = classpathFirebirdEmbeddedLibrary.getRootPath();
        try {
            allDeleted = deletePath(pathToDelete);
        } catch (IOException e) {
            log.log(ERROR, "Error deleting Firebird Embedded temporary files in " + pathToDelete, e);
            allDeleted = false;
        }

        if (!allDeleted) {
            log.log(INFO, "Could not fully delete {0}, creating deletion marker for cleanup on next run", pathToDelete);
            String deletionMarkerName = pathToDelete.getFileName().toString() + DELETION_MARKER_SUFFIX;
            Path deletionMarkerPath = pathToDelete.resolveSibling(deletionMarkerName);
            if (!Files.exists(deletionMarkerPath)) {
                try {
                    Files.createFile(deletionMarkerPath);
                } catch (IOException e) {
                    log.log(ERROR, "Could not create deletion marker for %s manual cleanup will be necessary"
                            .formatted(pathToDelete), e);
                }
            }
        }
    }

    /**
     * Delete the specified path, including any files inside the path if it is a directory.
     *
     * @param pathToDelete
     *         Path to delete
     * @return {@code true} if all files were deleted
     * @throws IOException
     *         if an I/O exception is thrown when accessing the starting file
     */
    private static boolean deletePath(Path pathToDelete) throws IOException {
        log.log(INFO, "Attempting to delete Firebird Embedded temporary files in {0}", pathToDelete);
        if (!Files.exists(pathToDelete)) return true;
        try (Stream<Path> filesToDelete = Files.walk(pathToDelete)) {
            return filesToDelete
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .map(File::delete)
                    .reduce(true, (a, b) -> a && b);
        }
    }

    /**
     * Finds deletion markers and (tries to) delete the associated temporary folder and the deletion marker.
     */
    private static void cleanupOldTemporaryFiles() {
        try {
            Path fileToLocateTempFolder = Files.createTempFile(TEMP_FOLDER_PREFIX, DELETION_MARKER_SUFFIX);
            Files.delete(fileToLocateTempFolder);
            Path tempFolder = fileToLocateTempFolder.getParent();
            try (Stream<Path> tempFiles = Files.list(tempFolder)) {
                tempFiles
                        .filter(Files::isRegularFile)
                        .filter(ClasspathFirebirdEmbeddedLoader::isJaybirdDeletionMarker)
                        .forEach(ClasspathFirebirdEmbeddedLoader::handleDeletionMarker);
            }
        } catch (IOException e) {
            log.log(ERROR, "Could not cleanup old Firebird Embedded temporary files", e);
        }
    }

    /**
     * Checks if a path is a Jaybird deletion marker.
     *
     * @param path
     *         Path to check
     * @return {@code true} if the path denotes a Jaybird deletion marker, {@code false} otherwise
     */
    private static boolean isJaybirdDeletionMarker(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith(DELETION_MARKER_SUFFIX)
                && fileName.startsWith(TEMP_FOLDER_PREFIX);
    }

    /**
     * Handle a deletion marker by deleting the identified temporary folder - if it exists - and the deletion marker.
     *
     * @param deletionMarkerPath
     *         Path of the deletion marker
     */
    private static void handleDeletionMarker(Path deletionMarkerPath) {
        log.log(TRACE, "Handling deletion marker {0}", deletionMarkerPath);
        String deletionMarkerName = deletionMarkerPath.getFileName().toString();
        String tempFolderName = deletionMarkerName.substring(0, deletionMarkerName.lastIndexOf(DELETION_MARKER_SUFFIX));
        Path tempFolder = deletionMarkerPath.resolveSibling(tempFolderName);
        try {
            if (deletePath(tempFolder)) {
                if (!deletionMarkerPath.toFile().delete()) {
                    log.log(TRACE, "Could not delete deletion marker {0}", deletionMarkerPath);
                }
            } else {
                log.log(TRACE, "Could not fully delete {0}", tempFolder);
            }
        } catch (IOException e) {
            log.log(ERROR, "Error deleting old Firebird Embedded temporary folder", e);
        }
    }

}
