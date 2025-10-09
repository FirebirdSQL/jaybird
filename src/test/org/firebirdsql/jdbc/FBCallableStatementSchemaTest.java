// SPDX-FileCopyrightText: Copyright 2025 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.common.extension.UsesDatabaseExtension;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.util.ObjectReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.firebirdsql.common.FbAssumptions.assumeSchemaSupport;
import static org.firebirdsql.common.assertions.ResultSetAssertions.assertNextRow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link FBCallableStatement} for its schema support.
 * <p>
 * Some schema-related testing occurs in {@link FBCallableStatementTest}.
 * </p>
 */
class FBCallableStatementSchemaTest {

    // NOTE Names of schemas and packages overlap, that is intentional
    @RegisterExtension
    static final UsesDatabaseExtension.UsesDatabaseForAll usesDatabase = UsesDatabaseExtension.usesDatabaseForAll(
            dbInitStatements());

    @BeforeAll
    static void requiresSchemaSupport() {
        assumeSchemaSupport();
    }

    private static List<String> dbInitStatements() {
        if (!getDefaultSupportInfo().supportsSchemas()) {
            // UsesDatabaseForAll extension is registered before evaluation of requiresSchemaSupport
            return List.of();
        }
        return Stream.of(
                        Stream.of("T1", "T2", "T3").map(FBCallableStatementSchemaTest::createSchema),
                        Stream.of(
                                createProcedure("PUBLIC", Proc.selectable("PROC1")),
                                createProcedure("PUBLIC", Proc.executable("PROC2")),
                                createProcedure("T1", Proc.executable("PROC1")),
                                createProcedure("T2", Proc.selectable("PROC1")),
                                createProcedure("T2", Proc.selectable("PROC2")),
                                createProcedure("T3", Proc.executable("PROC2"))),
                        createPackage("PUBLIC", "T2", Proc.executable("PROC2")),
                        createPackage("T1", "T3", Proc.executable("PROC1"), Proc.selectable("PROC2")),
                        createPackage("T1", "T2", Proc.selectable("PROC2")),
                        createPackage("T2", "T1", Proc.selectable("PROC1")),
                        createPackage("T2", "T2", Proc.executable("PROC1"), Proc.executable("PROC2")),
                        createPackage("T3", "T2", Proc.executable("PROC1"), Proc.selectable("PROC2"))
                )
                .reduce(Stream.of(), Stream::concat)
                .toList();
    }

    @SuppressWarnings("SqlSourceToSinkFlow")
    @ParameterizedTest
    @MethodSource
    void testProcedureResolution(String searchPath, String statement, ObjectReference expectedProcedure,
            boolean selectable) throws Exception {
        try (var connection = createConnection(searchPath);
             var cstmt = connection.prepareCall(statement).unwrap(FirebirdCallableStatement.class)) {
            assertEquals(selectable, cstmt.isSelectableProcedure(), "selectableProcedure");
            try (var rs = cstmt.executeQuery()) {
                assertNextRow(rs);
                assertEquals(expectedProcedure.toString(), rs.getString(1));
            }
        }
    }

    static Stream<Arguments> testProcedureResolution() {
        return Stream.of(
                testCase("PUBLIC", "{call PROC1}", ObjectReference.of("PUBLIC", "PROC1"), true),
                testCase("T1", "{call PUBLIC.PROC1}", ObjectReference.of("PUBLIC", "PROC1"), true),
                testCase("PUBLIC", "{call PROC2}", ObjectReference.of("PUBLIC", "PROC2"), false),
                testCase("PUBLIC", "{call T1.PROC1}", ObjectReference.of("T1", "PROC1"), false),
                testCase("PUBLIC", "{call T2.PROC2}", ObjectReference.of("T2", "PROC2"), true),
                testCase("PUBLIC", "{call T2.T2.PROC2}", ObjectReference.of("T2", "T2", "PROC2"), false),
                testCase("PUBLIC", "{call T2%PACKAGE.PROC2}", ObjectReference.of("PUBLIC", "T2", "PROC2"), false),
                testCase("T2,PUBLIC", "{call T2%PACKAGE.PROC2}", ObjectReference.of("T2", "T2", "PROC2"), false),
                testCase("T1,T2,T3", "{call PROC1}", ObjectReference.of("T1", "PROC1"), false),
                testCase("T2,T3,T1", "{call PROC1}", ObjectReference.of("T2", "PROC1"), true),
                testCase("T3,T1,T2", "{call PROC1}", ObjectReference.of("T1", "PROC1"), false),
                testCase("T1,T2,T3", "{call T3.PROC1}", ObjectReference.of("T1", "T3", "PROC1"), false),
                testCase("T1,T2,T3", "{call T3.PROC2}", ObjectReference.of("T3", "PROC2"), false),
                testCase("T1,T2,T3", "{call T3%SCHEMA.PROC2}", ObjectReference.of("T3", "PROC2"), false),
                testCase("T1,T2,T3", "{call T3%PACKAGE.PROC2}", ObjectReference.of("T1", "T3", "PROC2"), true));
    }

    private static Arguments testCase(String searchPath, String statement, ObjectReference expectedProcedure,
            boolean selectable) {
        return Arguments.of(searchPath, statement, expectedProcedure, selectable);
    }

    private static Connection createConnection(String searchPath) throws SQLException {
        return getConnectionViaDriverManager(PropertyNames.searchPath, searchPath);
    }



    private static String createSchema(String schema) {
        return "create schema " + ObjectReference.of(schema);
    }

    private static String createProcedure(String schema, Proc proc) {
        String procedureName = ObjectReference.of(schema, proc.name()).toString();
        return """
            create procedure %s returns (RESULT varchar(50))
            as
            begin
              RESULT = '%s';%s
            end""".formatted(procedureName, procedureName.replace("'", "''"), proc.selectable() ? "\n  suspend;" : "");
    }

    private static Stream<String> createPackage(String schema, String pkg, Proc... procs) {
        return Stream.of(createPackageHeader(schema, pkg, procs), createPackageBody(schema, pkg, procs));
    }

    private static String createPackageHeader(String schema, String pkg, Proc... procs) {
        var sb = new StringBuilder("create package ");
        ObjectReference.of(schema, pkg).append(sb, QuoteStrategy.DIALECT_3).append("\nas\nbegin\n");
        for (Proc proc : procs) {
            sb.append("  procedure ");
            QuoteStrategy.DIALECT_3.appendQuoted(proc.name(), sb).append(" returns (RESULT varchar(50));\n");
        }
        sb.append("end");
        return sb.toString();
    }

    private static String createPackageBody(String schema, String pkg, Proc... procs) {
        var sb = new StringBuilder("create package body ");
        ObjectReference.of(schema, pkg).append(sb, QuoteStrategy.DIALECT_3).append("\nas\nbegin\n");
        for (Proc proc : procs) {
            sb.append("  procedure ");
            QuoteStrategy.DIALECT_3.appendQuoted(proc.name(), sb)
                    .append(" returns (RESULT varchar(50))\n  as\n  begin\n    result = '");
            ObjectReference.of(schema, pkg, proc.name()).append(sb, QuoteStrategy.DIALECT_3).append("';\n");
            if (proc.selectable()) {
                sb.append("    suspend;\n");
            }
            sb.append("  end\n");
        }
        sb.append("end");
        return sb.toString();
    }

    record Proc(String name, boolean selectable) {

        static Proc selectable(String name) {
            return new Proc(name, true);
        }

        static Proc executable(String name) {
            return new Proc(name, false);
        }

    }

}
