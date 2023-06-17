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

import org.firebirdsql.jdbc.ClientInfoProvider.ClientInfoProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.message;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link ClientInfoProperty}.
 */
class ClientInfoPropertyTest {

    @Test
    void constructor_nameNull_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new ClientInfoProperty(null, "USER_SESSION"));
    }

    @Test
    void constructor_contextNull_throwsNPE() {
        assertThrows(NullPointerException.class, () -> new ClientInfoProperty("propertyName", null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "  ", "DDL_TRIGGER", "DOES_NOT_EXIST" })
    void constructor_unknownContext_throwsIllegalArgument(String context) {
        assertThrows(IllegalArgumentException.class, () -> new ClientInfoProperty("propertyName", context));
    }

    @ParameterizedTest
    @ValueSource(strings = { "property@USER_SESSION", "property@USER_TRANSACTION", "property@SYSTEM" })
    void constructor_contextInName_throwsIllegalArgument(String name) {
        var exception = assertThrows(IllegalArgumentException.class, () ->
                new ClientInfoProperty(name, "USER_SESSION"));
        // not checking list of contexts as it doesn't have a stable order
        assertThat(exception, message(startsWith("Name '%s' should not end in @ followed by [".formatted(name))));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            INPUT_NAME,                    EXPECTED_NAME,               EXPECTED_CONTEXT, EXPECTED_TO_STRING
            ApplicationName,               ApplicationName,             USER_SESSION,     ApplicationName
            ApplicationName@USER_SESSION,  ApplicationName,             USER_SESSION,     ApplicationName
            propertyName@SYSTEM,           propertyName,                SYSTEM,           propertyName@SYSTEM
            propertyName@USER_TRANSACTION, propertyName,                USER_TRANSACTION, propertyName@USER_TRANSACTION
            # Special cases: unknown context handled as property in USER_SESSION
            propertyName@DDL_TRIGGER,      propertyName@DDL_TRIGGER,    USER_SESSION,     propertyName@DDL_TRIGGER
            propertyName@DOES_NOT_EXIST,   propertyName@DOES_NOT_EXIST, USER_SESSION,     propertyName@DOES_NOT_EXIST
            # Special cases: property name ends in @
            propertyName@,                 propertyName@,               USER_SESSION,     propertyName@
            # Special cases: blank names/prefixes (valid in Firebird, so also allowed here)
            '',                            '',                          USER_SESSION,     ''
            ' ',                           ' ',                         USER_SESSION,     ' '
            @USER_SESSION,                 '',                          USER_SESSION,     ''
            ' @USER_SESSION',              ' ',                         USER_SESSION,     ' '
            @USER_TRANSACTION,             '',                          USER_TRANSACTION, @USER_TRANSACTION
            ' @USER_TRANSACTION',          ' ',                         USER_TRANSACTION, ' @USER_TRANSACTION'
            @SYSTEM,                       '',                          SYSTEM,           @SYSTEM
            ' @SYSTEM',                    ' ',                         SYSTEM,           ' @SYSTEM'
            """)
    void parse_happyPath(String inputName, String expectedName, String expectedContext, String expectedToString) {
        var prop = ClientInfoProperty.parse(inputName);

        assertEquals(expectedName, prop.name(), "name");
        assertEquals(expectedContext, prop.context(), "context");
        assertEquals(expectedToString, prop.toString(), "toString");
    }

    @Test
    void parse_nameNull_throwsNPE() {
        assertThrows(NullPointerException.class, () -> ClientInfoProperty.parse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "property@USER_SESSION@USER_SESSION", "property@USER_TRANSACTION@USER_SESSION",
            "property@SYSTEM@USER_SESSION" })
    void parse_repeatedContextInName_throwsIllegalArgument(String name) {
        var exception = assertThrows(IllegalArgumentException.class, () ->
                ClientInfoProperty.parse(name));
        // not checking list of contexts as it doesn't have a stable order
        assertThat(exception, message(
                startsWith("Name '%s' should not end in multiple occurrences of @ followed by [".formatted(name))));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, quoteCharacter = '`', textBlock = """
            PROPERTY,                  STRATEGY,  EXPECTED_OUTPUT
            ApplicationName,           DIALECT_3, `RDB$GET_CONTEXT('USER_SESSION','ApplicationName')`
            ApplicationName,           DIALECT_1, `RDB$GET_CONTEXT("USER_SESSION","ApplicationName")`
            property@USER_SESSION,     DIALECT_3, `RDB$GET_CONTEXT('USER_SESSION','property')`
            property@USER_TRANSACTION, DIALECT_3, `RDB$GET_CONTEXT('USER_TRANSACTION','property')`
            property@SYSTEM,           DIALECT_3, `RDB$GET_CONTEXT('SYSTEM','property')`
            property@DOES_NOT_EXIST,   DIALECT_3, `RDB$GET_CONTEXT('USER_SESSION','property@DOES_NOT_EXIST')`
            with"quote,                DIALECT_3, `RDB$GET_CONTEXT('USER_SESSION','with"quote')`
            with"quote,                DIALECT_1, `RDB$GET_CONTEXT("USER_SESSION","with""quote")`
            with'quote,                DIALECT_3, `RDB$GET_CONTEXT('USER_SESSION','with''quote')`
            with'quote,                DIALECT_1, `RDB$GET_CONTEXT("USER_SESSION","with'quote")`
            """)
    void appendAsGetContext(ClientInfoProperty property, QuoteStrategy quoteStrategy, String expectedOutput) {
        var sb = new StringBuilder();
        StringBuilder result = property.appendAsGetContext(sb, quoteStrategy);

        assertSame(sb, result, "Unexpected StringBuilder returned");
        assertEquals(expectedOutput, sb.toString());
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, quoteCharacter = '`', textBlock = """
            PROPERTY,                  VALUE, STRATEGY,  EXPECTED_OUTPUT
            ApplicationName,           ab,    DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','ApplicationName','ab')`
            ApplicationName,           ab,    DIALECT_1, `RDB$SET_CONTEXT("USER_SESSION","ApplicationName","ab")`
            ApplicationName,           ,      DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','ApplicationName',NULL)`
            ApplicationName,           ,      DIALECT_1, `RDB$SET_CONTEXT("USER_SESSION","ApplicationName",NULL)`
            ApplicationName,           ``,    DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','ApplicationName','')`
            ApplicationName,           ``,    DIALECT_1, `RDB$SET_CONTEXT("USER_SESSION","ApplicationName","")`
            property@USER_SESSION,     ab,    DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','property','ab')`
            property@USER_TRANSACTION, ab,    DIALECT_3, `RDB$SET_CONTEXT('USER_TRANSACTION','property','ab')`
            property@SYSTEM,           ab,    DIALECT_3, `RDB$SET_CONTEXT('SYSTEM','property','ab')`
            property@DOES_NOT_EXIST,   ab,    DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','property@DOES_NOT_EXIST','ab')`
            with"quote,                a"b,   DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','with"quote','a"b')`
            with"quote,                a"b,   DIALECT_1, `RDB$SET_CONTEXT("USER_SESSION","with""quote","a""b")`
            with'quote,                a'b,   DIALECT_3, `RDB$SET_CONTEXT('USER_SESSION','with''quote','a''b')`
            with'quote,                a'b,   DIALECT_1, `RDB$SET_CONTEXT("USER_SESSION","with'quote","a'b")`
            """)
    void appendAsSetContext(ClientInfoProperty property, String value, QuoteStrategy quoteStrategy,
            String expectedOutput) {
        var sb = new StringBuilder();
        StringBuilder result = property.appendAsSetContext(sb, quoteStrategy, value);

        assertSame(sb, result, "Unexpected StringBuilder returned");
        assertEquals(expectedOutput, sb.toString());
    }

}
