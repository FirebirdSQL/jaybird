package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.Operation;
import org.firebirdsql.gds.ng.FbDatabase;

import java.sql.SQLException;

/**
 * Implements {@link Operation} interface using the GDSHelper object
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public class GDSHelperOperation implements Operation {
    private final GDSHelper helper;

    public GDSHelperOperation(final FbDatabase database) {
        this.helper = new GDSHelper(database);
    }

    /**
     * Cancel the current operation.
     * Used to cancel statement execution and fetching of data.
     */
    @Override
    public void cancelOperation() throws SQLException {
        helper.cancelOperation();
    }
}
