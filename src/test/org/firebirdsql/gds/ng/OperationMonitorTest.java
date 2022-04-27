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
package org.firebirdsql.gds.ng;

import org.firebirdsql.common.rules.UsesDatabase;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.monitor.Operation;
import org.firebirdsql.gds.ng.monitor.OperationAware;
import org.junit.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.firebirdsql.common.FBTestProperties.getConnectionViaDriverManager;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OperationMonitorTest {

    @Rule
    public final UsesDatabase usesDatabase = UsesDatabase.noDatabase();
    private final TestOperationAware testOperationAware = new TestOperationAware();

    @Before
    public void initOperationMonitor() {
        OperationMonitor.initOperationAware(testOperationAware);
    }

    @After
    public void clearOperationMonitor() {
        OperationMonitor.initOperationAware(null);
    }

    @AfterClass
    public static void clearOperationMonitorAgain() {
        // paranoia: extra clear of OperationMonitor
        OperationMonitor.initOperationAware(null);
    }

    @Test
    public void startOperationNotifiesOnRegisteredInstance() {
        Operation operation = new DummyOperation();
        OperationMonitor.startOperation(operation);

        List<OperationReport> reportedOperations = testOperationAware.getReportedOperations();
        assertEquals("Unexpected number of operations", 1, reportedOperations.size());
        OperationReport operationReport = reportedOperations.get(0);
        assertEquals(OperationReport.Type.START, operationReport.getType());
        assertEquals(operation, operationReport.getOperation());
    }

    @Test
    public void endOperationNotifiesOnRegisteredInstance() {
        Operation operation = new DummyOperation();
        OperationMonitor.endOperation(operation);

        List<OperationReport> reportedOperations = testOperationAware.getReportedOperations();
        assertEquals("Unexpected number of operations", 1, reportedOperations.size());
        OperationReport operationReport = reportedOperations.get(0);
        assertEquals("Unexpected report type", OperationReport.Type.END, operationReport.getType());
        assertEquals("Unexpected operation", operation, operationReport.getOperation());
    }

    @Test
    public void notificationDuringSimpleSelect() throws Exception {
        usesDatabase.createDefaultDatabase();
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            List<OperationReport> reportedOperations = testOperationAware.getReportedOperations();
            assertEquals(0, reportedOperations.size());
            try (ResultSet rs = stmt.executeQuery("select 1 from RDB$DATABASE")) {
                assertEquals(2, reportedOperations.size());
                assertOperationReport(reportedOperations.get(0), OperationReport.Type.START,
                        Operation.Type.STATEMENT_EXECUTE);
                assertOperationReport(reportedOperations.get(1), OperationReport.Type.END,
                        Operation.Type.STATEMENT_EXECUTE);

                assertTrue("Expected a row", rs.next());
                // Native implementation does additional fetch during next()
                assertThat(reportedOperations, hasSize(greaterThanOrEqualTo(4)));
                assertOperationReport(reportedOperations.get(2), OperationReport.Type.START,
                        Operation.Type.STATEMENT_FETCH);
                assertOperationReport(reportedOperations.get(3), OperationReport.Type.END,
                        Operation.Type.STATEMENT_FETCH);
            }
        }
    }

    // Technically this test should probably belong in a FbStatementTest instead
    @Test
    public void synchronousCancellationDuringExecute() throws Exception {
        TestOperationAware syncCancelOperationAware = new TestOperationAware() {
            @Override
            public void startOperation(Operation operation) {
                super.startOperation(operation);
                if (operation.getType() == Operation.Type.STATEMENT_EXECUTE) {
                    try {
                        operation.cancel();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        OperationMonitor.initOperationAware(syncCancelOperationAware);
        usesDatabase.createDefaultDatabase();
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            List<OperationReport> reportedOperations = syncCancelOperationAware.getReportedOperations();
            assertEquals(0, reportedOperations.size());
            try (ResultSet ignored = stmt.executeQuery("select 1 from RDB$DATABASE")) {
                fail("should throw exception");
            } catch (SQLException e) {
                assertThat(e, errorCodeEquals(ISCConstants.isc_cancelled));
            }
            assertEquals(2, reportedOperations.size());
            assertOperationReport(reportedOperations.get(0), OperationReport.Type.START,
                    Operation.Type.STATEMENT_EXECUTE);
            assertOperationReport(reportedOperations.get(1), OperationReport.Type.END,
                    Operation.Type.STATEMENT_EXECUTE);
        }
    }

    // Technically this test should probably belong in a FbStatementTest instead
    @Test
    public void synchronousCancellationDuringFetch() throws Exception {
        TestOperationAware syncCancelOperationAware = new TestOperationAware() {
            @Override
            public void startOperation(Operation operation) {
                super.startOperation(operation);
                if (operation.getType() == Operation.Type.STATEMENT_FETCH) {
                    try {
                        operation.cancel();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        OperationMonitor.initOperationAware(syncCancelOperationAware);
        usesDatabase.createDefaultDatabase();
        try (Connection connection = getConnectionViaDriverManager();
             Statement stmt = connection.createStatement()) {
            connection.setAutoCommit(false);
            List<OperationReport> reportedOperations = syncCancelOperationAware.getReportedOperations();
            assertEquals(0, reportedOperations.size());
            try (ResultSet rs = stmt.executeQuery("select 1 from RDB$DATABASE")) {
                assertEquals(2, reportedOperations.size());
                assertOperationReport(reportedOperations.get(0), OperationReport.Type.START,
                        Operation.Type.STATEMENT_EXECUTE);
                assertOperationReport(reportedOperations.get(1), OperationReport.Type.END,
                        Operation.Type.STATEMENT_EXECUTE);

                rs.next();

                fail("should throw exception");
            } catch (SQLException e) {
                assertThat(e, errorCodeEquals(ISCConstants.isc_cancelled));
            }
            assertEquals(4, reportedOperations.size());
            assertOperationReport(reportedOperations.get(2), OperationReport.Type.START,
                    Operation.Type.STATEMENT_FETCH);
            assertOperationReport(reportedOperations.get(3), OperationReport.Type.END,
                    Operation.Type.STATEMENT_FETCH);
        }
    }

    static void assertOperationReport(OperationReport operationReport, OperationReport.Type reportType,
            Operation.Type operationType) {
        assertEquals("operationReport.type", reportType, operationReport.getType());
        assertEquals("operationReport.operation.type", operationType, operationReport.getOperation().getType());
    }

    static class TestOperationAware implements OperationAware {

        private final List<OperationReport> reportedOperations = new ArrayList<>();

        @Override
        public void startOperation(Operation operation) {
            reportedOperations.add(OperationReport.start(operation));
        }

        @Override
        public void endOperation(Operation operation) {
            reportedOperations.add(OperationReport.end(operation));
        }

        List<OperationReport> getReportedOperations() {
            return reportedOperations;
        }
    }

    static class OperationReport {

        private final Type type;
        private final Operation operation;

        private OperationReport(Type type, Operation operation) {
            this.type = type;
            this.operation = operation;
        }

        public Type getType() {
            return type;
        }

        public Operation getOperation() {
            return operation;
        }

        static OperationReport start(Operation operation) {
            return new OperationReport( Type.START, operation);
        }

        static OperationReport end(Operation operation) {
            return new OperationReport(Type.END, operation);
        }

        enum Type {
            START,
            END
        }
    }

    static class DummyOperation implements Operation {

        @Override
        public Type getType() {
            return null;
        }

        @Override
        public void cancel() {
            // does nothing
        }
    }
}
