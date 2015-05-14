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
package org.firebirdsql.gds.ng.wire.version11;

import org.firebirdsql.gds.ng.wire.AbstractFbWireDatabase;
import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.version10.TestV10EventHandling;
import org.junit.BeforeClass;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for events in {@link org.firebirdsql.gds.ng.wire.version11.V11Database}, reuses test for V10.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV11EventHandling extends TestV10EventHandling {

    private static final ProtocolDescriptor DUMMY_DESCRIPTOR = new Version11Descriptor();

    @BeforeClass
    public static void checkDbVersion() {
        assumeTrue(getDefaultSupportInfo().supportsProtocol(11));
    }

    @Override
    protected AbstractFbWireDatabase createDummyDatabase() {
        return new V11Database(DUMMY_CONNECTION, DUMMY_DESCRIPTOR);
    }

    @Override
    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version11Descriptor());
    }

    @Override
    protected Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return V11Database.class;
    }

}
