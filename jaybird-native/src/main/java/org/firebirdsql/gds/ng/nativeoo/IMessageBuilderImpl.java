package org.firebirdsql.gds.ng.nativeoo;

import org.firebirdsql.gds.ng.AbstractFbMessageBuilder;
import org.firebirdsql.gds.ng.FbBatch;

import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.AbstractFbMessageBuilder} to build messages for a native connection
 * using OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 6.0
 */
public class IMessageBuilderImpl extends AbstractFbMessageBuilder<FbBatch> {

    public IMessageBuilderImpl(FbBatch batch) throws SQLException {
        super(batch);
    }
}
