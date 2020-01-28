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
package org.firebirdsql.gds.ng.jna;

import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbDatabaseFactory} for establishing local connections using
 * the Firebird client library.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class FbLocalDatabaseFactory extends AbstractNativeDatabaseFactory {

    private static final FbLocalDatabaseFactory INSTANCE = new FbLocalDatabaseFactory();

    @Override
    protected FbClientLibrary getClientLibrary() {
        return FbClientDatabaseFactory.getInstance().getClientLibrary();
    }

    @Override
    protected <T extends IAttachProperties<T>> T filterProperties(T attachProperties) {
        T attachPropertiesCopy = attachProperties.asNewMutable();
        // Clear server name
        attachPropertiesCopy.setServerName(null);
        return attachPropertiesCopy;
    }

    @Override
    protected final FbClientLibrary createClientLibrary() {
        throw new UnsupportedOperationException("Access should be delegated to FbClientDatabaseFactory");
    }

    public static FbLocalDatabaseFactory getInstance() {
        return INSTANCE;
    }
}
