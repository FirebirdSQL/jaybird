// SPDX-FileCopyrightText: Copyright 2020-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class FbClientResourceTest {

    @RegisterExtension
    static final GdsTypeExtension testType = GdsTypeExtension.supportsNativeOnly();

    /**
     * Rationale: due to code changes, the dispose implementation didn't work but instead logged an exception
     */
    @Test
    void testDisposeImplementation() throws Exception {
        AbstractNativeDatabaseFactory factory = (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
        // Call to ensure resource is initialized
        factory.getClientLibrary();
        Field resourceField = AbstractNativeDatabaseFactory.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        FbClientResource resource = (FbClientResource) requireNonNull(resourceField.get(factory),
                "Test setup incomplete, resource not initialized");
        Method disposeImpl = FbClientResource.class.getDeclaredMethod("disposeImpl");
        disposeImpl.setAccessible(true);

        assertDoesNotThrow(() -> disposeImpl.invoke(resource));
    }
}