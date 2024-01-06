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
package org.firebirdsql.jdbc.metadata;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Clause}.
 *
 * @author Mark Rotteveel
 */
class ClauseTest {

    // Note: some tests repeat what is also tested by MetadataPatternParameterizedTest

    @Test
    void clauseSimpleValue() {
        Clause clause = new Clause("COLUMN", "SIMPLE");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition());
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition(true));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition(false));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition("", ""));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("prefix|", "|suffix"));
        assertEquals("COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("", "|suffix"));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) ", clause.getCondition("prefix|", ""));
        assertEquals("SIMPLE", clause.getValue());
    }

    @Test
    void clauseAnyCharacterWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH_WILDCARD");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\'  and ", clause.getCondition());
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\'  and ",
                clause.getCondition(true));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ", clause.getCondition(false));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ",
                clause.getCondition("", ""));
        assertEquals("prefix|trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' |suffix",
                clause.getCondition("prefix|", "|suffix"));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' |suffix",
                clause.getCondition("", "|suffix"));
        assertEquals("prefix|trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ",
                clause.getCondition("prefix|", ""));
        assertEquals("WITH_WILDCARD", clause.getValue());
    }

    @Test
    void clauseAllWildcardValue_inMiddle() {
        Clause clause = new Clause("COLUMN", "WITH%WILDCARD");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\'  and ", clause.getCondition());
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\'  and ",
                clause.getCondition(true));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ", clause.getCondition(false));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ",
                clause.getCondition("", ""));
        assertEquals("prefix|trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' |suffix",
                clause.getCondition("prefix|", "|suffix"));
        assertEquals("trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' |suffix",
                clause.getCondition("", "|suffix"));
        assertEquals("prefix|trim(trailing from COLUMN) like cast(? as varchar(73)) escape '\\' ",
                clause.getCondition("prefix|", ""));
        assertEquals("WITH%WILDCARD", clause.getValue());
    }

    @Test
    void clauseAllWildcardValue_atEnd() {
        Clause clause = new Clause("COLUMN", "WITH%");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("COLUMN starting with cast(? as varchar(73))  and ", clause.getCondition());
        assertEquals("COLUMN starting with cast(? as varchar(73))  and ", clause.getCondition(true));
        assertEquals("COLUMN starting with cast(? as varchar(73)) ", clause.getCondition(false));
        assertEquals("COLUMN starting with cast(? as varchar(73)) ", clause.getCondition("", ""));
        assertEquals("prefix|COLUMN starting with cast(? as varchar(73)) |suffix",
                clause.getCondition("prefix|", "|suffix"));
        assertEquals("COLUMN starting with cast(? as varchar(73)) |suffix", clause.getCondition("", "|suffix"));
        assertEquals("prefix|COLUMN starting with cast(? as varchar(73)) ", clause.getCondition("prefix|", ""));
        assertEquals("WITH", clause.getValue());
    }

    @Test
    void clauseEscapedAnyCharacterWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH\\_WILDCARD");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition());
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition(true));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition(false));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition("", ""));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("prefix|", "|suffix"));
        assertEquals("COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("", "|suffix"));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) ", clause.getCondition("prefix|", ""));
        assertEquals("WITH_WILDCARD", clause.getValue());
    }

    @Test
    void clauseEscapedAllWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH\\%WILDCARD");

        assertTrue(clause.hasCondition(), "Expected condition");
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition());
        assertEquals("COLUMN = cast(? as varchar(73))  and ", clause.getCondition(true));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition(false));
        assertEquals("COLUMN = cast(? as varchar(73)) ", clause.getCondition("", ""));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("prefix|", "|suffix"));
        assertEquals("COLUMN = cast(? as varchar(73)) |suffix", clause.getCondition("", "|suffix"));
        assertEquals("prefix|COLUMN = cast(? as varchar(73)) ", clause.getCondition("prefix|", ""));
        assertEquals("WITH%WILDCARD", clause.getValue());
    }

    @Test
    void clauseNullValue_noCondition() {
        validateNoCondition(null);
    }

    @Test
    void clauseAllPattern_noCondition() {
        validateNoCondition("%");
    }

    private void validateNoCondition(String conditionValue) {
        Clause clause = new Clause("COLUMN", conditionValue);

        assertFalse(clause.hasCondition(), "Expected no condition");
        assertThat("Expected no condition", clause.getCondition(), emptyString());
        assertThat("Expected no condition", clause.getCondition(true), emptyString());
        assertThat("Expected no condition", clause.getCondition(false), emptyString());
        assertThat("Expected no condition", clause.getCondition("", ""), emptyString());
        assertThat("Expected no condition", clause.getCondition("prefix|", "|suffix"), emptyString());
        assertThat("Expected no condition", clause.getCondition("", "|suffix"), emptyString());
        assertThat("Expected no condition", clause.getCondition("prefix|", ""), emptyString());
        assertNull(clause.getValue(), "Expected null value");
    }

}