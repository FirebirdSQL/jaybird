package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbBatchCompletionState;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

import java.sql.SQLException;

/**
 * Implementation of {@Link FbBatchCompletionState} for native batch execution.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IBatchCompletionStateImpl implements FbBatchCompletionState {

    private IBatchCompletionState state;
    private IDatabaseImpl database;
    private IUtil util;
    private IStatus status;

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
        if (state.findError(status, index) != FbBatchCompletionState.NO_MORE_ERRORS) {
            StringBuilder builder = new StringBuilder();
            IStatus errorStatus = database.getMaster().getStatus();
            state.getStatus(getStatus(), errorStatus, index);
            processStatus();

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

        util = database.getMaster().getUtilInterface();

        int updateCount = state.getSize(getStatus());
        processStatus();
        int unknownCount = 0;
        int successCount = 0;
        for (int p = 0; p < updateCount; ++p) {
            int s = state.getState(getStatus(), p);
            processStatus();
            switch (s) {
                case FbBatchCompletionState.EXECUTE_FAILED:
                    if (!print1) {
                        builder.append(String.format("Message Status\n", p));
                        print1 = true;
                    }
                    builder.append(String.format("%5d   Execute failed\n", p));
                    break;

                case FbBatchCompletionState.SUCCESS_NO_INFO:
                    ++unknownCount;
                    break;

                default:
                    if (!print1) {
                        builder.append(String.format("Message Status\n", p));
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
        for (int p = 0; (p = state.findError(status, p)) != FbBatchCompletionState.NO_MORE_ERRORS; ++p) {
            state.getStatus(getStatus(), errorStatus, p);
            processStatus();

            try (CloseableMemory memory = new CloseableMemory(1024)) {

                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                if (!print2) {
                    builder.append(String.format("\nDetailed errors status:\n", p));
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

        util = database.getMaster().getUtilInterface();

        int updateCount = state.getSize(getStatus());
        processStatus();

        int[] states = new int[updateCount];

        for (int p = 0; p < updateCount; ++p) {
            states[p] = state.getState(getStatus(), p);
            processStatus();
        }

        return states;
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
