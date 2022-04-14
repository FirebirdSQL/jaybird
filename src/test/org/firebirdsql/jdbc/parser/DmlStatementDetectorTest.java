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
package org.firebirdsql.jdbc.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Also serves as test for {@link ReturningClauseDetector}.
 */
class DmlStatementDetectorTest {

    private final DmlStatementDetector detector = new DmlStatementDetector();

    @Test
    void initialStatementType_typeUNKNOWN() {
        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(DmlStatementType.UNKNOWN);
    }

    @ParameterizedTest
    @MethodSource("dmlDetectionCases")
    void dmlDetection(String statement, DmlStatementType expectedType, Token expectedTableNameToken,
            boolean expectedReturningDetected, boolean expectedParserCompleted) {
        SqlParser parser = parserFor(statement);

        parser.parse();

        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(expectedType);
        assertThat(detector.getTableNameToken()).describedAs("tableNameToken").isEqualTo(expectedTableNameToken);
        assertThat(detector.returningClauseDetected())
                .describedAs("returningClauseDetected").isEqualTo(expectedReturningDetected);
        assertThat(parser.isCompleted())
                .describedAs("parser completed").isEqualTo(expectedParserCompleted);
    }

    static Stream<Arguments> dmlDetectionCases() {
        return Stream.of(
                // Not recognized as DML
                arguments("select * from rdb$database", DmlStatementType.OTHER, null, false, false),
                arguments("invalid tokens not recognized", DmlStatementType.OTHER, null, false, false),

                // DML
                // insert
                arguments("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        DmlStatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                arguments("insert into sometable (column1, column2) values (?, ?) returning id",
                        DmlStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                arguments("insert into sometable (column1, column2) default values returning id",
                        DmlStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                arguments("insert into sometable (column1, column2) select a, b from othertable",
                        DmlStatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                arguments("insert into sometable (column1, column2) select a, b from othertable returning id",
                        DmlStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                arguments("INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('Some text to insert') RETURNING *",
                        DmlStatementType.INSERT, new GenericToken(12, "TABLE_WITH_TRIGGER"), true, true),

                // delete
                arguments("delete from sometable",
                        DmlStatementType.DELETE, new GenericToken(12, "sometable"), false, true),
                arguments("delete from sometable returning column1",
                        DmlStatementType.DELETE, new GenericToken(12, "sometable"), true, true),
                arguments("delete from sometable as somealias where somealias.foo = 'bar'",
                        DmlStatementType.DELETE, new GenericToken(12, "sometable"), false, true),

                // update
                arguments("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        DmlStatementType.UPDATE, new QuotedIdentifierToken(7, "\"sometable\""), false, true),
                arguments("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        DmlStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                arguments("update sometable withalias set column1 = 1 returning -1 as foo",
                        DmlStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                arguments("update sometable \"withalias\" set column1 = 1 returning (id + 1) as foo",
                        DmlStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),

                // update or insert
                arguments("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        DmlStatementType.UPDATE_OR_INSERT, new GenericToken(22, "sometable"), false, true),

                // merge
                arguments("merge into sometable as somealias using othertable on somealias.x = othertable.y"
                                + "when matched then update set somealias.y = othertable.x"
                                + "when not matched then insert (x,y) values (othertable.x, othertable.y)",
                        DmlStatementType.MERGE, new GenericToken(11, "sometable"), false, true),

                // hairy examples with returning token elsewhere
                arguments("insert into returning (returning) values (true)",
                        DmlStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                arguments("insert into returning (returning) values (true) returning id",
                        DmlStatementType.INSERT, new GenericToken(12, "returning"), true, true),
                arguments("insert into returning (column1, column2) select a, returning from othertable",
                        DmlStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                arguments("insert into nothing (column1, column2) select a, returning /* comment */ from othertable",
                        DmlStatementType.INSERT, new GenericToken(12, "nothing"), false, true),
                arguments("insert into returning (column1, column2) select a, returning, c from othertable",
                        DmlStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                arguments("update sometable as \"somealias\" set column1 = column1 + returning",
                        DmlStatementType.UPDATE, new GenericToken(7, "sometable"), false, true),
                arguments("update returning returning set returning = not returning where returning",
                        DmlStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                arguments("update returning returning set returning = not returning where returning and x = 1",
                        DmlStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                arguments("update returning returning set returning = not returning where x = 1 or returning is true",
                        DmlStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                arguments("update likematch set column1 = 'x' where returning like 'a_c%'",
                        DmlStatementType.UPDATE, new GenericToken(7, "likematch"), false, true),
                // probably one of the worst cases (but actually easy for detection)
                arguments("update returning returning set returning = not returning where returning returning returning returning",
                        DmlStatementType.UPDATE, new GenericToken(7, "returning"), true, true),

                // invalid syntax
                arguments("update or invalid", DmlStatementType.OTHER, null, false, true),
                arguments("update or insert invalid", DmlStatementType.OTHER, null, false, true),
                arguments("delete sometable where x = y return column1", DmlStatementType.OTHER, null, false, false),
                arguments("update and invalid", DmlStatementType.OTHER, null, false, false),
                arguments("update sometable (invalid)", DmlStatementType.OTHER, null, false, false),
                arguments("update sometable as as invalid", DmlStatementType.OTHER, null, false, false),
                arguments("update or insert into default values", DmlStatementType.OTHER, null, false, false),
                arguments("merge sometable invalid", DmlStatementType.OTHER, null, false, false)
        );
    }

    private SqlParser parserFor(String statementText) {
        return SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(statementText);
    }

}