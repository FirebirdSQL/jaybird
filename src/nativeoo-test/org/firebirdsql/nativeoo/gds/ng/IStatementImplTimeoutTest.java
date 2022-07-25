package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.common.FBTestProperties;
import org.firebirdsql.common.extension.GdsTypeExtension;
import org.firebirdsql.gds.ng.AbstractStatementTimeoutTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.sql.SQLException;

/**
 * Tests for OO API statement timeouts. See  {@link org.firebirdsql.nativeoo.gds.ng.IStatementImpl}.
 *
 * @since 5.0
 */
class IStatementImplTimeoutTest extends AbstractStatementTimeoutTest {

    @RegisterExtension
    @Order(1)
    static final GdsTypeExtension testType = GdsTypeExtension.supportsFBOONativeOnly();

    private final AbstractNativeOODatabaseFactory factory =
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
