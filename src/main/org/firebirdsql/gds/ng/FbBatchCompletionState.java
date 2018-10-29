package org.firebirdsql.gds.ng;

import org.firebirdsql.nativeoo.gds.ng.FbException;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbBatchCompletionState {

    int EXECUTE_FAILED = -1;
    int SUCCESS_NO_INFO = -2;
    int NO_MORE_ERRORS = -1;

    int getSize() throws FbException;

    int getState(int index) throws FbException;

    String getError(int index) throws FbException;

    String getAllStates() throws FbException;
}
