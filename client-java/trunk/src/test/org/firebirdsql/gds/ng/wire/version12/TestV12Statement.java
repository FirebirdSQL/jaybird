/*
 * $Id$
 *
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
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.gds.ng.wire.FbWireDatabase;
import org.firebirdsql.gds.ng.wire.ProtocolCollection;
import org.firebirdsql.gds.ng.wire.version11.TestV11Statement;
import org.firebirdsql.gds.ng.wire.version11.V11Database;
import org.firebirdsql.gds.ng.wire.version11.Version11Descriptor;

/**
 * Tests for {@link org.firebirdsql.gds.ng.wire.version12.V12Statement}, reuses test for V11.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class TestV12Statement extends TestV11Statement {

    @Override
    protected ProtocolCollection getProtocolCollection() {
        return ProtocolCollection.create(new Version11Descriptor());
    }

    @Override
    protected Class<? extends FbWireDatabase> getExpectedDatabaseType() {
        return V11Database.class;
    }

    @Override
    protected boolean supportsTableAlias() {
        return true;
    }

}
