/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link Clause}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class ClauseTest {

    // Note: some tests repeat what is also tested by MetadataPatternParameterizedTest

    @Test
    public void clauseSimpleValue() {
        Clause clause = new Clause("COLUMN", "SIMPLE");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseAnyCharacterWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH_WILDCARD");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseAllWildcardValue_inMiddle() {
        Clause clause = new Clause("COLUMN", "WITH%WILDCARD");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseAllWildcardValue_atEnd() {
        Clause clause = new Clause("COLUMN", "WITH%");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseEscapedAnyCharacterWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH\\_WILDCARD");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseEscapedAllWildcardValue() {
        Clause clause = new Clause("COLUMN", "WITH\\%WILDCARD");

        assertTrue("Expected condition", clause.hasCondition());
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
    public void clauseNullValue_noCondition() {
        validateNoCondition(null);
    }

    @Test
    public void clauseAllPattern_noCondition() {
        validateNoCondition("%");
    }

    private void validateNoCondition(String conditionValue) {
        Clause clause = new Clause("COLUMN", conditionValue);

        assertFalse("Expected no condition", clause.hasCondition());
        assertThat("Expected no condition", clause.getCondition(), isEmptyString());
        assertThat("Expected no condition", clause.getCondition(true), isEmptyString());
        assertThat("Expected no condition", clause.getCondition(false), isEmptyString());
        assertThat("Expected no condition", clause.getCondition("", ""), isEmptyString());
        assertThat("Expected no condition", clause.getCondition("prefix|", "|suffix"), isEmptyString());
        assertThat("Expected no condition", clause.getCondition("", "|suffix"), isEmptyString());
        assertThat("Expected no condition", clause.getCondition("prefix|", ""), isEmptyString());
        assertNull("Expected null value", clause.getValue());
    }

}