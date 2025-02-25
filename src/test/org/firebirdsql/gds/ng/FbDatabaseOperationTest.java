// SPDX-FileCopyrightText: Copyright 2019-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.TestOperationAware.OperationReport;
import org.firebirdsql.gds.ng.monitor.Operation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;

import static org.firebirdsql.common.matchers.SQLExceptionMatchers.errorCodeEquals;
import static org.firebirdsql.common.matchers.SQLExceptionMatchers.fbMessageStartsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class FbDatabaseOperationTest {

    private final TestOperationAware testOperationAware = new TestOperationAware();
    private final List<OperationReport> reportedOperations = testOperationAware.getReportedOperations();
    @Mock
    private FbDatabase fbDatabase;

    @BeforeEach
    void initOperationMonitor() {
        OperationMonitor.initOperationAware(testOperationAware);
    }

    @AfterEach
    void clearOperationMonitor() {
        OperationMonitor.initOperationAware(null);
    }

    @AfterAll
    static void clearOperationMonitorAgain() {
        // paranoia: extra clear of OperationMonitor
        OperationMonitor.initOperationAware(null);
    }

    @Test
    void signalExecuteNotifiesExecuteStart() {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        assertEquals(1, reportedOperations.size(), "reported operations");
        assertOperationReport(reportedOperations.get(0), OperationReport.Type.START, Operation.Type.STATEMENT_EXECUTE,
                handle);
    }

    @Test
    void signalFetchNotifiesFetchStart() {
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase, () -> {});
        assertEquals(1, reportedOperations.size(), "reported operations");
        assertOperationReport(reportedOperations.get(0), OperationReport.Type.START, Operation.Type.STATEMENT_FETCH,
                handle);
    }

    @Test
    void closeOfExecuteNotifiesExecuteEnd() {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        handle.close();
        assertEquals(2, reportedOperations.size(), "reported operations");
        assertOperationReport(reportedOperations.get(1), OperationReport.Type.END, Operation.Type.STATEMENT_EXECUTE,
                handle);
    }

    @Test
    void closeOfFetchNotifiesFetchEnd() {
        class CompletionHandler implements Runnable {
            boolean hasRun;

            @Override
            public void run() {
                hasRun = true;
            }
        }
        CompletionHandler completionHandler = new CompletionHandler();
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase, completionHandler);
        handle.close();
        assertEquals(2, reportedOperations.size(), "reported operations");
        assertOperationReport(reportedOperations.get(1), OperationReport.Type.END, Operation.Type.STATEMENT_FETCH,
                handle);
        assertTrue(completionHandler.hasRun, "Expected completion handler to have been run");
    }

    @Test
    void unclosedExecuteAllowsCancellation() throws Exception {
        FbDatabaseOperation.signalExecute(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();

        operation.cancel();
        verify(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
    }

    @Test
    void unclosedFetchAllowsCancellation() throws Exception {
        FbDatabaseOperation.signalFetch(fbDatabase, () -> {});
        Operation operation = reportedOperations.get(0).getOperation();

        operation.cancel();
        verify(fbDatabase).cancelOperation(ISCConstants.fb_cancel_raise);
    }

    @Test
    void closedExecuteDisallowsCancellation() throws Exception {
        OperationCloseHandle handle = FbDatabaseOperation.signalExecute(fbDatabase);
        Operation operation = reportedOperations.get(0).getOperation();
        handle.close();

        SQLException exception = assertThrows(SQLException.class, operation::cancel);
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_operationClosed),
                fbMessageStartsWith(JaybirdErrorCodes.jb_operationClosed, "cancel")));
        verify(fbDatabase, never()).cancelOperation(ISCConstants.fb_cancel_raise);
    }

    @Test
    void closedFetchDisallowsCancellation() throws Exception {
        OperationCloseHandle handle = FbDatabaseOperation.signalFetch(fbDatabase, () -> {});
        Operation operation = reportedOperations.get(0).getOperation();
        handle.close();

        SQLException exception = assertThrows(SQLException.class, operation::cancel);
        assertThat(exception, allOf(
                errorCodeEquals(JaybirdErrorCodes.jb_operationClosed),
                fbMessageStartsWith(JaybirdErrorCodes.jb_operationClosed, "cancel")));
        verify(fbDatabase, never()).cancelOperation(ISCConstants.fb_cancel_raise);
    }

    private void assertOperationReport(OperationReport operationReport, OperationReport.Type reportType,
            Operation.Type operationType, OperationCloseHandle handle) {
        OperationMonitorTest.assertOperationReport(operationReport, reportType, operationType);
        assertEquals(handle, operationReport.getOperation(), "operationReport.operation");
    }
}