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
package org.firebirdsql.gds.ng.wire.crypt.arc4;

import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionIdentifier;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPlugin;
import org.firebirdsql.gds.ng.wire.crypt.EncryptionPluginSpi;

/**
 * ARC4 encryption plugin provider.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0.4
 */
public final class Arc4EncryptionPluginSpi implements EncryptionPluginSpi {

    static final EncryptionIdentifier ARC4_ID = new EncryptionIdentifier("Symmetric", "Arc4");

    @Override
    public EncryptionIdentifier getEncryptionIdentifier() {
        return ARC4_ID;
    }

    @Override
    public EncryptionPlugin createEncryptionPlugin(WireConnection<?, ?> connection) {
        return new Arc4EncryptionPlugin(connection.getClientAuthBlock());
    }
}
