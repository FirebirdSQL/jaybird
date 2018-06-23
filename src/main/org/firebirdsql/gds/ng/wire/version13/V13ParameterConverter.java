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

import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.ParameterConverter} for the version 13 protocol.
 * <p>
 * Adds support for the new authentication model of the V13 protocol.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class V13ParameterConverter extends V12ParameterConverter {

    private static final String JAYBIRD_VERSION;

    static {
        String jaybirdVersion;
        try {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("org.firebirdsql.version");
            jaybirdVersion = resourceBundle.getString("jaybird.version.display");
        } catch (Exception ex) {
            // Resource bundle missing, or key missing
            jaybirdVersion = "Jaybird (version unknown)";
        }
        JAYBIRD_VERSION = jaybirdVersion;
    }

    protected DatabaseParameterBuffer createDatabaseParameterBuffer(WireDatabaseConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        DatabaseParameterBuffer dpb =
                new DatabaseParameterBufferImp(DatabaseParameterBufferImp.DpbMetaData.DPB_VERSION_2, stringEncoding);
        dpb.addArgument(ISCConstants.isc_dpb_utf8_filename, 1);
        return dpb;
    }

    protected ServiceParameterBuffer createServiceParameterBuffer(WireServiceConnection connection) {
        final Encoding stringEncoding = connection.getEncodingFactory().getEncodingForFirebirdName("UTF8");
        ServiceParameterBuffer spb = new ServiceParameterBufferImp(
                ServiceParameterBufferImp.SpbMetaData.SPB_VERSION_3_ATTACH, stringEncoding);
        spb.addArgument(ISCConstants.isc_spb_utf8_filename, 1);
        return spb;
    }

    @Override
    protected void populateDefaultProperties(final WireDatabaseConnection connection,
            final DatabaseParameterBuffer dpb) throws SQLException {
        super.populateDefaultProperties(connection, dpb);

        dpb.addArgument(ISCConstants.isc_dpb_client_version, JAYBIRD_VERSION);
    }

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection connection,
            final ConnectionParameterBuffer pb) throws SQLException {
        if (!(connection instanceof WireConnection)) {
            throw new IllegalArgumentException(
                    "populateAuthenticationProperties should have been called with a WireConnection instance, was "
                            + connection.getClass().getName());
        }
        ClientAuthBlock clientAuthBlock = ((WireConnection) connection).getClientAuthBlock();
        if (clientAuthBlock == null || clientAuthBlock.isAuthComplete()) {
            return;
        }

        IAttachProperties props = connection.getAttachProperties();
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

        spb.addArgument(ISCConstants.isc_spb_client_version, JAYBIRD_VERSION);
    }
}
