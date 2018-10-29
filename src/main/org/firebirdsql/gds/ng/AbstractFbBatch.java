package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public abstract class AbstractFbBatch implements FbBatch {

    private static final Logger log = LoggerFactory.getLogger(AbstractFbBatch.class);

    private final Object syncObject;
    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    private final BatchParameterBuffer batchParameterBuffer;
    private FbTransaction transaction;
    private FbDatabase database;
    private String statement;
    private FbMessageMetadata metadata;

    protected AbstractFbBatch(FbDatabase database, FbTransaction transaction, String statement, FbMessageMetadata metadata, BatchParameterBuffer batchParameterBuffer) {
        this.syncObject = database.getSynchronizationObject();
        this.database = database;
        this.transaction = transaction;
        this.batchParameterBuffer = batchParameterBuffer;
        this.statement = statement;
        this.metadata = metadata;
    }

    protected AbstractFbBatch(FbDatabase database, FbTransaction transaction, String statement, BatchParameterBuffer batchParameterBuffer) {
        this.syncObject = database.getSynchronizationObject();
        this.database = database;
        this.transaction = transaction;
        this.batchParameterBuffer = batchParameterBuffer;
        this.statement = statement;
    }

    @Override
    public void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    protected final Object getSynchronizationObject() {
        return syncObject;
    }

    public BatchParameterBuffer getBatchParameterBuffer() {
        return batchParameterBuffer;
    }

    @Override
    public FbTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(FbTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public FbDatabase getDatabase() {
        return database;
    }

    public void setDatabase(FbDatabase database) {
        this.database = database;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}
