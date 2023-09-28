package org.firebirdsql.gds.ng;

import java.sql.SQLException;

/**
 * Getting a results of statements execution in a batch.
 * <p>
 * Returns after executing the {@link FbBatch} execute method.
 * </p>
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbBatchCompletionState {

    int EXECUTE_FAILED = -1;
    int SUCCESS_NO_INFO = -2;
    int NO_MORE_ERRORS = 0xFFFFFFFF;

    /**
     * @return Count of executed statements.
     */
    int getSize() throws SQLException;

    /**
     * @return The state of a statement with a specific index.
     */
    int getState(int index) throws SQLException;

    /**
     * @return The string with error of a statement with a specific index.
     */
    String getError(int index) throws SQLException;

    /**
     * @return Returns a string with the result of all statements,
     * including error descriptions.
     */
    String printAllStates() throws SQLException;

    /**
     * @return Returns an array with the result of all statements.
     */
    int[] getAllStates() throws SQLException;

    /**
     * @return a result of a batch execution.
     */
    BatchCompletion getBatchCompletion() throws SQLException;
}
