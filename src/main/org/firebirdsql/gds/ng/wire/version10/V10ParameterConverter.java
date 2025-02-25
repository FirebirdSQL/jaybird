// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version10;

import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.AbstractParameterConverter;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.firebirdsql.gds.ng.wire.auth.legacy.LegacyHash;

import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 10 protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V10ParameterConverter extends AbstractParameterConverter<WireDatabaseConnection, WireServiceConnection> {

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection<?, ?> connection,
            final ConnectionParameterBuffer pb) throws SQLException {
        IAttachProperties<?> props = connection.getAttachProperties();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (props.getUser() != null) {
            pb.addArgument(tagMapping.getUserNameTag(), props.getUser());
        }
        if (props.getPassword() != null) {
            pb.addArgument(tagMapping.getEncryptedPasswordTag(), LegacyHash.fbCrypt(props.getPassword()));
        }
    }

}
