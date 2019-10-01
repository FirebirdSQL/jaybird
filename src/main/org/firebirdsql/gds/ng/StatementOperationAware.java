package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.Operation;

/**
 * Abstract class for controlling the execution of statement
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public abstract class StatementOperationAware {
    // Implementation of this class that controls the execution of operations
    private static StatementOperationAware instance;

    /**
     * If there is an instance of class, then inform about operation starting
     * @param op operation
     */
    public static void startStatementOperation(Operation op) {
        if (instance != null)
            instance.startOperation(op);
    }

    /**
     * If there is an instance of class, then inform about operation finishing
     * @param op operation
     */
    public static void finishStatementOperation(Operation op) {
        if (instance != null)
            instance.finishOperation(op);
    }

    /**
     * Instance initialization
     * @param s an object that implements this class.
     */
    public static void initStatementOperationAware(StatementOperationAware s) {
        instance = s;
    }

    /**
     * Starting of operation
     * @param op operation.
     */
    public abstract void startOperation(final Operation op);

    /**
     * Finishing of operation
     * @param op operation.
     */
    public abstract void finishOperation(final Operation op);
}
