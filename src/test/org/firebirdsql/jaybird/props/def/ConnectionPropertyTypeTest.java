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
package org.firebirdsql.jaybird.props.def;

import org.firebirdsql.jaybird.props.DpbType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ConnectionPropertyTypeTest {

    @ParameterizedTest
    @CsvSource({
            "STRING , STRING",
            "INT    , INT",
            "BOOLEAN, SINGLE",
            "TRANSACTION_ISOLATION, NONE"
    })
    void defaultParameterType(ConnectionPropertyType connectionPropertyType, DpbType expectedDefaultParameterType) {
        assertThat(connectionPropertyType.getDefaultParameterType()).isEqualTo(expectedDefaultParameterType);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = "abc")
    void testSTRING_toType_String(String value) {
        assertThat(ConnectionPropertyType.STRING.toType(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({
            "       ,",
            "1      , 1",
            "5723892, 5723892"
    })
    void testSTRING_toType_Integer(Integer value, String expectedValue) {
        assertThat(ConnectionPropertyType.STRING.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "     ,",
            "true , true",
            "false, false"
    })
    void testSTRING_toType_Boolean(Boolean value, String expectedValue) {
        assertThat(ConnectionPropertyType.STRING.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    @ValueSource(strings = "abc")
    void testSTRING_asString(String value) {
        assertThat(ConnectionPropertyType.STRING.asString(value)).isSameAs(value);
    }

    @ParameterizedTest
    @CsvSource({
            "       ,",
            "1      ,1",
            "5723892, 5723892"
    })
    void testSTRING_asInteger(String value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.STRING.asInteger(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = "Not an integer")
    void testSTRING_asInteger_nonConvertibleValues(String value) {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> ConnectionPropertyType.STRING.asInteger(value));
    }

    @ParameterizedTest
    @CsvSource({
            "             ,",
            "true         , true",
            "false        , false",
            "not a boolean, false",
            // For backwards compatibility, empty string means true
            "''           , true"
    })
    void testSTRING_asBoolean(String value, Boolean expectedValue) {
        assertThat(ConnectionPropertyType.STRING.asBoolean(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "       ,",
            "1      , 1",
            "5723892, 5723892"
    })
    void testINT_toType_String(String value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.INT.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = "not an integer")
    void testINT_toType_String_inconvertibleValues(String value) {
        assertThatExceptionOfType(NumberFormatException.class).isThrownBy(() -> ConnectionPropertyType.INT.toType(value));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 1, 5473924 })
    void testINT_toType_Integer(Integer value) {
        assertThat(ConnectionPropertyType.INT.toType(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({
            "     ,",
            "true , 1",
            "false, 0"
    })
    void testINT_toType_Boolean(Boolean value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.INT.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "     ,",
            "1    , 1",
            "58293, 58293"
    })
    void testINT_asString(Integer value, String expectedValue) {
        assertThat(ConnectionPropertyType.INT.asString(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { 1, 58293 })
    void testINT_asInteger(Integer value) {
        assertThat(ConnectionPropertyType.INT.asInteger(value)).isSameAs(value);
    }

    @ParameterizedTest
    @CsvSource({
            " ,",
            "1, true",
            "0, false"
    })
    void testINT_asBoolean(Integer value, Boolean expectedValue) {
        assertThat(ConnectionPropertyType.INT.asBoolean(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3 })
    void testINT_asBoolean_inconvertibleValues(Integer value) {
        assertThatIllegalArgumentException().isThrownBy(() -> ConnectionPropertyType.INT.asBoolean(value));
    }

    @ParameterizedTest
    @CsvSource({
            ", ",
            "true         , true",
            "false        , false",
            // For backwards compatibility, empty string means true
            "''           , true",
            "not a boolean, false"
    })
    void testBOOLEAN_toType_String(String value, Boolean expectedValue) {
        assertThat(ConnectionPropertyType.BOOLEAN.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            " ,",
            "0, false",
            "1, true"
    })
    void testBOOLEAN_toType_Integer(Integer value, Boolean expectedValue) {
        assertThat(ConnectionPropertyType.BOOLEAN.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3 })
    void testBOOLEAN_toType_Integer_inconvertibleValues(Integer value) {
        assertThatIllegalArgumentException().isThrownBy(() -> ConnectionPropertyType.BOOLEAN.toType(value));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = { true, false })
    void testBOOLEAN_toType_Boolean(Boolean value) {
        assertThat(ConnectionPropertyType.BOOLEAN.toType(value)).isEqualTo(value);
    }

    @ParameterizedTest
    @CsvSource({
            "     ,",
            "true , true",
            "false, false"
    })
    void testBOOLEAN_asString(Boolean value, String expectedValue) {
        assertThat(ConnectionPropertyType.BOOLEAN.asString(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "     ,",
            "true , 1",
            "false, 0"
    })
    void testBOOLEAN_asInteger(Boolean value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.BOOLEAN.asInteger(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = { true, false })
    void testBOOLEAN_asBoolean(Boolean value) {
        assertThat(ConnectionPropertyType.BOOLEAN.asBoolean(value)).isSameAs(value);
    }

    @ParameterizedTest
    @CsvSource({
            "                            ,",
            "TRANSACTION_NONE            , 0",
            "0                           , 0",
            "TRANSACTION_READ_UNCOMMITTED, 1",
            "1                           , 1",
            "TRANSACTION_READ_COMMITTED  , 2",
            "2                           , 2",
            "TRANSACTION_REPEATABLE_READ , 4",
            "4                           , 4",
            "TRANSACTION_SERIALIZABLE    , 8",
            "8                           , 8"
    })
    void testTRANSACTION_ISOLATION_toType_string(String value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.toType(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 1",
            "2, 2",
            "4, 4",
            "8, 8",
            // unsupported isolation level value, mapped nonetheless
            "3, 3"
    })
    void testTRANSACTION_ISOLATION_toType_Integer(Integer value, Integer expectedValue) {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.toType(value)).isEqualTo(expectedValue);
    }

    @Test
    void testTRANSACTION_ISOLATION_toType_Boolean_nullAllowed() {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.toType((Boolean) null)).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testTRANSACTION_ISOLATION_toType_Boolean_notAllowed(boolean value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ConnectionPropertyType.TRANSACTION_ISOLATION.toType(value));
    }

    @ParameterizedTest
    @CsvSource({
            " ,",
            "0, TRANSACTION_NONE",
            "1, TRANSACTION_READ_UNCOMMITTED",
            "2, TRANSACTION_READ_COMMITTED",
            "4, TRANSACTION_REPEATABLE_READ",
            "8, TRANSACTION_SERIALIZABLE",
            // unsupported isolation level, mapped nonetheless
            "3, 3"
    })
    void testTRANSACTION_ISOLATION_asString(Integer value, String expectedValue) {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.asString(value)).isEqualTo(expectedValue);
    }

    @ParameterizedTest
    @ValueSource(ints = { Connection.TRANSACTION_NONE, Connection.TRANSACTION_READ_UNCOMMITTED,
            Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ,
            Connection.TRANSACTION_SERIALIZABLE, 3 })
    void testTRANSACTION_ISOLATION_asInteger(Integer value) {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.asInteger(value)).isEqualTo(value);
    }
    
    @Test
    void testTRANSACTION_ISOLATION_asBoolean_nullAllowed() {
        assertThat(ConnectionPropertyType.TRANSACTION_ISOLATION.asBoolean(null)).isNull();
    }
    
    @Test
    void testTRANSACTION_ISOLATION_asBoolean_nonNullNotAllowed() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ConnectionPropertyType.TRANSACTION_ISOLATION.asBoolean(1));
    }
}