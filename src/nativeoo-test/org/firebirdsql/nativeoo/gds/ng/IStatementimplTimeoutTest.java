package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.gds.ng.AbstractStatementTimeoutTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.junit.ClassRule;

import java.sql.SQLException;

public class IStatementimplTimeoutTest extends AbstractStatementTimeoutTest {

    @ClassRule
    public static final GdsTypeRule testType = GdsTypeRule.supportsFBOONativeOnly();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) FBTestProperties.getFbDatabaseFactory();

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return IDatabaseImpl.class;
    }

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }
}
