// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchPathExtractorTest {

    private final SearchPathExtractor extractor = new SearchPathExtractor();

    @Test
    void initialSearchPathListIsEmpty() {
        assertThat(extractor.getSearchPathList(), is(empty()));
    }

    @ParameterizedTest
    @MethodSource("extractionTestCases")
    void testSearchPathListExtraction(String searchPath, List<String> expectedSearchPathList) {
        SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(extractor)
                .of(searchPath)
                .parse();
        assertEquals(expectedSearchPathList, extractor.getSearchPathList());
    }

    static Stream<Arguments> extractionTestCases() {
        return Stream.of(
                Arguments.of("", List.of()),
                Arguments.of("\"PUBLIC\"", List.of("PUBLIC")),
                Arguments.of("\"PUBLIC\",\"SYSTEM\"", List.of("PUBLIC", "SYSTEM")),
                Arguments.of("UNQUOTED_SCHEMA,\"QUOTED_SCHEMA\"", List.of("UNQUOTED_SCHEMA", "QUOTED_SCHEMA")),
                Arguments.of("INVALID,,TWO_SEPARATORS", List.of()),
                Arguments.of("INVALID,ENDS_IN_SEPARATOR,", List.of()));
    }

}