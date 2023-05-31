package org.firebirdsql.gds.ng.nativeoo;

import org.firebirdsql.gds.ng.BatchCompletion;
import org.firebirdsql.gds.ng.FbBatchCompletionState;
import org.firebirdsql.jna.fbclient.CloseableMemory;
import org.firebirdsql.jna.fbclient.FbInterface.IBatchCompletionState;
import org.firebirdsql.jna.fbclient.FbInterface.IStatus;
import org.firebirdsql.jna.fbclient.FbInterface.IUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbBatchCompletionState} for native batch execution.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 6.0
 */
public class IBatchCompletionStateImpl implements FbBatchCompletionState {

    private final IBatchCompletionState state;
    private final IDatabaseImpl database;
    private final IStatus status;

    public IBatchCompletionStateImpl(IDatabaseImpl database, IBatchCompletionState state, IStatus status) {
        this.database = database;
        this.state = state;
        this.status = status;
    }

    @Override
    public int getSize() throws SQLException {
        int result = state.getSize(getStatus());
        processStatus();
        return result;
    }

    @Override
    public int getState(int index) throws SQLException {
        int result = state.getState(getStatus(), index);
        processStatus();
        return result;
    }

    @Override
    public String getError(int index) throws SQLException {
        if (state.findError(status, index) != NO_MORE_ERRORS) {
            StringBuilder builder = new StringBuilder();
            IStatus errorStatus = database.getMaster().getStatus();
            state.getStatus(getStatus(), errorStatus, index);
            processStatus();

            final IUtil util = database.getMaster().getUtilInterface();

            try (CloseableMemory memory = new CloseableMemory(1024)) {
                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                builder.append(memory.getString(0, getDatabase().getEncoding().getCharsetName()));
                return builder.toString();
            }
        }
        return "";
    }

    @Override
    public String printAllStates() throws SQLException {
        StringBuilder builder = new StringBuilder();

        boolean print1 = false;
        boolean print2 = false;

        final IUtil util = database.getMaster().getUtilInterface();

        int updateCount = state.getSize(getStatus());
        processStatus();
        int unknownCount = 0;
        int successCount = 0;
        for (int p = 0; p < updateCount; ++p) {
            int s = state.getState(getStatus(), p);
            processStatus();
            switch (s) {
                case EXECUTE_FAILED:
                    if (!print1) {
                        builder.append(String.format("Message Status %d\n", p));
                        print1 = true;
                    }
                    builder.append(String.format("%5d   Execute failed\n", p));
                    break;

                case SUCCESS_NO_INFO:
                    ++unknownCount;
                    break;

                default:
                    if (!print1) {
                        builder.append(String.format("Message Status %d\n", p));
                        print1 = true;
                    }
                    builder.append(String.format("%5d   Updated %d record(s)\n", p, s));
                    ++successCount;
                    break;
            }
        }
        builder.append(String.format("Summary: total=%d success=%d success(but no update info)=%d\n",
                updateCount, successCount, unknownCount));

        IStatus errorStatus = database.getMaster().getStatus();
        for (int p = 0; (p = state.findError(status, p)) != NO_MORE_ERRORS; ++p) {
            state.getStatus(getStatus(), errorStatus, p);
            processStatus();

            try (CloseableMemory memory = new CloseableMemory(1024)) {

                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                if (!print2) {
                    builder.append(String.format("\nDetailed errors status %d:\n", p));
                    print2 = true;
                }
                builder.append(String.format("Message %d: %s\n", p, memory.getString(0,
                        database.getEncoding().getCharsetName())));
            }
        }

        if (errorStatus != null)
            errorStatus.dispose();

        return builder.toString();
    }

    @Override
    public int[] getAllStates() throws SQLException {
        int updateCount = state.getSize(getStatus());
        processStatus();

        int[] states = new int[updateCount];

        for (int p = 0; p < updateCount; ++p) {
            states[p] = state.getState(getStatus(), p);
            processStatus();
        }

        return states;
    }

    @Override
    public BatchCompletion getBatchCompletion() throws SQLException {
        final IUtil util = database.getMaster().getUtilInterface();

        int elementCount = state.getSize(getStatus());
        processStatus();
        int updateCountsCount = 0;
        int detailedErrorsCount = 0;
        int simplifiedErrorsCount = 0;

        int[] updateCounts = new int[elementCount];
        int[] simplifiedErrors = new int[elementCount];

        for (int p = 0; p < elementCount; ++p) {
            int s = state.getState(getStatus(), p);
            processStatus();
            switch (s) {
                case EXECUTE_FAILED:
                    updateCounts[p] = s;
                    simplifiedErrors[p] = s;
                    updateCountsCount++;
                    simplifiedErrorsCount++;
                    break;

                case SUCCESS_NO_INFO:
                    updateCounts[p] = s;
                    updateCountsCount++;
                    break;

                default:
                    updateCounts[p] = s;
                    updateCountsCount++;
                    break;
            }
        }

        List<BatchCompletion.DetailedError> detailedErrors = new ArrayList<>();
        IStatus errorStatus = database.getMaster().getStatus();
        for (int p = 0; (p = state.findError(status, p)) != NO_MORE_ERRORS; ++p) {
            state.getStatus(getStatus(), errorStatus, p);
            processStatus();

            try (CloseableMemory memory = new CloseableMemory(1024)) {
                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                detailedErrors.add(new BatchCompletion.DetailedError(p, new SQLException(memory.getString(0,
                        database.getEncoding().getCharsetName()))));
            }
        }

        if (errorStatus != null)
            errorStatus.dispose();

        updateCounts = Arrays.copyOf(updateCounts, updateCountsCount);
        simplifiedErrors = Arrays.copyOf(simplifiedErrors, simplifiedErrorsCount);

        return new BatchCompletion(elementCount, updateCounts, detailedErrors, simplifiedErrors);
    }

    private IStatus getStatus() {
        status.init();
        return status;
    }

    private IDatabaseImpl getDatabase() {
        return database;
    }

    private void processStatus() throws SQLException {
        getDatabase().processStatus(status, null);
    }

}
