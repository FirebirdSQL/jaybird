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
package org.firebirdsql.jaybird.parser;

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
class StatementDetectorTest {

    private StatementDetector detector = new StatementDetector();

    @Test
    void initialStatementType_typeUNKNOWN() {
        detector = new StatementDetector();
        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(StatementType.UNKNOWN);
    }

    @ParameterizedTest
    @MethodSource("detectionCases")
    void testDetection(boolean detectReturning, String statement, StatementType expectedType,
            Token expectedTableNameToken, boolean expectedReturningDetected, boolean expectedParserCompleted) {
        detector = new StatementDetector(detectReturning);
        SqlParser parser = parserFor(statement);

        parser.parse();

        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(expectedType);
        assertThat(detector.getTableNameToken()).describedAs("tableNameToken").isEqualTo(expectedTableNameToken);
        assertThat(detector.returningClauseDetected())
                .describedAs("returningClauseDetected").isEqualTo(expectedReturningDetected);
        assertThat(parser.isCompleted())
                .describedAs("parser completed").isEqualTo(expectedParserCompleted);
    }

    static Stream<Arguments> detectionCases() {
        return Stream.of(
                // SELECT
                detectReturning("select * from RDB$DATABASE", StatementType.SELECT, false),
                noDetect("select * from RDB$DATABASE", StatementType.SELECT, false),
                detectReturning("/* a comment */ select * from RDB$DATABASE", StatementType.SELECT, false),
                // Presence of select as first keyword is sufficient
                detectReturning("select", StatementType.SELECT, true),
                detectReturning("with a as (select 1 as col from rdb$database) select * from a",
                        StatementType.SELECT, false),
                // Presence of with as first keyword is sufficient
                detectReturning("with", StatementType.SELECT, true),

                // DML
                // insert
                detectReturning("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        StatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                noDetect("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        StatementType.INSERT, new GenericToken(12, "sometable"), false),
                detectReturning("insert into sometable (column1, column2) values (?, ?) returning id",
                        StatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                noDetect("insert into sometable (column1, column2) values (?, ?) returning id",
                        StatementType.INSERT, new GenericToken(12, "sometable"), false),
                detectReturning("insert into sometable (column1, column2) default values returning id",
                        StatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable",
                        StatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable returning id",
                        StatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                detectReturning("INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('Some text to insert') RETURNING *",
                        StatementType.INSERT, new GenericToken(12, "TABLE_WITH_TRIGGER"), true, true),

                // delete
                detectReturning("delete from sometable",
                        StatementType.DELETE, new GenericToken(12, "sometable"), false, true),
                noDetect("delete from sometable", StatementType.DELETE, new GenericToken(12, "sometable"), true),
                detectReturning("delete from sometable returning column1",
                        StatementType.DELETE, new GenericToken(12, "sometable"), true, true),
                noDetect("delete from sometable returning column1",
                        StatementType.DELETE, new GenericToken(12, "sometable"), false),
                detectReturning("delete from sometable as somealias where somealias.foo = 'bar'",
                        StatementType.DELETE, new GenericToken(12, "sometable"), false, true),

                // update
                detectReturning("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        StatementType.UPDATE, new QuotedIdentifierToken(7, "\"sometable\""), false, true),
                noDetect("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        StatementType.UPDATE, new QuotedIdentifierToken(7, "\"sometable\""), false),
                detectReturning("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        StatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                noDetect("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        StatementType.UPDATE, new GenericToken(7, "sometable"), false),
                detectReturning("update sometable withalias set column1 = 1 returning -1 as foo",
                        StatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                detectReturning("update sometable \"withalias\" set column1 = 1 returning (id + 1) as foo",
                        StatementType.UPDATE, new GenericToken(7, "sometable"), true, true),

                // update or insert
                detectReturning("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        StatementType.UPDATE_OR_INSERT, new GenericToken(22, "sometable"), false, true),
                noDetect("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        StatementType.UPDATE_OR_INSERT, new GenericToken(22, "sometable"), false),

                // merge
                detectReturning("merge into sometable as somealias using othertable on somealias.x = othertable.y"
                                + "when matched then update set somealias.y = othertable.x"
                                + "when not matched then insert (x,y) values (othertable.x, othertable.y)",
                        StatementType.MERGE, new GenericToken(11, "sometable"), false, true),
                noDetect("merge into sometable as somealias using othertable on somealias.x = othertable.y"
                                + "when matched then update set somealias.y = othertable.x"
                                + "when not matched then insert (x,y) values (othertable.x, othertable.y)",
                        StatementType.MERGE, new GenericToken(11, "sometable"), false),

                // hairy examples with returning token elsewhere
                detectReturning("insert into returning (returning) values (true)",
                        StatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("insert into returning (returning) values (true) returning id",
                        StatementType.INSERT, new GenericToken(12, "returning"), true, true),
                detectReturning("insert into returning (column1, column2) select a, returning from othertable",
                        StatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("insert into nothing (column1, column2) select a, returning /* comment */ from othertable",
                        StatementType.INSERT, new GenericToken(12, "nothing"), false, true),
                detectReturning("insert into returning (column1, column2) select a, returning, c from othertable",
                        StatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("update sometable as \"somealias\" set column1 = column1 + returning",
                        StatementType.UPDATE, new GenericToken(7, "sometable"), false, true),
                detectReturning("update returning returning set returning = not returning where returning",
                        StatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update returning returning set returning = not returning where returning and x = 1",
                        StatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update returning returning set returning = not returning where x = 1 or returning is true",
                        StatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update likematch set column1 = 'x' where returning like 'a_c%'",
                        StatementType.UPDATE, new GenericToken(7, "likematch"), false, true),
                // probably one of the worst cases (but actually easy for detection)
                detectReturning("update returning returning set returning = not returning where returning returning returning returning",
                        StatementType.UPDATE, new GenericToken(7, "returning"), true, true),

                // invalid syntax
                detectReturning("update or invalid", StatementType.OTHER, true),
                noDetect("update or invalid", StatementType.OTHER, true),
                detectReturning("update or insert invalid", StatementType.OTHER, true),
                detectReturning("delete sometable where x = y return column1", StatementType.OTHER, false),
                noDetect("delete sometable where x = y return column1", StatementType.OTHER, false),
                detectReturning("update and invalid", StatementType.OTHER, false),
                detectReturning("update sometable (invalid)", StatementType.OTHER, false),
                detectReturning("update sometable as as invalid", StatementType.OTHER, false),
                detectReturning("update or insert into default values", StatementType.OTHER, false),
                detectReturning("merge sometable invalid", StatementType.OTHER, false),

                // OTHER (cases include invalid statements)
                detectReturning("execute procedure test 'value1', 'value2'", StatementType.OTHER, false),
                noDetect("execute procedure test 'value1', 'value2'", StatementType.OTHER, false),
                detectReturning("execute block returns (id integer) as begin id = 1; suspend; end",
                        StatementType.OTHER, false),
                noDetect("execute block returns (id integer) as begin id = 1; suspend; end",
                        StatementType.OTHER, false),
                detectReturning("set transaction read write wait isolation level snapshot", StatementType.OTHER, false),
                detectReturning("alter session reset", StatementType.OTHER, false),
                detectReturning("create table test (col1 integer)", StatementType.OTHER, false),
                detectReturning("invalid tokens not recognized", StatementType.OTHER, false),
                noDetect("invalid tokens not recognized", StatementType.OTHER, false)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static Arguments detectReturning(String statement, StatementType expectedType,
            Token expectedTableNameToken, boolean expectedReturningDetected, boolean expectedParserCompleted) {
        return arguments(true, statement, expectedType, expectedTableNameToken, expectedReturningDetected,
                expectedParserCompleted);
    }

    private static Arguments detectReturning(String statement, StatementType expectedType, boolean expectedParserCompleted) {
        return arguments(true, statement, expectedType, null, false, expectedParserCompleted);
    }

    private static Arguments noDetect(String statement, StatementType expectedType, Token expectedTableNameToken,
            boolean expectedParserCompleted) {
        return arguments(false, statement, expectedType, expectedTableNameToken, false, expectedParserCompleted);
    }

    private static Arguments noDetect(String statement, StatementType expectedType,
            boolean expectedParserCompleted) {
        return arguments(false, statement, expectedType, null, false, expectedParserCompleted);
    }

    private SqlParser parserFor(String statementText) {
        return SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(statementText);
    }

}