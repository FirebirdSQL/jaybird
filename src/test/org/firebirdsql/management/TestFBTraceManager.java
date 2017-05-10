/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.management;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link FBTraceManager}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBTraceManager {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    //@formatter:off
    private static final String TEST_FILE_CONTENT =
            "Test file content\n"
            + "line 2\n"
            + "line 3\n";
    //@formatter:on

    @SuppressWarnings("deprecation")
    @Test
    public void loadConfigurationFromFile() throws Exception {
        temporaryFolder.create();
        File file = temporaryFolder.newFile();
        Path filePath = file.toPath();
        Files.write(filePath, TEST_FILE_CONTENT.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

        String fileContent = new FBTraceManager().loadConfigurationFromFile(file.getAbsolutePath());

        assertEquals("Unexpected file content", TEST_FILE_CONTENT, fileContent);
    }
}
