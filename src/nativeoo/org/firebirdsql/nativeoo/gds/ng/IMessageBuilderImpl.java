package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.AbstractFbMessageBuilder;
import org.firebirdsql.gds.ng.FbBatch;

import java.sql.SQLException;

/**
 * Implementation of {@link AbstractFbMessageBuilder}
 * to build messages for a native connection using OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IMessageBuilderImpl extends AbstractFbMessageBuilder {

    public IMessageBuilderImpl(FbBatch batch) throws SQLException {
        super(batch);
    }
}
