// SPDX-FileCopyrightText: Copyright 2021-2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(LocalStatementType.UNKNOWN);
    }

    @ParameterizedTest
    @MethodSource("detectionCases")
    void testDetection(boolean detectReturning, String statement, LocalStatementType expectedType,
            Token expectedSchemaToken, Token expectedTableNameToken, boolean expectedReturningDetected,
            boolean expectedParserCompleted) {
        detector = new StatementDetector(detectReturning);
        SqlParser parser = parserFor(statement);

        parser.parse();

        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(expectedType);
        assertThat(detector.getSchemaToken()).describedAs("schemaToken").isEqualTo(expectedSchemaToken);
        assertThat(detector.getTableNameToken()).describedAs("tableNameToken").isEqualTo(expectedTableNameToken);
        assertThat(detector.returningClauseDetected())
                .describedAs("returningClauseDetected").isEqualTo(expectedReturningDetected);
        assertThat(parser.isCompleted())
                .describedAs("parser completed").isEqualTo(expectedParserCompleted);
    }

    static Stream<Arguments> detectionCases() {
        return Stream.of(
                // SELECT
                detectReturning("select * from RDB$DATABASE", LocalStatementType.SELECT, false),
                noDetect("select * from RDB$DATABASE", LocalStatementType.SELECT, false),
                detectReturning("/* a comment */ select * from RDB$DATABASE", LocalStatementType.SELECT, false),
                // Presence of select as first keyword is sufficient
                detectReturning("select", LocalStatementType.SELECT, true),
                detectReturning("with a as (select 1 as col from rdb$database) select * from a",
                        LocalStatementType.SELECT, false),
                // Presence of with as first keyword is sufficient
                detectReturning("with", LocalStatementType.SELECT, true),

                // EXECUTE PROCEDURE
                detectReturning("execute procedure test 'value1', 'value2'",
                        LocalStatementType.EXECUTE_PROCEDURE, false),
                noDetect("execute procedure test 'value1', 'value2'", LocalStatementType.EXECUTE_PROCEDURE, false),
                // Presence of execute procedure as first two keywords is sufficient
                detectReturning("execute procedure", LocalStatementType.EXECUTE_PROCEDURE, true),

                // DML
                // insert
                detectReturning("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                noDetect("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), false),
                detectReturning("insert into sometable (column1, column2) values (?, ?) returning id",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                noDetect("insert into sometable (column1, column2) values (?, ?) returning id",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), false),
                detectReturning("insert into sometable (column1, column2) default values returning id",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), false, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable returning id",
                        LocalStatementType.INSERT, new GenericToken(12, "sometable"), true, true),
                detectReturning("INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('Some text to insert') RETURNING *",
                        LocalStatementType.INSERT, new GenericToken(12, "TABLE_WITH_TRIGGER"), true, true),
                detectReturning("insert into other_schema.sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, new GenericToken(12, "other_schema"),
                        new GenericToken(25, "sometable"), false, true),
                detectReturning("insert into other_schema.\"sometable\" values (1, 2) returning id1, id2",
                        LocalStatementType.INSERT, new GenericToken(12, "other_schema"),
                        new QuotedIdentifierToken(25, "\"sometable\""), true, true),
                noDetect("insert into other_schema.\"sometable\" values (1, 2) returning id1, id2",
                        LocalStatementType.INSERT, new GenericToken(12, "other_schema"),
                        new QuotedIdentifierToken(25, "\"sometable\""), false),

                // delete
                detectReturning("delete from sometable",
                        LocalStatementType.DELETE, new GenericToken(12, "sometable"), false, true),
                noDetect("delete from sometable", LocalStatementType.DELETE, new GenericToken(12, "sometable"), true),
                detectReturning("delete from sometable returning column1",
                        LocalStatementType.DELETE, new GenericToken(12, "sometable"), true, true),
                noDetect("delete from sometable returning column1",
                        LocalStatementType.DELETE, new GenericToken(12, "sometable"), false),
                detectReturning("delete from sometable as somealias where somealias.foo = 'bar'",
                        LocalStatementType.DELETE, new GenericToken(12, "sometable"), false, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\"",
                        LocalStatementType.DELETE, new QuotedIdentifierToken(12, "\"OTHER_SCHEMA\""),
                        new QuotedIdentifierToken(27, "\"sometable\""), false, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\" returning column1",
                        LocalStatementType.DELETE, new QuotedIdentifierToken(12, "\"OTHER_SCHEMA\""),
                        new QuotedIdentifierToken(27, "\"sometable\""), true, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\" as \"x\" returning column1",
                        LocalStatementType.DELETE, new QuotedIdentifierToken(12, "\"OTHER_SCHEMA\""),
                        new QuotedIdentifierToken(27, "\"sometable\""), true, true),

                // update
                detectReturning("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        LocalStatementType.UPDATE, new QuotedIdentifierToken(7, "\"sometable\""), false, true),
                noDetect("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        LocalStatementType.UPDATE, new QuotedIdentifierToken(7, "\"sometable\""), false),
                detectReturning("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        LocalStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                noDetect("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        LocalStatementType.UPDATE, new GenericToken(7, "sometable"), false),
                detectReturning("update sometable withalias set column1 = 1 returning -1 as foo",
                        LocalStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                detectReturning("update sometable \"withalias\" set column1 = 1 returning (id + 1) as foo",
                        LocalStatementType.UPDATE, new GenericToken(7, "sometable"), true, true),
                detectReturning("update PUBLIC.sometable set column1 = 2 returning calculated_column",
                        LocalStatementType.UPDATE, new GenericToken(7, "PUBLIC"), new GenericToken(14, "sometable"),
                        true, true),

                // update or insert
                detectReturning("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        LocalStatementType.UPDATE_OR_INSERT, new GenericToken(22, "sometable"), false, true),
                noDetect("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        LocalStatementType.UPDATE_OR_INSERT, new GenericToken(22, "sometable"), false),

                // merge
                detectReturning("merge into sometable as somealias using othertable on somealias.x = othertable.y"
                                + "when matched then update set somealias.y = othertable.x"
                                + "when not matched then insert (x,y) values (othertable.x, othertable.y)",
                        LocalStatementType.MERGE, new GenericToken(11, "sometable"), false, true),
                noDetect("merge into sometable as somealias using othertable on somealias.x = othertable.y"
                                + "when matched then update set somealias.y = othertable.x"
                                + "when not matched then insert (x,y) values (othertable.x, othertable.y)",
                        LocalStatementType.MERGE, new GenericToken(11, "sometable"), false),

                // hairy examples with returning token elsewhere
                detectReturning("insert into returning (returning) values (true)",
                        LocalStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("insert into returning (returning) values (true) returning id",
                        LocalStatementType.INSERT, new GenericToken(12, "returning"), true, true),
                detectReturning("insert into returning (column1, column2) select a, returning from othertable",
                        LocalStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("insert into nothing (column1, column2) select a, returning /* comment */ from othertable",
                        LocalStatementType.INSERT, new GenericToken(12, "nothing"), false, true),
                detectReturning("insert into returning (column1, column2) select a, returning, c from othertable",
                        LocalStatementType.INSERT, new GenericToken(12, "returning"), false, true),
                detectReturning("update sometable as \"somealias\" set column1 = column1 + returning",
                        LocalStatementType.UPDATE, new GenericToken(7, "sometable"), false, true),
                detectReturning("update returning returning set returning = not returning where returning",
                        LocalStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update returning returning set returning = not returning where returning and x = 1",
                        LocalStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update returning returning set returning = not returning where x = 1 or returning is true",
                        LocalStatementType.UPDATE, new GenericToken(7, "returning"), false, true),
                detectReturning("update likematch set column1 = 'x' where returning like 'a_c%'",
                        LocalStatementType.UPDATE, new GenericToken(7, "likematch"), false, true),
                // probably one of the worst cases (but actually easy for detection)
                detectReturning("update returning returning set returning = not returning where returning returning returning returning",
                        LocalStatementType.UPDATE, new GenericToken(7, "returning"), true, true),

                // Transaction statements

                // COMMIT (including retain, which is detected as OTHER)
                noDetect("commit", LocalStatementType.HARD_COMMIT, true),
                noDetect("commit work", LocalStatementType.HARD_COMMIT, true),
                noDetect("commit retain", LocalStatementType.OTHER, true),
                noDetect("commit work retain", LocalStatementType.OTHER, true),

                // ROLLBACK (including retain and to savepoint, which are detected as OTHER)
                noDetect("rollback", LocalStatementType.HARD_ROLLBACK, true),
                noDetect("rollback work", LocalStatementType.HARD_ROLLBACK, true),
                noDetect("rollback retain", LocalStatementType.OTHER, true),
                noDetect("rollback work retain", LocalStatementType.OTHER, true),
                noDetect("rollback to savepoint 'XYZ'", LocalStatementType.OTHER, false),
                noDetect("rollback work to savepoint 'XYZ'", LocalStatementType.OTHER, false),

                // SET TRANSACTION
                noDetect("set transaction",
                        LocalStatementType.SET_TRANSACTION, true),
                detectReturning("set transaction read write wait isolation level snapshot",
                        LocalStatementType.SET_TRANSACTION, false),
                noDetect("set transaction read write wait isolation level snapshot",
                        LocalStatementType.SET_TRANSACTION, false),
                // We ignore everything after SET TRANSACTION, the server will do further parsing
                noDetect("set transaction syntax error", LocalStatementType.SET_TRANSACTION, false),

                // Other savepoint statements
                noDetect("savepoint 'XYZ'", LocalStatementType.OTHER, false),
                noDetect("release savepoint 'XYZ'", LocalStatementType.OTHER, false),
                noDetect("release savepoint 'XYZ' only", LocalStatementType.OTHER, false),

                // invalid syntax
                detectReturning("update or invalid", LocalStatementType.OTHER, true),
                noDetect("update or invalid", LocalStatementType.OTHER, true),
                detectReturning("update or insert invalid", LocalStatementType.OTHER, true),
                detectReturning("delete sometable where x = y return column1", LocalStatementType.OTHER, false),
                noDetect("delete sometable where x = y return column1", LocalStatementType.OTHER, false),
                detectReturning("update and invalid", LocalStatementType.OTHER, false),
                detectReturning("update sometable (invalid)", LocalStatementType.OTHER, false),
                detectReturning("update sometable as as invalid", LocalStatementType.OTHER, false),
                detectReturning("update or insert into default values", LocalStatementType.OTHER, false),
                detectReturning("merge sometable invalid", LocalStatementType.OTHER, false),

                // OTHER (cases include invalid statements)
                detectReturning("execute block returns (id integer) as begin id = 1; suspend; end",
                        LocalStatementType.OTHER, false),
                noDetect("execute block returns (id integer) as begin id = 1; suspend; end",
                        LocalStatementType.OTHER, false),
                detectReturning("alter session reset", LocalStatementType.OTHER, false),
                detectReturning("create table test (col1 integer)", LocalStatementType.OTHER, false),
                noDetect("set time zone 'UTC'", LocalStatementType.OTHER, false),
                detectReturning("invalid tokens not recognized", LocalStatementType.OTHER, false),
                noDetect("invalid tokens not recognized", LocalStatementType.OTHER, false),
                noDetect("commit syntax error", LocalStatementType.OTHER, false),
                noDetect("commit work work", LocalStatementType.OTHER, true),
                noDetect("commit work syntax error", LocalStatementType.OTHER, false),
                noDetect("rollback syntax error", LocalStatementType.OTHER, false),
                noDetect("rollback work work", LocalStatementType.OTHER, true),
                noDetect("rollback work syntax error", LocalStatementType.OTHER, false)
        );
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType,
            boolean expectedParserCompleted) {
        return detectReturning(statement, expectedType, null, false, expectedParserCompleted);
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType,
            Token expectedTableNameToken, boolean expectedReturningDetected, boolean expectedParserCompleted) {
        return detectReturning(statement, expectedType, null, expectedTableNameToken, expectedReturningDetected,
                expectedParserCompleted);
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType,
            Token expectedSchemaToken, Token expectedTableNameToken, boolean expectedReturningDetected,
            boolean expectedParserCompleted) {
        return testCase(true, statement, expectedType, expectedSchemaToken, expectedTableNameToken,
                expectedReturningDetected, expectedParserCompleted);
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType,
            boolean expectedParserCompleted) {
        return noDetect(statement, expectedType, null, null, expectedParserCompleted);
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType, Token expectedTableNameToken,
            boolean expectedParserCompleted) {
        return noDetect(statement, expectedType, null, expectedTableNameToken, expectedParserCompleted);
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType, Token expectedSchemaToken,
            Token expectedTableNameToken, boolean expectedParserCompleted) {
        return testCase(false, statement, expectedType, expectedSchemaToken, expectedTableNameToken, false,
                expectedParserCompleted);
    }

    private static Arguments testCase(boolean detectReturning, String statement, LocalStatementType expectedType,
            Token expectedSchemaToken, Token expectedTableNameToken, boolean expectedReturningDetected,
            boolean expectedParserCompleted) {
        return arguments(detectReturning, statement, expectedType, expectedSchemaToken, expectedTableNameToken,
                expectedReturningDetected, expectedParserCompleted);
    }

    private SqlParser parserFor(String statementText) {
        return SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(statementText);
    }

}