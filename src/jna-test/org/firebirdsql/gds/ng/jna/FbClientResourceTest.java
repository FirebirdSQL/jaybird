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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.junit.ClassRule;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class FbClientResourceTest {

    @ClassRule
    public static final GdsTypeRule typeRule = GdsTypeRule.supportsNativeOnly();

    /**
     * Rationale: due to code changes, the dispose implementation didn't work but instead logged an exception
     */
    @Test
    public void testDisposeImplementation() throws Exception {
        AbstractNativeDatabaseFactory factory = (AbstractNativeDatabaseFactory) FBTestProperties.getFbDatabaseFactory();
        // Call to ensure resource is initialized
        factory.getClientLibrary();
        Field resourceField = AbstractNativeDatabaseFactory.class.getDeclaredField("resource");
        resourceField.setAccessible(true);
        FbClientResource resource = (FbClientResource) requireNonNull(resourceField.get(factory),
                "Test setup incomplete, resource not initialized");
        Method disposeImpl = FbClientResource.class.getDeclaredMethod("disposeImpl");
        disposeImpl.setAccessible(true);

        // Should not throw an exception
        disposeImpl.invoke(resource);
    }
}