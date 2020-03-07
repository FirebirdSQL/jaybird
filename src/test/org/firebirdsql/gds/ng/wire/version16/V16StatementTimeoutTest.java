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
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.common.rules.GdsTypeRule;
import org.firebirdsql.common.rules.RequireProtocol;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.AbstractStatementTimeoutTest;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.util.Unstable;
import org.junit.ClassRule;

import java.sql.SQLException;

import static org.firebirdsql.common.rules.RequireProtocol.requireProtocolVersion;

/**
 * Tests for {@link V16Statement} timeouts in the V16 protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
@Unstable("Tests might spuriously fail if the connection is slow (eg for remote database tests)")
public class V16StatementTimeoutTest extends AbstractStatementTimeoutTest {

    @ClassRule
    public static final RequireProtocol requireProtocol = requireProtocolVersion(16);

    @ClassRule
    public static final GdsTypeRule gdsTypeRule = GdsTypeRule.excludesNativeOnly();

    private final V16CommonConnectionInfo commonConnectionInfo;

    public V16StatementTimeoutTest() {
        this(new V16CommonConnectionInfo());
    }

    protected V16StatementTimeoutTest(V16CommonConnectionInfo commonConnectionInfo) {
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
                EncodingFactory.getPlatformDefault(), getProtocolCollection());
        gdsConnection.socketConnect();
        return gdsConnection.identify();
    }
}
