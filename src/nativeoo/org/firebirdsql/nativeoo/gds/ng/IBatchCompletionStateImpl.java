package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbBatchCompletionState;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

/**
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
    public int getSize() throws FbException {
        return state.getSize(status);
    }

    @Override
    public int getState(int index) throws FbException {
        return state.getState(status, index);
    }

    @Override
    public String getError(int index) throws FbException {
        if (state.findError(status, index) != FbBatchCompletionState.NO_MORE_ERRORS) {
            StringBuilder builder = new StringBuilder();
            IStatus errorStatus = database.getMaster().getStatus();
            state.getStatus(status, errorStatus, index);

            try (CloseableMemory memory = new CloseableMemory(1024)) {
                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                builder.append(memory.getString(0));
                return builder.toString();
            }
        }
        return "";
    }

    @Override
    public String getAllStates() throws FbException {

        StringBuilder builder = new StringBuilder();

        boolean print1 = false;
        boolean print2 = false;

        util = database.getMaster().getUtilInterface();

        int updateCount = state.getSize(status);
        int unknownCount = 0;
        int successCount = 0;
        for (int p = 0; p < updateCount; ++p) {
            int s = state.getState(status, p);
            switch (s) {
                case FbBatchCompletionState.EXECUTE_FAILED:
                    if (!print1) {
                        builder.append(String.format("Message Status\n", p));
                        print1 = true;
                    }
                    builder.append(String.format("%5d Execute failed\n", p));
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
            state.getStatus(status, errorStatus, p);

            try (CloseableMemory memory = new CloseableMemory(1024)) {

                util.formatStatus(memory, (int) memory.size() - 1, errorStatus);
                if (!print2) {
                    builder.append(String.format("\nDetailed errors status:\n", p));
                    print2 = true;
                }
                builder.append(String.format("Message %d: %s\n", p, memory.getString(0)));
            }
        }

        if (errorStatus != null)
            errorStatus.dispose();

        return builder.toString();
    }

}
