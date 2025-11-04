// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.util;

import org.firebirdsql.jdbc.QuoteStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchPathHelperTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "  "})
    void parseSearchPath_emptyList_forNullOrEmptyOrBlank(String searchPath) {
        assertThat(SearchPathHelper.parseSearchPath(searchPath), is(empty()));
    }

    @ParameterizedTest
    @MethodSource("searchPathCases")
    void parseSearchPath(String inputSearchPath, List<String> expectedSearchPathList, QuoteStrategy ignored1,
            String ignored2) {
        assertEquals(expectedSearchPathList, SearchPathHelper.parseSearchPath(inputSearchPath));
    }

    @ParameterizedTest
    @MethodSource("searchPathCases")
    void toSearchPath(String ignored, List<String> inputSearchPathList, QuoteStrategy quoteStrategy,
            String expectedSearchPath) {
        assertEquals(expectedSearchPath, SearchPathHelper.toSearchPath(inputSearchPathList, quoteStrategy));
    }

    static Stream<Arguments> searchPathCases() {
        return Stream.of(
                Arguments.of("", List.of(), QuoteStrategy.DIALECT_3, ""),
                Arguments.of("", List.of(), QuoteStrategy.DIALECT_1, ""),
                Arguments.of("PUBLIC, SYSTEM", List.of("PUBLIC", "SYSTEM"), QuoteStrategy.DIALECT_3,
                        "\"PUBLIC\", \"SYSTEM\""),
                Arguments.of("PUBLIC, SYSTEM", List.of("PUBLIC", "SYSTEM"), QuoteStrategy.DIALECT_1, "PUBLIC, SYSTEM"),
                Arguments.of("\"PUBLIC\", \"SYSTEM\"", List.of("PUBLIC", "SYSTEM"), QuoteStrategy.DIALECT_3,
                        "\"PUBLIC\", \"SYSTEM\""),
                Arguments.of("\"PUBLIC\", \"SYSTEM\"", List.of("PUBLIC", "SYSTEM"), QuoteStrategy.DIALECT_1,
                        "PUBLIC, SYSTEM")
        );
    }

}