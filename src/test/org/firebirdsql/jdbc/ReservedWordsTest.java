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

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Test to check if a list of keywords are actually reserved words by creating a table with a column with that name.
 *
 * @author Mark Rotteveel
 */
@Disabled
class ReservedWordsTest {

    private static final String KEYWORDS_FILE = "reserved_words_3_0.txt";
    private static final List<String> RESERVED_WORDS = new ArrayList<>();

    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll();

    @BeforeAll
    static void loadReservedWords() throws Exception {
        //noinspection ConstantConditions
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                ReservedWordsTest.class.getResourceAsStream(KEYWORDS_FILE), StandardCharsets.UTF_8))) {
            String line;
            while((line = in.readLine()) != null) {
                String keyword = line.trim();
                if (!keyword.isEmpty() && !keyword.startsWith("#")) {
                    RESERVED_WORDS.add(keyword);
                }
            }
        }
    }

    @Test
    void checkReservedWords() throws Exception {
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            int count = 1;
            List<String> keywordWithoutError = new ArrayList<>();
            for (String keyword : RESERVED_WORDS) {
                try {
                    stmt.execute(format("create table table%d (%s integer)", ++count, keyword));
                    keywordWithoutError.add(keyword);
                } catch (SQLException e) {
                    if (e.getErrorCode() != ISCConstants.isc_dsql_token_unk_err) {
                        throw e;
                    }
                    // ignore
                }
            }
            assertThat("Some keywords unexpectedly did not result in an error", keywordWithoutError, is(empty()));
        }
    }
}
