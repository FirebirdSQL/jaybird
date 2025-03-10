// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-FileCopyrightText: Copyright 2015-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.*;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.gds.impl.ServiceParameterBufferImp;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.wire.WireConnection;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.WireServiceConnection;
import org.firebirdsql.gds.ng.wire.auth.ClientAuthBlock;
import org.firebirdsql.gds.ng.wire.version12.V12ParameterConverter;
import org.firebirdsql.jaybird.Version;
import org.firebirdsql.jaybird.fb.constants.DpbItems;
import org.firebirdsql.jaybird.fb.constants.SpbItems;

import java.sql.SQLException;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 13 protocol.
 * <p>
 * Adds support for the new authentication model of the V13 protocol.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V13ParameterConverter extends V12ParameterConverter {

    @Override
    protected DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        DatabaseParameterBuffer dpb =
                new DatabaseParameterBufferImp(DatabaseParameterBufferImp.DpbMetaData.DPB_VERSION_2, stringEncoding);
        dpb.addArgument(DpbItems.isc_dpb_utf8_filename);
        return dpb;
    }

    @Override
    protected ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        ServiceParameterBuffer spb = new ServiceParameterBufferImp(
                ServiceParameterBufferImp.SpbMetaData.SPB_VERSION_3_ATTACH, stringEncoding);
        spb.addArgument(SpbItems.isc_spb_utf8_filename);
        return spb;
    }

    @Override
    protected void populateDefaultProperties(final WireDatabaseConnection connection,
            final DatabaseParameterBuffer dpb) throws SQLException {
        super.populateDefaultProperties(connection, dpb);

        dpb.addArgument(DpbItems.isc_dpb_client_version, Version.JAYBIRD_DISPLAY_VERSION);
    }

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection<?, ?> connection,
            final ConnectionParameterBuffer pb) throws SQLException {
        if (!(connection instanceof WireConnection)) {
            throw new IllegalArgumentException(
                    "populateAuthenticationProperties should have been called with a WireConnection instance, was "
                            + connection.getClass().getName());
        }
        ClientAuthBlock clientAuthBlock = ((WireConnection<?, ?>) connection).getClientAuthBlock();
        if (clientAuthBlock == null || clientAuthBlock.isAuthComplete()) {
            return;
        }

        IAttachProperties<?> props = connection.getAttachProperties();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (props.getUser() != null) {
            pb.addArgument(tagMapping.getUserNameTag(), props.getUser());
        }

        clientAuthBlock.authFillParametersBlock(pb);
    }

    @Override
    protected void populateDefaultProperties(final WireServiceConnection connection,
            final ServiceParameterBuffer spb) throws SQLException {
        super.populateDefaultProperties(connection, spb);

        spb.addArgument(SpbItems.isc_spb_client_version, Version.JAYBIRD_DISPLAY_VERSION);
    }
}
