// SPDX-FileCopyrightText: Copyright 2021-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jaybird.parser;

import org.firebirdsql.jaybird.util.ObjectReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
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
        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(LocalStatementType.UNKNOWN);
    }

    private SqlParser parserFor(String statementText) {
        return SqlParser.withReservedWords(FirebirdReservedWords.latest())
                .withVisitor(detector)
                .of(statementText);
    }

    @ParameterizedTest
    @MethodSource("detectionCases")
    void testDetection(boolean detectReturning, String statement, LocalStatementType expectedType,
            ObjectReference expectedTargetObject, boolean expectedReturningDetected, boolean expectedParserCompleted,
            List<ProcedureArgument> expectedProcedureArguments) {
        detector = new StatementDetector(detectReturning);
        SqlParser parser = parserFor(statement);

        parser.parse();

        assertThat(detector.getStatementType()).describedAs("statementType").isEqualTo(expectedType);
        assertThat(detector.getTargetObject()).describedAs("targetObject").isEqualTo(Optional.ofNullable(expectedTargetObject));
        assertThat(detector.returningClauseDetected())
                .describedAs("returningClauseDetected").isEqualTo(expectedReturningDetected);
        assertThat(parser.isCompleted())
                .describedAs("parser completed").isEqualTo(expectedParserCompleted);
        assertThat(detector.getProcedureArguments()).describedAs("procedureArguments")
                .isEqualTo(expectedProcedureArguments);
    }

    static Stream<Arguments> detectionCases() {
        return Stream.of(
                // SELECT
                detectReturning("select * from RDB$DATABASE", LocalStatementType.SELECT),
                noDetect("select * from RDB$DATABASE", LocalStatementType.SELECT),
                detectReturning("/* a comment */ select * from RDB$DATABASE", LocalStatementType.SELECT),
                // Presence of select as first keyword is sufficient
                detectReturning("select", LocalStatementType.SELECT, true),
                detectReturning("with a as (select 1 as col from rdb$database) select * from a",
                        LocalStatementType.SELECT),
                // Presence of with as first keyword is sufficient
                detectReturning("with", LocalStatementType.SELECT, true),

                // SELECT: Parenthesized query expressions
                detectReturning("(select * from RDB$DATABASE)", LocalStatementType.SELECT),
                noDetect("(select * from RDB$DATABASE)", LocalStatementType.SELECT),
                noDetect("((select * from RDB$DATABASE))", LocalStatementType.SELECT),
                // Presence of only the open parenthesis is sufficient
                detectReturning("(", LocalStatementType.SELECT, true),

                // EXECUTE PROCEDURE
                detectReturning("execute procedure test 'value1', 'value2'",
                        LocalStatementType.EXECUTE_PROCEDURE, ObjectReference.of("TEST"), false, false),
                noDetect("execute procedure test('value1', 'value2')",
                        LocalStatementType.EXECUTE_PROCEDURE, ObjectReference.of("TEST"), false),
                detectReturning("execute procedure test",
                        LocalStatementType.EXECUTE_PROCEDURE, ObjectReference.of("TEST"), false, true),
                noDetect("execute procedure \"some_schema\".\"test\"",
                        LocalStatementType.EXECUTE_PROCEDURE, ObjectReference.of("some_schema", "test"), true),
                // incomplete
                noDetect("execute procedure", LocalStatementType.OTHER, true),
                noDetect("execute procedure ", LocalStatementType.OTHER, true),

                // DML
                // insert
                detectReturning("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), false, true),
                noDetect("insert into sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), false),
                detectReturning("insert into sometable (column1, column2) values (?, ?) returning id",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), true, true),
                noDetect("insert into sometable (column1, column2) values (?, ?) returning id",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), false),
                detectReturning("insert into sometable (column1, column2) default values returning id",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), true, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), false, true),
                detectReturning("insert into sometable (column1, column2) select a, b from othertable returning id",
                        LocalStatementType.INSERT, ObjectReference.of("SOMETABLE"), true, true),
                detectReturning("INSERT INTO TABLE_WITH_TRIGGER(TEXT) VALUES ('Some text to insert') RETURNING *",
                        LocalStatementType.INSERT, ObjectReference.of("TABLE_WITH_TRIGGER"), true, true),
                detectReturning("insert into other_schema.sometable (id, column1, column2) values (?, ?, ?)",
                        LocalStatementType.INSERT, ObjectReference.of("OTHER_SCHEMA", "SOMETABLE"), false, true),
                detectReturning("insert into other_schema.\"sometable\" values (1, 2) returning id1, id2",
                        LocalStatementType.INSERT, ObjectReference.of("OTHER_SCHEMA", "sometable"), true, true),
                noDetect("insert into other_schema.\"sometable\" values (1, 2) returning id1, id2",
                        LocalStatementType.INSERT, ObjectReference.of("OTHER_SCHEMA", "sometable"), false),

                // delete
                detectReturning("delete from sometable",
                        LocalStatementType.DELETE, ObjectReference.of("SOMETABLE"), false, true),
                noDetect("delete from sometable", LocalStatementType.DELETE, ObjectReference.of("SOMETABLE"), true),
                detectReturning("delete from sometable returning column1",
                        LocalStatementType.DELETE, ObjectReference.of("SOMETABLE"), true, true),
                noDetect("delete from sometable returning column1",
                        LocalStatementType.DELETE, ObjectReference.of("SOMETABLE"), false),
                detectReturning("delete from sometable as somealias where somealias.foo = 'bar'",
                        LocalStatementType.DELETE, ObjectReference.of("SOMETABLE"), false, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\"",
                        LocalStatementType.DELETE, ObjectReference.of("OTHER_SCHEMA", "sometable"), false, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\" returning column1",
                        LocalStatementType.DELETE, ObjectReference.of("OTHER_SCHEMA", "sometable"), true, true),
                detectReturning("delete from \"OTHER_SCHEMA\".\"sometable\" as \"x\" returning column1",
                        LocalStatementType.DELETE, ObjectReference.of("OTHER_SCHEMA", "sometable"), true, true),

                // update
                detectReturning("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        LocalStatementType.UPDATE, ObjectReference.of("sometable"), false, true),
                noDetect("update \"sometable\" set column1 = 1, column2 = column2 + 1 where x = y",
                        LocalStatementType.UPDATE, ObjectReference.of("sometable"), false),
                detectReturning("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        LocalStatementType.UPDATE, ObjectReference.of("SOMETABLE"), true, true),
                noDetect("update sometable set column1 = 1, column2 = column2 + 1 where x = y returning column2, x",
                        LocalStatementType.UPDATE, ObjectReference.of("SOMETABLE"), false),
                detectReturning("update sometable withalias set column1 = 1 returning -1 as foo",
                        LocalStatementType.UPDATE, ObjectReference.of("SOMETABLE"), true, true),
                detectReturning("update sometable \"withalias\" set column1 = 1 returning (id + 1) as foo",
                        LocalStatementType.UPDATE, ObjectReference.of("SOMETABLE"), true, true),
                detectReturning("update PUBLIC.sometable set column1 = 2 returning calculated_column",
                        LocalStatementType.UPDATE, ObjectReference.of("PUBLIC", "SOMETABLE"), true, true),

                // update or insert
                detectReturning("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        LocalStatementType.UPDATE_OR_INSERT, ObjectReference.of("SOMETABLE"), false, true),
                noDetect("update or insert into sometable (id, column1, column2) values (?, ?, (? * 2)) matching (id)",
                        LocalStatementType.UPDATE_OR_INSERT, ObjectReference.of("SOMETABLE"), false),

                // merge
                detectReturning("""
                        merge into sometable as somealias using othertable on somealias.x = othertable.y
                          when matched then update set somealias.y = othertable.x
                          when not matched then insert (x,y) values (othertable.x, othertable.y)""",
                        LocalStatementType.MERGE, ObjectReference.of("SOMETABLE"), false, true),
                noDetect("""
                        merge into sometable as somealias using othertable on somealias.x = othertable.y
                          when matched then update set somealias.y = othertable.x
                          when not matched then insert (x,y) values (othertable.x, othertable.y)""",
                        LocalStatementType.MERGE, ObjectReference.of("SOMETABLE"), false),

                // hairy examples with returning token elsewhere
                detectReturning("insert into returning (returning) values (true)",
                        LocalStatementType.INSERT, ObjectReference.of("RETURNING"), false, true),
                detectReturning("insert into returning (returning) values (true) returning id",
                        LocalStatementType.INSERT, ObjectReference.of("RETURNING"), true, true),
                detectReturning("insert into returning (column1, column2) select a, returning from othertable",
                        LocalStatementType.INSERT, ObjectReference.of("RETURNING"), false, true),
                detectReturning("insert into nothing (column1, column2) select a, returning /* comment */ from othertable",
                        LocalStatementType.INSERT, ObjectReference.of("NOTHING"), false, true),
                detectReturning("insert into returning (column1, column2) select a, returning, c from othertable",
                        LocalStatementType.INSERT, ObjectReference.of("RETURNING"), false, true),
                detectReturning("update sometable as \"somealias\" set column1 = column1 + returning",
                        LocalStatementType.UPDATE, ObjectReference.of("SOMETABLE"), false, true),
                detectReturning("update returning returning set returning = not returning where returning",
                        LocalStatementType.UPDATE, ObjectReference.of("RETURNING"), false, true),
                detectReturning("update returning returning set returning = not returning where returning and x = 1",
                        LocalStatementType.UPDATE, ObjectReference.of("RETURNING"), false, true),
                detectReturning("update returning returning set returning = not returning where x = 1 or returning is true",
                        LocalStatementType.UPDATE, ObjectReference.of("RETURNING"), false, true),
                detectReturning("update likematch set column1 = 'x' where returning like 'a_c%'",
                        LocalStatementType.UPDATE, ObjectReference.of("LIKEMATCH"), false, true),
                // probably one of the worst cases (but actually easy for detection)
                detectReturning("update returning returning set returning = not returning where returning returning returning returning",
                        LocalStatementType.UPDATE, ObjectReference.of("RETURNING"), true, true),

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
                noDetect("rollback to savepoint 'XYZ'", LocalStatementType.OTHER),
                noDetect("rollback work to savepoint 'XYZ'", LocalStatementType.OTHER, false),

                // SET TRANSACTION
                noDetect("set transaction",
                        LocalStatementType.SET_TRANSACTION, true),
                detectReturning("set transaction read write wait isolation level snapshot",
                        LocalStatementType.SET_TRANSACTION, false),
                noDetect("set transaction read write wait isolation level snapshot",
                        LocalStatementType.SET_TRANSACTION, false),
                // We ignore everything after SET TRANSACTION, the server will do further parsing
                noDetect("set transaction syntax error", LocalStatementType.SET_TRANSACTION),

                // Other savepoint statements
                noDetect("savepoint 'XYZ'", LocalStatementType.OTHER),
                noDetect("release savepoint 'XYZ'", LocalStatementType.OTHER),
                noDetect("release savepoint 'XYZ' only", LocalStatementType.OTHER),

                // USING ... DO <statement>
                noDetect("""
                        using (p1 integer = ?, p2 integer = ?)
                          declare function subfunc (i1 integer) returns integer
                          as
                          begin
                            return i1;
                          end
                        
                          declare procedure subproc (i1 integer) returns (o1 integer)
                          as
                          begin
                            for select case status when 'found' then 1 else 0 end from some_table where i1 = :i1
                              into o1 do
                            begin
                              suspend;
                            end
                          end
                        do
                        -- The main query
                        select subfunc(:p1) + o1 from subproc(:p2 + ?)""",
                        LocalStatementType.SELECT),
                detectReturning("""
                        using (val integer = ?)
                        do insert into generic_table (col_a, col_b) values (:val, :val);""",
                        LocalStatementType.INSERT, ObjectReference.of("GENERIC_TABLE"), false, true),
                detectReturning("""
                        using (val integer = ?)
                        do insert into generic_table (col_a, col_b) values (:val, :val) returning id""",
                        LocalStatementType.INSERT, ObjectReference.of("GENERIC_TABLE"), true, true),
                // Annoyingly, DO is not a reserved word
                noDetect("""
                        using (DO integer = ?)
                          -- forward declared
                          declare function sub_func(DO integer) returns integer;
                          declare function sub_func2(i1 integer) returns integer
                          as
                            declare variable DO integer = 5;
                          begin
                            return sub_func(i1 + DO);
                          end
                          declare function sub_func(DO integer) returns integer
                          as
                          begin
                            return DO * 2;
                          end
                        do update "sometable" set column1 = sub_func(1), column2 = column2 + 1 where x = do""",
                        LocalStatementType.UPDATE, ObjectReference.of("sometable"), false),
                detectReturning("""
                        using
                          declare DO integer = 1;
                        do delete from "sometable" where x = do returning id""",
                        LocalStatementType.DELETE, ObjectReference.of("sometable"), true, true),
                // Combined with call escape: not supported
                noDetect("using (SOME_VALUE integer = ?) do {call someproc(:SOME_VALUE)}",
                        LocalStatementType.JDBC_ESCAPE_AFTER_USING),

                // JDBC call escape
                noDetect("{call someproc}", LocalStatementType.JDBC_CALL_ESCAPE, ObjectReference.of("SOMEPROC"), true),
                TestCaseBuilder.noDetect("{call someproc", LocalStatementType.OTHER).expectParserCompleted().build(),
                TestCaseBuilder.noDetect("{call someproc(param1, param2)}", LocalStatementType.JDBC_CALL_ESCAPE)
                        .expectTargetObject(ObjectReference.of("SOMEPROC"))
                        .expectProcedureArguments(new ProcedureArgument.Expression("param1", 0),
                                new ProcedureArgument.Expression(" param2", 0))
                        .expectParserCompleted().build(),
                noDetect("{?=call someproc}",
                        LocalStatementType.JDBC_CALL_RETURN_ESCAPE, ObjectReference.of("SOMEPROC"), true),
                noDetect("{? = call someproc}",
                        LocalStatementType.JDBC_CALL_RETURN_ESCAPE, ObjectReference.of("SOMEPROC"), true),
                TestCaseBuilder.noDetect("{? = call someproc(?)}", LocalStatementType.JDBC_CALL_RETURN_ESCAPE)
                        .expectTargetObject(ObjectReference.of("SOMEPROC"))
                        .expectProcedureArguments(new ProcedureArgument.PositionalParameter("?"))
                        .expectParserCompleted().build(),
                // NOTE: Missing closing brace
                noDetect("{call someproc(param1, param2)", LocalStatementType.OTHER, true),
                noDetect("{?=call someproc(param1, param2)", LocalStatementType.OTHER, true),

                // Firebird 6+ CALL
                noDetect("call insert_customer('LECLERC', 'CHARLES', null, ?)",
                        LocalStatementType.CALL, ObjectReference.of("INSERT_CUSTOMER"), false),
                noDetect("""
                        call insert_customer(
                            last_name => 'LECLERC',
                            first_name => 'CHARLES',
                            last_name => ?,
                            id => ?)""", LocalStatementType.CALL, ObjectReference.of("INSERT_CUSTOMER"), false),

                // invalid syntax
                noDetect("insert from invalid", LocalStatementType.OTHER),
                detectReturning("update or invalid", LocalStatementType.OTHER, true),
                noDetect("update or invalid", LocalStatementType.OTHER, true),
                detectReturning("update or insert invalid", LocalStatementType.OTHER, true),
                detectReturning("delete sometable where x = y return column1", LocalStatementType.OTHER),
                noDetect("delete sometable where x = y return column1", LocalStatementType.OTHER),
                noDetect("delete into sometable", LocalStatementType.OTHER, false),
                detectReturning("update and invalid", LocalStatementType.OTHER),
                detectReturning("update sometable (invalid)", LocalStatementType.OTHER),
                detectReturning("update sometable as as invalid", LocalStatementType.OTHER),
                detectReturning("update or insert into default values", LocalStatementType.OTHER),
                detectReturning("update or delete from something", LocalStatementType.OTHER),
                detectReturning("merge sometable invalid", LocalStatementType.OTHER),
                detectReturning("merge from sometable invalid", LocalStatementType.OTHER),
                noDetect("execute procedure (SOMETHING)", LocalStatementType.OTHER),

                // OTHER (cases include invalid statements)
                detectReturning("execute block returns (id integer) as begin id = 1; suspend; end",
                        LocalStatementType.OTHER),
                noDetect("execute block returns (id integer) as begin id = 1; suspend; end",
                        LocalStatementType.OTHER),
                detectReturning("alter session reset", LocalStatementType.OTHER),
                detectReturning("create table test (col1 integer)", LocalStatementType.OTHER),
                noDetect("set time zone 'UTC'", LocalStatementType.OTHER),
                noDetect("set something on", LocalStatementType.OTHER),
                detectReturning("invalid tokens not recognized", LocalStatementType.OTHER),
                noDetect("invalid tokens not recognized", LocalStatementType.OTHER),
                noDetect("commit syntax error", LocalStatementType.OTHER),
                noDetect("commit work work", LocalStatementType.OTHER, true),
                noDetect("commit work syntax error", LocalStatementType.OTHER),
                noDetect("rollback syntax error", LocalStatementType.OTHER),
                noDetect("rollback work work", LocalStatementType.OTHER, true),
                noDetect("rollback work syntax error", LocalStatementType.OTHER),
                noDetect("{fn user()}", LocalStatementType.OTHER),
                noDetect("{?>call someproc}", LocalStatementType.OTHER),
                noDetect("{? NOT_EQUALS", LocalStatementType.OTHER, true),
                noDetect("{?=NOT_CALL", LocalStatementType.OTHER, true),
                noDetect("{call}", LocalStatementType.OTHER, true),
                noDetect("{call someproc() NOT_A_CURLY_BRACE", LocalStatementType.OTHER, true),
                noDetect("call ()", LocalStatementType.OTHER),
                noDetect("execute block as begin end", LocalStatementType.OTHER),
                noDetect("execute function XYZ()", LocalStatementType.OTHER)
        );
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType) {
        return TestCaseBuilder.detectReturning(statement, expectedType).build();
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType,
            boolean expectedParserCompleted) {
        return TestCaseBuilder.detectReturning(statement, expectedType)
                .expectParserCompleted(expectedParserCompleted)
                .build();
    }

    private static Arguments detectReturning(String statement, LocalStatementType expectedType,
            ObjectReference expectedTargetObject, boolean expectedReturningDetected, boolean expectedParserCompleted) {
        return TestCaseBuilder.detectReturning(statement, expectedType)
                .expectTargetObject(expectedTargetObject)
                .expectReturningDetected(expectedReturningDetected)
                .expectParserCompleted(expectedParserCompleted)
                .build();
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType) {
        return TestCaseBuilder.noDetect(statement, expectedType).build();
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType,
            boolean expectedParserCompleted) {
        return TestCaseBuilder.noDetect(statement, expectedType)
                .expectParserCompleted(expectedParserCompleted)
                .build();
    }

    private static Arguments noDetect(String statement, LocalStatementType expectedType,
            ObjectReference expectedTargetObject, boolean expectedParserCompleted) {
        return TestCaseBuilder.noDetect(statement, expectedType)
                .expectTargetObject(expectedTargetObject)
                .expectParserCompleted(expectedParserCompleted)
                .build();
    }

    @SuppressWarnings("unused")
    private static final class TestCaseBuilder {

        private final boolean detectReturning;
        private final String statement;
        private final LocalStatementType expectedType;
        private Object expectedTargetObject;
        private boolean expectedReturningDetected;
        private boolean expectedParserCompleted;
        private List<ProcedureArgument> expectedProcedureArguments = List.of();

        private TestCaseBuilder(boolean detectReturning, String statement, LocalStatementType expectedType) {
            this.detectReturning = detectReturning;
            this.statement = statement;
            this.expectedType = expectedType;
        }

        static TestCaseBuilder noDetect(String statement, LocalStatementType expectedType) {
            return new TestCaseBuilder(false, statement, expectedType);
        }

        static TestCaseBuilder detectReturning(String statement, LocalStatementType expectedType) {
            return new TestCaseBuilder(true, statement, expectedType);
        }

        TestCaseBuilder expectReturningDetected(boolean expectedReturningDetected) {
            this.expectedReturningDetected = expectedReturningDetected;
            return this;
        }

        TestCaseBuilder expectReturningDetected() {
            return expectReturningDetected(true);
        }

        TestCaseBuilder expectParserCompleted(boolean expectedParserCompleted) {
            this.expectedParserCompleted = expectedParserCompleted;
            return this;
        }

        TestCaseBuilder expectParserCompleted() {
            return expectParserCompleted(true);
        }

        TestCaseBuilder expectTargetObject(ObjectReference expectedTargetObject) {
            this.expectedTargetObject = expectedTargetObject;
            return this;
        }

        TestCaseBuilder expectedProcedureArguments(List<ProcedureArgument> expectedProcedureArguments) {
            this.expectedProcedureArguments = expectedProcedureArguments;
            return this;
        }

        TestCaseBuilder expectProcedureArguments(ProcedureArgument... expectedProcedureArguments) {
            return expectedProcedureArguments(List.of(expectedProcedureArguments));
        }

        private Arguments build() {
            return arguments(detectReturning, statement, expectedType, expectedTargetObject, expectedReturningDetected,
                    expectedParserCompleted, expectedProcedureArguments);
        }

    }

}