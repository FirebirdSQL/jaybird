// SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
    void withTemporarySystemProperty_propertyDidNotExist(String testValue) {
        try (var ignored = SystemPropertyHelper.withTemporarySystemProperty(TEST_PROPERTY_NAME, testValue)) {
            assertEquals(testValue, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value in try");
        }
        assertNull(System.getProperty(TEST_PROPERTY_NAME), "Unexpected value after try");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "Test value", INITIAL_VALUE })
    void withTemporarySystemProperty_propertyExisted(String testValue) {
        System.setProperty(TEST_PROPERTY_NAME, INITIAL_VALUE);
        try (var ignored = SystemPropertyHelper.withTemporarySystemProperty(TEST_PROPERTY_NAME, testValue)) {
            assertEquals(testValue, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value in try");
        }
        assertEquals(INITIAL_VALUE, System.getProperty(TEST_PROPERTY_NAME), "Unexpected value after try");
    }

}