// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link IdentifierChain}.
 */
class IdentifierChainTest {

    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            nameList,                expectedSize, expectedDialect3
            SIMPLE_NAME,             1,            "SIMPLE_NAME"
            SIMPLE_NAME.lower_case,  2,            "SIMPLE_NAME"."lower_case"
            ONE.TWO.THREE,           3,            "ONE"."TWO"."THREE"
            """)
    void identifier(String nameList, int expectedSize, String expectedDialect3) {
        List<Identifier> identifiers = toIdentifiers(nameList);
        var chain = new IdentifierChain(identifiers);

        assertEquals(expectedDialect3, chain.toString(), "toString()");
        assertEquals(expectedDialect3, chain.toString(QuoteStrategy.DIALECT_3), "toString(DIALECT_3)");
        assertEquals(nameList, chain.toString(QuoteStrategy.DIALECT_1), "toString(DIALECT_1)");
        assertEquals(expectedDialect3, chain.append(new StringBuilder(), QuoteStrategy.DIALECT_3).toString(),
                "append(..., DIALECT_3)");
        assertEquals(nameList, chain.append(new StringBuilder(), QuoteStrategy.DIALECT_1).toString(),
                "append(..., DIALECT_1)");
        assertEquals(expectedSize, chain.size(), "size");
        assertEquals(identifiers, chain.toList(), "toList");
    }

    @Test
    void emptyIdentifierList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IdentifierChain(emptyList()));
    }

    @SuppressWarnings({ "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" })
    @ParameterizedTest
    @ValueSource(strings = { "SIMPLE_NAME", "lower_case", "Example3" })
    void equalsAndHashCode_betweenIdentifierAndIdentifierChain(String name) {
        var identifier = new Identifier(name);
        var chain = new IdentifierChain(List.of(identifier));

        assertTrue(chain.equals(identifier), "chain.equals(identifier)");
        assertTrue(identifier.equals(chain), "identifier.equals(chain)");
        assertEquals(chain.hashCode(), identifier.hashCode(), "hashCode");
    }

    @SuppressWarnings("SimplifiableAssertion")
    @Test
    void equalsBetweenIdentifierChain() {
        var identifiers = List.of(new Identifier("NAME1"), new Identifier("NAME2"));

        assertTrue(new IdentifierChain(identifiers).equals(new IdentifierChain(identifiers)), "chain.equals(chain)");
    }

    @SuppressWarnings({ "SimplifiableAssertion", "EqualsBetweenInconvertibleTypes" })
    @ParameterizedTest
    @CsvSource(useHeadersInDisplayName = true, textBlock = """
            nameList1,       nameList2
            EXAMPLE,         example
            EXAMPLE,         EXAMPLE.example
            Example,         exAmple
            example.EXAMPLE, EXAMPLE.example
            example.EXAMPLE, EXAMPLE.example.Example
            example.EXAMPLE, EXAMPLE
            """)
    void notEquals(String nameList1, String nameList2) {
        List<Identifier> identifiers1 = toIdentifiers(nameList1);
        IdentifierChain chain1 = new IdentifierChain(identifiers1);
        List<Identifier> identifiers2 = toIdentifiers(nameList2);
        IdentifierChain chain2 = new IdentifierChain(identifiers2);

        assertFalse(chain1.equals(chain2), "equals");
        if (chain2.size() == 1) {
            assertFalse(chain1.equals(chain2.at(0)), "equals with identifier");
        }
        assertFalse(chain1.equals(new IdentifierChain(CollectionUtils.concat(identifiers1, identifiers2))),
                "equals with chain with same prefix");
    }

    @Test
    void toList() {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        assertEquals(identifiers, chain.toList(), "toList");
    }

    @Test
    void at() {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        for (int i = 0; i < identifiers.size(); i++) {
            assertEquals(identifiers.get(i), chain.at(i), "at(" + i + ")");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 3, 10 })
    void at_outOfRange(int index) {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        assertThrows(IndexOutOfBoundsException.class, () -> chain.at(index));
    }

    @Test
    void first() {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        assertEquals(identifiers.get(0), chain.first(), "first");
    }

    @Test
    void last() {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        assertEquals(identifiers.get(2), chain.last(), "last");
    }

    @Test
    void stream() {
        List<Identifier> identifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(identifiers);

        assertEquals(identifiers, chain.stream().toList(), "stream");
    }

    @Test
    void resolve_twoChains() {
        List<Identifier> allIdentifiers = Stream.of("ONE", "TWO", "THREE", "FOUR").map(Identifier::new).toList();
        var chain1 = new IdentifierChain(allIdentifiers.subList(0, 2));
        var chain2 = new IdentifierChain(allIdentifiers.subList(2, 4));

        assertEquals(new IdentifierChain(allIdentifiers), chain1.resolve(chain2), "resolve");
    }

    @Test
    void resolve_chainAndIdentifier() {
        List<Identifier> allIdentifiers = Stream.of("ONE", "TWO", "THREE").map(Identifier::new).toList();
        var chain = new IdentifierChain(allIdentifiers.subList(0, 2));
        var identifier3 = allIdentifiers.get(2);

        assertEquals(new IdentifierChain(allIdentifiers), chain.resolve(identifier3), "resolve");
    }

    @ParameterizedTest
    @EnumSource(Identifier.Scope.class)
    void toString_includesScopeIdentifierSCHEMAorPACKAGE(Identifier.Scope scope) {
        String expected = "\"ONE\"" + (scope != Identifier.Scope.UNKNOWN ? "%" + scope : "") + ".\"TWO\"";

        var chain = new IdentifierChain(List.of(new Identifier("ONE", scope), new Identifier("TWO")));

        assertEquals(expected, chain.toString());
    }

    private static List<Identifier> toIdentifiers(String nameList) {
        return Stream.of(nameList.split("\\.")).map(Identifier::new).toList();
    }
}