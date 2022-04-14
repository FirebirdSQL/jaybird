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
package org.firebirdsql.jdbc;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FBConnectionPropertiesTest {

    @ParameterizedTest
    @CsvSource({
            // documented key-value format
            "'a=b',           a,   b",
            "'key=value',     key, value",
            // documented key-only format
            "a,               a,   ''",
            // extra whitespace
            "'a = b',         a,   b",
            // undocumented key-only formats
            "a=,              a,   ''",
            "' a =     ',     a,   ''",
            "'a = b    ',     a,   b",
            // esoteric results (had different effect in Jaybird 4 and earlier)
            "'a:b',           a:b, ''",
            "'a b',           a b, ''",
            "'a:',            a:,  ''",
            "'a:=b',          a:,  b",
            "'a==b',          a, =b"
    })
    void testSetNonStandardProperty(String input, String expectedPropertyName, String expectedPropertyValue) {
        FBConnectionProperties props = new FBConnectionProperties();
        props.setNonStandardProperty(input);

        Map<ConnectionProperty, Object> propsMap = props.connectionPropertyValues();
        assertThat(propsMap).containsEntry(ConnectionProperty.unknown(expectedPropertyName), expectedPropertyValue);
    }

    @Test
    void testSetNonStandardProperty_invalidFormats() {
        FBConnectionProperties props = new FBConnectionProperties();

        assertThatIllegalArgumentException()
                .isThrownBy(() -> props.setNonStandardProperty("=a=b"));
    }

}