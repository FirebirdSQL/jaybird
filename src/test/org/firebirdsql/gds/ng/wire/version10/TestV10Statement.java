/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.impl.jni.EmbeddedGDSFactoryPlugin;
import org.firebirdsql.gds.impl.jni.NativeGDSFactoryPlugin;
import org.firebirdsql.gds.ng.AbstractStatementTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.junit.ClassRule;

import java.sql.SQLException;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Statement}. This test class can
 * be sub-classed for tests running on newer protocol versions.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV10Statement extends AbstractStatementTest {

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludes(
            EmbeddedGDSFactoryPlugin.EMBEDDED_TYPE_NAME,
            NativeGDSFactoryPlugin.NATIVE_TYPE_NAME);

    private final V10CommonConnectionInfo commonConnectionInfo;

    public TestV10Statement() {
        this(new V10CommonConnectionInfo());
    }

    protected TestV10Statement(V10CommonConnectionInfo commonConnectionInfo) {
        this.commonConnectionInfo = commonConnectionInfo;
    }

    protected final ProtocolCollection getProtocolCollection() {
        return commonConnectionInfo.getProtocolCollection();
    }

    @Override
    protected final Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return commonConnectionInfo.getExpectedDatabaseType();
    }

    @Override
    protected final FbDatabase createDatabase() throws SQLException {
        WireDatabaseConnection gdsConnection = new WireDatabaseConnection(connectionInfo,
                EncodingFactory.getDefaultInstance(), getProtocolCollection());
        gdsConnection.socketConnect();
        return gdsConnection.identify();
    }
}
