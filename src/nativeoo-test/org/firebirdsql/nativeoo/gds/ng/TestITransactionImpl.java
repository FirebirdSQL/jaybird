package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.gds.ng.AbstractTransactionTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.jna.AbstractNativeDatabaseFactory;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.sql.SQLException;

/**
 * Tests for {@link org.firebirdsql.nativeoo.gds.ng.ITransactionImpl}.
 *
 * @since 4.0
 */
public class TestITransactionImpl extends AbstractTransactionTest {

    private static final String gdsType = "FBOONATIVE";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private AbstractNativeOODatabaseFactory factory =
            (AbstractNativeOODatabaseFactory) GDSFactory.getDatabaseFactoryForType(GDSType.getType(gdsType));

    @Override
    protected FbDatabase createDatabase() throws SQLException {
        return factory.connect(connectionInfo);
    }

    @Override
    protected Class<? extends FbDatabase> getExpectedDatabaseType() {
        return IDatabaseImpl.class;
    }
}
