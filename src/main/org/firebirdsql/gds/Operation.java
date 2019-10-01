package org.firebirdsql.gds;

import java.sql.SQLException;

/**
 * Interface for working with current actions
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public interface Operation {
    /**
     * Cancel current operation.
     */
    void cancelOperation() throws SQLException;
}
