/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ng.fields.BlrCalculator;
import org.firebirdsql.gds.ng.wire.*;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.wire.ProtocolDescriptor} that returns null for the
 * <code>createXXX</code> methods.
 * <p>
 * For testing purposes only
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public class EmptyProtocolDescriptor extends AbstractProtocolDescriptor {

    public EmptyProtocolDescriptor(final int version, final int architecture, final int minimumType, final int maximumType, final int weight) {
        super(version, architecture, minimumType, maximumType, weight);
    }

    @Override
    public FbWireDatabase createDatabase(final WireConnection connection) {
        return null;
    }

    @Override
    public FbWireTransaction createTransaction(final FbWireDatabase database) {
        return null;
    }

    @Override
    public FbWireStatement createStatement(final FbWireDatabase database) {
        return null;
    }

    @Override
    public DatabaseParameterBuffer createDatabaseParameterBuffer(final WireConnection connection) {
        return null;
    }

    @Override
    public BlrCalculator createBlrCalculator(FbWireDatabase database) {
        return null;
    }
}
