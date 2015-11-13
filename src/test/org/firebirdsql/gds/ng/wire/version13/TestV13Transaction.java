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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.gds.ng.wire.version12.TestV12Transaction;
import org.junit.BeforeClass;

import static org.firebirdsql.common.FBTestProperties.getDefaultSupportInfo;
import static org.junit.Assume.assumeTrue;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version10.V10Transaction} in the version 13 protocol
 * (note: there is no version 13 specific implementation of this class).
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestV13Transaction extends TestV12Transaction {

    @BeforeClass
    public static void checkDbVersion() {
        assumeTrue(getDefaultSupportInfo().supportsProtocol(13));
    }

    public TestV13Transaction() {
        this(new V13CommonConnectionInfo());
    }

    protected TestV13Transaction(V13CommonConnectionInfo commonConnectionInfo) {
        super(commonConnectionInfo);
    }
}
