// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.Identifier;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ObjectReferenceExtractorTest {

    @ParameterizedTest
    @MethodSource
    void extractObjectReference(String text, boolean verifyScope, ObjectReference expectedObjectReference) {
        ObjectReference objectReference = parse(text);

        assertEquals(expectedObjectReference, objectReference);
        if (verifyScope) {
            for (int idx = 0; idx < expectedObjectReference.size(); idx++) {
                assertEquals(expectedObjectReference.at(idx).scope(), objectReference.at(idx).scope(),
                        "unexpected scope for identifier at index " + idx);
            }
        }
    }

    static Stream<Arguments> extractObjectReference() {
        return Stream.of(
                testCase("table_name", "TABLE_NAME"),
                testCase("/* comment */ \"table_name\" -- comment", "table_name"),
                testCase("identifier1.identifier2", "IDENTIFIER1", "IDENTIFIER2"),
                testCase("identifier1.identifier2.identifier3", "IDENTIFIER1", "IDENTIFIER2", "IDENTIFIER3"),
                testCase("identifier1%schema.identifier2",
                        new Identifier("IDENTIFIER1", Identifier.Scope.SCHEMA), new Identifier("IDENTIFIER2")),
                testCase("\"identifier1\"%package.identifier2",
                        new Identifier("identifier1", Identifier.Scope.PACKAGE), new Identifier("IDENTIFIER2")),
                testCase("identifier1.identifier2 where x = y", "IDENTIFIER1", "IDENTIFIER2"),
                testCase("identifier1.identifier2.identifier3 as some_alias",
                        "IDENTIFIER1", "IDENTIFIER2", "IDENTIFIER3"),
                // Not actually valid in Firebird, but interpreted as the end of the identifier chain
                testCase("identifier1%5", "IDENTIFIER1")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INCOMPLETE.",
            "INCOMPLETE. /* comment */",
            "INCOMPLETE%",
            "INCOMPLETE% /* comment */",
            ".PERIOD_START",
            "%SCOPE_SPECIFIER_START",
            "SECOND.not an identifier"
    })
    void expectedParserFailures(String text) {
        assertThrows(IllegalStateException.class, () -> parse(text));
    }

    private static Arguments testCase(String text, String... expectedIdentifierNames) {
        return testCase(text, false, ObjectReference.of(expectedIdentifierNames));
    }

    private static Arguments testCase(String text, Identifier... expectedIdentifiers) {
        return testCase(text, true, ObjectReference.ofIdentifiers(expectedIdentifiers));
    }

    private static Arguments testCase(String text, boolean verifyScope, ObjectReference expectedObjectReference) {
        return Arguments.of(text, verifyScope, expectedObjectReference);
    }

    private static ObjectReference parse(String text) {
        var detector = new ObjectReferenceExtractor();
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(text)
                .parse();
        return detector.toObjectReference();
    }

}