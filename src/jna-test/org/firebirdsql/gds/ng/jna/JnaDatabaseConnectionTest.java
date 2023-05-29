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
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.NativeLibrary;
import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jna.fbclient.FbClientLibrary;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Mark Rotteveel
 * @since 3.0
 */
class JnaDatabaseConnectionTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    private final AbstractNativeDatabaseFactory factory =
            (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
    private final FbConnectionProperties connectionInfo = FBTestProperties.getDefaultFbConnectionProperties();

    @Test
    void construct_clientLibraryNull_IllegalArgument() {
        assertThrows(NullPointerException.class, () -> new JnaDatabaseConnection(null, connectionInfo));
    }

    @Test
    void getClientLibrary_returnsSuppliedLibrary() throws Exception {
        final FbClientLibrary clientLibrary = factory.getClientLibrary();
        JnaDatabaseConnection connection = new JnaDatabaseConnection(clientLibrary, connectionInfo);

        assertSame(clientLibrary, connection.getClientLibrary(), "Expected returned client library to be identical");
    }

    @SuppressWarnings("resource")
    @Test
    void identify_unconnected() throws Exception {
        JnaDatabaseConnection connection = new JnaDatabaseConnection(factory.getClientLibrary(), connectionInfo);

        FbDatabase db = connection.identify();

        assertFalse(db.isAttached(), "Expected isAttached() to return false");
        assertThat("Expected zero-valued connection handle", db.getHandle(), equalTo(0));
        assertNull(db.getServerVersion(), "Expected version string to be null");
        assertNull(db.getServerVersion(), "Expected version should be null");
    }

    // NOTE: This test only works if it is run in isolation, and requires custom (system specific) paths
    @Disabled
    @Test
    void getClientLibrary_withSpecificPath_libraryFile() {
        var libraryFile = new File("D:\\DevSoft\\Firebird\\Firebird-3.0.10.33601-0_x64\\fbclient.dll");
        File actualFile = getActualLibraryFile(libraryFile);
        assertEquals(libraryFile, actualFile);
    }

    // NOTE: This test only works if it is run in isolation, and requires custom (system specific) paths
    @Disabled
    @Test
    void getClientLibrary_withSpecificPath_libraryDirectory() {
        var libraryFile = new File("D:\\DevSoft\\Firebird\\Firebird-3.0.10.33601-0_x64\\fbclient.dll");
        var libraryDirectory = libraryFile.getParentFile();
        File actualFile = getActualLibraryFile(libraryDirectory);
        assertEquals(libraryFile, actualFile);
    }

    @SuppressWarnings("EmptyTryBlock")
    private File getActualLibraryFile(File libraryFile) {
        connectionInfo.setProperty(PropertyNames.nativeLibraryPath, libraryFile.toString());
        // Connect will fail, but should initialize the library
        try (var ignored = factory.connect(connectionInfo)) {
            //ignored
        } catch (SQLException ignored) {
        }
        InvocationHandler ih = Proxy.getInvocationHandler(factory.getClientLibrary());
        if (ih instanceof FbClientFeatureAccessHandler fah) {
            NativeLibrary nativeLibrary = fah.getNativeLibrary();
            return nativeLibrary.getFile();
        } else {
            throw new AssertionFailedError("Unexpected invocation handler");
        }
    }
}
