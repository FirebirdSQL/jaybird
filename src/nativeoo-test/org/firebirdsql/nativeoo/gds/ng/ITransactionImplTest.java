package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ng.AbstractTransactionTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.junit.ClassRule;

import java.sql.SQLException;

/**
 * Tests for {@link org.firebirdsql.nativeoo.gds.ng.ITransactionImpl}.
 *
 * @since 4.0
 */
public class ITransactionImplTest extends AbstractTransactionTest {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsFBOONativeOnly();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return IDatabaseImpl.class;
    }
}
