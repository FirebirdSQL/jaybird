// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Identifier}.
 */
class IdentifierTest {

    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "  " })
    void nameNullEmptyOrBlank_notAllowed(@Nullable String name) {
        assertThrows(IllegalArgumentException.class, () -> new Identifier(name));
    }

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            name,        expectedDialect3
            SIMPLE_NAME, "SIMPLE_NAME"
            lower_case,  "lower_case"
            """)
    void identifier(String name, String expectedDialect3) {
        var identifier = new Identifier(name);

        assertEquals(name, identifier.name(), "name()");
        assertEquals(expectedDialect3, identifier.toString(), "toString()");
        assertEquals(expectedDialect3, identifier.toString(QuoteStrategy.DIALECT_3), "toString(DIALECT_3)");
        assertEquals(name, identifier.toString(QuoteStrategy.DIALECT_1), "toString(DIALECT_1)");
        assertEquals(expectedDialect3, identifier.append(new StringBuilder(), QuoteStrategy.DIALECT_3).toString(),
                "append(..., DIALECT_3)");
        assertEquals(name, identifier.append(new StringBuilder(), QuoteStrategy.DIALECT_1).toString(),
                "append(..., DIALECT_1)");
        assertEquals(1, identifier.size(), "size");
    }

    @ParameterizedTest
    @ValueSource(strings = { " SPACE_PREFIX", "SPACE_SUFFIX ", " SPACE_BOTH " })
    void nameIsTrimmed(String name) {
        var identifier = new Identifier(name);

        assertEquals(name.trim(), identifier.name());
    }

    @SuppressWarnings({ "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" })
    @ParameterizedTest
    @ValueSource(strings = { "SIMPLE_NAME", "lower_case", "Example3" })
    void equalsAndHashCode_betweenIdentifierAndIdentifierChain(String name) {
        var identifier = new Identifier(name);
        var chain = new IdentifierChain(List.of(identifier));

        assertTrue(identifier.equals(chain), "identifier.equals(chain)");
        assertTrue(chain.equals(identifier), "chain.equals(identifier)");
        assertEquals(identifier.hashCode(), chain.hashCode(), "hashCode");
    }

    @SuppressWarnings({ "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" })
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            name1,   name2
            EXAMPLE, example
            Example, exAmple
            """)
    void notEquals(String name1, String name2) {
        var identifier1 = new Identifier(name1);
        var identifier2 = new Identifier(name2);

        assertFalse(identifier1.equals(identifier2), "equals");
        assertFalse(identifier1.equals(new IdentifierChain(List.of(identifier2))), "equals with chain");
        assertFalse(identifier1.equals(new IdentifierChain(List.of(identifier1, identifier2))),
                "equals with chain with same prefix");
    }

    @Test
    void toList() {
        var identifier = new Identifier("EXAMPLE");

        assertEquals(List.of(identifier), identifier.toList(), "toList");
    }

    @Test
    void at() {
        var identifier = new Identifier("EXAMPLE");

        assertEquals(identifier, identifier.at(0), "at");
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 1, 10})
    void at_outOfRange(int index) {
        var identifier = new Identifier("EXAMPLE");

        assertThrows(IndexOutOfBoundsException.class, () -> identifier.at(index));
    }

    @Test
    void first() {
        var identifier = new Identifier("EXAMPLE");

        assertEquals(identifier, identifier.first(), "first");
    }

    @Test
    void last() {
        var identifier = new Identifier("EXAMPLE");

        assertEquals(identifier, identifier.last(), "last");
    }

    @Test
    void stream() {
        var identifier = new Identifier("EXAMPLE");

        assertEquals(List.of(identifier), identifier.stream().toList(), "stream");
    }

    @Test
    void resolve_twoIdentifiers() {
        var identifier1 = new Identifier("EXAMPLE_1");
        var identifier2 = new Identifier("EXAMPLE_2");

        assertEquals(new IdentifierChain(List.of(identifier1, identifier2)), identifier1.resolve(identifier2),
                "resolve");
    }

    @Test
    void resolve_identifierAndChain() {
        var identifier1 = new Identifier("EXAMPLE_1");
        var identifier2 = new Identifier("EXAMPLE_2");
        var identifier3 = new Identifier("EXAMPLE_3");
        var chain = new IdentifierChain(List.of(identifier2, identifier3));

        assertEquals(new IdentifierChain(List.of(identifier1, identifier2, identifier3)), identifier1.resolve(chain),
                "resolve");
    }

    @ParameterizedTest
    @EnumSource(Identifier.Scope.class)
    void toString_includesScopeIdentifierSCHEMAorPACKAGE(Identifier.Scope scope) {
        String expected = "\"EXAMPLE_1\"" + (scope != Identifier.Scope.UNKNOWN ? "%" + scope : "");
        var identifier = new Identifier("EXAMPLE_1", scope);

        assertEquals(expected, identifier.toString());
    }

}