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

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.OperationMonitorTest.OperationReport;
import org.firebirdsql.gds.ng.OperationMonitorTest.TestOperationAware;
import org.firebirdsql.gds.ng.monitor.Operation;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;
import java.util.List;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FbDatabaseOperationTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final TestOperationAware testOperationAware = new TestOperationAware();
    private final List<OperationReport> reportedOperations = testOperationAware.getReportedOperations();
    private final FbDatabase fbDatabase = context.mock(FbDatabase.class);

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
    public void signalExecuteNotifiesExecuteStart() {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        assertEquals("reported operations", 1, reportedOperations.size());
        assertOperationReport(reportedOperations.get(0), OperationReport.Type.START, Operation.Type.STATEMENT_EXECUTE,
                handle);
    }

    @Test
    public void signalFetchNotifiesFetchStart() {
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase);
        assertEquals("reported operations", 1, reportedOperations.size());
        assertOperationReport(reportedOperations.get(0), OperationReport.Type.START, Operation.Type.STATEMENT_FETCH,
                handle);
    }

    @Test
    public void closeOfExecuteNotifiesExecuteEnd() {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        handle.close();
        assertEquals("reported operations", 2, reportedOperations.size());
        assertOperationReport(reportedOperations.get(1), OperationReport.Type.END, Operation.Type.STATEMENT_EXECUTE,
                handle);
    }

    @Test
    public void closeOfFetchNotifiesFetchEnd() {
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase);
        handle.close();
        assertEquals("reported operations", 2, reportedOperations.size());
        assertOperationReport(reportedOperations.get(1), OperationReport.Type.END, Operation.Type.STATEMENT_FETCH,
                handle);
    }

    @Test
    public void unclosedExecuteAllowsCancellation() throws Exception {
        FbDatabaseOperation.signalExecute(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();

        context.checking(new Expectations() {{
            oneOf(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
        }});

        operation.cancel();
    }

    @Test
    public void unclosedFetchAllowsCancellation() throws Exception {
        FbDatabaseOperation.signalFetch(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();

        context.checking(new Expectations() {{
            oneOf(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
        }});

        operation.cancel();
    }

    @Test
    public void closedExecuteDisallowsCancellation() throws Exception {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();
        handle.close();

        context.checking(new Expectations() {{
            never(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
        }});

        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_operationClosed),
                fbMessageStartsWith(JaybirdErrorCodes.jb_operationClosed, "cancel")
        ));

        operation.cancel();
    }

    @Test
    public void closedFetchDisallowsCancellation() throws Exception {
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();
        handle.close();

        context.checking(new Expectations() {{
            never(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
        }});

        expectedException.expect(SQLException.class);
        expectedException.expect(allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_operationClosed),
                fbMessageStartsWith(JaybirdErrorCodes.jb_operationClosed, "cancel")
        ));

        operation.cancel();
    }

    void assertOperationReport(OperationReport operationReport, OperationReport.Type reportType,
            Operation.Type operationType, OperationCloseHandle handle) {
        OperationMonitorTest.assertOperationReport(operationReport, reportType, operationType);
        assertEquals("operationReport.operation", handle, operationReport.getOperation());
    }
}