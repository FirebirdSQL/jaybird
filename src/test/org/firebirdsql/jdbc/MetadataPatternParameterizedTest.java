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
package org.firebirdsql.jdbc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class MetadataPatternParameterizedTest {

    private final String metadataPattern;
    private final MetadataPattern.ConditionType expectedConditionType;
    private final String expectedConditionValue;

    public MetadataPatternParameterizedTest(String metadataPattern, MetadataPattern.ConditionType expectedConditionType,
            String expectedConditionValue) {
        this.metadataPattern = metadataPattern;
        this.expectedConditionType = expectedConditionType;
        this.expectedConditionValue = expectedConditionValue;
    }

    @Parameterized.Parameters(name = "{index}: {0} => {1} value {2}")
    public static List<Object[]> parameters() {
        return asList(
                testCase(null, MetadataPattern.ConditionType.NONE, null),
                testCase("%", MetadataPattern.ConditionType.NONE, null),
                testCase("", MetadataPattern.ConditionType.SQL_EQUALS, ""),
                testCase("_", MetadataPattern.ConditionType.SQL_LIKE, "_"),
                testCase("\\", MetadataPattern.ConditionType.SQL_EQUALS, "\\"),
                testCase("\\\\", MetadataPattern.ConditionType.SQL_EQUALS, "\\"),
                testCase("a", MetadataPattern.ConditionType.SQL_EQUALS, "a"),
                testCase("a%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "a"),
                testCase("\\a%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "\\a"),
                testCase("ab c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab c"),
                testCase("ab\\_c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab_c"),
                testCase("ab\\%c%", MetadataPattern.ConditionType.SQL_STARTING_WITH, "ab%c"),
                testCase("a_b%", MetadataPattern.ConditionType.SQL_LIKE, "a_b%"),
                testCase("a%b%", MetadataPattern.ConditionType.SQL_LIKE, "a%b%"),
                testCase("a\\__b%", MetadataPattern.ConditionType.SQL_LIKE, "a\\__b%"),
                testCase("a_", MetadataPattern.ConditionType.SQL_LIKE, "a_"),
                testCase("%abc", MetadataPattern.ConditionType.SQL_LIKE, "%abc"),
                testCase("_abc", MetadataPattern.ConditionType.SQL_LIKE, "_abc"),
                testCase("_ab\\c", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\\\c"),
                testCase("_ab\\", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\\\"),
                testCase("_ab\\_", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\_"),
                testCase("_ab\\%", MetadataPattern.ConditionType.SQL_LIKE, "_ab\\%"),
                testCase("ab\\%cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab%cd"),
                testCase("ab\\_cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab_cd"),
                testCase("ab\\_\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab_\\cd"),
                testCase("a_\\_\\cd", MetadataPattern.ConditionType.SQL_LIKE, "a_\\_\\\\cd"),
                testCase("ab\\\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\cd"),
                testCase("ab\\cd", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\cd"),
                testCase("ab\\", MetadataPattern.ConditionType.SQL_EQUALS, "ab\\")
        );
    }

    @Test
    public void testCompile() {
        MetadataPattern compiledPattern = MetadataPattern.compile(metadataPattern);

        assertEquals("conditionType", expectedConditionType, compiledPattern.getConditionType());
        assertEquals("conditionValue", expectedConditionValue, compiledPattern.getConditionValue());
    }

    private static Object[] testCase(String metadataPattern, MetadataPattern.ConditionType expectedConditionType,
            String expectedConditionValue) {
        return new Object[] { metadataPattern, expectedConditionType, expectedConditionValue };
    }
}