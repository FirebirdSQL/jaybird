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
package org.firebirdsql.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Mark Rotteveel
 */
class SystemPropertyHelperTest {

    private static final String TEST_PROPERTY_NAME = "org.firebirdsql.common.SystemPropertyHelperTest";
    private static final String INITIAL_VALUE = "Initial value";

    @BeforeEach
    void removeTestProperty() {
        System.clearProperty(TEST_PROPERTY_NAME);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "Test value", INITIAL_VALUE })
    void withTemporarySystemProperty_propertyDidNotExist(String testValue) throws Exception {
        try (var ignored = SystemPropertyHelper.withTemporarySystemProperty(TEST_PROPERTY_NAME, testValue)) {
            assertEquals(testValue, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value in try");
        }
        assertNull(System.getProperty(TEST_PROPERTY_NAME), "Unexpected value after try");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "Test value", INITIAL_VALUE })
    void withTemporarySystemProperty_propertyExisted(String testValue) throws Exception{
        System.setProperty(TEST_PROPERTY_NAME, INITIAL_VALUE);
        try (var ignored = SystemPropertyHelper.withTemporarySystemProperty(TEST_PROPERTY_NAME, testValue)) {
            assertEquals(testValue, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value in try");
        }
        assertEquals(INITIAL_VALUE, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value after try");
    }

}