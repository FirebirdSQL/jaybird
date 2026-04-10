// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.common;

import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDirFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * JUnit {@link TempDirFactory} that by default creates in the mapped database directory, or otherwise falls back
 * to the default temporary directory.
 */
@NullMarked
public class MappedTempDirFactory implements TempDirFactory {

    @Override
    public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
            throws Exception {
        Optional<Path> optMappedDatabaseDirectory = FBTestProperties.getMappedDatabaseDirectory();
        if (optMappedDatabaseDirectory.isPresent()) {
            return Files.createTempDirectory(optMappedDatabaseDirectory.get(), "junit-");
        }
        return Files.createTempDirectory("junit-");
    }
    
}
